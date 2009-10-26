package com.ikokoon.hudson;

import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.lang.ref.WeakReference;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerProxy;

/**
 * This is the Stapler 'proxy'. It serves the result to the front end.
 * 
 * @author Michael Couck
 * @see 12.08.09
 * @version 01.00
 */
@SuppressWarnings("unchecked")
public class SerenityBuildAction implements StaplerProxy, Action {

	private Logger logger = Logger.getLogger(SerenityBuildAction.class);
	/** The Hudson build owner, i.e. the action that really did the build. */
	private final AbstractBuild owner;
	/** The result from the build for Serenity. */
	private transient WeakReference<ISerenityResult> result;
	/** Just a string that has the total aggregated metrics for the build. */
	private String metrics;

	/**
	 * Constructor takes the Hudson build owner and the result that will be presented to the front end for displaying the data from teh build and
	 * metrics.
	 * 
	 * @param owner
	 *            the build owner that generated the build
	 * @param result
	 *            the result from Serenity that will be presented to the front end
	 */
	public SerenityBuildAction(AbstractBuild owner, ISerenityResult result) {
		logger.info("SerenityBuildAction:");
		if (owner == null) {
			throw new RuntimeException("owner cannot be null");
		}
		setResult(result);
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayName() {
		logger.info("SerenityBuildAction:getDisplayName");
		return "Serenity Report";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIconFileName() {
		logger.info("SerenityBuildAction:getIconFile");
		return "graph.gif";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUrlName() {
		logger.info("SerenityBuildAction:getUrlName");
		return "serenity";
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getTarget() {
		logger.info("SerenityBuildAction:getTarget");
		return getResult();
	}

	private void setResult(ISerenityResult result) {
		logger.info("SerenityBuildAction:setResult");
		this.result = new WeakReference(result);
		this.metrics = result.getMetrics();
	}

	public ISerenityResult getResult() {
		logger.info("SerenityBuildAction:getResult");
		if (!hasResult()) {
			// try to reload from file
			reloadReport();
		}
		if (!hasResult()) {
			// return empty result
			return new SerenityResult(owner);
		}
		return result.get();
	}

	private boolean hasResult() {
		logger.info("SerenityBuildAction:hasResult");
		return result != null && result.get() != null;
	}

	private void reloadReport() {
		logger.info("SerenityBuildAction:reloadReport");
		ISerenityResult result = new SerenityResult(owner);
		setResult(result);
	}

	public String getMetrics() {
		logger.info("SerenityBuildAction:getMetrics");
		return metrics;
	}
}