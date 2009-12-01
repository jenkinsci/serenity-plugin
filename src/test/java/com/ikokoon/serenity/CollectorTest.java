package com.ikokoon.serenity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.target.consumer.TargetConsumer;
import com.ikokoon.toolkit.Toolkit;

/**
 * This just tests that the coverage collector doesn't blow up. The tests are for executing the collector for the line that is executed and checking
 * that the package, class, method and line are added to the data model.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class CollectorTest extends ATest implements IConstants {

	@Test
	@SuppressWarnings("unchecked")
	public void collectCoverageLineExecutor() {
		Configuration.getConfiguration().includedPackages.add(packageName);
		Configuration.getConfiguration().includedPackages.add(Toolkit.dotToSlash(packageName));
		// After this we expect a package, a class, a method and a line element
		Collector.collectCoverage(className, Double.toString(lineNumber), methodName, methodSignature);
		// We must test that the package is correct

		List<Object> parameters = new ArrayList<Object>();
		parameters.add(packageName);
		Package pakkage = (Package) dataBase.find(parameters);
		assertNotNull(pakkage);

		// We must test that the class element is correct
		parameters.clear();
		parameters.add(className);
		Class klass = (Class) dataBase.find(parameters);
		assertNotNull(klass);

		// We must test that the method element is correct
		assertTrue(klass.getChildren().size() > 0);

		parameters.clear();
		parameters = new ArrayList<Object>();
		parameters.add(className);
		parameters.add(methodName);
		parameters.add(lineNumber);
		Line line = (Line) dataBase.find(parameters);
		assertNotNull(line);

		assertEquals(1.0, line.getCounter());

		Collector.collectCoverage(className, Double.toString(lineNumber), methodName, methodSignature);

		assertEquals(2.0, line.getCounter());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void collectMetricsInterface() {
		Collector.collectInterface(className, access);
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(className);
		Class klass = (Class) dataBase.find(parameters);
		assertNotNull(klass);
		assertTrue(klass.getInterfaze());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void collectComplexity() {
		Collector.collectComplexity(className, methodName, methodSignature, complexity, 1000);
		Collector.collectComplexity(className, methodName, methodSignature, complexity, 1000);
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(className);
		parameters.add(methodName);
		parameters.add(methodSignature);
		Method method = (Method) dataBase.find(parameters);
		assertNotNull(method);
		assertTrue(complexity == method.getComplexity());

		Collector.collectComplexity(TargetConsumer.class.getName(), methodName + ":1", methodSignature + ":1", complexity, 1000);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void collectMetricsAfferentEfferent() {
		Class toDelete = (Class) dataBase.find(Toolkit.hash(className));
		dataBase.remove(toDelete.getId());
		toDelete = (Class) dataBase.find(Toolkit.hash(className));
		assertNull(toDelete);

		Collector.collectEfferentAndAfferent(className, Logger.class.getName());
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(packageName);
		Package<?, ?> pakkage = (Package) dataBase.find(parameters);
		assertNotNull(pakkage);
		boolean containsLogger = false;
		outer: for (Class<?, ?> klass : ((List<Class<?, ?>>) pakkage.getChildren())) {
			for (Afferent afferent : klass.getAfferent()) {
				if (afferent.getName().indexOf(Logger.class.getPackage().getName()) > -1) {
					containsLogger = true;
					break outer;
				}
			}
		}
		assertTrue(containsLogger);
	}

	@Test
	public void collectLinePerformance() {
		int iterations = 10000;
		double collectionsPerSecond = PerformanceTester.execute(new PerformanceTester.IPerform() {
			public void execute() {
				String lineNumber = Double.toString(System.currentTimeMillis() * Math.random());
				Collector.collectCoverage(className, lineNumber, methodName, methodSignature);
			}
		}, "line collections for collector new line", iterations);
		assertTrue(collectionsPerSecond > 10000);

		final String lineNumber = Double.toString(System.currentTimeMillis() * Math.random());
		collectionsPerSecond = PerformanceTester.execute(new PerformanceTester.IPerform() {
			public void execute() {
				Collector.collectCoverage(className, lineNumber, methodName, methodSignature);
			}
		}, "line counter collections", iterations);
		assertTrue(collectionsPerSecond > 10000);

		collectionsPerSecond = PerformanceTester.execute(new PerformanceTester.IPerform() {
			public void execute() {
				Collector.getLine(className, methodName, methodSignature, lineNumber);
			}
		}, "get line", iterations);
	}

}