package com.ikokoon.serenity.instrumentation.coverage;

import com.ikokoon.serenity.Collector;
import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.logging.Logger;

/**
 * This class actually enhances the lines to call the collector class which gathers the data on the lines that are executed during the unit tests.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.07.09
 */
public class CoverageMethodAdapter extends MethodVisitor {

    /**
     * The LOGGER for the class.
     */
    private Logger logger = Logger.getLogger(CoverageMethodAdapter.class.getName());

    /**
     * The type of parameters that the {@link Collector} takes in the coverage collection method.
     */
    private Type stringType = Type.getType(String.class);
    /**
     * The type parameter for the line number in the {@link Collector} collect coverage method.
     */
    private Type intType = Type.getType(int.class);
    /**
     * The array of type parameters for the {@link Collector} for the coverage method.
     */
    protected Type[] types = new Type[]{stringType, stringType, stringType, intType};

    /**
     * The name of the class ({@link Collector}) that will be the collector for the method adapter.
     */
    private String collectorClassName = Type.getInternalName(Collector.class);
    /**
     * The coverage method that is called on the {@link Collector} by the added instructions.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private String collectorMethodName = "collectCoverage";
    /**
     * The byte code signature of the coverage method in the {@link Collector}.
     */
    private String collectorMethodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, types);

    /**
     * The name of the class that this method adapter is enhancing the methods for.
     */
    private String className;
    /**
     * The name of the method that is being enhanced.
     */
    private String methodName;
    /**
     * The description of the method being enhanced.
     */
    private String methodDescription;

    /**
     * The constructor initialises a {@link CoverageMethodAdapter} that takes all the interesting items for the method that is to be enhanced
     * including the parent method visitor.
     *
     * @param methodVisitor     the method visitor of the parent
     * @param className         the name of the class the method belongs to
     * @param access            the access to the method
     * @param methodName        the name of the method
     * @param methodDescription the description of the method
     */
    @SuppressWarnings("UnusedParameters")
    public CoverageMethodAdapter(final MethodVisitor methodVisitor, final Integer access, final String className, final String methodName, final String methodDescription) {
        super(Opcodes.ASM5, methodVisitor);
        this.className = Toolkit.slashToDot(className);
        this.methodName = methodName;
        this.methodDescription = methodDescription;
        logger.fine("Class name : " + className + ", name : " + methodName + ", desc : " + methodDescription);
    }

    /**
     * This is the method that actually adds the instructions to the enhanced class. It adds an instruction to call a collector class which then
     * collects the data about each line being called. This method puts five strings onto the stack. These are then popped by the call to the
     * collector class and passed as parameters to the collector method.
     */
    public void visitLineNumber(int lineNumber, Label label) {
        logger.fine("visitLineNumber : " + lineNumber + ", " + label + ", " + label.getOffset() + ", " + className + ", " + methodName);
        this.mv.visitLdcInsn(className);
        this.mv.visitLdcInsn(methodName);
        this.mv.visitLdcInsn(methodDescription);
        this.mv.visitLdcInsn(lineNumber);
        //noinspection deprecation
        this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, collectorClassName, collectorMethodName, collectorMethodDescription);
        this.mv.visitLineNumber(lineNumber, label);
    }

}