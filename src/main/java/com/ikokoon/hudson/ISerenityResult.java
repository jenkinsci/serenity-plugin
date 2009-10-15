package com.ikokoon.hudson;

/**
 * This is the interface for the Serenity result that will be presented to the
 * front end via Stapler.
 * 
 * @author Michael Couck
 * @since 18.08.09
 * @version 01.00
 */
public interface ISerenityResult {

	/**
	 * Access to a string containing the aggregated data for the build
	 * 
	 * @return the aggregated data for the build
	 */
	public String getMetrics();

}
