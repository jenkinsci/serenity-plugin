package com.ikokoon.hudson;

import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.lang.ref.WeakReference;

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

	/** The Hudson build owner, i.e. the action that really did the build. */
	private final AbstractBuild owner;
	/** The result from the build for Serenity. */
	private transient WeakReference<ISerenityResult> result;

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
		System.out.println("SerenityBuildAction:");
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
		System.out.println("SerenityBuildAction:getDisplayName");
		return "Serenity Report";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIconFileName() {
		System.out.println("SerenityBuildAction:getIconFile");
		return "graph.gif";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUrlName() {
		System.out.println("SerenityBuildAction:getUrlName");
		return "serenity";
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getTarget() {
		System.out.println("SerenityBuildAction:getTarget");
		return getResult();
	}

	private void setResult(ISerenityResult result) {
		System.out.println("SerenityBuildAction:setResult");
		this.result = new WeakReference(result);
	}

	public ISerenityResult getResult() {
		System.out.println("SerenityBuildAction:getResult");
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
		System.out.println("SerenityBuildAction:hasResult");
		return result != null && result.get() != null;
	}

	private void reloadReport() {
		System.out.println("SerenityBuildAction:reloadReport");
		ISerenityResult result = new SerenityResult(owner);
		setResult(result);
	}

}