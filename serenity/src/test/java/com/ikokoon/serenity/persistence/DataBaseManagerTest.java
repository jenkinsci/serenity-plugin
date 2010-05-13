package com.ikokoon.serenity.persistence;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.LoggingConfigurator;

public class DataBaseManagerTest extends ATest {

	@BeforeClass
	public static void setup() {
		LoggingConfigurator.configure();
	}

	@Test
	public void getDataBase() {
		IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, "./serenity/dummy/dummy.odb", mockInternalDataBase);
		assertNotNull(dataBase);
		dataBase.close();

		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, "./serenity/dummy/dummy.odb", dataBase);
		assertNotNull(dataBase);
		dataBase.close();
	}
}
