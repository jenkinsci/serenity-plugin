package com.ikokoon.serenity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.instrumentation.coverage.CoverageClassAdapterChecker;
import com.ikokoon.serenity.instrumentation.coverage.CoverageMethodAdapterChecker;
import com.ikokoon.target.Target;

/**
 * This class tests that the transformer adds the Collector instructions to the class byte code.
 *
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class TransformerTest extends ATest {

	/** The LOGGER for the class. */
	private Instrumentation instrumentation = null; // createMock(Instrumentation.class);
	private ProtectionDomain protectionDomain; // = createMock(ProtectionDomain.class);

	@Before
	public void setUp() {
		// Call the premain to load stuff we need
		Transformer.premain(null, instrumentation);
		Transformer.removeShutdownHook();
	}

	@Test
	public void transform() throws Exception {
		byte[] classBytes = getClassBytes(className);
		byte[] sourceBytes = getSourceBytes(className);
		Exception exception = null;
		try {
			visitClass(CoverageMethodAdapterChecker.class, className, classBytes, sourceBytes);
		} catch (Exception e) {
			exception = e;
		}
		assertNotNull(exception);

		Transformer transformer = new Transformer();
		Class<?> classBeingRedefined = Class.forName(className);
		ClassLoader classLoader = TransformerTest.class.getClassLoader();
		classBytes = transformer.transform(classLoader, className, classBeingRedefined, protectionDomain, classBytes);

		// We need to verify that the collector instructions have been added
		visitClass(CoverageClassAdapterChecker.class, className, classBytes, sourceBytes);
	}

	@Test
	public void excluded() {
		assertTrue(Configuration.getConfiguration().excluded(Object.class.getName()));
		assertFalse(Configuration.getConfiguration().excluded(InputStream.class.getName()));
	}

	@Test
	public void included() {
		assertTrue(Configuration.getConfiguration().included(Target.class.getName()));
		assertFalse(Configuration.getConfiguration().included(Object.class.getName()));
	}

}