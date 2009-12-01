package com.ikokoon.serenity.instrumentation.complexity;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import com.ikokoon.serenity.instrumentation.VisitorFactory;

/**
 * This is the top level class adapter for collecting the complexity for the classes. It just calls the complexity method adapter where the real work
 * happens.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class ComplexityClassAdapter extends ClassAdapter {

	private Logger logger = Logger.getLogger(this.getClass());
	/** The name of the class that is being instrumented. */
	private String className;

	public ComplexityClassAdapter(ClassVisitor visitor, String className) {
		super(visitor);
		this.className = className;
		logger.debug("Constructor : " + className);
	}

	public MethodVisitor visitMethod(int access, String methodName, String methodDescription, String signature, String[] exceptions) {
		logger.debug("visitMethod : " + access + ", " + methodName + ", " + methodDescription + ", " + signature + ", " + exceptions);
		MethodVisitor methodVisitor = super.visitMethod(access, methodName, methodDescription, signature, exceptions);
		// MethodAdapter methodAdapter = new CoverageMethodAdapter(methodVisitor, className, name, desc);
		MethodAdapter methodAdapter = (MethodAdapter) VisitorFactory.getMethodVisitor(methodVisitor, ComplexityMethodAdapter.class, className,
				methodName, methodDescription);
		return methodAdapter;
	}

}
