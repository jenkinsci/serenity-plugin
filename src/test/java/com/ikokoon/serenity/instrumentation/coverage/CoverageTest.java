package com.ikokoon.serenity.instrumentation.coverage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.serenity.model.Line;

public class CoverageTest extends ATest {

	@Test
	@SuppressWarnings("unchecked")
	public void visit() throws Exception {
		byte[] classBytes = getClassBytes(className);
		byte[] sourceBytes = getSourceBytes(className);
		// Verify that the coverage instructions are not in the byte code
		Exception exception = null;
		try {
			visitClass(CoverageMethodAdapterChecker.class, className, classBytes, sourceBytes);
		} catch (Exception e) {
			exception = e;
		}
		assertNotNull(exception);

		// Add the coverage instructions
		ByteArrayOutputStream source = new ByteArrayOutputStream();
		source.write(sourceBytes);
		ClassWriter writer = (ClassWriter) VisitorFactory.getClassVisitor(new Class[] { CoverageClassAdapter.class }, className, classBytes, source);
		classBytes = writer.toByteArray();

		// Verify the byte code is valid
		CheckClassAdapter.verify(new ClassReader(classBytes), false, new PrintWriter(System.out));

		// Verify each line has a call to collect the coverage
		visitClass(CoverageClassAdapterChecker.class, className, classBytes, sourceBytes);

		final byte[] finalClassBytes = classBytes;
		final String finalClassName = className;
		// Call the complex method on the target and verify that the Collector was called with the new
		ClassLoader loader = new ClassLoader() {
			public Class loadClass(String className) throws ClassNotFoundException {
				if (className == finalClassName) {
					return this.defineClass(className, finalClassBytes, 0, finalClassBytes.length);
				}
				return super.loadClass(className);
			}
		};
		Thread.currentThread().setContextClassLoader(loader);
		Object target = loader.loadClass(className).newInstance();
		Method method = target.getClass().getDeclaredMethod("complexMethod",
				new Class[] { String.class, String.class, String.class, Integer.class, Integer.class });
		method.invoke(target, new Object[] { "", "", "", 0, 0 });
		// Now verify that the Collector class was called from the added instructions
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(className);
		parameters.add(methodName);
		parameters.add(96d);
		Line<?, ?> line = (Line) dataBase.find(Line.class, parameters);
		assertNotNull(line);
		assertTrue(line.getCounter() > 0);
	}

}