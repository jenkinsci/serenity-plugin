package com.ikokoon.serenity.instrumentation.dependency;

import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;

import com.ikokoon.serenity.instrumentation.VisitorFactory;

public class DependencyFieldAdapter implements FieldVisitor {

	private Logger logger = Logger.getLogger(this.getClass());
	private FieldVisitor visitor;
	private String className;

	public DependencyFieldAdapter(FieldVisitor visitor, String className, String description, String signature) {
		this.visitor = visitor;
		this.className = className;
		logger.debug("Class name : " + className + ", " + description + ", " + signature);
		VisitorFactory.getSignatureVisitor(className, description);
		if (signature != null) {
			VisitorFactory.getSignatureVisitor(className, signature);
		}
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		logger.debug("visitAnnotation : " + desc + ", " + visible);
		AnnotationVisitor annotationVisitor = visitor.visitAnnotation(desc, visible);
		AnnotationVisitor adapter = VisitorFactory.getAnnotationVisitor(annotationVisitor, className, desc);
		return adapter;
	}

	public void visitAttribute(Attribute attr) {
		// We don't care about attributes
		visitor.visitAttribute(attr);
	}

	public void visitEnd() {
		// What can we get here?
		visitor.visitEnd();
	}

}