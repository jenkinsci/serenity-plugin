package com.ikokoon.serenity.instrumentation.coverage;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CoverageClassAdapterChecker extends ClassVisitor {

    public CoverageClassAdapterChecker(ClassVisitor visitor) {
        super(Opcodes.ASM5, visitor);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return VisitorFactory.getMethodVisitor(methodVisitor, CoverageMethodAdapterChecker.class, access, null, name, desc);
    }

}
