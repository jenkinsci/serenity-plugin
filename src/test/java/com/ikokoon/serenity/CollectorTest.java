package com.ikokoon.serenity;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
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
		Collector.collectCoverage(className, Double.toString(lineNumber), methodName, methodDescription);
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
		Collector.collectComplexity(className, methodName, methodDescription, complexity, 1000);
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(className);
		parameters.add(methodName);
		parameters.add(methodDescription);
		Method method = (Method) dataBase.find(parameters);
		assertNotNull(method);
		assertTrue(complexity == method.getComplexity());
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
		int iterations = 1000;
		// String className = this.className;
		String lineNumber = Double.toString(this.lineNumber);
		String methodName = this.methodName;
		String methodDescription = this.methodDescription;
		double start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			if (i % 1000 == 0) {
				logger.info("Iteration : " + i);
			}
			// className = this.className + i;
			methodName = this.methodName + i;
			lineNumber = Double.toString(this.lineNumber + i);
			Collector.collectCoverage(this.className, lineNumber, methodName, methodDescription);
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
		// Toolkit.dump(dataBase);
	}

}