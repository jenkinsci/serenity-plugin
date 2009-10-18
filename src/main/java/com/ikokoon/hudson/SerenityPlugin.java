package com.ikokoon.hudson;

import hudson.Plugin;

import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ikokoon.IConstants;

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
		URL url = SerenityPlugin.class.getResource(IConstants.LOG_4_J_PROPERTIES);
		if (url != null) {
			PropertyConfigurator.configure(url);
		} else {
			Properties properties = getProperties();
			PropertyConfigurator.configure(properties);
		}
		logger = Logger.getLogger(SerenityPlugin.class);
		logger.info("Loaded logging properties from : " + url);
	}

	private Properties getProperties() {
		Properties properties = new Properties();
		// Root Logger
		properties.put("log4j.rootLogger", "INFO, ikokoon, file");
		properties.put("log4j.rootCategory", "INFO, ikokoon");

		// Serenity application logging file output
		properties.put("log4j.appender.file", "org.apache.log4j.DailyRollingFileAppender");
		properties.put("log4j.appender.file.Threshold", "DEBUG");
		properties.put("log4j.appender.file.File", "./serenity/serenity.log");
		properties.put("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
		properties.put("log4j.appender.file.layout.ConversionPattern", "%d{HH:mm:ss,SSS} %-5p %C:%L - %m%n");
		properties.put("log4j.appender.file.Append", "false");

		// Serenity application logging console output
		properties.put("log4j.appender.ikokoon", "org.apache.log4j.ConsoleAppender");
		properties.put("log4j.appender.ikokoon.Threshold", "DEBUG");
		properties.put("log4j.appender.ikokoon.ImmediateFlush", "true");
		properties.put("log4j.appender.ikokoon.layout", "org.apache.log4j.PatternLayout");
		properties.put("log4j.appender.ikokoon.layout.ConversionPattern", "%d{HH:mm:ss,SSS} %-5p %C:%L - %m%n");

		// Set the Serenity categories and thresholds
		properties.put("log4j.category.net", "WARN");
		properties.put("log4j.category.com", "WARN");
		properties.put("log4j.category.org", "WARN");

		// Specific thresholds
		properties.put("log4j.category.com.ikokoon", "INFO");
		properties.put("log4j.category.com.ikokoon.toolkit", "WARN");
		properties.put("log4j.category.com.ikokoon.persistence", "INFO");
		properties.put("log4j.category.com.ikokoon.instrumentation.process", "INFO");
		properties.put("log4j.category.com.ikokoon.instrumentation.coverage", "INFO");
		properties.put("log4j.category.com.ikokoon.instrumentation.complexity", "INFO");
		properties.put("log4j.category.com.ikokoon.instrumentation.dependency", "INFO");
		properties.put("log4j.category.com.ikokoon.instrumentation.profiling", "INFO");
		return properties;
	}
}
