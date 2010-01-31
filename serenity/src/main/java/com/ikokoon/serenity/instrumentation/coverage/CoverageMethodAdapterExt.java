package com.ikokoon.serenity.instrumentation.coverage;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.toolkit.Toolkit;

/**
 * @see CoverageClassAdapterExt
 * @author Michael Couck
 * @since 19.01.10
 * @version 01.00
 */
public class CoverageMethodAdapterExt extends MethodAdapter {

	private Logger logger = Logger.getLogger(CoverageMethodAdapterExt.class);
	private String className;
	private String linesArrayName;
	private String linesArrayDescription;
	public static int lines = 0;

	public CoverageMethodAdapterExt(MethodVisitor methodVisitor, String className, String linesArrayName, String linesArrayDescription) {
		super(methodVisitor);
		this.className = Toolkit.slashToDot(className);
		this.linesArrayName = linesArrayName;
		this.linesArrayDescription = linesArrayDescription;
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

}