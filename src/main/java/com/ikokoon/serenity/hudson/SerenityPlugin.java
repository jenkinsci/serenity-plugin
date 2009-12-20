package com.ikokoon.serenity.hudson;

import hudson.Plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.LoggingConfigurator;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

/**
 * Entry point of a plugin.
 * 
 * <p>
 * There must be one class in each plugin. See JavaDoc of for more about what can be done on this class.
 * 
 * @plugin
 * 
 * @author Michael Couck
 * @since 25.07.09
 * @version 01.00
 */
public class SerenityPlugin extends Plugin {

	/** The logger for the plugin class. */
	private Logger logger;

	/**
	 * Constructor initialises the logging and the database.
	 */
	public SerenityPlugin() {
		LoggingConfigurator.configure();
		logger = Logger.getLogger(SerenityPlugin.class);

		// TODO - why is this here, remove me. We don't need a database in the plugin do we?
		File file = new File(IConstants.DATABASE_FILE_ODB);
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

	/**
	 * {@inheritDoc}
	 */
	public void setServletContext(ServletContext context) {
		super.setServletContext(context);
		// Copy the jars from the lib directory in the plugin to the Hudson application root so the
		// applet can be found by the browser
		copyAppletJars(context);
	}

	public void stop() {
		Map<String, IDataBase> dataBases = IDataBase.DataBaseManager.getDataBases();
		IDataBase[] dataBasesArray = dataBases.values().toArray(new IDataBase[dataBases.values().size()]);
		for (IDataBase dataBase : dataBasesArray) {
			dataBase.close();
		}
	}

	private void copyAppletJars(ServletContext context) {
		String webAppRootDirPath = context.getRealPath("");
		logger.debug("Web App Root Dir : " + webAppRootDirPath);

		URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
		logger.debug("Url : " + url);
		String sourceDirPath = url.toExternalForm().replaceFirst("file:/", "");

		File classes = new File(sourceDirPath);
		File destination = new File(webAppRootDirPath);
		File lib = new File(classes.getParentFile(), "lib");

		logger.debug("Copying the applet libraries from : " + lib + ", to destination : " + destination);
		Toolkit.copyFile(lib, destination);

		lib = new File(classes.getParentFile(), "serenity/WEB-INF/lib");
		logger.debug("Copying the applet libraries from : " + lib + ", to destination : " + destination);
		Toolkit.copyFile(lib, destination);
	}

}
