package com.ikokoon.serenity.instrumentation;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

public class InstrumentationMethodAdapterChecker extends MethodAdapter {

	private boolean isCovered = false;
	protected String className;
	private String collectorMethodName;
	private String collectorMethodDescription;

	public InstrumentationMethodAdapterChecker(MethodVisitor methodVisitor, String collectorMethodName, String collectorMethodDescription) {
		super(methodVisitor);
		this.collectorMethodName = collectorMethodName;
		this.collectorMethodDescription = collectorMethodDescription;
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (name.equals(collectorMethodName) && desc.equals(collectorMethodDescription)) {
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
