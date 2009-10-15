package com.ikokoon.instrumentation.profiling;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * TODO - @see {@link ProfilingClassAdapter}
 * 
 * @author Michael Couck
 * @since 30.09.09
 * @version 01.00
 */
public class ProfilingMethodAdapter extends MethodAdapter implements Opcodes {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(ProfilingMethodAdapter.class);
	/** The name of the class that this method adapter is enhancing the methods for. */
	@SuppressWarnings("unused")
	private String className;
	/** The name of the method that is being enhanced. */
	private String name;
	/** The description of the method being enhanced. */
	@SuppressWarnings("unused")
	private String desc;

	/**
	 * The constructor takes all the interesting items for the method that is to be enhanced.
	 * 
	 * @param methodVisitor
	 *            the parent method visitor
	 * @param className
	 *            the name of the class to enhance
	 * @param name
	 *            the name of the method to enhance
	 * @param desc
	 *            the method description
	 */
	public ProfilingMethodAdapter(MethodVisitor methodVisitor, String className, String name, String desc) {
		super(methodVisitor);
		this.className = className;
		this.name = name;
		this.desc = desc;
		logger.debug("Class name : " + className + ", name : " + name + ", desc : " + desc);
		// TODO - the method starts here I guess, so add the start instruction to the method here
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitJumpInsn(int opcode, Label paramLabel) {
		logger.debug("visitJumpInsn - " + opcode);
		// TODO - if the jump instruction is a return then the method ended here didn't it? Or what at the end, presuming the method completed
		// naturally of course, which it may or may not. At every jump we need to add the method end instruction and pop the start time from the
		// stack, no?
		super.visitJumpInsn(opcode, paramLabel);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitEnd() {
		logger.debug("visitEnd - " + name);
		// In the case that the method reaches the end of the method instructions then gather the start time and duration and end time
		super.visitEnd();
	}

}