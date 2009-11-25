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

/**
 * Please @see SourceClassAdapter for more on dependency.
 * 
 * @author Michael Couck
 * @since 18.07.09
 * @version 01.00
 */
public class DependencyMethodAdapter extends MethodAdapter implements Opcodes {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(DependencyMethodAdapter.class);
	/** The name of the class that this method adapter is enhancing the methods for. */
	private String className;
	/** The name of the method. */
	private String methodName;
	/** The byte code description of the method, i.e. the signature. */
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
	public DependencyMethodAdapter(MethodVisitor methodVisitor, String className, String methodName, String methodDescription) {
		super(methodVisitor);
		this.className = className;
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
		VisitorFactory.getSignatureVisitor(className, desc);
		return super.visitAnnotation(desc, visible);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitFieldInst - " + owner + ", " + name + ", " + desc);
		}
		VisitorFactory.getSignatureVisitor(className, desc);
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLineNumber(int lineNumber, Label label) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitLineNumber : " + lineNumber + ", " + label + ", " + label.getOffset() + ", " + className + ", " + methodName);
		}
		Collector.collectLines(className, Double.toString(lineNumber), methodName, methodDescription);
		super.visitLineNumber(lineNumber, label);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitLocalVariable - " + name + ", " + desc + ", " + signature + ", " + start + ", " + end + ", " + index);
		}
		VisitorFactory.getSignatureVisitor(className, desc);
		if (signature != null) {
			VisitorFactory.getSignatureVisitor(className, signature);
		}
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitMethodInst - " + opcode + ", " + owner + ", " + name + ", " + desc);
		}
		VisitorFactory.getSignatureVisitor(className, desc);
		super.visitMethodInsn(opcode, owner, name, desc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitMultiANewArrayInsn(String desc, int dims) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitMultiANewArrayInst - " + desc + ", " + dims);
		}
		VisitorFactory.getSignatureVisitor(className, desc);
		super.visitMultiANewArrayInsn(desc, dims);
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitParameterAnnotation - " + parameter + ", " + desc + ", " + visible);
		}
		VisitorFactory.getSignatureVisitor(className, desc);
		return super.visitParameterAnnotation(parameter, desc, visible);
	}

}