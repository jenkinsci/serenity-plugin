package com.ikokoon.serenity.instrumentation;

import org.apache.log4j.Logger;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 30.09.09
 * @version 01.00
 */
public class AMethodAdapter extends MethodAdapter implements Opcodes {

	/** The logger for the class. */
	protected Logger logger = Logger.getLogger(AMethodAdapter.class);
	/** The name of the class that this method adapter is enhancing the methods for. */
	protected String className;
	/** The name of the method that is being enhanced. */
	protected String methodName;
	/** The description of the method being enhanced. */
	protected String methodDescription;

	public AMethodAdapter(MethodVisitor methodVisitor, String className, String methodName, String methodDescription) {
		super(methodVisitor);
		this.className = Toolkit.slashToDot(className);
		this.methodName = methodName;
		this.methodDescription = methodDescription;
	}

}