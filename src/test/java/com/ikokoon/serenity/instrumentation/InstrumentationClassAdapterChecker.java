package com.ikokoon.serenity.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InstrumentationClassAdapterChecker extends ClassVisitor {

    private String collectorMethodName;
    private String collectorMethodDescription;

    public InstrumentationClassAdapterChecker(ClassVisitor classVisitor, String collectorMethodName, String collectorMethodDescription) {
        super(Opcodes.ASM5, classVisitor);
        this.collectorMethodName = collectorMethodName;
        this.collectorMethodDescription = collectorMethodDescription;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
        super.visit(version, access, className, signature, superName, interfaces);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        // MethodVisitor methodVisitor, String collectorMethodName, String collectorMethodDescription
        return new InstrumentationMethodAdapterChecker(methodVisitor, collectorMethodName, collectorMethodDescription);
    }

}
