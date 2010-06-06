package com.ikokoon.serenity.instrumentation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.instrumentation.profiling.ProfilingClassAdapter;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

public class InstrumentationTest extends ATest {

	private IDataBase dataBase;

	@Before
	public void clear() {
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, mockInternalDataBase);
		DataBaseToolkit.clear(dataBase);
		Collector.setDataBase(dataBase);
	}

	@Test(expected = RuntimeException.class)
	public void checkNoProfilingInstructions() throws IOException {
		byte[] classBytes = getClassBytes(this.className);
		byte[] sourceBytes = getSourceBytes(this.className);
		// Verify that the profiling instructions are not in the byte code
		ByteArrayOutputStream source = new ByteArrayOutputStream();
		source.write(sourceBytes);
		ClassReader reader = new ClassReader(classBytes);
		ClassWriter writer = new ClassWriter(reader, true);
		// ClassVisitor classVisitor, Class<?> methodVisitorClass, String collectorMethodName, String collectorMethodDescription
		ClassVisitor visitor = new InstrumentationClassAdapterChecker(writer, IConstants.collectStart, IConstants.profilingMethodDescription);
		logger.debug("Adding class visitor : " + visitor);
		reader.accept(visitor, false);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void checkProfilingInstructions() throws Exception {
		byte[] classBytes = getClassBytes(this.className);
		byte[] sourceBytes = getSourceBytes(this.className);

		// Add the profiling instructions
		ByteArrayOutputStream source = new ByteArrayOutputStream();
		source.write(sourceBytes);
		ClassReader reader = new ClassReader(classBytes);
		ClassWriter writer = new ClassWriter(reader, true);
		ProfilingClassAdapter profilingClassAdapter = new ProfilingClassAdapter(writer, className);
		reader.accept(profilingClassAdapter, false);
		classBytes = writer.toByteArray();

		// Verify that the profiling instructions are not in the byte code
		reader = new ClassReader(classBytes);
		writer = new ClassWriter(reader, true);
		// ClassVisitor classVisitor, Class<?> methodVisitorClass, String collectorMethodName, String collectorMethodDescription
		InstrumentationClassAdapterChecker instrumentationClassAdapterChecker = new InstrumentationClassAdapterChecker(writer,
				IConstants.collectStart, IConstants.profilingMethodDescription);
		reader.accept(instrumentationClassAdapterChecker, false);

		File file = new File("./" + this.className);
		file.createNewFile();
		Toolkit.setContents(file, classBytes);
		logger.warn("Wrote class to : " + file.getAbsolutePath());

		final byte[] finalClassBytes = classBytes;
		final String finalClassName = this.className;
		// Call the complex method on the target and verify that the Collector was called with the new instructions
		ClassLoader loader = new ClassLoader() {
			public java.lang.Class<?> loadClass(String className) throws ClassNotFoundException {
				if (className == finalClassName) {
					return this.defineClass(className, finalClassBytes, 0, finalClassBytes.length);
				}
				return super.loadClass(className);
			}
		};
		Thread.currentThread().setContextClassLoader(loader);
		Object target = loader.loadClass(className).newInstance();
		Toolkit.executeMethod(target, methodName, new Object[] { "", "", "", 1, 2 });
		DataBaseToolkit.dump(dataBase, null, "Dump after profiling");

		Method<Class<?, ?>, Line<?, ?>> method = dataBase.find(Method.class, Arrays.asList(className, methodName,
				methodDescription));
		logger.warn("Method : " + method);
	}

	@After
	public void close() {
		dataBase.close();
	}

}