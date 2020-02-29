package com.ikokoon.serenity.instrumentation.dependency;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.logging.Logger;

/**
 * This class visits the annotation description collecting the dependency information on the class that defines the annotation.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21.11.09
 */
public class DependencyAnnotationAdapter extends AnnotationVisitor {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * The parent annotation visitor.
     */
    private AnnotationVisitor annotationVisitor;
    /**
     * The class that has the annotation.
     */
    private String className;

    /**
     * Constructor initialises a {@link DependencyAnnotationAdapter}, takes the annotation visitor parent, the class name that uses the annotation and
     * the description of the annotation.
     *
     * @param annotationVisitor the parent annotation visitor
     * @param className         the class name that uses the annotation
     * @param description       the description of the annotation
     */
    public DependencyAnnotationAdapter(AnnotationVisitor annotationVisitor, String className, String description) {
        super(Opcodes.ASM5, annotationVisitor);
        logger.fine("Class name : " + className + ", " + description);
        this.className = Toolkit.slashToDot(className);
        this.annotationVisitor = annotationVisitor;
        VisitorFactory.getSignatureVisitor(className, description);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(String name, Object value) {
        logger.fine("visit : " + className + ", " + name + ", " + value);
        if (name != null && value != null) {
            try {
                VisitorFactory.getSignatureVisitor(className, value.toString());
            } catch (StringIndexOutOfBoundsException e) {
                // We swallow this exception more or less because some annotations may contain
                // Strings that are not even remotely like a class name, like Remote/bean for example
                logger.fine("String out of bounds for : " + className + ", " + name + ", " + value);
            }
        }
        if (name != null) {
            annotationVisitor.visit(name, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        logger.fine("visitAnnotation : " + className + ", " + name + ", " + desc);
        AnnotationVisitor visitor = annotationVisitor.visitAnnotation(name, desc);
        return VisitorFactory.getAnnotationVisitor(visitor, name, desc);
    }

    /**
     * {@inheritDoc}
     */
    public AnnotationVisitor visitArray(String name) {
        logger.fine("visitArray : " + className + ", " + name);
        return annotationVisitor.visitArray(name);
    }

    /**
     * {@inheritDoc}
     */
    public void visitEnd() {
        logger.fine("visitEnd : " + className);
        annotationVisitor.visitEnd();
    }

    /**
     * {@inheritDoc}
     */
    public void visitEnum(String name, String desc, String value) {
        logger.fine("visitEnum : " + className + ", " + name + ", " + desc + ", " + value);
        VisitorFactory.getSignatureVisitor(className, desc);
        if (name != null) {
            annotationVisitor.visitEnum(name, desc, value);
        }
    }

}