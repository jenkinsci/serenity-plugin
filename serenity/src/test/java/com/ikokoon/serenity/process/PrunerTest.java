package com.ikokoon.serenity.process;

import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.target.discovery.Discovery;

/**
 * @author Michael Couck
 * @since 10.01.10
 * @version 01.00
 */
public class PrunerTest extends ATest implements IConstants {

	static {
		Configuration.getConfiguration().includedPackages.clear();
		Configuration.getConfiguration().includedPackages.add(Discovery.class.getPackage().getName());
		Configuration.getConfiguration().includedPackages.add("edu.umd.cs.findbugs");
	}

	@Before
	public void before() {
		DataBaseToolkit.clear(dataBase);
		new Accumulator(null).execute();
		new Cleaner(null, dataBase).execute();
		new Aggregator(null, dataBase).execute();
	}

	@Test
	public void execute() {
		new Pruner(null, dataBase).execute();
	}

}