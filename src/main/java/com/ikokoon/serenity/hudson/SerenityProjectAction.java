package com.ikokoon.serenity.hudson;

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
		logger.info("getDisplayName");
		return "Serenity report";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIconFileName() {
		logger.info("getIconFileName");
		return "graph.gif";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUrlName() {
		logger.info("getUrlName");
		return "serenity";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSearchUrl() {
		logger.info("getSearchUrl");
		return getUrlName();
	}

	public ISerenityResult getLastResult() {
		logger.info("getLastBuild");
		Run build = owner.getLastStableBuild();
		if (build != null) {
			SerenityBuildAction action = build.getAction(SerenityBuildAction.class);
			return action.getResult();
		} else {
			return null;
		}
	}

	public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
		logger.info("doIndex");
		if (hasResult()) {
			rsp.sendRedirect2("../lastStableBuild/serenity");
		} else {
			rsp.sendRedirect2("nocoverage");
		}
	}

	public boolean hasResult() {
		logger.info("hasResult");
		return true;
	}
}