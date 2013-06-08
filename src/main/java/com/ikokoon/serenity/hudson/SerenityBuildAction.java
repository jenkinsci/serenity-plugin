package com.ikokoon.serenity.hudson;

import hudson.model.Action;
import hudson.model.AbstractBuild;

import java.lang.ref.WeakReference;

import org.kohsuke.stapler.StaplerProxy;

/**
 * This is the Stapler 'proxy'. It serves the chain of results objects to the front end.
 * 
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SerenityBuildAction implements StaplerProxy, Action {

	/** The Hudson build owner, i.e. the action that really did the build. */
	private final AbstractBuild owner;
	/** The result from the build for Serenity. */
	private transient WeakReference<ISerenityResult> result;

	/**
	 * Constructor takes the Hudson build owner and the result that will be presented to the front end for displaying the data from teh build and metrics.
	 * 
	 * @param owner the build owner that generated the build
	 * @param result the result from Serenity that will be presented to the front end
	 */
	public SerenityBuildAction(AbstractBuild owner, ISerenityResult result) {
		if (owner == null) {
			throw new RuntimeException("Owner cannot be null");
		}
		setResult(result);
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayName() {
		return "Serenity Report";
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
	public Object getTarget() {
		return getResult();
	}

	public ISerenityResult getResult() {
		if (!hasResult()) {
			reloadReport();
		}
		if (!hasResult()) {
			return new SerenityResult(owner);
		}
		return result.get();
	}

	private void setResult(ISerenityResult result) {
		this.result = new WeakReference(result);
	}

	private boolean hasResult() {
		return result != null && result.get() != null;
	}

	private void reloadReport() {
		ISerenityResult result = new SerenityResult(owner);
		setResult(result);
	}

}