package com.ikokoon.serenity.hudson;

import hudson.Plugin;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.ikokoon.IConstants;
import com.ikokoon.serenity.LoggingConfigurator;

/**
 * Entry point of a plugin.
 * 
 * <p>
 * There must be one class in each plugin. See javadoc of for more about what can be done on this class.
 * 
 * @plugin
 * 
 * @author Michael Couck
 * @since 25.07.09
 * @version 01.00
 */
public class SerenityPlugin extends Plugin {

	private Logger logger;

	public SerenityPlugin() {
		LoggingConfigurator.configure();
		logger = Logger.getLogger(SerenityPlugin.class);

		File file = new File(IConstants.DATABASE_FILE);
		if (!file.exists()) {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.error("Exception creating a new database file.", e);
			}
		}
	}

}
