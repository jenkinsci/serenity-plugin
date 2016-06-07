package com.ikokoon.toolkit;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.persistence.IDataBase;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class TypeTest extends ATest {

    @Test
    public void test() {
        Type type = Type.getType("Lcom/ikokoon/target/Target<**>;");
        printType(type);
        type = Type.getType("Ljava/util/Set<Ljava/lang/String;>;");
        printType(type);
        type = Type.getType("Ljava/util/List<Ljava/lang/Class<Lorg/objectweb/asm/ClassVisitor;>;>;");
        printType(type);
        type = Type.getType("Ljava/util/Map<Ljava/lang/String;Lcom/ikokoon/serenity/persistence/IDataBase;>;");
        printType(type);

        type = Type.getType("Lcom/ikokoon/target/Target<**>;");
        printType(type);
        type = Type.getType("Ljava/util/Set<Ljava/lang/String;>;");
        printType(type);
        type = Type.getType("Ljava/util/List<Ljava/lang/Class<Lorg/objectweb/asm/ClassVisitor;>;>;");
        printType(type);
        type = Type.getType("Ljava/util/Map<Ljava/lang/String;Lcom/ikokoon/serenity/persistence/IDataBase;>;");
        printType(type);

        String internalName = Type.getInternalName(Collector.class);
        LOGGER.debug("Internal name : " + internalName);
        String methodDescriptor = Type.getMethodDescriptor(Type.getType(String.class), Type.getType(Project.class), Type.getType(IDataBase.class));
        LOGGER.debug("Method descriptor : " + methodDescriptor);
    }

    private void printType(Type type) {
        if (type != null) {
            LOGGER.warn("Type : class name : " + type.getClassName() + ", descriptor : " + type.getDescriptor() + ", internal name : "
                    + type.getInternalName() + ", op code : " + type.getOpcode(Opcodes.ISTORE));
        }
    }

}
