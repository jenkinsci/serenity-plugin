package com.ikokoon.serenity.instrumentation.coverage;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * This is the class visitor that visits the class structures and invokes the method visitor for the coverage functionality.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.07.09
 */
public class CoverageClassAdapter extends ClassVisitor implements Opcodes {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * The name of the class that is being instrumented.
     */
    private String className;

    /**
     * Constructs a {@link CoverageClassAdapter} that takes the parent class visitor and the class name that will be enhanced with instructions to
     * invoke the {@link com.ikokoon.serenity.Collector} that collects the instructions that are executed.
     *
     * @param visitor   the parent class visitor
     * @param className that name of the class that will be enhanced with coverage instructions
     */
    public CoverageClassAdapter(ClassVisitor visitor, String className) {
        super(Opcodes.ASM5, visitor);
        this.className = Toolkit.slashToDot(className);
        logger.fine("Constructor : " + className);
    }

    /**
     * This is the method that calls the MethodAdapter that will enhance the class methods with instructions that will enable data to be collected for
     * the class at runtime producing a line coverage report for the class.
     */
    public MethodVisitor visitMethod(int access, String methodName, String methodDescription, String methodSignature, String[] exceptions) {
        logger.fine("visitMethod : " + access + ", " + methodName + ", " + methodDescription + ", " + methodSignature + ", " + Arrays.toString(exceptions));
        MethodVisitor methodVisitor = super.visitMethod(access, methodName, methodDescription, methodSignature, exceptions);
        return VisitorFactory.getMethodVisitor(methodVisitor, CoverageMethodAdapter.class, access, className,
                methodName, methodDescription);
    }

}
