package com.ikokoon.hudson;

import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class HelloWorldProjectAction extends Actionable implements ProminentProjectAction {

	/** The real owner that generated the build. */
	private AbstractProject owner;

	/**
	 * Construstor takes the real build from Hudson.
	 * 
	 * @param owner
	 *            the build that generated the actual build
	 */
	public HelloWorldProjectAction(AbstractProject owner) {
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayName() {
		return "Serenity report";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIconFileName() {
		return "graph.gif";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUrlName() {
		return "serenity";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSearchUrl() {
		return getUrlName();
	}

	public Object getLastResult() {
		Run build = owner.getLastStableBuild();
		if (build != null) {
			// HelloWorldBuildAction action = build.getAction(HelloWorldBuildAction.class);
			// return action.getResult();
			return null;
		} else {
			return null;
		}
	}

	public String getMetrics() {
		Run build = owner.getLastStableBuild();
		if (build != null) {
			// HelloWorldBuildAction action = build.getAction(HelloWorldBuildAction.class);
			// return action.getMetrics();
			return null;
		} else {
			return null;
		}
	}

	public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
		if (hasResult()) {
			// "../lastStableBuild/coverage"
			rsp.sendRedirect2("../lastStableBuild/serenity");
		} else {
			rsp.sendRedirect2("nocoverage");
		}
		// We might redirect to some nodata document, but let's assume there's always last build with coverage stuff...
	}

	public boolean hasResult() {
		return getMetrics() != null;
	}
}