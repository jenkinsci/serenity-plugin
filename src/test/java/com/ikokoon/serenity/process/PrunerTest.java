package com.ikokoon.serenity.process;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.target.discovery.Discovery;
import com.ikokoon.toolkit.Executer;
import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 10.01.10
 * @version 01.00
 */
public class PrunerTest extends ATest implements IConstants {

	@Before
	public void addPackages() {
		Configuration.getConfiguration().includedPackages.clear();
		Configuration.getConfiguration().includedPackages.add(Discovery.class.getPackage().getName());
		Configuration.getConfiguration().includedPackages.add("edu.umd.cs.findbugs");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void execute() {
		File sourceOdbDataBaseFile = new File("./src/test/resources/findbugs/serenity.odb");
		File targetOdbDataBaseFile = new File("./src/test/resources/findbugs/test/serenity.odb");

		Toolkit.copyFile(sourceOdbDataBaseFile, targetOdbDataBaseFile);

		IDataBase odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, targetOdbDataBaseFile.getAbsolutePath(), null);
		final IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, odbDataBase);

		double pruneDuration = Executer.execute(new Executer.IPerform() {
			public void execute() {
				new Pruner(null, dataBase).execute();
			}
		}, "PrunerTest : ", 1);
		logger.warn("Prune duration : " + pruneDuration);

		DataBaseToolkit.dump(odbDataBase, new DataBaseToolkit.ICriteria() {
			public boolean satisfied(Composite<?, ?> composite) {
				if (Package.class.isAssignableFrom(composite.getClass())) {
					assertTrue(((Package) composite).getAfferent().size() == 0);
				}
				if (Class.class.isAssignableFrom(composite.getClass())) {
					assertTrue(((Class) composite).getAfferent().size() == 0);
				}
				if (Method.class.isAssignableFrom(composite.getClass())) {
					assertTrue(((Method) composite).getChildren().size() == 0);
				}
				return false;
			}
		}, "Pruner test : ");

		dataBase.close();
	}

}