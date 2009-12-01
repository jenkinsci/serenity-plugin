package com.ikokoon.serenity.instrumentation.complexity;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.instrumentation.coverage.CoverageMethodAdapter;

/**
 * This class just visits the byte code in the classes and collects the complexity metrics for the class.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class ComplexityMethodAdapter extends MethodAdapter {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(CoverageMethodAdapter.class);

	/** The name of the class that this method adapter is enhancing the methods for. */
	private String className;
	/** The name of the method that is being enhanced. */
	private String methodName;
	/** The description of the method being enhanced. */
	private String methodDescription;

	/** The complexity counter, start with one and increment for each jump instruction. */
	private int complexityCounter = 1;
	/** The total number of lines for the method. */
	private double lineCounter;

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
	public ComplexityMethodAdapter(MethodVisitor methodVisitor, String className, String methodName, String methodDescription) {
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
		lineCounter++;
		this.mv.visitLineNumber(lineNumber, label);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitJumpInsn(int opcode, Label paramLabel) {
		logger.debug("visitJumpInsn:" + opcode);
		complexityCounter++;
		this.mv.visitJumpInsn(opcode, paramLabel);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitEnd() {
		logger.debug("visitEnd:" + className + ", " + methodName + ", " + methodDescription + ", " + lineCounter);
		Collector.collectComplexity(className, methodName, methodDescription, complexityCounter, lineCounter);
		this.mv.visitEnd();
	}

	// visitTryCatchBlock
	// visitLookupSwitchInsn
	// visitTableSwitchInsn
	// visitMethodInsn
	// visitInsn

}