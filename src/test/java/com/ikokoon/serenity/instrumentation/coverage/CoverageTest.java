package com.ikokoon.serenity.instrumentation.coverage;

import static org.junit.Assert.assertNotNull;

import java.io.PrintWriter;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.instrumentation.VisitorFactory;

public class CoverageTest extends ATest {

	@Test
	@SuppressWarnings("unchecked")
	public void visit() {
		byte[] classBytes = getClassBytes(className);
		byte[] sourceBytes = getSourceBytes(className);
		// Verify that the coverage instructions are not in the byte code
		Exception exception = null;
		try {
			visitClass(CoverageMethodAdapterChecker.class, classBytes, sourceBytes);
		} catch (Exception e) {
			exception = e;
		}
		assertNotNull(exception);

		// Add the coverage instructions
		ClassWriter writer = (ClassWriter) VisitorFactory.getClassVisitor(new Class[] { CoverageClassAdapter.class }, className, classBytes,
				sourceBytes);
		classBytes = writer.toByteArray();

		// Verify the byte code is valid
		CheckClassAdapter.verify(new ClassReader(classBytes), false, new PrintWriter(System.out));

		// Verify each line has a call to collect the coverage
		visitClass(CoverageClassAdapterChecker.class, classBytes, sourceBytes);
	}

}