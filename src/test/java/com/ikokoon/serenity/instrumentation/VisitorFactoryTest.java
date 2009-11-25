package com.ikokoon.serenity.instrumentation;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.objectweb.asm.ClassVisitor;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.instrumentation.coverage.CoverageClassAdapter;

public class VisitorFactoryTest extends ATest {

	@Test
	@SuppressWarnings("unchecked")
	public void getClassVisitor() {
		byte[] classBytes = getClassBytes(className);
		byte[] sourceBytes = getSourceBytes(className);
		ClassVisitor visitor = VisitorFactory.getClassVisitor(new Class[] { CoverageClassAdapter.class }, className, classBytes, sourceBytes);
		assertNotNull(visitor);
	}

}
