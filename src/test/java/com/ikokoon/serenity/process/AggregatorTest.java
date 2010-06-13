package com.ikokoon.serenity.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.instrumentation.dependency.DependencyClassAdapter;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.aggregator.MethodAggregator;
import com.ikokoon.serenity.process.aggregator.PackageAggregator;
import com.ikokoon.target.discovery.Discovery;
import com.ikokoon.target.discovery.IDiscovery;
import com.ikokoon.toolkit.Executer;
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

	@Before
	public void clear() {
		Configuration.getConfiguration().includedPackages.clear();
		Configuration.getConfiguration().includedPackages.add(Discovery.class.getPackage().getName());
		Configuration.getConfiguration().includedPackages.add("edu.umd.cs.findbugs");
		DataBaseToolkit.clear(dataBase);
		Collector.initialize(dataBase);
	}

	@Test
	public void aggregate() {
		// D:/Eclipse/workspace/serenity/work/jobs/Discovery/builds/2010-02-27_19-28-14/serenity
		double aggregationDuration = Executer.execute(new Executer.IPerform() {
			public void execute() {
				File odbDataBaseFile = new File("./src/test/resources/findbugs/serenity.odb");

				IDataBase odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), null);
				IDataBase ramDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, "ramDatabase.ram", odbDataBase);

				new Aggregator(null, ramDataBase).execute();

				Project<?, ?> project = (Project<?, ?>) ramDataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
				if (project != null) {
					logger.warn(ToStringBuilder.reflectionToString(project));
				} else {
					logger.warn("Project : " + project);
				}

				ramDataBase.close();

				odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), mockInternalDataBase);
				project = (Project<?, ?>) odbDataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
				logger.warn(ToStringBuilder.reflectionToString(project));

				odbDataBase.close();
			}
		}, "AggregatorTest : ", 1);
		logger.warn("Aggregation took : " + aggregationDuration);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void innerClasses() {
		visitClass(DependencyClassAdapter.class, IDiscovery.class.getName());
		visitClass(DependencyClassAdapter.class, Discovery.class.getName());

		Project project = new Project();
		project.setTimestamp(new Date());
		dataBase.persist(project);

		Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash(Discovery.class.getPackage().getName()));
		assertNotNull(pakkage);
		Class<?, ?> klass = dataBase.find(Class.class, Toolkit.hash(Discovery.class.getName()));
		assertNotNull(klass);
		Method<?, ?> method = dataBase.find(Method.class, Toolkit.hash(klass.getName(), "getAnonymousInnerClass", "()V"));
		assertNotNull(method);

		Aggregator aggregator = new Aggregator(null, dataBase);
		aggregator.execute();

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

		// assertEquals(22d, pakkage.getLines());
		// Sigma : (class lines / package lines) * class complexity
		// ((15 / 65) * 13.333333333333332) + ((50 / 65) * 25) = 3.07692 + 19.2307 = 22.30692307692308
		assertEquals(1d, pakkage.getComplexity());
		// ((15 / 65) * 26.666666666666664) + ((50 / 65) * 7.996) = 6.1538 + 6.5107 = 12.298461538461538
		assertEquals(0d, pakkage.getCoverage());
		// i / (i + im) = 1 / 2 = 0.5
		assertEquals(0d, pakkage.getAbstractness());
		// e / (e + a) = 3 / 5 = 0.6666666666666666
		assertEquals(1d, pakkage.getStability());
		// d=|-stability + -abstractness + 1|/sqrt(-1²+-1²) = |-0.6666666666666666 + -0.5 + 1|sqrt(-1sq + -1sq) =
		assertEquals(0.0d, pakkage.getDistance());
		assertEquals(1d, pakkage.getInterfaces());
		assertEquals(5d, pakkage.getImplementations());
		assertEquals(0d, pakkage.getEfference());
		assertEquals(0d, pakkage.getAfference());
		assertEquals(6, pakkage.getChildren().size());
	}

	@Test
	public void aggregateMethods() throws Exception {
		visitClass(DependencyClassAdapter.class, IDiscovery.class.getName());
		visitClass(DependencyClassAdapter.class, Discovery.class.getName());

		Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash(Discovery.class.getPackage().getName()));

		Set<Class<?, ?>> classes = new TreeSet<Class<?, ?>>();
		Set<Method<?, ?>> methods = new TreeSet<Method<?, ?>>();
		Set<Line<?, ?>> lines = new TreeSet<Line<?, ?>>();
		getClassesMethodsAndLines(pakkage, classes, methods, lines);

		for (Method<?, ?> method : methods) {
			new MethodAggregator(dataBase, method).aggregate();
			double executed = getExecuted(method);
			double coverage = (executed / (double) method.getChildren().size()) * 100d;
			assertEquals(Math.round(coverage), Math.round(method.getCoverage()));
		}
	}

	@Test
	public void aggregateClass() throws Exception {
		visitClass(DependencyClassAdapter.class, IDiscovery.class.getName());
		visitClass(DependencyClassAdapter.class, Discovery.class.getName());

		Class<?, ?> klass = dataBase.find(Class.class, Toolkit.hash(Discovery.class.getName()));
		assertEquals(getComplexity(klass), klass.getComplexity());
		assertEquals(getCoverage(klass), klass.getCoverage());
		assertEquals(getStability(klass), klass.getStability());
	}

	@Test
	public void aggregatePackage() throws Exception {
		visitClass(DependencyClassAdapter.class, IDiscovery.class.getName());
		visitClass(DependencyClassAdapter.class, Discovery.class.getName());

		Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash(Discovery.class.getPackage().getName()));

		new PackageAggregator(dataBase, pakkage).aggregate();

		assertEquals(getAbstractness(pakkage), pakkage.getAbstractness());
		assertEquals(getComplexity(pakkage), pakkage.getComplexity());
		assertEquals(getCoverage(pakkage), pakkage.getCoverage());
		assertEquals(getDistance(pakkage), pakkage.getDistance());
		assertEquals(getStability(pakkage), pakkage.getStability());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void aggregateProject() throws Exception {
		// new Accumulator(null).execute();
		// visitClass(DependencyClassAdapter.class, IDiscovery.class.getName());
		// visitClass(DependencyClassAdapter.class, Discovery.class.getName());

		File odbDataBaseFile = new File("./src/test/resources/isearch/serenity.odb");
		IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), null);

		Project<?, ?> project = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		project.setAbstractness(0);
		project.setClasses(0);
		project.setComplexity(0);
		project.setCoverage(0);
		project.setDistance(0);
		project.setLines(0);
		project.setMethods(0);
		project.setPackages(0);
		project.setStability(0);

		dataBase.persist(project);

		DataBaseToolkit.execute(dataBase, new Package(), new DataBaseToolkit.Executer() {
			public void execute(Object object) {
				Package pakkage = (Package) object;
				pakkage.setAbstractness(0);
				pakkage.setAfference(0);
				pakkage.setComplexity(0);
				pakkage.setCoverage(0);
				pakkage.setDistance(0);
				pakkage.setEfference(0);
				pakkage.setExecuted(0);
				pakkage.setImplementations(0);
				pakkage.setInterfaces(0);
				pakkage.setLines(0);
				pakkage.setStability(0);
			}
		});
		DataBaseToolkit.execute(dataBase, new Class(), new DataBaseToolkit.Executer() {
			public void execute(Object object) {
				Class klass = (Class) object;
				klass.setAfference(0);
				klass.setComplexity(0);
				klass.setCoverage(0);
				klass.setEfference(0);
				klass.setStability(0);
			}
		});
		new Aggregator(null, dataBase).execute();
		project = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		assertTrue(project.getAbstractness() != 0);
		assertTrue(project.getClasses() != 0);
		assertTrue(project.getComplexity() != 0);
		assertTrue(project.getCoverage() != 0);
		assertTrue(project.getDistance() != 0);
		assertTrue(project.getLines() != 0);
		assertTrue(project.getMethods() != 0);
		assertTrue(project.getPackages() != 0);
		assertTrue(project.getStability() != 0);
	}

	@SuppressWarnings( { "unused", "unchecked" })
	private double getComplexity(List<Package> pakkages) {
		return 0d;
	}

	@SuppressWarnings( { "unchecked", "unused" })
	private double getAbstractness(List<Package> pakkages) {
		double interfaces = 0d;
		double implementations = 0d;
		for (Package<?, ?> pakkage : pakkages) {
			interfaces += pakkage.getInterfaces();
			implementations += pakkage.getImplementations();
		}
		return interfaces / implementations > 0 ? implementations : 0;
	}

	protected double getStability(Package<?, ?> pakkage) {
		double efferent = 0d;
		double afferent = 0d;
		for (Class<?, ?> klass : pakkage.getChildren()) {
			efferent += klass.getEfferent().size();
			afferent = klass.getAfferent().size();
		}
		double denominator = efferent + afferent;
		return denominator > 0 ? efferent / denominator : 1d;
	}

	protected double getDistance(Package<?, ?> pakkage) {
		// TODO - validate this test - A + I = 1
		double a = -1, b = -1;
		double distance = Math.abs(-pakkage.getStability() + -pakkage.getAbstractness() + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		return distance;
	}

	protected double getCoverage(Package<?, ?> pakkage) {
		double coverage = 0d;
		for (Class<?, ?> klass : pakkage.getChildren()) {
			coverage += klass.getCoverage();
		}
		return coverage / pakkage.getChildren().size();
	}

	protected double getComplexity(Package<?, ?> pakkage) {
		double complexity = 0d;
		for (Class<?, ?> klass : pakkage.getChildren()) {
			complexity += klass.getComplexity();
		}
		complexity = complexity / pakkage.getChildren().size();
		return complexity;
	}

	protected double getAbstractness(Package<?, ?> pakkage) {
		return pakkage.getInterfaces() / pakkage.getImplementations();
	}

	protected double getStability(Class<?, ?> klass) {
		double numerator = klass.getEfferent().size();
		double denominator = klass.getEfferent().size() + klass.getAfferent().size();
		return denominator > 0 ? numerator / denominator : 0d;
	}

	protected double getCoverage(Class<?, ?> klass) {
		double coverage = 0d;
		for (Method<?, ?> method : klass.getChildren()) {
			coverage += method.getCoverage();
		}
		coverage = coverage / (double) klass.getChildren().size() * 100d;
		return coverage;
	}

	protected double getComplexity(Class<?, ?> klass) {
		double complexity = 0d;
		for (Method<?, ?> method : klass.getChildren()) {
			complexity += method.getComplexity();
		}
		complexity = complexity / klass.getChildren().size();
		return Math.max(1, complexity);
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

}