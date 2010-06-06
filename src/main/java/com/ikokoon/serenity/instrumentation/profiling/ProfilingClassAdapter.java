package com.ikokoon.serenity.instrumentation.profiling;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Michael Couck
 * @since 30.09.09
 * @version 01.00
 */
public class ProfilingClassAdapter extends ClassAdapter implements Opcodes {

	private String className;

	public ProfilingClassAdapter(ClassVisitor visitor, String className) {
		super(visitor);
		this.className = className;
	}

	public MethodVisitor visitMethod(int access, String methodName, String methodDescription, String methodSignature, String[] exceptions) {
		MethodVisitor methodVisitor = super.visitMethod(access, methodName, methodDescription, methodSignature, exceptions);
		// MethodVisitor methodVisitor, Integer access, String className, String methodName, String methodDescription
		MethodAdapter methodAdapter = new ProfilingMethodAdapter(methodVisitor, access, className, methodName, methodDescription);
		return methodAdapter;
	}

}
