package com.ikokoon.serenity.instrumentation.complexity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.toolkit.Toolkit;

public class ComplexityTest extends ATest {

	@Test
	@SuppressWarnings("unchecked")
	public void visit() throws Exception {
		byte[] classBytes = getClassBytes(className);
		byte[] sourceBytes = getSourceBytes(className);
		VisitorFactory.getClassVisitor(new java.lang.Class[] { ComplexityClassAdapter.class }, className, classBytes, sourceBytes);

		Class<?, ?> klass = (Class<?, ?>) dataBase.find(Toolkit.hash(className));
		assertNotNull(klass);
		Method<?, ?> method = (Method<?, ?>) dataBase.find(Toolkit.hash(className, methodName, methodSignature));
		assertNotNull(method);
		assertTrue(method.getComplexity() == 23);
	}

}
