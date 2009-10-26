package com.ikokoon.instrumentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ikokoon.ATest;
import com.ikokoon.IConstants;
import com.ikokoon.instrumentation.model.Afferent;
import com.ikokoon.instrumentation.model.Class;
import com.ikokoon.instrumentation.model.Method;
import com.ikokoon.instrumentation.model.Package;
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
	public void collectCoverageLineExecutor() {
		Configuration.getConfiguration().includedPackages.add(packageName);
		Configuration.getConfiguration().includedPackages.add(Toolkit.dotToSlash(packageName));
		// After this we expect a package, a class, a method and a line element
		Collector.collectCoverage(className, Double.toString(lineNumber), methodName, methodDescription);
		// We must test that the package is correct

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(NAME, packageName);
		Package pakkage = dataBase.find(Package.class, parameters);
		assertNotNull(pakkage);

		// We must test that the class element is correct
		parameters.clear();
		parameters.put(NAME, className);
		Class klass = dataBase.find(Class.class, parameters);
		assertNotNull(klass);
		// We must test that the method element is correct
		parameters.clear();
		parameters.put(PARENT, klass);
		List<Method> methods = dataBase.find(Method.class, parameters, 0, Integer.MAX_VALUE);
		assertTrue(methods.size() > 0);
	}

	@Test
	public void collectCoverageLineCounter() {
		Collector.collectCoverage(className, methodName, methodDescription);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(NAME, className);
		Class klass = dataBase.find(Class.class, parameters);

		parameters.clear();
		parameters.put(PARENT, klass);
		parameters.put(NAME, methodName);
		parameters.put(DESCRIPTION, methodDescription);
		Method method = dataBase.find(Method.class, parameters);
		assertNotNull(method);
		assertEquals(methodName, method.getName());
	}

	@Test
	public void collectMetricsInterface() {
		Collector.collectMetrics(className, access);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(NAME, className);
		Class klass = dataBase.find(Class.class, parameters);
		assertNotNull(klass);
		assertTrue(klass.getInterfaze());
	}

	@Test
	public void collectComplexity() {
		Toolkit.dump(dataBase);
		Collector.collectComplexity(className, methodName, methodDescription, complexity, 1000);
		Toolkit.dump(dataBase);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(NAME, methodName);
		parameters.put(DESCRIPTION, methodDescription);
		Method method = dataBase.find(Method.class, parameters);
		assertNotNull(method);
		assertTrue(complexity == method.getComplexity());
	}

	@Test
	public void collectMetricsAfferentEfferent() {
		Collector.collectMetrics(className, Logger.class.getName());
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(NAME, packageName);
		Package pakkage = dataBase.find(Package.class, parameters);
		assertNotNull(pakkage);
		boolean containsLogger = false;
		outer: for (Class baseKlass : pakkage.getChildren()) {
			Class klass = (Class) baseKlass;
			for (Afferent afferent : klass.getAfferentPackages()) {
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
		String className = this.className;
		String lineNumber = Double.toString(this.lineNumber);
		String methodName = this.methodName;
		String methodDescription = this.methodDescription;
		double start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			if (i % 1000 == 0) {
				logger.info("Iteration : " + i);
			}
			double random = Math.random();
			if (random < 0.1d) {
				className = this.className + random;
			}
			if (random > 0.9d) {
				methodName = this.methodName + random;
			}
			if (random > 0.2d && random < 0.8d) {
				lineNumber = Double.toString(this.lineNumber + random);
			}
			Collector.collectCoverage(className, lineNumber, methodName, methodDescription);
		}
		double end = System.currentTimeMillis();
		double duration = (end - start) / 1000d;
		double collectionsPerSecond = iterations / duration;
		logger.warn("Inserts : Iterations : " + iterations + ", duration : " + duration + ", collections per second : " + collectionsPerSecond);

		start = System.currentTimeMillis();
		lineNumber = Double.toString(this.lineNumber);
		for (int i = 0; i < iterations; i++) {
			Collector.collectCoverage(this.className, lineNumber, this.methodName, this.methodDescription);
		}
		end = System.currentTimeMillis();
		duration = (end - start) / 1000;
		collectionsPerSecond = iterations / duration;
		logger.warn("No inserts : Iterations : " + iterations + ", duration : " + duration + ", collections per second : " + collectionsPerSecond);

		start = System.currentTimeMillis();
		lineNumber = Double.toString(this.lineNumber);
		for (int i = 0; i < iterations; i++) {
			Collector.getLine(className, methodName, methodDescription, lineNumber);
		}
		end = System.currentTimeMillis();
		duration = (end - start) / 1000d;
		collectionsPerSecond = iterations / duration;
		logger.warn("Get line : " + iterations + ", duration : " + duration + ", collections per second : " + collectionsPerSecond);
		dataBase.close();

		// Toolkit.dump(dataBase);
	}

}