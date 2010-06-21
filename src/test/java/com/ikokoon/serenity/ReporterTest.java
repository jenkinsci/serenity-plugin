package com.ikokoon.serenity;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
		String html = Reporter.methodSeries(dataBase);
		File file = new File(Reporter.METHOD_SERIES_FILE);
		Toolkit.setContents(file, html.getBytes());
	}

	@Test
	public void methodNetSeries() throws Exception {
		String html = Reporter.methodNetSeries(dataBase);
		File file = new File(Reporter.METHOD_NET_SERIES_FILE);
		Toolkit.setContents(file, html.getBytes());
	}

	@Test
	public void methodChangeSeries() throws Exception {
		String html = Reporter.methodChangeSeries(dataBase);
		File file = new File(Reporter.METHOD_CHANGE_SERIES_FILE);
		Toolkit.setContents(file, html.getBytes());
	}

	@Test
	public void methodNetChangeSeries() throws Exception {
		String html = Reporter.methodNetChangeSeries(dataBase);
		File file = new File(Reporter.METHOD_NET_CHANGE_SERIES_FILE);
		Toolkit.setContents(file, html.getBytes());
	}

	@Test
	public void report() {
		Reporter.report(dataBase);
	}

}