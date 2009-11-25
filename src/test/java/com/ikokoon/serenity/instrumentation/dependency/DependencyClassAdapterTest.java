package com.ikokoon.serenity.instrumentation.dependency;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.target.consumer.Annotation;
import com.ikokoon.target.consumer.TargetConsumer;
import com.ikokoon.toolkit.Toolkit;

public class DependencyClassAdapterTest extends ATest {

	@Test
	public void visit() throws Exception {
		visitClass(className);
		visitClass(TargetConsumer.class.getName());

		Package<?, ?> pakkage = (Package<?, ?>) dataBase.find(Toolkit.hash(java.lang.Class.forName(className).getPackage().getName()));
		assertNotNull(pakkage);
		Class<?, ?> klass = (Class<?, ?>) dataBase.find(Toolkit.hash(className));
		assertNotNull(klass);

		List<Afferent> afferent = klass.getAfferent();
		List<Efferent> efferent = klass.getEfferent();
		assertTrue(containsAfferentPackage(afferent, Logger.class.getPackage().getName()));
		assertTrue(containsAfferentPackage(afferent, Serializable.class.getPackage().getName()));
		assertTrue(containsAfferentPackage(afferent, Annotation.class.getPackage().getName()));

		assertTrue(containsEfferentPackage(efferent, TargetConsumer.class.getPackage().getName()));
	}

	private void visitClass(String className) {
		String classPath = Toolkit.dotToSlash(className) + ".class";
		InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(classPath);
		byte[] classfileBuffer = Toolkit.getContents(inputStream).toByteArray();
		byte[] sourcefileBuffer = new byte[0];

		ClassReader reader = new ClassReader(classfileBuffer);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
		ClassVisitor visitor = new DependencyClassAdapter(writer, className, classfileBuffer, sourcefileBuffer);
		reader.accept(visitor, 0);
	}

	private boolean containsAfferentPackage(List<Afferent> afferent, String name) {
		for (Afferent aff : afferent) {
			if (aff.getName().indexOf(name) > -1) {
				return true;
			}
		}
		return false;
	}

	private boolean containsEfferentPackage(List<Efferent> efferent, String name) {
		for (Efferent eff : efferent) {
			if (eff.getName().indexOf(name) > -1) {
				return true;
			}
		}
		return false;
	}
}
