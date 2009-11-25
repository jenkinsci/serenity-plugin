package com.ikokoon.serenity.instrumentation.coverage;

import org.apache.log4j.Logger;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.ikokoon.serenity.Collector;

public class CoverageMethodAdapterChecker extends MethodAdapter {

	private Logger logger = Logger.getLogger(this.getClass());
	private boolean isCovered = false;

	private Type stringType = Type.getType(String.class);
	private Type[] types = new Type[] { stringType, stringType, stringType, stringType };

	private String className = Type.getInternalName(Collector.class);
	private String methodName = "collectCoverage";
	private String methodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, types);

	public CoverageMethodAdapterChecker(MethodVisitor methodVisitor) {
		super(methodVisitor);
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		logger.debug("visitMethod:" + opcode + ", " + owner + ", " + name + ", " + desc);
		if (owner.equals(className) && name.equals(methodName) && desc.equals(methodDescription)) {
			isCovered = true;
		}
		super.visitMethodInsn(opcode, owner, name, desc);
	}

	public void visitEnd() {
		super.visitEnd();
		if (!isCovered) {
			throw new RuntimeException("Class not covered : ");
		}
	}

}
