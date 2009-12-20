package com.ikokoon.serenity.instrumentation.dependency;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.target.consumer.Annotation;
import com.ikokoon.target.consumer.TargetConsumer;
import com.ikokoon.toolkit.Toolkit;

public class DependencyTest extends ATest {

	@Test
	public void visit() throws Exception {
		visitClass(DependencyClassAdapter.class, className);
		visitClass(DependencyClassAdapter.class, TargetConsumer.class.getName());

		Package<?, ?> pakkage = (Package<?, ?>) dataBase.find(Package.class, Toolkit.hash(java.lang.Class.forName(className).getPackage().getName()));
		assertNotNull(pakkage);
		Class<?, ?> klass = (Class<?, ?>) dataBase.find(Class.class, Toolkit.hash(className));
		assertNotNull(klass);

		List<Afferent> afferent = klass.getAfferent();
		List<Efferent> efferent = klass.getEfferent();
		assertTrue(containsAfferentPackage(afferent, Logger.class.getPackage().getName()));
		assertTrue(containsAfferentPackage(afferent, Serializable.class.getPackage().getName()));
		assertTrue(containsAfferentPackage(afferent, Annotation.class.getPackage().getName()));

		assertTrue(containsEfferentPackage(efferent, TargetConsumer.class.getPackage().getName()));

		// Test that Annotation has a reference to AnnotationAnnotation and visa versa
		// Test for field annotations and method annotations
		// Test for cyclic references?
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
