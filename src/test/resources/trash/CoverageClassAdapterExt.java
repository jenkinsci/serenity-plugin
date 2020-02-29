package com.ikokoon.serenity.instrumentation.coverage;

import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.*;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * This is an alternative class for adding coverage instructions line by line. The process is as follows:
 * <p>
 * 1) Add an array of integers the size of the lines in the class, to the class.<br>
 * 2) At each line increment the indexed line in the array.<br>
 * 3) Add a method to the class to retrieve the array of line counters.<br>
 * 4) At the end of the processing, i.e. when the JVM exist walk over the classes and accumulate all the data.<br>
 * <p>
 * This method could possibly be faster, with less overhead in the actual class, but has not been fully implemented nor tested as of the writing as
 * the original method for coverage is sufficient for current purposes.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19.01.10
 */
public class CoverageClassAdapterExt extends ClassVisitor implements Opcodes {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    /**
     * The name of the class that is being instrumented.
     */
    private String className;
    private String linesArrayName = Toolkit.replaceAll(this.getClass().getName(), ".", "$") + "$Lines";
    private String linesArrayDescription = Type.getInternalName(int[].class);
    private String linesArrayMethodName = "get$" + linesArrayName;
    @SuppressWarnings("RedundantArrayCreation")
    private String linesArrayMethodDescription = Type.getMethodDescriptor(Type.getType(int[].class), new Type[0]);

    @SuppressWarnings("FieldCanBeLocal")
    private String constantPoolInitMethodName = "<clinit>";
    @SuppressWarnings("RedundantArrayCreation")
    private String constantPoolInitMethodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[0]);

    public CoverageClassAdapterExt(ClassVisitor visitor, String className) {
        super(Opcodes.ASM5, visitor);
        this.className = Toolkit.slashToDot(className);
        logger.fine("Constructor : " + className + ", " + linesArrayName + ", " + linesArrayDescription);
    }

    public MethodVisitor visitMethod(int access, String methodName, String methodDescription, String methodSignature, String[] exceptions) {
        logger.fine("visitMethod : " + access + ", " + methodName + ", " + methodDescription + ", " + methodSignature + ", " + Arrays.toString(exceptions));
        MethodVisitor methodVisitor = super.visitMethod(access, methodName, methodDescription, methodSignature, exceptions);
        CoverageMethodAdapterExt methodAdapter = new CoverageMethodAdapterExt(methodVisitor, className, linesArrayName, linesArrayDescription);
        logger.fine("Lines : " + methodAdapter.getLines());
        return methodAdapter;
    }

    public void visitEnd() {
        FieldVisitor fv = super.visitField(ACC_PRIVATE + ACC_STATIC, linesArrayName, linesArrayDescription, null, null);
        fv.visitEnd();

        MethodVisitor mv = super.visitMethod(ACC_STATIC, constantPoolInitMethodName, constantPoolInitMethodDescription, null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(5, l0);
        mv.visitIntInsn(BIPUSH, 10);
        mv.visitIntInsn(NEWARRAY, T_INT);
        mv.visitFieldInsn(PUTSTATIC, Toolkit.dotToSlash(className), linesArrayName, linesArrayDescription);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLineNumber(3, l1);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();

        mv = super.visitMethod(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, linesArrayMethodName, linesArrayMethodDescription, null, null);
        mv.visitCode();
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitLineNumber(8, l2);
        mv.visitFieldInsn(GETSTATIC, Toolkit.dotToSlash(className), linesArrayName, linesArrayDescription);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();
    }

}