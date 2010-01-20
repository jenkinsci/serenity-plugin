package com.ikokoon.serenity.instrumentation.coverage;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.ikokoon.serenity.Collector;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class actually enhances the lines to call the collector class which gathers the data on the lines that are executed during the unit tests.
 *
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class CoverageMethodAdapter extends MethodAdapter {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(CoverageMethodAdapter.class);

	/** The type of parameters that the {@link Collector} takes in the coverage collection method. */
	private Type stringType = Type.getType(String.class);
	/** The type parameter for the line number in the {@link Collector} collect coverage method. */
	private Type intType = Type.getType(int.class);
	/** The array of type parameters for the {@link Collector} for the coverage method. */
	private Type[] types = new Type[] { stringType, stringType, stringType, intType };

	/** The name of the class ({@link Collector}) that will be the collector for the method adapter. */
	private String collectorClassName = Type.getInternalName(Collector.class);
	/** The coverage method that is called on the {@link Collector} by the added instructions. */
	private String collectorMethodName = "collectCoverage";
	/** The byte code signature of the coverage method in the {@link Collector}. */
	private String collectorMethodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, types);

	/** The name of the class that this method adapter is enhancing the methods for. */
	private String className;
	/** The name of the method that is being enhanced. */
	private String methodName;
	/** The description of the method being enhanced. */
	private String methodDescription;

	/**
	 * The constructor initialises a {@link CoverageMethodAdapter} that takes all the interesting items for the method that is to be enhanced
	 * including the parent method visitor.
	 *
	 * @param methodVisitor
	 *            the method visitor of the parent
	 * @param className
	 *            the name of the class the method belongs to
	 * @param methodName
	 *            the name of the method
	 * @param methodDescription
	 *            the description of the method
	 */
	public CoverageMethodAdapter(MethodVisitor methodVisitor, Integer access, String className, String methodName, String methodDescription) {
		super(methodVisitor);
		this.className = Toolkit.slashToDot(className);
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
		this.mv.visitLdcInsn(className);
		this.mv.visitLdcInsn(methodName);
		this.mv.visitLdcInsn(methodDescription);
		this.mv.visitLdcInsn(lineNumber);
		this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, collectorClassName, collectorMethodName, collectorMethodDescription);
		this.mv.visitLineNumber(lineNumber, label);
	}

}