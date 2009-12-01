package com.ikokoon.serenity.instrumentation.complexity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.toolkit.Toolkit;

/**
 * TODO - re-do the complexity adapter adding the methods that could be associated to jump instructions.
 * 
 * @author Michael Couck
 * @since 25.11.09
 * @version 01.00
 */
public class ComplexityTest extends ATest {

	@Test
	public void visit() throws Exception {
		visitClass(ComplexityClassAdapter.class, className);
		Class<?, ?> klass = (Class<?, ?>) dataBase.find(Toolkit.hash(className));
		assertNotNull(klass);
		Method<?, ?> method = (Method<?, ?>) dataBase.find(Toolkit.hash(className, methodName, methodSignature));
		assertNotNull(method);
		assertTrue(method.getComplexity() == 22 || method.getComplexity() == 24);
	}

}
