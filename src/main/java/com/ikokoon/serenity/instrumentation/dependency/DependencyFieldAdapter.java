package com.ikokoon.serenity.instrumentation.dependency;

import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.toolkit.Toolkit;

/**
 * Visits and collects the dependency metrics for a field in a class.
 * 
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
public class DependencyFieldAdapter implements FieldVisitor {

	private Logger logger = Logger.getLogger(this.getClass());
	/** The parent visitor. */
	private FieldVisitor visitor;
	/** The name of the class the field is in. */
	private String className;

	/**
	 * Constructor initialises a {@link DependencyFieldAdapter} and takes the parent field visitor, the name of the class the field is in, the
	 * description of the field in byte code and the signature in byte code style.
	 * 
	 * @param visitor
	 *            the parent field visitor
	 * @param className
	 *            the name of the class he field is in
	 * @param description
	 *            the byte code description of the field
	 * @param signature
	 *            the byte code signature of the field
	 */
	public DependencyFieldAdapter(FieldVisitor visitor, String className, String description, String signature) {
		this.visitor = visitor;
		this.className = Toolkit.slashToDot(className);
		logger.debug("Class name : " + className + ", " + description + ", " + signature);
		VisitorFactory.getSignatureVisitor(className, description);
		if (signature != null) {
			VisitorFactory.getSignatureVisitor(className, signature);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		logger.debug("visitAnnotation : " + desc + ", " + visible);
		AnnotationVisitor annotationVisitor = visitor.visitAnnotation(desc, visible);
		AnnotationVisitor adapter = VisitorFactory.getAnnotationVisitor(annotationVisitor, className, desc);
		return adapter;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitAttribute(Attribute attr) {
		// We don't care about attributes
		visitor.visitAttribute(attr);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitEnd() {
		// What can we get here?
		visitor.visitEnd();
	}

}