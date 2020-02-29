package com.ikokoon.serenity;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.IDataBase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.logging.Logger;

/**
 * This test needs to have assertions. TODO implement the real tests.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19.06.10
 */
public class ProfilerTest extends ATest implements IConstants {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private static IDataBase dataBase;

    @BeforeClass
    public static void beforeClass() {
        ATest.beforeClass();
        String dataBaseFile = "./src/test/resources/isearch/serenity.odb";
        dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, mockInternalDataBase);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void averageMethodNetTime() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double averageMethodNetTime = Profiler.averageMethodNetTime(method);
                logger.info("Average net method time : method : " + method.getName() + " - " + averageMethodNetTime);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void averageMethodTime() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double averageMethodTime = Profiler.averageMethodTime(method);
                logger.info("Average method : method : " + method.getName() + " - " + averageMethodTime);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodChange() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double methodChange = Profiler.methodChange(method);
                logger.info("Method change : method : " + method.getName() + " - " + methodChange);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodChangeSeries() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                List<Double> series = Profiler.methodChangeSeries(method);
                logger.info("Method change series : " + method.getName() + " - " + series);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodNetChange() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double methodNetChange = Profiler.methodNetChange(method);
                logger.info("Method net change : method : " + method.getName() + " - " + methodNetChange);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodNetChangeSeries() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                List<Double> methodNetChangeSeries = Profiler.methodNetChangeSeries(method);
                logger.info("Method net change series : method : " + method.getName() + " - " + methodNetChangeSeries);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodNetSeries() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                List<Double> methodNetSeries = Profiler.methodNetSeries(method);
                logger.info("Method net series : method : " + method.getName() + " - " + methodNetSeries);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodSeries() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                List<Double> series = Profiler.methodSeries(method);
                logger.info("Method series : " + method.getName() + " - " + series);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void totalMethodTime() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double totalMethodTime = Profiler.totalMethodTime(method);
                logger.info("Total method time : method : " + method.getName() + " - " + totalMethodTime);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void totalNetMethodTime() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double totalNetMethodTime = Profiler.totalNetMethodTime(method);
                logger.info("Total net method time : method : " + method.getName() + " - " + totalNetMethodTime);
            }
        }
    }

}