package com.ikokoon.serenity.hudson;

import hudson.Plugin;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;

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

	private transient ServletContext context;
	private String applicationContext;
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

	@Override
	public void start() throws Exception {
		load();
		super.start();
	}

	@Override
	public void configure(StaplerRequest req, JSONObject formData) throws IOException {
		save();
	}

	@Override
	public void setServletContext(ServletContext context) {
		this.context = context;
	}

	@Override
	public void stop() {
		Map<String, IDataBase> dataBases = IDataBase.DataBaseManager.getDataBases();
		IDataBase[] dataBasesArray = dataBases.values().toArray(new IDataBase[dataBases.values().size()]);
		for (IDataBase dataBase : dataBasesArray) {
			dataBase.close();
		}
	}
	
	public String getApplicationContext() {
		return applicationContext;
	}

}
