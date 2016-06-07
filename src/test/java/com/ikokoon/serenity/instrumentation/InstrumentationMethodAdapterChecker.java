package com.ikokoon.serenity.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InstrumentationMethodAdapterChecker extends MethodVisitor {

    private boolean isCovered = false;
    protected String className;
    private String collectorMethodName;
    private String collectorMethodDescription;

    public InstrumentationMethodAdapterChecker(MethodVisitor methodVisitor, String collectorMethodName, String collectorMethodDescription) {
        super(Opcodes.ASM5, methodVisitor);
        this.collectorMethodName = collectorMethodName;
        this.collectorMethodDescription = collectorMethodDescription;
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, final boolean itf) {
        if (name.equals(collectorMethodName) && desc.equals(collectorMethodDescription)) {
            isCovered = true;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    public void visitEnd() {
        super.visitEnd();
        if (!isCovered) {
            throw new RuntimeException("Class not covered : ");
        }
    }

}
