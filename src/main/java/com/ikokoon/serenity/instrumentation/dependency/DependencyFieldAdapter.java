package com.ikokoon.serenity.instrumentation.dependency;

import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visits and collects the dependency metrics for a field in a class.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09.12.09
 */
public class DependencyFieldAdapter extends FieldVisitor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * The parent visitor.
     */
    private FieldVisitor visitor;
    /**
     * The name of the class the field is in.
     */
    private String className;

    /**
     * Constructor initialises a {@link DependencyFieldAdapter} and takes the parent field visitor, the name of the class the field is in, the
     * description of the field in byte code and the signature in byte code style.
     *
     * @param visitor     the parent field visitor
     * @param className   the name of the class he field is in
     * @param description the byte code description of the field
     * @param signature   the byte code signature of the field
     */
    public DependencyFieldAdapter(FieldVisitor visitor, String className, String description, String signature) {
        super(Opcodes.ASM5, visitor);
        this.visitor = visitor;
        this.className = Toolkit.slashToDot(className);
        logger.debug("Class name : " + this.className + ", " + description + ", " + signature);
        VisitorFactory.getSignatureVisitor(this.className, description);
        if (signature != null) {
            VisitorFactory.getSignatureVisitor(this.className, signature);
            String targetClassName = Type.getType(description).getClassName();
            Collector.collectEfferentAndAfferent(this.className, targetClassName);
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