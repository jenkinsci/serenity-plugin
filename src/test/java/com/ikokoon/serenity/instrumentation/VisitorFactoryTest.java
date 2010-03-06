package com.ikokoon.serenity.instrumentation;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.instrumentation.complexity.ComplexityClassAdapter;
import com.ikokoon.serenity.instrumentation.complexity.ComplexityMethodAdapter;
import com.ikokoon.serenity.instrumentation.coverage.CoverageClassAdapter;
import com.ikokoon.serenity.instrumentation.dependency.DependencyClassAdapter;
import com.ikokoon.serenity.instrumentation.profiling.ProfilingClassAdapter;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;

public class VisitorFactoryTest extends ATest {

	private IDataBase dataBase;

	@Before
	public void clear() {
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, internalDataBase);
		DataBaseToolkit.clear(dataBase);
		Collector.setDataBase(dataBase);
	}

	@After
	public void close() {
		dataBase.close();
	}

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

		MethodVisitor methodVisitor = VisitorFactory.getMethodVisitor(new MethodAdapter(null), ComplexityMethodAdapter.class, Opcodes.ACC_PUBLIC,
				className, methodName, "methodDescription");
		assertNotNull(methodVisitor);
	}

}
