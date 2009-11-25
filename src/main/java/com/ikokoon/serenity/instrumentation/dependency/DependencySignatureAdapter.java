package com.ikokoon.serenity.instrumentation.dependency;

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

	private String className;

	public DependencySignatureAdapter(String className) {
		this.className = className;
	}

	public void visitClassType(String name) {
		Collector.collectEfferentAndAfferent(className, name);
	}

	public SignatureVisitor visitTypeArgument(char wildcard) {
		return this;
	}

	public void visitTypeVariable(String name) {
		// signatureVisitor.visitTypeVariable(name);
	}

	public void visitBaseType(char descriptor) {
		// signatureVisitor.visitBaseType(descriptor);
	}

	public void visitFormalTypeParameter(String name) {
		// signatureVisitor.visitFormalTypeParameter(name);
	}

	public void visitInnerClassType(String name) {
		// signatureVisitor.visitInnerClassType(name);
	}

	public SignatureVisitor visitArrayType() {
		return this;
	}

	public SignatureVisitor visitClassBound() {
		return this;
	}

	public void visitEnd() {
		// signatureVisitor.visitEnd();
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
		// signatureVisitor.visitTypeArgument();
	}

}