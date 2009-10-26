package com.ikokoon.hudson;

import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * This is the project build that presents the BuildAction to Hudson.
 * 
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@SuppressWarnings("unchecked")
public class SerenityAction extends Actionable implements ProminentProjectAction {

	private Logger logger = Logger.getLogger(SerenityAction.class);
	/** The real owner that generated the build. */
	private AbstractProject owner;

	/**
	 * Constructor takes the real build from Hudson.
	 * 
	 * @param owner
	 *            the build that generated the actual build
	 */
	public SerenityAction(AbstractProject owner) {
		logger.info("SerenityAction:");
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayName() {
		logger.info("SerenityAction:getDisplayName");
		return "Serenity report";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIconFileName() {
		logger.info("SerenityAction:getIconfFileName");
		return "graph.gif";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUrlName() {
		logger.info("SerenityAction:getUrlName");
		return "serenity";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSearchUrl() {
		logger.info("SerenityAction:getSearchUrl");
		return getUrlName();
	}

	public ISerenityResult getLastResult() {
		logger.info("SerenityAction:getLastResult");
		Run build = owner.getLastStableBuild();
		if (build != null) {
			SerenityBuildAction action = build.getAction(SerenityBuildAction.class);
			return action.getResult();
		} else {
			class SerenityResult implements ISerenityResult {
				public String getMetrics() {
					return "metrics";
				}
			}
			return new SerenityResult();
		}
	}

	public String getMetrics() {
		logger.info("SerenityAction:getMetrics");
		Run build = owner.getLastStableBuild();
		if (build != null) {
			SerenityBuildAction action = build.getAction(SerenityBuildAction.class);
			return action.getMetrics();
		} else {
			return "Metrics";
		}
	}

	public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
		logger.info("SerenityAction:doIndex");
		if (hasResult()) {
			// "../lastStableBuild/coverage"
			rsp.sendRedirect2("../lastStableBuild/serenity");
		} else {
			rsp.sendRedirect2("nocoverage");
		}
		// We might redirect to some nodata document, but let's assume there's
		// always last build with coverage stuff...
	}

	public boolean hasResult() {
		logger.info("SerenityAction:hasResult");
		return getMetrics() != null;
	}
}