package com.ikokoon.toolkit;

import static org.junit.Assert.assertNotNull;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.persistence.IDataBaseEvent;
import com.ikokoon.serenity.persistence.IDataBaseListener;

public class ObjectFactoryTest extends ATest {

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

		IDataBaseListener dataBaseListener = new IDataBaseListener() {
			public void fireDataBaseEvent(IDataBaseEvent dataBaseEvent) {
			}
		};
		dataBase.close();
		IDataBase iDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, IConstants.DATABASE_FILE_ODB, true, null);
		IDataBase dataBase = ObjectFactory.getObject(DataBaseRam.class, iDataBase, dataBaseListener, true);
		assertNotNull(dataBase);

		dataBase.close();
		iDataBase.close();
		iDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, IConstants.DATABASE_FILE_ODB, true, null);
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, true, iDataBase);
		Object dataBaseField = Toolkit.getValue(dataBase.getClass(), dataBase, "dataBase");
		assertNotNull(dataBaseField);
	}

}
