package com.ikokoon.serenity.instrumentation.profiling;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.toolkit.Toolkit;

/**
 * TODO - implement a simple profiling strategy, start an instruction at the start of the method and end it at the end of the method and take the
 * time, simple.
 * 
 * @author Michael Couck
 * @since 30.09.09
 * @version 01.00
 */
public class ProfilingClassAdapter extends ClassAdapter implements Opcodes {

	/** The name of the class that is being instrumented. */
	private String className;

	public ProfilingClassAdapter(ClassVisitor visitor, String className) {
		super(visitor);
		this.className = Toolkit.slashToDot(className);
	}

	/**
	 * TODO - implement me
	 */
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
		MethodAdapter methodAdapter = new ProfilingMethodAdapter(methodVisitor, className, name, desc);
		return methodAdapter;
	}

}
