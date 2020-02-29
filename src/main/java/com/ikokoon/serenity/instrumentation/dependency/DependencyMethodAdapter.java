package com.ikokoon.serenity.instrumentation.dependency;

import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.*;

import java.util.logging.Logger;

/**
 * This class visits the method instructions and collects dependency metrics on the method.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18.07.09
 */
public class DependencyMethodAdapter extends MethodVisitor implements Opcodes {

    /**
     * The LOGGER for the class.
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * The name of the class that this method adapter parsing for dependency metrics.
     */
    private String className;
    /**
     * The name of the method.
     */
    private String methodName;
    /**
     * The byte code description of the method, i.e. the signature.
     */
    private String methodDescription;

    /**
     * The constructor initialises a {@link DependencyMethodAdapter} and takes all the interesting items for the method that are used for the
     * collection of the data.
     *
     * @param methodVisitor     the method visitor of the parent
     * @param access            the access to the method
     * @param className         the name of the class the method belongs to
     * @param methodName        the name of the method
     * @param methodDescription the description of the method
     */
    public DependencyMethodAdapter(MethodVisitor methodVisitor, @SuppressWarnings("UnusedParameters") Integer access, String className,
                                   String methodName, String methodDescription) {
        super(Opcodes.ASM5, methodVisitor);
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
        logger.fine("Class name : " + className + ", name : " + methodName + ", desc : " + methodDescription);
    }

    /**
     * This method recursively visits array types.
     *
     * @param argumentType the type to visit
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
        logger.fine("visitAnnotation : " + desc + ", " + visible);
        VisitorFactory.getSignatureVisitor(className, desc);
        return this.mv.visitAnnotation(desc, visible);
    }

    /**
     * {@inheritDoc}
     */
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        logger.fine("visitFieldInst : " + owner + ", " + name + ", " + desc);
        VisitorFactory.getSignatureVisitor(className, desc);
        this.mv.visitFieldInsn(opcode, owner, name, desc);
    }

    /**
     * {@inheritDoc}
     */
    public void visitLineNumber(int lineNumber, Label label) {
        logger.fine("visitLineNumber : " + className + ", " + lineNumber + ", " + label + ", " + label.getOffset() + ", " + className + ", "
                + methodName);
        Collector.collectLine(className, methodName, methodDescription, lineNumber);
        this.mv.visitLineNumber(lineNumber, label);
    }

    /**
     * {@inheritDoc}
     */
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        logger.fine("visitLocalVariable : " + name + ", " + desc + ", " + signature + ", " + start + ", " + end + ", " + index);
        VisitorFactory.getSignatureVisitor(className, desc);
        if (signature != null) {
            VisitorFactory.getSignatureVisitor(className, signature);
        }
        this.mv.visitLocalVariable(name, desc, signature, start, end, index);
    }

    /**
     * {@inheritDoc}
     */
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean inf) {
        logger.fine("visitMethodInst : " + opcode + ", " + owner + ", " + name + ", " + desc);
        VisitorFactory.getSignatureVisitor(className, desc);
        this.mv.visitMethodInsn(opcode, owner, name, desc, inf);
    }

    /**
     * {@inheritDoc}
     */
    public void visitMultiANewArrayInsn(String desc, int dims) {
        logger.fine("visitMultiANewArrayInst : " + desc + ", " + dims);
        VisitorFactory.getSignatureVisitor(className, desc);
        this.mv.visitMultiANewArrayInsn(desc, dims);
    }

    /**
     * {@inheritDoc}
     */
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        logger.fine("visitParameterAnnotation : " + parameter + ", " + desc + ", " + visible);
        VisitorFactory.getSignatureVisitor(className, desc);
        return this.mv.visitParameterAnnotation(parameter, desc, visible);
    }

}