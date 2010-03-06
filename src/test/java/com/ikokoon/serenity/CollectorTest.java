package com.ikokoon.serenity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.target.consumer.TargetConsumer;
import com.ikokoon.toolkit.PerformanceTester;
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

	private IDataBase dataBase;

	@Before
	public void open() {
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, internalDataBase);
		DataBaseToolkit.clear(dataBase);
		Configuration.getConfiguration().includedPackages.add(packageName);
		Configuration.getConfiguration().includedPackages.add(Toolkit.dotToSlash(packageName));
		Collector.setDataBase(dataBase);
	}

	@After
	public void close() {
		dataBase.close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void collectCoverageLineExecutor() {
		// After this we expect a package, a class, a method and a line element
		// dataBase.close();
		// dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, true, null);
		Collector.collectCoverage(className, methodName, methodSignature, (int) lineNumber);
		DataBaseToolkit.dump(dataBase, new DataBaseToolkit.ICriteria() {
			public boolean satisfied(Composite<?, ?> composite) {
				return true;
			}
		}, this.getClass().getSimpleName() + " database dump");

		// We must test that the package is correct
		Long packageId = Toolkit.hash(packageName);
		logger.warn("Looking for package with id : " + packageId + ", " + packageName);
		Package pakkage = (Package) dataBase.find(Package.class, packageId);
		assertNotNull(pakkage);

		// We must test that the class element is correct
		Long classId = Toolkit.hash(className);
		Class klass = (Class) dataBase.find(Class.class, classId);
		assertNotNull(klass);

		// We must test that the method element is correct
		assertTrue(klass.getChildren().size() > 0);

		Long lineId = Toolkit.hash(className, methodName, lineNumber);

		Line line = (Line) dataBase.find(Line.class, lineId);
		assertNotNull(line);

		assertEquals(1.0, line.getCounter());

		Collector.collectCoverage(className, methodName, methodSignature, (int) lineNumber);

		assertEquals(2.0, line.getCounter());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void collectMetricsInterface() {
		Collector.collectAccess(className, access);
		Class klass = (Class) dataBase.find(Class.class, Toolkit.hash(className));
		assertNotNull(klass);
		assertTrue(klass.getInterfaze());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void collectComplexity() {
		Collector.collectComplexity(className, methodName, methodSignature, complexity/* , 1000 */);
		Collector.collectComplexity(className, methodName, methodSignature, complexity/* , 1000 */);

		Method method = (Method) dataBase.find(Method.class, Toolkit.hash(className, methodName, methodSignature));
		assertNotNull(method);
		assertTrue(complexity == method.getComplexity());

		Collector.collectComplexity(TargetConsumer.class.getName(), methodName + ":1", methodSignature + ":1", complexity/* , 1000 */);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void collectMetricsAfferentEfferent() {
		Collector.collectCoverage(className, methodName, methodSignature, (int) lineNumber);

		Class toDelete = (Class) dataBase.find(Class.class, Toolkit.hash(className));
		dataBase.remove(Class.class, toDelete.getId());
		toDelete = (Class) dataBase.find(Class.class, Toolkit.hash(className));
		assertNull(toDelete);

		Collector.collectEfferentAndAfferent(className, Logger.class.getName());

		Package<?, ?> pakkage = (Package) dataBase.find(Package.class, Toolkit.hash(packageName));
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
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.IPerform() {
			public void execute() {
				double lineNumber = System.currentTimeMillis() * Math.random();
				Collector.collectCoverage(className, methodName, methodSignature, (int) lineNumber);
			}
		}, "line collections for collector new line", iterations);
		assertTrue(executionsPerSecond > 1000);

		final double lineNumber = System.currentTimeMillis() * Math.random();
		executionsPerSecond = PerformanceTester.execute(new PerformanceTester.IPerform() {
			public void execute() {
				Collector.collectCoverage(className, methodName, methodSignature, (int) lineNumber);
			}
		}, "line counter collections", iterations);
		assertTrue(executionsPerSecond > 1000);
	}

}