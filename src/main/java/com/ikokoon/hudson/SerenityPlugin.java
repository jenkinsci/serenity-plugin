package com.ikokoon.hudson;

import hudson.Plugin;
import hudson.tasks.BuildStep;

import java.net.URL;

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
		}
		logger = Logger.getLogger(SerenityPlugin.class);
		logger.info("Loaded logging properties from : " + url);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("deprecation")
	public void start() throws Exception {
		// plugins normally extend Hudson by providing custom implementations
		// of 'extension points'. In this example, we'll add one builder.
		BuildStep.PUBLISHERS.add(SerenityPublisher.DESCRIPTOR);
		BuildStep.PUBLISHERS.add(HelloWorldPublisher.DESCRIPTOR);
	}

}
