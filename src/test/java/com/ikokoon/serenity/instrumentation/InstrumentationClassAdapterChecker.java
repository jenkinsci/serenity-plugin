package com.ikokoon.serenity.instrumentation;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class InstrumentationClassAdapterChecker extends ClassAdapter {

	private String collectorMethodName;
	private String collectorMethodDescription;

	public InstrumentationClassAdapterChecker(ClassVisitor classVisitor, String collectorMethodName, String collectorMethodDescription) {
		super(classVisitor);
		this.collectorMethodName = collectorMethodName;
		this.collectorMethodDescription = collectorMethodDescription;
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
		// MethodVisitor methodVisitor, String collectorMethodName, String collectorMethodDescription
		MethodVisitor methodAdapter = new InstrumentationMethodAdapterChecker(methodVisitor, collectorMethodName, collectorMethodDescription);
		return methodAdapter;
	}

}
