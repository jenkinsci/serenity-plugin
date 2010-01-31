package com.ikokoon.toolkit;

import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.persistence.IDataBase;

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

		type = Type.getObjectType("Lcom/ikokoon/target/Target<**>;");
		printType(type);
		type = Type.getObjectType("Ljava/util/Set<Ljava/lang/String;>;");
		printType(type);
		type = Type.getObjectType("Ljava/util/List<Ljava/lang/Class<Lorg/objectweb/asm/ClassVisitor;>;>;");
		printType(type);
		type = Type.getObjectType("Ljava/util/Map<Ljava/lang/String;Lcom/ikokoon/serenity/persistence/IDataBase;>;");
		printType(type);

		Type[] argumentTypes = Type.getArgumentTypes("(Ljava/util/Map<Ljava/lang/String;Lcom/ikokoon/serenity/persistence/IDataBase;>;)"
				+ "Ljava/util/Map<Ljava/lang/String;Lcom/ikokoon/serenity/persistence/IDataBase;>;");
		for (Type argumentType : argumentTypes) {
			printType(argumentType);
		}
		String internalName = Type.getInternalName(Collector.class);
		logger.debug("Internal name : " + internalName);
		String methodDescriptor = Type.getMethodDescriptor(Type.getType(String.class), new Type[] { Type.getType(Project.class),
				Type.getType(IDataBase.class) });
		logger.debug("Method descriptor : " + methodDescriptor);
	}

	private void printType(Type type) {
		if (type != null) {
			logger.debug("Type : class name : " + type.getClassName() + ", descriptor : " + type.getDescriptor() + ", internal name : "
					+ type.getInternalName() + ", op code : " + type.getOpcode(Opcodes.ISTORE));
		}
	}

}
