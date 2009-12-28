package com.ikokoon.serenity.instrumentation.dependency;

import org.apache.log4j.Logger;
import org.objectweb.asm.signature.SignatureVisitor;

import com.ikokoon.serenity.Collector;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class visits a class signature and extracts the dependency information.
 * 
 * @author Michael Couck
 * @since 21.11.09
 * @version 01.00
 */
public class DependencySignatureAdapter implements SignatureVisitor {

	private Logger logger = Logger.getLogger(this.getClass());
	/** The name of the class to visit the byte code signature for. */
	private String className;

	/**
	 * Constructor initialises a {@link DependencySignatureAdapter} and takes the class name for the signature.
	 * 
	 * @param className
	 *            the name of the class that this signature will be parsed for
	 */
	public DependencySignatureAdapter(String className) {
		this.className = Toolkit.slashToDot(className);
		logger.debug("Class name : " + className);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitClassType(String name) {
		logger.debug("visitClassType : " + name);
		String normedName = Toolkit.slashToDot(name);
		Collector.collectEfferentAndAfferent(className, normedName);
	}

	/**
	 * {@inheritDoc}
	 */
	public SignatureVisitor visitTypeArgument(char wildcard) {
		logger.debug("visitTypeArgument : " + wildcard);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitTypeVariable(String name) {
		logger.debug("visitTypeVariable : " + name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitBaseType(char descriptor) {
		logger.debug("visitBaseType : " + descriptor);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitFormalTypeParameter(String name) {
		logger.debug("visitFormalTypeParameter : " + name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitInnerClassType(String name) {
		logger.debug("visitInnerClassType : " + name);
	}

	/**
	 * {@inheritDoc}
	 */
	public SignatureVisitor visitArrayType() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SignatureVisitor visitClassBound() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitEnd() {
		logger.debug("visitEnd : ");
	}

	/**
	 * {@inheritDoc}
	 */
	public SignatureVisitor visitExceptionType() {
		logger.debug("visitExceptionType : ");
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SignatureVisitor visitInterface() {
		logger.debug("visitInterface : ");
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SignatureVisitor visitInterfaceBound() {
		logger.debug("visitInterfaceBound : ");
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SignatureVisitor visitParameterType() {
		logger.debug("visitParameterType : ");
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SignatureVisitor visitReturnType() {
		logger.debug("visitReturnType : ");
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public SignatureVisitor visitSuperclass() {
		logger.debug("visitSuperClass : ");
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitTypeArgument() {
		logger.debug("visitArgumentType : ");
	}

}