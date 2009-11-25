package com.ikokoon.serenity.instrumentation.coverage;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

public class CoverageClassAdapterChecker extends ClassAdapter {

	public CoverageClassAdapterChecker(ClassVisitor visitor) {
		super(visitor);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
		MethodAdapter methodAdapter = new CoverageMethodAdapterChecker(methodVisitor);
		return methodAdapter;
	}

}
