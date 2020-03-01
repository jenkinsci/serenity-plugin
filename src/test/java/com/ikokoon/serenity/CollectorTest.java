package com.ikokoon.serenity;

import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.target.consumer.TargetConsumer;
import com.ikokoon.toolkit.Executer;
import com.ikokoon.toolkit.Toolkit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * This just tests that the coverage collector doesn't blow up. The tests are for executing the collector for the line that is executed and checking
 * that the package, class, method and line are added to the data model.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.07.09
 */
public class CollectorTest extends ATest implements IConstants {

    private IDataBase dataBase;

    @Before
    public void open() {
        dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, mockInternalDataBase);
        DataBaseToolkit.clear(dataBase);
        Configuration.getConfiguration().includedPackages.add(packageName);
        Configuration.getConfiguration().includedPackages.add(Toolkit.dotToSlash(packageName));
        Collector.initialize(dataBase);
    }

    @After
    public void close() {
        dataBase.close();
    }

    @Test
    @SuppressWarnings({"rawtypes"})
    public void collectCoverageLineExecutor() {
        // After this we expect a package, a class, a method and a line element
        // dataBase.close();
        // dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, true, null);
        Collector.collectCoverage(className, methodName, methodDescription, (int) lineNumber);
        /*DataBaseToolkit.dump(dataBase, new DataBaseToolkit.ICriteria() {
            public boolean satisfied(Composite<?, ?> composite) {
                return true;
            }
        }, this.getClass().getSimpleName() + " database dump");*/

        // We must test that the package is correct
        Long packageId = Toolkit.hash(packageName);
        LOGGER.severe("Looking for package with id : " + packageId + ", " + packageName);
        Package pakkage = dataBase.find(Package.class, packageId);
        assertNotNull(pakkage);

        // We must test that the class element is correct
        Long classId = Toolkit.hash(className);
        Class klass = dataBase.find(Class.class, classId);
        assertNotNull(klass);

        // We must test that the method element is correct
        assertTrue(klass.getChildren().size() > 0);

        Long lineId = Toolkit.hash(className, methodName, lineNumber);

        Line line = dataBase.find(Line.class, lineId);
        assertNotNull(line);

        assertEquals(1.0, line.getCounter(), 0);

        Collector.collectCoverage(className, methodName, methodDescription, (int) lineNumber);

        assertEquals(2.0, line.getCounter(), 0);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void collectMetricsInterface() {
        Collector.collectAccess(className, access);
        Class klass = dataBase.find(Class.class, Toolkit.hash(className));
        assertNotNull(klass);
        assertTrue(klass.getInterfaze());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void collectComplexity() {
        Collector.collectComplexity(className, methodName, methodDescription, complexity/* , 1000 */);
        Collector.collectComplexity(className, methodName, methodDescription, complexity/* , 1000 */);

        Method method = dataBase.find(Method.class, Toolkit.hash(className, methodName, methodDescription));
        assertNotNull(method);
        assertTrue(complexity == method.getComplexity());

        Collector.collectComplexity(TargetConsumer.class.getName(), methodName + ":1", methodDescription + ":1", complexity/* , 1000 */);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void collectMetricsAfferentEfferent() {
        Collector.collectCoverage(className, methodName, methodDescription, (int) lineNumber);

        Class toDelete = dataBase.find(Class.class, Toolkit.hash(className));
        dataBase.remove(Class.class, toDelete.getId());
        toDelete = dataBase.find(Class.class, Toolkit.hash(className));
        assertNull(toDelete);

        Collector.collectEfferentAndAfferent(className, Logger.class.getName());

        Package<?, ?> pakkage = (Package) dataBase.find(Package.class, Toolkit.hash(packageName));
        assertNotNull(pakkage);
        // DataBaseToolkit.dump(dataBase, null, null);
        assertEquals("<e:java.util.logging>", pakkage.getChildren().get(0).getEfferent().get(0).getName());
    }

    @Test
    public void collectLinePerformance() {
        int iterations = 10000;
        double executionsPerSecond = Executer.execute(new Executer.IPerform() {
            public void execute() {
                double lineNumber = System.currentTimeMillis() * Math.random();
                Collector.collectCoverage(className, methodName, methodDescription, (int) lineNumber);
            }
        }, "line collections for collector new line", iterations);
        assertTrue(executionsPerSecond > 1000);

        final double lineNumber = System.currentTimeMillis() * Math.random();
        executionsPerSecond = Executer.execute(new Executer.IPerform() {
            public void execute() {
                Collector.collectCoverage(className, methodName, methodDescription, (int) lineNumber);
            }
        }, "line counter collections", iterations);
        assertTrue(executionsPerSecond > 1000);
    }

}