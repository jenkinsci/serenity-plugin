package com.ikokoon.serenity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

/**
 * This is a central class for initialising the Log4j logging parameters.
 *
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
public class LoggingConfigurator {

	/** Whether the logging has already be initialised. */
	private static boolean initilised = false;
	private static final String SERENITY_LOG_FILE = "./serenity/serenity.log";

	static {
		configure();
	}

	/**
	 * Configures the Jog4j properties. In the case the log4j.properties file is not on the classpath in the META-INF directory hard coded properties
	 * will be used.
	 */
	public static void configure() {
		if (!initilised) {
			checkLogFolder(SERENITY_LOG_FILE);
			InputStream inputStream = LoggingConfigurator.class.getResourceAsStream(IConstants.LOG_4_J_PROPERTIES);
			System.out.println("Log4j stream : " + inputStream);
			Properties properties = null;
			if (inputStream != null) {
				try {
					properties = new Properties();
					properties.load(inputStream);
				} catch (Exception e) {
					e.printStackTrace();
					properties = getProperties();
				}
			} else {
				properties = getProperties();
			}
			PropertyConfigurator.configure(properties);
			initilised = true;
		}
	}

	private static void checkLogFolder(String filePath) {
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Log file : " + file.getAbsolutePath());
	}

	private static Properties getProperties() {
		Properties properties = new Properties();
		// Root Logger
		properties.put("log4j.rootLogger", "warn, ikokoon, file");
		properties.put("log4j.rootCategory", "warn, ikokoon");

		// Serenity application logging file output
		properties.put("log4j.appender.file", "org.apache.log4j.DailyRollingFileAppender");
		properties.put("log4j.appender.file.Threshold", "DEBUG");
		properties.put("log4j.appender.file.File", SERENITY_LOG_FILE);

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
		properties.put("log4j.category.com.ikokoon", "warn");
		properties.put("log4j.category.com.ikokoon.serenity", "warn");
		return properties;
	}

}
