package com.ikokoon.serenity.instrumentation.dependency;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;

import com.ikokoon.serenity.instrumentation.VisitorFactory;

public class DependencyFieldAdapter implements FieldVisitor {

	private FieldVisitor visitor;
	private String className;

	public DependencyFieldAdapter(FieldVisitor visitor, String className, String description, String signature) {
		this.visitor = visitor;
		this.className = className;
		VisitorFactory.getSignatureVisitor(className, description);
		if (signature != null) {
			VisitorFactory.getSignatureVisitor(className, signature);
		}
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		AnnotationVisitor annotationVisitor = visitor.visitAnnotation(desc, visible);
		AnnotationVisitor adapter = VisitorFactory.getAnnotationVisitor(annotationVisitor, className, desc);
		return adapter;
	}

	public void visitAttribute(Attribute attr) {
		// We don't care about attributes
	}

	public void visitEnd() {
		// What can we get here?
	}

}
