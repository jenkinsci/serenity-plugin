package com.ikokoon.serenity.instrumentation.profiling;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.toolkit.Toolkit;

/**
 * @see {@link ProfilingClassAdapter}
 *
 * @author Michael Couck
 * @since 30.09.09
 * @version 01.00
 */
public class ProfilingMethodAdapter extends MethodAdapter implements Opcodes {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(ProfilingMethodAdapter.class);
	/** The name of the class that this method adapter is enhancing the methods for. */
	private String className;
	private String methodName;
	/** The description of the method being enhanced. */
	private String methodDescription;

	private String objectName = "java/lang/Object";
	private String threadName = "java/lang/Thread";
	private String sleep = "sleep";
	private String wait = "wait";
	private String join = "join";
	private String yield = "yield";

	private boolean clinit = false;
	private boolean init = false;

	/**
	 * The constructor takes all the interesting items for the method that is to be enhanced.
	 *
	 * @param methodVisitor
	 *            the parent method visitor
	 * @param className
	 *            the name of the class to enhance
	 * @param methodName
	 *            the name of the method to enhance
	 * @param methodDescription
	 *            the method description
	 */
	public ProfilingMethodAdapter(MethodVisitor methodVisitor, Integer access, String className, String methodName, String methodDescription) {
		super(methodVisitor);
		this.className = Toolkit.slashToDot(className);
		this.methodName = methodName;
		this.methodDescription = methodDescription;
		logger.debug("Class name : " + className + ", name : " + methodName + ", desc : " + methodDescription);

		if (methodName.equals("<clinit>")) {
			clinit = true;
		} else if (methodName.startsWith("<init>")) {
			init = true;
		}
	}

	public void visitCode() {
		if (clinit) {
			// super.visitCode()
			this.mv.visitCode();
			return;
		}
		if (init) {
			// Because the collectAllocation method looks at the class + method
			// of the caller this call needs to come before the call to
			insertInstruction(IConstants.collectorClassName, IConstants.collectAllocation, IConstants.profilingMethodDescription);
			// this.visitLdcInsn(className);
			// this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, collectorClassName, allocationMethodName, collectorMethodDescription);
			// this.visitMethodInsn(INVOKESTATIC, collectorClassName, "collectAllocation", "(Ljava/lang/String;)V");
		}
		insertInstruction(IConstants.collectorClassName, IConstants.collectStart, IConstants.profilingMethodDescription);
		// this.visitLdcInsn(className);
		// this.visitLdcInsn(methodName);
		// this.visitMethodInsn(INVOKESTATIC, collectorClassName, "collectStart", "(Ljava/lang/String;Ljava/lang/String;)V");
		this.mv.visitCode();
		// super.visitCode();
	}

	public void visitInsn(int inst) {
		if (clinit) {
			// super.visitInsn(inst);
			this.mv.visitInsn(inst);
			return;
		}
		switch (inst) {
		case Opcodes.ARETURN:
		case Opcodes.DRETURN:
		case Opcodes.FRETURN:
		case Opcodes.IRETURN:
		case Opcodes.LRETURN:
		case Opcodes.RETURN:
		case Opcodes.ATHROW:
			insertInstruction(IConstants.collectorClassName, IConstants.collectEnd, IConstants.profilingMethodDescription);
			// this.visitLdcInsn(className);
			// this.visitLdcInsn(methodName);
			// this.visitMethodInsn(INVOKESTATIC, collectorClassName, "collectEnd", "(Ljava/lang/String;Ljava/lang/String;)V");
			break;
		default:
			break;
		}
		if (Opcodes.MONITORENTER == inst) {
			insertInstruction(IConstants.collectorClassName, IConstants.collectStartWait, IConstants.profilingMethodDescription);
			// this.visitLdcInsn(className);
			// this.visitLdcInsn(methodName);
			// this.visitMethodInsn(INVOKESTATIC, collectorClassName, "beginWait", "(Ljava/lang/String;Ljava/lang/String;)V");
			// super.visitInsn(inst);
			this.mv.visitInsn(inst);
			insertInstruction(IConstants.collectorClassName, IConstants.collectEndWait, IConstants.profilingMethodDescription);
			// this.visitLdcInsn(className);
			// this.visitLdcInsn(methodName);
			// this.visitMethodInsn(INVOKESTATIC, collectorClassName, "endWait", "(Ljava/lang/String;Ljava/lang/String;)V");
		} else {
			// super.visitInsn(inst);
			this.mv.visitInsn(inst);
		}
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (isWaitInsn(opcode, owner, name, desc)) {
			insertInstruction(IConstants.collectorClassName, IConstants.collectStartWait, IConstants.profilingMethodDescription);
			// this.visitLdcInsn(className);
			// this.visitLdcInsn(methodName);
			// this.visitMethodInsn(INVOKESTATIC, collectorClassName, "beginWait", "(Ljava/lang/String;Ljava/lang/String;)V");
			// super.visitMethodInsn(opcode, owner, name, desc);
			this.mv.visitMethodInsn(opcode, owner, name, desc);
			insertInstruction(IConstants.collectorClassName, IConstants.collectEndWait, IConstants.profilingMethodDescription);
			// this.visitLdcInsn(className);
			// this.visitLdcInsn(methodName);
			// this.visitMethodInsn(INVOKESTATIC, collectorClassName, "endWait", "(Ljava/lang/String;Ljava/lang/String;)V");
		} else {
			// super.visitMethodInsn(opcode, owner, name, desc);
			this.mv.visitMethodInsn(opcode, owner, name, desc);
		}
	}

	private void insertInstruction(String collectorClassName, String collectorMethodName, String collectorMethodDescription) {
		this.mv.visitLdcInsn(this.className);
		this.mv.visitLdcInsn(this.methodName);
		this.mv.visitLdcInsn(this.methodDescription);
		this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, collectorClassName, collectorMethodName, collectorMethodDescription);
	}

	/**
	 * Code to handle unwinding the call stack when an exception is thrown.
	 */
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		// super.visitTryCatchBlock(start, end, handler, type);
		this.mv.visitTryCatchBlock(start, end, handler, type);
		// Note: static initializers aren't measured, so make sure that the exception isn't being caught in one
		if (type != null && !clinit) {
			// handler.info = new ExceptionInfo(type);
			// int offset = handler.getOffset();
		}
	}

	@Override
	public void visitLabel(Label label) {
		// super.visitLabel(label);
		this.mv.visitLabel(label);
		if (true /* label.info instanceof ExceptionInfo */) {
			// this.visitLdcInsn(className);
			// this.visitLdcInsn(methodName);
			// this.visitLdcInsn(((ExceptionInfo) label.info).type);
			// this.visitMethodInsn(INVOKESTATIC, collectorClassName, "unwind", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
		}
	}

	// class ExceptionInfo {
	// String type;
	//
	// ExceptionInfo(String type) {
	// this.type = type;
	// }
	// }

	private boolean isWaitInsn(int opcode, String owner, String name, String desc) {

		switch (opcode) {
		case Opcodes.INVOKESTATIC: {
			if (threadName.equals(owner)) {
				if (sleep.equals(name)) {
					if ("(J)V".equals(desc) || "(JI)V".equals(desc)) {
						return true;
					}
				}
			}
			if (threadName.equals(owner)) {
				if (yield.equals(name)) {
					if ("()V".equals(desc)) {
						return true;
					}
				}
			}
		}
		case Opcodes.INVOKEVIRTUAL: {
			if (objectName.equals(owner)) {
				if (wait.equals(name)) {
					if ("()V".equals(desc) || "(J)V".equals(desc) || "(JI)V".equals(desc)) {
						return true;
					}
				}
			}
			if (threadName.equals(owner)) {
				if (join.equals(name)) {
					if ("()V".equals(desc) || "(J)V".equals(desc) || "(JI)V".equals(desc)) {
						return true;
					}
				}
			}
		}
		}
		return false;

		// boolean isWait = (opcode == Opcodes.INVOKESTATIC && threadName.equals(owner) && "sleep".equals(name) && ("(J)V".equals(desc) || "(JI)V"
		// .equals(desc)));
		// if (isWait) {
		// return true;
		// }
		// isWait = (opcode == Opcodes.INVOKEVIRTUAL && objectName.equals(owner) && "wait".equals(name) && ("()V".equals(desc) || "(J)V".equals(desc)
		// || "(JI)V"
		// .equals(desc)));
		// if (isWait) {
		// return true;
		// }
		// isWait = (opcode == Opcodes.INVOKEVIRTUAL && threadName.equals(owner) && "join".equals(name) && ("()V".equals(desc) || "(J)V".equals(desc)
		// || "(JI)V"
		// .equals(desc)));
		// if (isWait) {
		// return true;
		// }
		// isWait = (opcode == Opcodes.INVOKESTATIC && threadName.equals(owner) && "yield".equals(name) && "()V".equals(desc));
		// if (isWait) {
		// return true;
		// }
		// return isWait;
	}

}