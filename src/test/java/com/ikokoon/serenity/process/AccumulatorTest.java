package com.ikokoon.serenity.process;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.StringTokenizer;

import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.Transformer;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.target.Target;
import com.ikokoon.toolkit.Toolkit;

/**
 * This is the test for the accumulator the looks through all the classes on the classpath that were not loaded at runtime and does the dependency,
 * coverage and so on for them.
 * 
 * @author Michael Couck
 * @since 24.07.09
 * @version 01.00
 */
public class AccumulatorTest extends ATest implements IConstants {

	@Before
	public void initilize() {
		super.initilize();
		Transformer.premain(null, null);
		String classPath = System.getProperty("java.class.path");
		classPath += ";" + new File(".", "/target/serenity.jar").getAbsolutePath() + ";";
		classPath = Toolkit.replaceAll(classPath, "\\.\\", "\\");
		classPath = Toolkit.replaceAll(classPath, "/./", "/");
		System.setProperty("java.class.path", classPath);
		StringTokenizer stringTokenizer = new StringTokenizer(classPath, ";");
		while (stringTokenizer.hasMoreTokens()) {
			logger.debug(stringTokenizer.nextToken());
		}
	}

	@Test
	public void accumulate() {
		logger.info("Included : " + Configuration.getConfiguration().includedPackages);
		logger.info("Excluded : " + Configuration.getConfiguration().excludedPackages);
		Accumulator accumulator = new Accumulator(null);
		accumulator.execute();
		Class<?, ?> klass = (Class<?, ?>) dataBase.find(Class.class, Toolkit.hash(Target.class.getName()));
		assertNotNull(klass);
	}
}
