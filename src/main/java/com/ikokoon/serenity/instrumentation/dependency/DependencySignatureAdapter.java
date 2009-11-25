package com.ikokoon.serenity.instrumentation.dependency;

import org.apache.log4j.Logger;
import org.objectweb.asm.signature.SignatureVisitor;

import com.ikokoon.serenity.Collector;

/**
 * This class visits a class signature and extracts the dependency information.
 * 
 * @author Michael Couck
 * @since 21.11.09
 * @version 01.00
 */
public class DependencySignatureAdapter implements SignatureVisitor {

	private Logger logger = Logger.getLogger(this.getClass());
	private String className;

	public DependencySignatureAdapter(String className) {
		this.className = className;
	}

	public void visitClassType(String name) {
		logger.debug("visitClassType:" + name);
		Collector.collectEfferentAndAfferent(className, name);
	}

	public SignatureVisitor visitTypeArgument(char wildcard) {
		logger.debug("visitTypeArgument:" + wildcard);
		return this;
	}

	public void visitTypeVariable(String name) {
		logger.debug("visitTypeVariable:" + name);
	}

	public void visitBaseType(char descriptor) {
		logger.debug("visitBaseType:" + descriptor);
	}

	public void visitFormalTypeParameter(String name) {
		logger.debug("visitFormalTypeParameter:" + name);
	}

	public void visitInnerClassType(String name) {
		logger.debug("visitInnerClassType:" + name);
	}

	public SignatureVisitor visitArrayType() {
		return this;
	}

	public SignatureVisitor visitClassBound() {
		return this;
	}

	public void visitEnd() {
		logger.debug("visitEnd:");
	}

	public SignatureVisitor visitExceptionType() {
		return this; // signatureVisitor.visitExceptionType();
	}

	public SignatureVisitor visitInterface() {
		return this; // signatureVisitor.visitInterface();
	}

	public SignatureVisitor visitInterfaceBound() {
		return this; // signatureVisitor.visitInterfaceBound();
	}

	public SignatureVisitor visitParameterType() {
		return this; // signatureVisitor.visitParameterType();
	}

	public SignatureVisitor visitReturnType() {
		return this; // signatureVisitor.visitReturnType();
	}

	public SignatureVisitor visitSuperclass() {
		return this; // signatureVisitor.visitSuperclass();
	}

	public void visitTypeArgument() {
		logger.debug("visitArgumentType:");
	}

}