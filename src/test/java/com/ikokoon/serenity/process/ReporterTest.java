package com.ikokoon.serenity.process;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.Profiler;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

/**
 * This test needs to have assertions. TODO implement the real tests.
 *
 * @author Michael Couck
 * @since 19.06.10
 * @version 01.00
 */
@Ignore
public class ReporterTest extends ATest {

	private static IDataBase dataBase;

	@BeforeClass
	public static void beforeClass() {
		ATest.beforeClass();
		String dataBaseFile = "./src/test/resources/isearch/serenity.odb";
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, mockInternalDataBase);
	}

	@AfterClass
	public static void afterClass() {
		ATest.afterClass();
		dataBase.close();
	}

	@Test
	public void methodSeries() throws Exception {
		System.setProperty(IConstants.TIME_UNIT, "1000");
		String dataBaseFile = "./src/test/resources/profiler/serenity.odb";
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, mockInternalDataBase);
		Profiler.initialize(dataBase);
		String html = new Reporter(null, dataBase).methodSeries(dataBase);
		File file = new File(IConstants.METHOD_SERIES_FILE);
		Toolkit.setContents(file, html.getBytes());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void buildGraph() {
		File chartDirectory = new File(IConstants.SERENITY_DIRECTORY + File.separatorChar + IConstants.CHARTS);
		Toolkit.deleteFile(chartDirectory, 3);
		// DataBaseToolkit.dump(dataBase, null, "ReporterTest");
		String className = "com.ikokoon.search.listener.EventPersistenceListener";
		long id = Toolkit.hash(className);
		Class klass = dataBase.find(Class.class, id);
		List<Method> methods = klass.getChildren();
		for (Method method : methods) {
			List<Double> methodSeries = Profiler.methodSeries(method);
			logger.warn("Method series : " + methodSeries);
			String graph = new Reporter(null, dataBase).buildGraph(IConstants.METHOD_SERIES, method, methodSeries);
			logger.info("Built graph : " + graph);
		}
	}

	@Test
	public void report() {
		new Reporter(null, dataBase).execute();
	}

}