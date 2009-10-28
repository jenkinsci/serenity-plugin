package com.ikokoon.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.ikokoon.ATest;
import com.ikokoon.target.Target;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class tests that the transformer adds the Collector instructions to the class byte code.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class TransformerTest extends ATest {

	/** The logger for the class. */
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(TransformerTest.class);
	private Instrumentation instrumentation = null; // createMock(Instrumentation.class);
	private ProtectionDomain protectionDomain; // = createMock(ProtectionDomain.class);

	@Before
	public void setUp() {
		// Call the premain to load stuff we need
		Transformer.premain(null, instrumentation);
	}

	@Test
	public void transform() throws Exception {
		String classPath = Toolkit.dotToSlash(className) + ".class";
		InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(classPath);
		byte[] classfileBuffer = Toolkit.getContents(inputStream).toByteArray();
		String byteCodes = new String(classfileBuffer);
		assertTrue(byteCodes.indexOf(Collector.class.getSimpleName()) == -1);

		Transformer coverageTransformer = new Transformer();
		Class<?> classBeingRedefined = Class.forName(className);
		ClassLoader classLoader = TransformerTest.class.getClassLoader();
		classfileBuffer = coverageTransformer.transform(classLoader, className, classBeingRedefined, protectionDomain, classfileBuffer);

		// We need to verify that the collector instructions have been added
		byteCodes = new String(classfileBuffer);
		assertTrue(byteCodes.indexOf(Collector.class.getSimpleName()) > -1);
	}

	@Test
	public void excluded() {
		assertFalse(Configuration.getConfiguration().excluded(Target.class.getName()));
		assertTrue(Configuration.getConfiguration().excluded(Object.class.getName()));
	}

	@Test
	public void included() {
		assertTrue(Configuration.getConfiguration().included(Target.class.getName()));
		assertFalse(Configuration.getConfiguration().included(Object.class.getName()));
	}

}
