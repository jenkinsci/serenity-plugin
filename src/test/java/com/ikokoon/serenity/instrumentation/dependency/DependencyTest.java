package com.ikokoon.serenity.instrumentation.dependency;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.target.consumer.Annotation;
import com.ikokoon.target.consumer.TargetConsumer;
import com.ikokoon.toolkit.Toolkit;

public class DependencyTest extends ATest {

	@Test
	public void visitInner() throws Exception {
		// logger.info("***************************************************");
		// visitClass(DependencyClassAdapter.class, Discovery.class.getName());
		// logger.info("***************************************************");
		// visitClass(DependencyClassAdapter.class, Discovery.InnerClass.class.getName());
		// logger.info("***************************************************");
		// visitClass(DependencyClassAdapter.class, Discovery.InnerClass.InnerInnerClass.class.getName());
	}

	@Test
	public void visit() throws Exception {
		// DataBaseToolkit.clear(dataBase);
		Configuration.getConfiguration().includedPackages.add(className);
		Configuration.getConfiguration().includedPackages.add(packageName);
		Configuration.getConfiguration().includedPackages.add(Logger.class.getPackage().getName());

		visitClass(DependencyClassAdapter.class, className);

		Package<?, ?> pakkage = (Package<?, ?>) dataBase.find(Package.class, Toolkit.hash(packageName));
		assertNotNull(pakkage);
		Class<?, ?> klass = (Class<?, ?>) dataBase.find(Class.class, Toolkit.hash(className));
		assertNotNull(klass);

		List<Afferent> afferent = klass.getAfferent();
		List<Efferent> efferent = klass.getEfferent();
		assertTrue(containsAfferentPackage(afferent, Logger.class.getPackage().getName()));
		assertTrue(containsAfferentPackage(afferent, Serializable.class.getPackage().getName()));
		assertTrue(containsAfferentPackage(afferent, Annotation.class.getPackage().getName()));

		visitClass(DependencyClassAdapter.class, TargetConsumer.class.getName());
		assertTrue(containsEfferentPackage(efferent, TargetConsumer.class.getPackage().getName()));

		dataBase.remove(Package.class, Toolkit.hash(packageName));

		// Test that Annotation has a reference to AnnotationAnnotation and visa versa
		// Test for field annotations and method annotations
		// Test for cyclic references?
	}

	private boolean containsAfferentPackage(List<Afferent> afferent, String name) {
		for (Afferent aff : afferent) {
			logger.debug("Afferent : " + aff);
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
