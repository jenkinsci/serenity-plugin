package com.ikokoon.serenity.instrumentation.profiling;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.toolkit.Toolkit;

public class ProfilingClassAdviceAdapter extends ClassAdapter implements Opcodes {

	protected Logger logger = Logger.getLogger(this.getClass());
	private String className;

	public ProfilingClassAdviceAdapter(ClassVisitor visitor, String className) {
		super(visitor);
		this.className = Toolkit.slashToDot(className);
	}

	public MethodVisitor visitMethod(int access, final String methodName, final String methodDescription, String methodSignature, String[] exceptions) {
		MethodVisitor methodVisitor = super.visitMethod(access, methodName, methodDescription, methodSignature, exceptions);
		MethodVisitor methodAdapter = getMethodAdapter(methodVisitor, access, methodName, methodDescription);
		return methodAdapter;
	}

	private MethodVisitor getMethodAdapter(MethodVisitor methodVisitor, int access, final String methodName, final String methodDescription) {
		// logger.warn("Access : " + access + ", " + Opcodes.ACC_ABSTRACT + ", " + Opcodes.ACC_INTERFACE);
		if (access == Opcodes.ACC_ABSTRACT + Opcodes.ACC_PUBLIC) {
			return methodVisitor;
		} else if (access == Opcodes.ACC_ABSTRACT + Opcodes.ACC_PRIVATE) {
			return methodVisitor;
		} else if (access == Opcodes.ACC_ABSTRACT + Opcodes.ACC_PROTECTED) {
			return methodVisitor;
		}

		MethodAdapter methodAdapter = new ProfilingMethodAdviceAdapter(methodVisitor, access, methodName, methodDescription) {

			private Label[] catchBlockLabels = new Label[0];

			@Override
			protected void onMethodEnter() {
				if (methodName.equals("<init>") || methodName.equals("<clinit>")) {
					insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_ALLOCATION, IConstants.PROFILING_METHOD_DESCRIPTION);
				}
				insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_START, IConstants.PROFILING_METHOD_DESCRIPTION);
			}

			@Override
			protected void onMethodExit(int inst) {
				insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_END, IConstants.PROFILING_METHOD_DESCRIPTION);
			}

			@Override
			public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
				super.visitTryCatchBlock(start, end, handler, type);
				if (type != null) {
					// We have to end the wait regardless of the exception type
					Label[] copyCatchBlockLabels = new Label[catchBlockLabels.length + 1];
					System.arraycopy(catchBlockLabels, 0, copyCatchBlockLabels, 0, catchBlockLabels.length);
					copyCatchBlockLabels[copyCatchBlockLabels.length - 1] = handler;
					catchBlockLabels = copyCatchBlockLabels;
				}
			}

			@Override
			public void visitLabel(Label label) {
				super.visitLabel(label);
				for (Label interruptedCatchBlockLabel : catchBlockLabels) {
					if (label == interruptedCatchBlockLabel) {
						// This is an exception label, handler, so stop the wait, we don't know what the
						// caught exception was so to be safe just call the stop wait
						insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_END_WAIT, IConstants.PROFILING_METHOD_DESCRIPTION);
					}
				}
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				if (isWaitInsn(opcode, owner, name, desc)) {
					insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_START_WAIT, IConstants.PROFILING_METHOD_DESCRIPTION);
					super.visitMethodInsn(opcode, owner, name, desc);
					insertInstruction(IConstants.COLLECTOR_CLASS_NAME, IConstants.COLLECT_END_WAIT, IConstants.PROFILING_METHOD_DESCRIPTION);
				} else {
					super.visitMethodInsn(opcode, owner, name, desc);
				}
			}

			private void insertInstruction(String collectorClassName, String collectorMethodName, String collectorMethodDescription) {
				super.visitLdcInsn(className);
				super.visitLdcInsn(methodName);
				super.visitLdcInsn(methodDescription);
				super.visitMethodInsn(Opcodes.INVOKESTATIC, collectorClassName, collectorMethodName, collectorMethodDescription);
			}
		};
		return methodAdapter;
	}

}