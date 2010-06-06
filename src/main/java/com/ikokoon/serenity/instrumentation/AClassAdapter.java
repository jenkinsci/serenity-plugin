package com.ikokoon.serenity.instrumentation;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 01.06.10
 * @version 01.00
 */
public abstract class AClassAdapter extends ClassAdapter implements Opcodes {

	protected Logger logger = Logger.getLogger(this.getClass());
	protected String className;
	protected Class<?> methodAdapterClass;

	public AClassAdapter(ClassVisitor visitor, Class<?> methodAdapterClass, String className) {
		super(visitor);
		this.methodAdapterClass = methodAdapterClass;
		this.className = Toolkit.slashToDot(className);
		logger.debug("Constructor : " + className);
	}

	public MethodVisitor visitMethod(int access, String methodName, String methodDescription, String methodSignature, String[] exceptions) {
		logger.debug("visitMethod : " + access + ", " + methodName + ", " + methodDescription + ", " + methodSignature + ", " + exceptions);
		MethodVisitor methodVisitor = super.visitMethod(access, methodName, methodDescription, methodSignature, exceptions);
		MethodAdapter methodAdapter = (MethodAdapter) VisitorFactory.getMethodVisitor(methodVisitor, methodAdapterClass, access, className,
				methodName, methodDescription);
		return methodAdapter;
	}

}
