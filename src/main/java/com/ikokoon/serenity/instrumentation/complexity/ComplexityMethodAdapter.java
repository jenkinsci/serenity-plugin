package com.ikokoon.serenity.instrumentation.complexity;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import com.ikokoon.serenity.Collector;

/**
 * This class just visits the byte code in the classes and collects the complexity metrics for the class.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class ComplexityMethodAdapter extends MethodAdapter {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(ComplexityMethodAdapter.class);
	/** The name of the class that this method adapter is analysing the methods for. */
	private String className;
	/** The name of the method that is being analysed. */
	private String name;
	/** The description of the method being analysed. */
	private String desc;
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
	 * @param name
	 *            the name of the method
	 */
	public ComplexityMethodAdapter(MethodVisitor methodVisitor, String className, String name, String desc) {
		super(methodVisitor);
		this.className = className;
		this.name = name;
		this.desc = desc;
		logger.debug("Class name : " + className + ", name : " + name + ", desc : " + desc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLineNumber(int lineNumber, Label label) {
		lineCounter++;
		super.visitLineNumber(lineNumber, label);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitJumpInsn(int opcode, Label paramLabel) {
		logger.debug("visitJumpInsn - " + opcode);
		complexityCounter++;
		super.visitJumpInsn(opcode, paramLabel);
	}

	// visitTryCatchBlock
	// visitLookupSwitchInsn
	// visitTableSwitchInsn
	// visitMethodInsn
	// visitInsn

	/**
	 * {@inheritDoc}
	 */
	public void visitEnd() {
		logger.debug("visitEnd - " + className + ", " + name + ", " + desc + ", " + lineCounter);
		Collector.collectComplexity(className, name, desc, complexityCounter, lineCounter);
		super.visitEnd();
	}

}