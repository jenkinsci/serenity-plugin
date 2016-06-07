package com.ikokoon.serenity.instrumentation.coverage;

import com.ikokoon.serenity.Collector;
import org.apache.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class CoverageMethodAdapterChecker extends MethodVisitor {

    private Logger logger = Logger.getLogger(this.getClass());
    private boolean isCovered = false;

    private Type stringType = Type.getType(String.class);
    private Type intType = Type.getType(int.class);
    private Type[] types = new Type[]{stringType, stringType, stringType, intType};

    private String className = Type.getInternalName(Collector.class);
    @SuppressWarnings("FieldCanBeLocal")
    private String methodName = "collectCoverage";
    private String methodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, types);

    public CoverageMethodAdapterChecker(MethodVisitor methodVisitor) {
        super(Opcodes.ASM5, methodVisitor);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean inf) {
        logger.debug("visitMethod:" + opcode + ", " + owner + ", " + name + ", " + desc);
        if (owner.equals(className) && name.equals(methodName) && desc.equals(methodDescription)) {
            isCovered = true;
        }
        super.visitMethodInsn(opcode, owner, name, desc, inf);
    }

    public void visitEnd() {
        super.visitEnd();
        if (!isCovered) {
            throw new RuntimeException("Class not covered : ");
        }
    }

}
