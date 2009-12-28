package com.ikokoon.serenity;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

	static {
		configure();
	}

	/**
	 * Configures the Jog4j properties. In the case the log4j.properties file is not on the classpath in the META-INF directory hard coded properties
	 * will be used.
	 */
	public static void configure() {
		if (!initilised) {
			URL url = LoggingConfigurator.class.getResource(IConstants.LOG_4_J_PROPERTIES);
			if (url != null) {
				PropertyConfigurator.configure(url);
			} else {
				Properties properties = getProperties();
				PropertyConfigurator.configure(properties);
			}
			initilised = true;
		}
	}

	private static Properties getProperties() {
		Properties properties = new Properties();
		// Root Logger
		properties.put("log4j.rootLogger", "INFO, ikokoon, file");
		properties.put("log4j.rootCategory", "INFO, ikokoon");

		// Serenity application logging file output
		properties.put("log4j.appender.file", "org.apache.log4j.DailyRollingFileAppender");
		properties.put("log4j.appender.file.Threshold", "DEBUG");
		properties.put("log4j.appender.file.File", "./serenity/serenity.log");

		File file = new File("./serenity/serenity.log");
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

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
		properties.put("log4j.category.com.ikokoon.toolkit", "INFO");
		properties.put("log4j.category.com.ikokoon.persistence", "INFO");
		properties.put("log4j.category.com.ikokoon.instrumentation.process", "INFO");
		properties.put("log4j.category.com.ikokoon.instrumentation.coverage", "INFO");
		properties.put("log4j.category.com.ikokoon.instrumentation.complexity", "INFO");
		properties.put("log4j.category.com.ikokoon.instrumentation.dependency", "INFO");
		properties.put("log4j.category.com.ikokoon.instrumentation.profiling", "INFO	");
		return properties;
	}

}
