package com.ikokoon.serenity.hudson;

import hudson.Plugin;

import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.LoggingConfigurator;
import com.ikokoon.serenity.persistence.IDataBase;

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
		logger.warn("Loaded plugin : " + this.getClass().getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setServletContext(ServletContext context) {
		super.setServletContext(context);
	}

	@Override
	public void start() throws Exception {
		super.start();
	}

	@Override
	public void stop() {
		Map<String, IDataBase> dataBases = IDataBase.DataBaseManager.getDataBases();
		IDataBase[] dataBasesArray = dataBases.values().toArray(new IDataBase[dataBases.values().size()]);
		for (IDataBase dataBase : dataBasesArray) {
			dataBase.close();
		}
	}

}
