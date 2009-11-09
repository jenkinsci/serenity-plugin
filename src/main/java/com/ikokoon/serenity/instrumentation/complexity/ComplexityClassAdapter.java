package com.ikokoon.serenity.instrumentation.complexity;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * This is the top level class adapter for collecting the complexity for the classes. It just calls the complexity method adapter where the real work
 * happens.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class ComplexityClassAdapter extends ClassAdapter {

	/** The name of the class that is being instrumented. */
	private String className;

	/**
	 * Constructor takes the parent visitor and the name of the class that complexity will be collected for.
	 * 
	 * @param visitor
	 *            the parent visitor in the chain of visitors
	 * @param className
	 *            the name of the class to collect complexity for
	 */
	public ComplexityClassAdapter(ClassVisitor visitor, String className) {
		super(visitor);
		this.className = className;
	}

	/**
	 * This method calls the complexity method adapter that will collect the complexity for each method as the byte code for the class is parsed.
	 */
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
		MethodAdapter methodAdapter = new ComplexityMethodAdapter(methodVisitor, className, name, desc);
		return methodAdapter;
	}

}
