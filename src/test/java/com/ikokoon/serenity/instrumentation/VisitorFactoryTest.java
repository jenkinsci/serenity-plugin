package com.ikokoon.serenity.instrumentation;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;

import org.junit.Test;
import org.objectweb.asm.ClassVisitor;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.instrumentation.complexity.ComplexityClassAdapter;
import com.ikokoon.serenity.instrumentation.coverage.CoverageClassAdapter;
import com.ikokoon.serenity.instrumentation.dependency.DependencyClassAdapter;
import com.ikokoon.serenity.instrumentation.profiling.ProfilingClassAdapter;

public class VisitorFactoryTest extends ATest {

	@Test
	@SuppressWarnings("unchecked")
	public void getClassVisitor() throws Exception {
		byte[] classBytes = getClassBytes(className);
		byte[] sourceBytes = getSourceBytes(className);
		Class[] adapters = new Class[] { CoverageClassAdapter.class, ComplexityClassAdapter.class, DependencyClassAdapter.class,
				ProfilingClassAdapter.class };

		ByteArrayOutputStream source = new ByteArrayOutputStream();
		source.write(sourceBytes);
		ClassVisitor visitor = VisitorFactory.getClassVisitor(adapters, className, classBytes, source);
		assertNotNull(visitor);
	}

}
