package com.ikokoon.serenity.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Executer;
import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 10.01.10
 * @version 01.00
 */
public class CleanerTest extends ATest implements IConstants {

	private String className = "Tag";
	private String packageName = "spelling";
	private String databaseFile = "./database/tag.odb";

	@Before
	public void addPackages() {
		Configuration.getConfiguration().includedPackages.clear();
		Configuration.getConfiguration().includedPackages.add("com.ikokoon");
		Configuration.getConfiguration().includedPackages.add("edu.umd.cs.findbugs");
		Configuration.getConfiguration().excludedPackages.add(className);
		Configuration.getConfiguration().excludedPackages.add(packageName);

		File sourceOdbDataBaseFile = new File("./src/test/resources/isearch/merge/tag.odb");
		File targetOdbDataBaseFile = new File(databaseFile);
		if (!targetOdbDataBaseFile.getParentFile().exists()) {
			targetOdbDataBaseFile.getParentFile().mkdirs();
		}
		Toolkit.copyFile(sourceOdbDataBaseFile, targetOdbDataBaseFile);
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, targetOdbDataBaseFile.getAbsolutePath(), null);
	}

	@After
	public void after() {
		Toolkit.deleteFile(new File(databaseFile).getParentFile(), 3);
	}

	@Test
	public void execute() {
		DataBaseToolkit.dump(dataBase, null, null);
		assertTrue("", containsPattern(dataBase, className));
		assertTrue(containsPattern(dataBase, packageName));
		Executer.execute(new Executer.IPerform() {
			public void execute() {
				new Cleaner(null, dataBase).execute();
			}
		}, "CleanerTest : ", 1);
		assertFalse(containsPattern(dataBase, className));
		assertFalse(containsPattern(dataBase, packageName));
	}

	@SuppressWarnings("rawtypes")
	private boolean containsPattern(final IDataBase odbDataBase, final String pattern) {
		// Assert that there are not spelling packages in the database
		List<Package> packages = odbDataBase.find(Package.class);
		for (Package<?, ?> pakkage : packages) {
			if (pakkage.getName().contains(pattern)) {
				return Boolean.TRUE;
			}
		}
		List<Class> classes = dataBase.find(Class.class);
		for (final Class clazz : classes) {
			if (clazz.getName().contains(pattern)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

}