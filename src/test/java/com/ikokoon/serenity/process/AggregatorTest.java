package com.ikokoon.serenity.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.target.discovery.Discovery;
import com.ikokoon.toolkit.Toolkit;

/**
 * This is the test for the aggregator. The aggregator takes the collected data on the methods, classes and packages and calculates the metrics like
 * the abstractness the stability and so on.
 * 
 * @author Michael Couck
 * @since 02.08.09
 * @version 01.00
 */
public class AggregatorTest extends ATest implements IConstants {

	static {
		Configuration.getConfiguration().includedPackages.clear();
		Configuration.getConfiguration().includedPackages.add(Discovery.class.getPackage().getName());
		Configuration.getConfiguration().includedPackages.add("edu.umd.cs.findbugs");
	}

	@Before
	public void before() {
		DataBaseToolkit.clear(dataBase);
		new Accumulator(null).execute();
		new Cleaner(null, dataBase).execute();
		new Aggregator(null, dataBase).execute();
	}

	@Test
	public void innerClasses() {
		Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash(Discovery.class.getPackage().getName()));
		assertNotNull(pakkage);
		Class<?, ?> klass = dataBase.find(Class.class, Toolkit.hash(Discovery.class.getName()));
		assertNotNull(klass);
		Method<?, ?> method = dataBase.find(Method.class, Toolkit.hash(klass.getName(), "getAnonymousInnerClass", "()V"));
		assertNotNull(method);

		assertEquals(0d, method.getCoverage());
		assertEquals(3, method.getChildren().size());

		// Check the class
		assertEquals(4, klass.getChildren().size()); // 10 + 15
		// Sigma n=1, n, (method lines / class lines) * method complexity
		// ((10 / 15) * 10) + ((5 / 15) * 20) = 6.666r + 6.6666r = 13.3333r
		assertEquals(1d, klass.getComplexity());
		// ((10 / 15) * 20) + ((5 / 15) * 40) = 13.33r + 13.333r =
		assertEquals(0d, klass.getCoverage());
		// e / e + a = 2 / 2 + 1 = 0.666r
		assertEquals(1d, klass.getStability());
		assertEquals(0d, klass.getEfference());
		assertEquals(0d, klass.getAfference());
		assertEquals(false, klass.getInterfaze());

		assertEquals(22d, pakkage.getLines());
		// Sigma : (class lines / package lines) * class complexity
		// ((15 / 65) * 13.333333333333332) + ((50 / 65) * 25) = 3.07692 + 19.2307 = 22.30692307692308
		assertEquals(0d, pakkage.getComplexity());
		// ((15 / 65) * 26.666666666666664) + ((50 / 65) * 7.996) = 6.1538 + 6.5107 = 12.298461538461538
		assertEquals(0d, pakkage.getCoverage());
		// i / (i + im) = 1 / 2 = 0.5
		assertEquals(0d, pakkage.getAbstractness());
		// e / (e + a) = 3 / 5 = 0.6666666666666666
		assertEquals(1d, pakkage.getStability());
		// d=|-stability + -abstractness + 1|/sqrt(-1²+-1²) = |-0.6666666666666666 + -0.5 + 1|sqrt(-1sq + -1sq) =
		assertEquals(0.0d, pakkage.getDistance());
		assertEquals(0d, pakkage.getInterfaces());
		assertEquals(7d, pakkage.getImplementations());
		assertEquals(0d, pakkage.getEfference());
		assertEquals(0d, pakkage.getAfference());
		assertEquals(7, pakkage.getChildren().size());
	}

	@Test
	public void aggregateMethods() throws Exception {
		Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash(Discovery.class.getPackage().getName()));
		randomize(pakkage);

		Set<Class<?, ?>> classes = new TreeSet<Class<?, ?>>();
		Set<Method<?, ?>> methods = new TreeSet<Method<?, ?>>();
		Set<Line<?, ?>> lines = new TreeSet<Line<?, ?>>();
		getClassesMethodsAndLines(pakkage, classes, methods, lines);

		new Aggregator(null, dataBase).aggregateMethods(Arrays.asList(methods.toArray(new Method<?, ?>[methods.size()])));
		for (Method<?, ?> method : methods) {
			double executed = getExecuted(method);
			double coverage = (executed / (double) method.getChildren().size()) * 100d;
			assertEquals(Math.round(coverage), Math.round(method.getCoverage()));
		}
	}

	@Test
	public void aggregateClass() throws Exception {
		Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash(Discovery.class.getPackage().getName()));
		randomize(pakkage);
		for (Class<?, ?> klass : pakkage.getChildren()) {
			new Aggregator(null, dataBase).aggregateClass(klass);
			assertEquals(getComplexity(klass), klass.getComplexity());
			assertEquals(getCoverage(klass), klass.getCoverage());
			assertEquals(getStability(klass), klass.getStability());
		}
	}

	@Test
	public void aggregatePackage() throws Exception {
		Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash(Discovery.class.getPackage().getName()));
		randomize(pakkage);
		new Aggregator(null, dataBase).aggregatePackage(pakkage);
		assertEquals(getAbstractness(pakkage), pakkage.getAbstractness());
		assertEquals(getComplexity(pakkage), pakkage.getComplexity());
		assertEquals(getCoverage(pakkage), pakkage.getCoverage());
		assertEquals(getDistance(pakkage), pakkage.getDistance());
		assertEquals(getStability(pakkage), pakkage.getStability());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void aggregateProject() throws Exception {
		Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash(Discovery.class.getPackage().getName()));
		randomize(pakkage);
		List<Package> pakkages = dataBase.find(Package.class);

		Project<?, ?> project = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));

		new Aggregator(null, dataBase).aggregateProject(project);

		assertEquals(getAbstractness(pakkages), project.getAbstractness());
		project.getComplexity();
		project.getCoverage();
		project.getDistance();
		project.getLines();
		project.getMethods();
		project.getPackages();
		project.getStability();
		project.getClasses();
	}

	@SuppressWarnings( { "unused", "unchecked" })
	private double getComplexity(List<Package> pakkages) {
		return 0d;
	}

	@SuppressWarnings("unchecked")
	private double getAbstractness(List<Package> pakkages) {
		double interfaces = 0d;
		double implementations = 0d;
		for (Package<?, ?> pakkage : pakkages) {
			interfaces += pakkage.getInterfaces();
			implementations += pakkage.getImplementations();
		}
		return interfaces / implementations > 0 ? implementations : 0;
	}

	private double getStability(Package<?, ?> pakkage) {
		double efferent = 0d;
		double afferent = 0d;
		for (Class<?, ?> klass : pakkage.getChildren()) {
			efferent += klass.getEfferent().size();
			afferent = klass.getAfferent().size();
		}
		double denominator = efferent + afferent;
		return denominator > 0 ? efferent / denominator : 1d;
	}

	private double getDistance(Package<?, ?> pakkage) {
		// TODO - validate this test - A + I = 1
		double a = -1, b = -1;
		double distance = Math.abs(-pakkage.getStability() + -pakkage.getAbstractness() + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		return distance;
	}

	private double getCoverage(Package<?, ?> pakkage) {
		double coverage = 0d;
		for (Class<?, ?> klass : pakkage.getChildren()) {
			coverage += klass.getCoverage();
		}
		return coverage / pakkage.getChildren().size();
	}

	private double getComplexity(Package<?, ?> pakkage) {
		double complexity = 0d;
		for (Class<?, ?> klass : pakkage.getChildren()) {
			complexity += klass.getComplexity();
		}
		return complexity / pakkage.getChildren().size();
	}

	private double getAbstractness(Package<?, ?> pakkage) {
		return pakkage.getInterfaces() / pakkage.getImplementations();
	}

	private double getStability(Class<?, ?> klass) {
		double numerator = klass.getEfferent().size();
		double denominator = klass.getEfferent().size() + klass.getAfferent().size();
		return denominator > 0 ? numerator / denominator : 1d;
	}

	private double getCoverage(Class<?, ?> klass) {
		double coverage = 0d;
		for (Method<?, ?> method : klass.getChildren()) {
			coverage += method.getCoverage();
		}
		coverage = coverage / (double) klass.getChildren().size() * 100d;
		return coverage;
	}

	private double getComplexity(Class<?, ?> klass) {
		double complexity = 0d;
		for (Method<?, ?> method : klass.getChildren()) {
			complexity += method.getComplexity();
		}
		complexity = complexity / klass.getChildren().size();
		return complexity;
	}

	private void getClassesMethodsAndLines(Package<?, ?> pakkage, Set<Class<?, ?>> classes, Set<Method<?, ?>> methods, Set<Line<?, ?>> lines) {
		for (Class<?, ?> klass : pakkage.getChildren()) {
			classes.add(klass);
			for (Method<?, ?> method : klass.getChildren()) {
				methods.add(method);
				for (Line<?, ?> line : method.getChildren()) {
					lines.add(line);
				}
			}
		}

	}

	private double getExecuted(Method<?, ?> method) {
		double executed = 0d;
		for (Line<?, ?> line : method.getChildren()) {
			if (line.getCounter() > 0d) {
				executed++;
			}
		}
		return executed;
	}

	@SuppressWarnings("unchecked")
	private void randomize(Package<?, ?> pakkage) {
		assertNotNull(pakkage);
		for (Class<?, ?> klass : pakkage.getChildren()) {
			List<Afferent> afferent = new ArrayList<Afferent>();
			List<Efferent> efferent = new ArrayList<Efferent>();
			for (double i = Math.random() * 10; i > 0; i--) {
				Afferent a = new Afferent();
				a.setName("" + i);
				a.setParent((Composite) klass);
			}
			for (double i = Math.random() * 10; i > 0; i--) {
				Efferent e = new Efferent();
				e.setName("" + i);
				e.setParent((Composite) klass);
			}
			klass.setAfferent(afferent);
			klass.setEfferent(efferent);
			for (Method<?, ?> method : klass.getChildren()) {
				double complexity = Math.round(Math.random() * 10);
				method.setComplexity(complexity);
				for (Line<?, ?> line : method.getChildren()) {
					if (line.getCounter() <= 0d) {
						if (Math.random() > 0.5) {
							line.setCounter(1d);
						}
					}
				}
			}
		}
	}

}