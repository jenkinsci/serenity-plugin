package com.ikokoon.serenity.hudson;

import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * An object in the chain of proxy objects that serve the front end in Hudson.
 * 
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
@SuppressWarnings("rawtypes")
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
		logger.debug("SerenityProjectAction:");
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayName() {
		logger.debug("getDisplayName");
		return "Serenity report";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIconFileName() {
		logger.debug("getIconFileName");
		return "graph.gif";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUrlName() {
		logger.debug("getUrlName");
		return "serenity";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSearchUrl() {
		logger.debug("getSearchUrl");
		return getUrlName();
	}

	public ISerenityResult getLastResult() {
		logger.debug("getLastResult");
		Run build = owner.getLastStableBuild();
		if (build == null) {
			build = owner.getLastBuild();
		}
		if (build != null) {
			SerenityBuildAction action = build.getAction(SerenityBuildAction.class);
			if (action != null) {
				return action.getResult();
			}
		}
		return null;
	}

	public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
		logger.debug("doIndex");
		if (hasResult()) {
			rsp.sendRedirect2("../lastBuild/serenity");
		} else {
			rsp.sendRedirect2("nocoverage");
		}
	}

	public boolean hasResult() {
		logger.debug("hasResult");
		return getLastResult() != null;
	}

}