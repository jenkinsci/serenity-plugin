package com.ikokoon.serenity.instrumentation.coverage;

import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @version 01.00
 * @see CoverageClassAdapterExt
 * @since 19.01.10
 */
public class CoverageMethodAdapterExt extends MethodVisitor {

    private String className;
    private String linesArrayName;
    private String linesArrayDescription;
    private int lines = 0;

    public CoverageMethodAdapterExt(MethodVisitor methodVisitor, String className, String linesArrayName, String linesArrayDescription) {
        super(Opcodes.ASM5, methodVisitor);
        this.className = Toolkit.slashToDot(className);
        this.linesArrayName = linesArrayName;
        this.linesArrayDescription = linesArrayDescription;
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.debug("Class name : " + className + ", lines array name : " + linesArrayName + ", lines array desc : " + linesArrayDescription);
    }

    public void visitLineNumber(int lineNumber, Label label) {
        // At each line increment the line counter for the line
        mv.visitFieldInsn(Opcodes.GETSTATIC, Toolkit.dotToSlash(className), linesArrayName, linesArrayDescription);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.DUP2);
        mv.visitInsn(Opcodes.IALOAD);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IADD);
        mv.visitInsn(Opcodes.IASTORE);
        mv.visitLineNumber(lineNumber, label);
        lines = Math.max(lines, lineNumber);
    }

    public int getLines() {
        return lines;
    }

}