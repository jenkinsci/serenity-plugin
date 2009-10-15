package com.ikokoon.hudson;

import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@SuppressWarnings("unchecked")
public class SerenityProjectAction extends Actionable implements ProminentProjectAction {

	private Logger logger = Logger.getLogger(SerenityProjectAction.class);
	/** The real owner that generated the build. */
	private AbstractProject owner;

	/**
	 * Constructor takes the real build from Hudson.
	 * 
	 * @param owner
	 *            the build that generated the actual build
	 */
	public SerenityProjectAction(AbstractProject owner) {
		logger.info("SerenityProjectAction:");
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayName() {
		logger.info("SerenityProjectAction:getDisplayName");
		return "Serenity report";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIconFileName() {
		logger.info("SerenityProjectAction:getIconFileName");
		return "graph.gif";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUrlName() {
		logger.info("SerenityProjectAction:getUrlName");
		return "serenity";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSearchUrl() {
		logger.info("SerenityProjectAction:getSearchUrl");
		return getUrlName();
	}

	public ISerenityResult getLastResult() {
		logger.info("SerenityProjectAction:getLastBuild");
		Run build = owner.getLastStableBuild();
		if (build != null) {
			SerenityBuildAction action = build.getAction(SerenityBuildAction.class);
			return action.getResult();
		} else {
			return null;
		}
	}

	public String getMetrics() {
		logger.info("SerenityProjectAction:getMetrics");
		Run build = owner.getLastStableBuild();
		if (build != null) {
			SerenityBuildAction action = build.getAction(SerenityBuildAction.class);
			return action.getMetrics();
		} else {
			return null;
		}
	}

	public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
		logger.info("SerenityProjectAction:doIndex");
		if (hasResult()) {
			// "../lastStableBuild/coverage"
			rsp.sendRedirect2("../lastStableBuild/serenity");
		} else {
			rsp.sendRedirect2("nocoverage");
		}
		// We might redirect to some nodata document, but let's assume there's always last build with coverage stuff...
	}

	public boolean hasResult() {
		logger.info("SerenityProjectAction:hasResult");
		return getMetrics() != null;
	}
}