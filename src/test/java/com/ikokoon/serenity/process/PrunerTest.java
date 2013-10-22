package com.ikokoon.serenity.process;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.persistence.DataBaseOdb;
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
	@SuppressWarnings("rawtypes")
	public void execute() {
		File sourceOdbDataBaseFile = new File("./src/test/resources/isearch/merge/tag.odb");
		File targetOdbDataBaseFile = new File("./src/test/resources/isearch/prune/tag.odb");
		if (!targetOdbDataBaseFile.getParentFile().exists()) {
			targetOdbDataBaseFile.getParentFile().mkdirs();
		}

		Toolkit.copyFile(sourceOdbDataBaseFile, targetOdbDataBaseFile);

		final IDataBase odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, targetOdbDataBaseFile.getAbsolutePath(), null);

		// Assert that there are no more afferent and lines in the database
		List<Line> lines = odbDataBase.find(Line.class);
		assertTrue(lines.size() > 0);
		List<Afferent> afferent = odbDataBase.find(Afferent.class);
		assertTrue(afferent.size() > 0);
		List<Efferent> efferent = odbDataBase.find(Efferent.class);
		assertTrue(efferent.size() > 0);

		double pruneDuration = Executer.execute(new Executer.IPerform() {
			public void execute() {
				new Pruner(null, odbDataBase).execute();
			}
		}, "PrunerTest : ", 1);
		LOGGER.warn("Prune duration : " + pruneDuration);
		// Assert that there are no more afferent and lines in the database
		lines = odbDataBase.find(Line.class);
		assertEquals(0, lines.size());
		afferent = odbDataBase.find(Afferent.class);
		assertEquals(0, afferent.size());
		efferent = odbDataBase.find(Efferent.class);
		assertEquals(0, efferent.size());

		odbDataBase.close();

		Toolkit.deleteFile(targetOdbDataBaseFile.getParentFile(), 3);
	}

}