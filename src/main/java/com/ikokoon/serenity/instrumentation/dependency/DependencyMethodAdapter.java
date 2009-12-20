package com.ikokoon.serenity.instrumentation.dependency;

import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class visits the method instructions and collects dependency metrics on the method.
 * 
 * @author Michael Couck
 * @since 18.07.09
 * @version 01.00
 */
public class DependencyMethodAdapter extends MethodAdapter implements Opcodes {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(this.getClass());
	/** The name of the class that this method adapter parsing for dependency metrics. */
	private String className;
	/** The name of the method. */
	private String methodName;
	/** The byte code description of the method, i.e. the signature. */
	private String methodDescription;

	/**
	 * The constructor initialises a {@link DependencyMethodAdapter} and takes all the interesting items for the method that are used for the
	 * collection of the data.
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
	public DependencyMethodAdapter(MethodVisitor methodVisitor, String className, String methodName, String methodDescription) {
		super(methodVisitor);
		this.className = Toolkit.slashToDot(className);
		this.methodName = methodName;
		this.methodDescription = methodDescription;
		Type[] argumentTypes = Type.getArgumentTypes(methodDescription);
		for (Type argumentType : argumentTypes) {
			if (argumentType.getSort() == Type.OBJECT) {
				Collector.collectEfferentAndAfferent(className, argumentType.getClassName());
			}
			if (argumentType.getSort() == Type.ARRAY) {
				visitArray(argumentType);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Class name : " + className + ", name : " + methodName + ", desc : " + methodDescription);
		}
	}

	/**
	 * This method recursively visits array types.
	 * 
	 * @param argumentType
	 *            the type to visit
	 */
	private void visitArray(Type argumentType) {
		if (argumentType.getSort() == Type.OBJECT) {
			Collector.collectEfferentAndAfferent(className, argumentType.getClassName());
		}
		if (argumentType.getSort() == Type.ARRAY) {
			argumentType = argumentType.getElementType();
			visitArray(argumentType);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		logger.debug("visitAnnotation : " + desc + ", " + visible);
		VisitorFactory.getSignatureVisitor(className, desc);
		return this.mv.visitAnnotation(desc, visible);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitFieldInst : " + owner + ", " + name + ", " + desc);
		}
		VisitorFactory.getSignatureVisitor(className, desc);
		this.mv.visitFieldInsn(opcode, owner, name, desc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLineNumber(int lineNumber, Label label) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitLineNumber : " + lineNumber + ", " + label + ", " + label.getOffset() + ", " + className + ", " + methodName);
		}
		Collector.collectLines(className, methodName, methodDescription, lineNumber);
		this.mv.visitLineNumber(lineNumber, label);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitLocalVariable : " + name + ", " + desc + ", " + signature + ", " + start + ", " + end + ", " + index);
		}
		VisitorFactory.getSignatureVisitor(className, desc);
		if (signature != null) {
			VisitorFactory.getSignatureVisitor(className, signature);
		}
		this.mv.visitLocalVariable(name, desc, signature, start, end, index);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitMethodInst : " + opcode + ", " + owner + ", " + name + ", " + desc);
		}
		VisitorFactory.getSignatureVisitor(className, desc);
		this.mv.visitMethodInsn(opcode, owner, name, desc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitMultiANewArrayInsn(String desc, int dims) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitMultiANewArrayInst : " + desc + ", " + dims);
		}
		VisitorFactory.getSignatureVisitor(className, desc);
		this.mv.visitMultiANewArrayInsn(desc, dims);
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitParameterAnnotation : " + parameter + ", " + desc + ", " + visible);
		}
		VisitorFactory.getSignatureVisitor(className, desc);
		return this.mv.visitParameterAnnotation(parameter, desc, visible);
	}

}