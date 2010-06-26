package com.ikokoon.serenity.persistence;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;

public class DataBaseManagerTest extends ATest {

	@BeforeClass
	public static void setup() {
		LoggingConfigurator.configure();
	}

	@Test
	public void getDataBase() {
		File dataBaseFile = new File("./src/test/resources/dummy.odb");
		IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile.getAbsolutePath(), mockInternalDataBase);
		assertNotNull(dataBase);
		dataBase.close();

		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, dataBaseFile.getAbsolutePath(), dataBase);
		assertNotNull(dataBase);
		dataBase.close();

		Toolkit.deleteFile(dataBaseFile, 3);
	}
}
