package com.ikokoon.serenity.instrumentation.complexity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

/**
 * Tests the complexity of a class. This is a functional test rather than a unit test in fact.
 *
 * @author Michael Couck
 * @since 25.11.09
 * @version 01.00
 */
public class ComplexityTest extends ATest {

	private IDataBase dataBase;

	@Before
	public void clear() {
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, mockInternalDataBase);
		DataBaseToolkit.clear(dataBase);
	}

	@After
	public void close() {
		dataBase.close();
	}

	@Test
	public void visit() throws Exception {
		Collector.setDataBase(dataBase);

		visitClass(ComplexityClassAdapter.class, className);
		Class<?, ?> klass = (Class<?, ?>) dataBase.find(Class.class, Toolkit.hash(className));
		assertNotNull(klass);
		Method<?, ?> method = (Method<?, ?>) dataBase.find(Method.class, Toolkit.hash(className, methodName, methodSignature));
		assertNotNull(method);
		// This assertion depends on the compiler, in some cases the compiler will optimise the code removing
		// a jump instruction
		assertTrue(method.getComplexity() == 23 || method.getComplexity() == 25);
		dataBase.remove(klass.getClass(), klass.getId());
	}

}
