package com.ikokoon.serenity.instrumentation.complexity;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * This is the top level class adapter for collecting the complexity for the classes. It just calls the complexity method adapter where the real work
 * happens.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.07.09
 */
public class ComplexityClassAdapter extends ClassVisitor {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    /**
     * The name of the class that is being instrumented.
     */
    private String className;

    /**
     * Constructor initialises a {@link ComplexityClassAdapter} ComplexityClassAdapter with the parent visitor and the name of the class that will be
     * parsed for complexity or jump instructions.
     *
     * @param visitor   the parent visitor what will be called to collect the byte code
     * @param className the name of the class being parsed
     */
    public ComplexityClassAdapter(ClassVisitor visitor, String className) {
        super(Opcodes.ASM5, visitor);
        this.className = Toolkit.slashToDot(className);
        logger.fine("Constructor : " + className);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
        super.visit(version, access, className, signature, superName, interfaces);
    }

    /**
     * {@inheritDoc}
     */
    public MethodVisitor visitMethod(int access, String methodName, String methodDescription, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, methodName, methodDescription, signature, exceptions);
        return VisitorFactory.getMethodVisitor(methodVisitor, ComplexityMethodAdapter.class, access,
                className, methodName, methodDescription);
    }

}
