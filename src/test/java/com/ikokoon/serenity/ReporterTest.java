package com.ikokoon.serenity;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

public class ReporterTest extends ATest {

	private static IDataBase dataBase;

	@BeforeClass
	public static void beforeClass() {
		ATest.beforeClass();
		String dataBaseFile = "./src/test/resources/isearch/serenity.odb";
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, mockInternalDataBase);
	}

	@Test
	public void methodSeries() throws Exception {
		String html = Reporter.methodSeries(dataBase);
		File file = new File("./methodSeries.html");
		if (!file.exists()) {
			file.createNewFile();
		}
		Toolkit.setContents(file, html.getBytes());
	}

	@Test
	public void methodNetSeries() throws Exception {
		String html = Reporter.methodNetSeries(dataBase);
		File file = new File("./methodNetSeries.html");
		if (!file.exists()) {
			file.createNewFile();
		}
		Toolkit.setContents(file, html.getBytes());
	}

	@Test
	public void methodChangeSeries() throws Exception {
		String html = Reporter.methodChangeSeries(dataBase);
		File file = new File("./methodChangeSeries.html");
		if (!file.exists()) {
			file.createNewFile();
		}
		Toolkit.setContents(file, html.getBytes());
	}

	@Test
	public void methodNetChangeSeries() throws Exception {
		String html = Reporter.methodNetChangeSeries(dataBase);
		File file = new File("./methodNetChangeSeries.html");
		if (!file.exists()) {
			file.createNewFile();
		}
		Toolkit.setContents(file, html.getBytes());
	}

}