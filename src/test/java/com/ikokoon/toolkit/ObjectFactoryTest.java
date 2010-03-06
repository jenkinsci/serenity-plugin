package com.ikokoon.toolkit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.LoggingConfigurator;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.persistence.IDataBaseListener;

public class ObjectFactoryTest {

	@BeforeClass
	public static void setup() {
		LoggingConfigurator.configure();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getObject() throws Exception {
		InputStream inputStream = new ByteArrayInputStream(new byte[0]);
		Object[] parameters = new Object[] { inputStream, new String("Dummy") };
		Class<XMLDecoder> klass = (Class<XMLDecoder>) this.getClass().getClassLoader().loadClass(XMLDecoder.class.getName());
		XMLDecoder decoder = ObjectFactory.getObject(klass, parameters);
		assertNotNull(decoder);

		String string = ObjectFactory.getObject(String.class, parameters);
		assertNotNull(string);

		IDataBaseListener dataBaseListener = mock(IDataBaseListener.class);

		IDataBase internalDataBase = mock(IDataBase.class);
		IDataBase dataBase = ObjectFactory.getObject(DataBaseRam.class, internalDataBase, dataBaseListener, true);
		assertNotNull(dataBase);

		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, internalDataBase);
		Object dataBaseField = Toolkit.getValue(dataBase.getClass(), dataBase, "dataBase");
		assertNotNull(dataBaseField);

		dataBase.close();

		dataBase = new DataBaseOdb(dataBaseListener, "./serenity/dummy.odb");
		assertFalse(dataBase.isClosed());
		dataBase.close();

		dataBase = new DataBaseOdb(dataBaseListener, "./serenity/dummy.odb");
		assertFalse(dataBase.isClosed());
		dataBase.close();

		dataBase = new DataBaseOdb(dataBaseListener, "./serenity/dummy.odb");
		assertFalse(dataBase.isClosed());
		dataBase.close();

		dataBase = ObjectFactory.getObject(DataBaseOdb.class, new Object[] { dataBaseListener, "./serenity/dummy.odb", false });
		assertFalse(dataBase.isClosed());
	}

}
