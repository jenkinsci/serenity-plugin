package com.ikokoon.instrumentation.coverage;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.instrumentation.Collector;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class actually enhances the lines to call the collector class. Please see the JavaDoc in the CoverageClassAdapter for method details.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class CoverageMethodAdapter extends MethodAdapter {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(CoverageMethodAdapter.class);
	/** The name of the class that will be the collector for the method adapter. */
	private String profileClassName = Toolkit.dotToSlash(Collector.class.getName());
	/** The method that is called by the added instructions. */
	private String profileMethodName = "collectCoverage";
	/** The name of the class that this method adapter is enhancing the methods for. */
	private String className;
	/** The name of the method that is being enhanced. */
	private String methodName;
	/** The description of the method being enhanced. */
	private String methodDescription;

	/**
	 * The constructor takes all the interesting items for the method that is to be enhanced.
	 * 
	 * @param methodVisitor
	 *            the method visitor of the parent
	 * @param className
	 *            the name of the class the method belongs to
	 * @param access
	 *            the access code for the method
	 * @param name
	 *            the name of the method
	 * @param desc
	 *            the description of the method
	 * @param exceptions
	 *            exceptions that can be thrown by the method
	 */
	public CoverageMethodAdapter(MethodVisitor methodVisitor, String className, String methodName, String methodDescription) {
		super(methodVisitor);
		this.className = className;
		this.methodName = methodName;
		this.methodDescription = methodDescription;
		logger.debug("Class name : " + className + ", name : " + methodName + ", desc : " + methodDescription);
	}

	/**
	 * This is the method that actually adds the instructions to the enhanced class. It adds an instruction to call a collector class which then
	 * collects the data about each line being called. This method puts five strings onto the stack. These are then popped by the call to the
	 * collector class and passed as parameters to the collector method.
	 */
	public void visitLineNumber(int lineNumber, Label label) {
		logger.debug("visitLineNumber : " + lineNumber + ", " + label + ", " + label.getOffset() + ", " + className + ", " + methodName);
		this.visitLdcInsn(className);
		this.visitLdcInsn(Integer.toString(lineNumber));
		this.visitLdcInsn(methodName);
		this.visitLdcInsn(methodDescription);
		String invocation = Toolkit.classesToByteCodeSignature(null, String.class, String.class, String.class, String.class);
		this.visitMethodInsn(Opcodes.INVOKESTATIC, profileClassName, profileMethodName, invocation);
		super.visitLineNumber(lineNumber, label);
	}

	/**
	 * Visits the end of the method, at which point we know how many lines were called by threads.
	 */
	@Override
	public void visitEnd() {
		logger.debug("visitEnd : " + className + ", method name : " + methodName);
		Collector.collectCoverage(className, methodName, methodDescription);
		super.visitEnd();
	}

}