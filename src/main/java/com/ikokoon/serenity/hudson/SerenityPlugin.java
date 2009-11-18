package com.ikokoon.serenity.hudson;

import hudson.Plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.LoggingConfigurator;
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

	public void setServletContext(ServletContext context) {
		super.setServletContext(context);
		copyAppletJars(context);
	}

	private void copyAppletJars(ServletContext context) {
		String webAppRootDirPath = context.getRealPath("");
		if (!webAppRootDirPath.endsWith("hudson")) {
			webAppRootDirPath += "hudson";
		}
		logger.debug("Web App Root Dir : " + webAppRootDirPath);

		URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
		logger.debug("Url : " + url);
		String sourceDirPath = url.toExternalForm().replaceFirst("file:/", "");

		File source = new File(sourceDirPath);
		File destination = new File(webAppRootDirPath);
		if (!source.exists()) {
			source = new File(source.getParent(), "temp");
		}

		logger.debug("Copying the applet classes from : " + source + ", to destination : " + destination);

		Toolkit.copyFile(source, destination);
	}

}
