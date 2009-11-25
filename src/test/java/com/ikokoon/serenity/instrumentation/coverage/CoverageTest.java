package com.ikokoon.serenity.instrumentation.coverage;

import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;

import org.junit.Test;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.instrumentation.VisitorFactory;

public class CoverageTest extends ATest {

	@Test
	@SuppressWarnings("unchecked")
	public void visit() {
		byte[] classBytes = getClassBytes(className);
		byte[] sourceBytes = getSourceBytes(className);
		ClassWriter writer = (ClassWriter) VisitorFactory.getClassVisitor(new Class[] { CoverageClassAdapter.class }, className, classBytes,
				sourceBytes);

		classBytes = writer.toByteArray();

		String string = new String(classBytes);
		assertTrue(string.indexOf(Collector.class.getSimpleName()) > -1);

		// Verify the byte code is valid
		CheckClassAdapter.verify(new ClassReader(classBytes), false, new PrintWriter(System.out));

		// Verify each line has a call to collect the coverage
		ClassReader reader = new ClassReader(classBytes);
		writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
		ClassVisitor visitor = new CoverageClassAdapterChecker(writer);
		reader.accept(visitor, 0);
	}

	public class CoverageClassAdapterChecker extends ClassAdapter {

		public CoverageClassAdapterChecker(ClassVisitor visitor) {
			super(visitor);
		}

		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
			MethodAdapter methodAdapter = new CoverageMethodAdapterChecker(methodVisitor);
			return methodAdapter;
		}

	}

	public class CoverageMethodAdapterChecker extends MethodAdapter {

		private boolean isCovered = false;

		private Type stringType = Type.getType(String.class);
		private Type[] types = new Type[] { stringType, stringType, stringType, stringType };

		private String className = Type.getInternalName(Collector.class);
		private String methodName = "collectCoverage";
		private String methodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, types);

		public CoverageMethodAdapterChecker(MethodVisitor methodVisitor) {
			super(methodVisitor);
		}

		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			logger.info("visitMethod:" + opcode + ", " + owner + ", " + name + ", " + desc);
			if (owner.equals(className) && name.equals(methodName) && desc.equals(methodDescription)) {
				isCovered = true;
			}
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		public void visitEnd() {
			super.visitEnd();
			if (!isCovered) {
				throw new RuntimeException("Class not covered : ");
			}
		}

	}

}