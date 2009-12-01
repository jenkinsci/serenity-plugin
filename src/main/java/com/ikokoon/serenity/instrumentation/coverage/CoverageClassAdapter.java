package com.ikokoon.serenity.instrumentation.coverage;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.serenity.instrumentation.VisitorFactory;

/**
 * This is the class visitor that visits the class structures and invokes the method visitor for the coverage functionality.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class CoverageClassAdapter extends ClassAdapter implements Opcodes {

	private Logger logger = Logger.getLogger(this.getClass());
	/** The name of the class that is being instrumented. */
	private String className;

	public CoverageClassAdapter(ClassVisitor visitor, String className) {
		super(visitor);
		this.className = className;
		logger.debug("Constructor : " + className);
	}

	/**
	 * This is the method that calls the MethodAdapter that will enhance the class methods with instructions that will enable data to be collected for
	 * the class at runtime producing a line coverage report for the class.
	 */
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		logger.debug("visitMethod : " + access + ", " + name + ", " + desc + ", " + signature + ", " + exceptions);
		MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
		// MethodAdapter methodAdapter = new CoverageMethodAdapter(methodVisitor, className, name, desc);
		MethodAdapter methodAdapter = (MethodAdapter) VisitorFactory.getMethodVisitor(methodVisitor, CoverageMethodAdapter.class, className, name,
				desc);
		return methodAdapter;
	}

}
