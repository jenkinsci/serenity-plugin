package com.ikokoon.instrumentation;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import com.ikokoon.ATest;
import com.ikokoon.instrumentation.coverage.CoverageClassAdapter;
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
	private Instrumentation instrumentation = createMock(Instrumentation.class);
	private ProtectionDomain protectionDomain; // = createMock(ProtectionDomain.class);

	@Before
	public void setUp() {
		// Call the premain to load stuff we need
		Transformer.premain(null, instrumentation);
		Configuration.getConfiguration().includedPackages.add(Target.class.getPackage().getName());
		System.setProperty(Transformer.INCLUDED_ADAPTERS_PROPERTY, CoverageClassAdapter.class.getName());
	}

	@Test
	public void transform() throws Exception {
		String classPath = Toolkit.dotToSlash(Target.class.getName()) + ".class";
		InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(classPath);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int read = -1;
		byte[] bytes = new byte[1024];
		while ((read = inputStream.read(bytes)) > -1) {
			byteArrayOutputStream.write(bytes, 0, read);
		}
		byte[] classfileBuffer = byteArrayOutputStream.toByteArray();
		String byteCodes = new String(classfileBuffer);
		assertTrue(byteCodes.indexOf(Collector.class.getSimpleName()) == -1);

		Transformer coverageTransformer = new Transformer();
		String className = Target.class.getName();
		Class<Target> classBeingRedefined = Target.class;
		ClassLoader classLoader = TransformerTest.class.getClassLoader();
		classfileBuffer = coverageTransformer.transform(classLoader, className, classBeingRedefined, protectionDomain, classfileBuffer);

		// We need to verify that the collector instructions have been added
		byteCodes = new String(classfileBuffer);
		assertTrue(byteCodes.indexOf(Collector.class.getSimpleName()) > -1);

		ClassVisitor classVisitor = null; // new ClassAdapter();

	}

	private ClassVisitor getVerifier(byte[] classfileBuffer) {
		ClassReader reader = new ClassReader(classfileBuffer);
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassVisitor classVisitor = new ClassAdapter(classWriter) {

		};
		reader.accept(classVisitor, 0);
		return classVisitor;
	}

	public class VerifierClassAdapter extends ClassAdapter {
		public VerifierClassAdapter(ClassVisitor visitor) {
			super(visitor);
		}
	}

	public class VerifierMethodAdapter extends MethodAdapter {
		public VerifierMethodAdapter(MethodVisitor visitor) {
			super(visitor);
		}
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
