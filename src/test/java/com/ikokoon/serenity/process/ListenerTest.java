package com.ikokoon.serenity.process;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.Messenger;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.Listener;
import com.ikokoon.toolkit.Toolkit;

@Ignore
public class ListenerTest extends ATest {

	private static IDataBase dataBase;

	@BeforeClass
	public static void beforeClass() {
		ATest.beforeClass();
		String dataBaseFile = "./src/test/resources/isearch/merge/tag.odb";
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, mockInternalDataBase);
	}

	@AfterClass
	public static void afterClass() {
		ATest.afterClass();
		dataBase.close();
	}

	@Test
	public void listen() throws Exception {
		String localhost = "127.0.0.1";
		new Listener(null, dataBase).execute();
		Messenger.main(new String[] { localhost, IConstants.REPORT });
		Thread.sleep(10000);
		// Verify that the reports are written
		File file = new File(IConstants.SERENITY_DIRECTORY);
		LOGGER.warn("Searching directory : " + file.getAbsolutePath());
		List<File> files = new ArrayList<File>();
		Toolkit.findFiles(new File("."), new Toolkit.IFileFilter() {
			public boolean matches(File file) {
				return file.getName().equals(IConstants.METHOD_SERIES);
			}
		}, files);
		LOGGER.warn("Report files : " + files);
		assertTrue(files.size() > 0);
	}

}