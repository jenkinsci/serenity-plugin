package com.ikokoon.serenity.instrumentation.complexity;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.instrumentation.coverage.CoverageMethodAdapter;
import com.ikokoon.toolkit.Toolkit;

/**
 * TODO - add the interesting methods to the collection of the complexity. Do we need to add try catch? And what about multiple catch? One for each
 * potential exception thrown? No?
 * 
 * This class just visits the byte code in the classes and collects the complexity metrics for the class. Complexity is calculated by adding one every
 * time there is a jump instruction.
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
	// private double lineCounter;
	/**
	 * The constructor initialises a {@link ComplexityMethodAdapter} that takes all the interesting items for the method that is to be enhanced
	 * including the parent method visitor.
	 * 
	 * @param methodVisitor
	 *            the method visitor of the parent
	 * @param className
	 *            the name of the class the method belongs to
	 * @param methodName
	 *            the name of the method that will be collected for complexity
	 * @param methodDescription
	 *            the description of the method, i.e. the byte code signature
	 */
	public ComplexityMethodAdapter(MethodVisitor methodVisitor, String className, String methodName, String methodDescription) {
		super(methodVisitor);
		this.className = Toolkit.slashToDot(className);
		this.methodName = methodName;
		this.methodDescription = methodDescription;
		logger.debug("Class name : " + className + ", name : " + methodName + ", desc : " + methodDescription);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLineNumber(int lineNumber, Label label) {
		logger.debug("visitLineNumber : " + lineNumber + ", " + label + ", " + label.getOffset() + ", " + className + ", " + methodName);
		// lineCounter++;
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
		logger.debug("visitEnd:" + className + ", " + methodName + ", " + methodDescription/* + ", " + lineCounter */);
		Collector.collectComplexity(className, methodName, methodDescription, complexityCounter/* , lineCounter */);
		this.mv.visitEnd();
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		// TODO - implement me
		this.mv.visitTryCatchBlock(start, end, handler, type);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLookupSwitchInsn(Label dflt, int keys[], Label labels[]) {
		// TODO - implement me
		this.mv.visitLookupSwitchInsn(dflt, keys, labels);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label labels[]) {
		// TODO - implement me
		this.mv.visitTableSwitchInsn(min, max, dflt, labels);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		// TODO - implement me
		this.mv.visitMethodInsn(opcode, owner, name, desc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitInsn(int opcode) {
		// TODO - implement me
		this.mv.visitInsn(opcode);
	}

}