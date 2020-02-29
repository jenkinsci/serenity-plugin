package com.ikokoon.serenity.instrumentation.complexity;

import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.instrumentation.coverage.CoverageMethodAdapter;
import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.logging.Logger;

/**
 * TODO - add the interesting methods to the collection of the complexity. Do we need to add try catch? And what about multiple catch? One for each
 * potential exception thrown? No?
 * <p>
 * This class just visits the byte code in the classes and collects the complexity metrics for the class. Complexity is calculated by adding one every
 * time there is a jump instruction.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.07.09
 */
public class ComplexityMethodAdapter extends MethodVisitor {

    /**
     * The LOGGER for the class.
     */
    private Logger logger = Logger.getLogger(CoverageMethodAdapter.class.getName());

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
     * The complexity counter, start with one and increment for each jump instruction/decision point. This will give the approximate value of the
     * McCabe method:<br>
     * <p>
     * M = E − N + 2P where
     * <p>
     * M = cyclomatic complexity <br>
     * E = the number of edges of the graph<br>
     * N = the number of nodes of the graph<br>
     * P = the number of connected components.<br>
     */
    private int complexityCounter = 1;

    /**
     * The constructor initialises a {@link ComplexityMethodAdapter} that takes all the interesting items for the method that is to be enhanced
     * including the parent method visitor.
     *
     * @param methodVisitor     the method visitor of the parent
     * @param className         the name of the class the method belongs to
     * @param access            the access to the
     * @param methodName        the name of the method that will be collected for complexity
     * @param methodDescription the description of the method, i.e. the byte code signature
     */
    public ComplexityMethodAdapter(MethodVisitor methodVisitor, @SuppressWarnings("UnusedParameters") Integer access, String className, String methodName, String methodDescription) {
        super(Opcodes.ASM5, methodVisitor);
        this.className = Toolkit.slashToDot(className);
        this.methodName = methodName;
        this.methodDescription = methodDescription;
        logger.fine("Class name : " + className + ", name : " + methodName + ", desc : " + methodDescription);
    }

    /**
     * {@inheritDoc}
     */
    public void visitLineNumber(int lineNumber, Label label) {
        logger.fine("visitLineNumber : " + lineNumber + ", " + label + ", " + label.getOffset() + ", " + className + ", " + methodName);
        this.mv.visitLineNumber(lineNumber, label);
    }

    /**
     * {@inheritDoc}
     */
    public void visitJumpInsn(int opcode, Label paramLabel) {
        logger.fine("visitJumpInsn:" + opcode);
        complexityCounter++;
        this.mv.visitJumpInsn(opcode, paramLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void visitEnd() {
        logger.fine("visitEnd:" + className + ", " + methodName + ", " + methodDescription/* + ", " + lineCounter */);
        Collector.collectComplexity(className, methodName, methodDescription, complexityCounter/* , lineCounter */);
        this.mv.visitEnd();
    }

    /**
     * {@inheritDoc}
     */
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        logger.fine("visitTryCatchBlock : " + className + ", " + methodName);
        complexityCounter++;
        this.mv.visitTryCatchBlock(start, end, handler, type);
    }

    /**
     * {@inheritDoc}
     */
    public void visitLookupSwitchInsn(Label dflt, int keys[], Label labels[]) {
        logger.fine("visitlookupSwitchInst : " + className + ", " + methodName);
        complexityCounter++;
        this.mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    /**
     * {@inheritDoc}
     */
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label labels[]) {
        logger.fine("visitTableSwitchInst : " + className + ", " + methodName);
        complexityCounter++;
        this.mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    /**
     * {@inheritDoc}
     */
    public void visitInsn(int opcode) {
        logger.fine("visitInst : " + className + ", " + methodName);
        // I could be an ATHROW. Do we count as a jump instruction?
        this.mv.visitInsn(opcode);
    }

}