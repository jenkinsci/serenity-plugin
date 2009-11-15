package com.ikokoon.serenity.hudson;

/**
 * This is the interface for the Serenity result that will be presented to the front end via Stapler.
 * 
 * @author Michael Couck
 * @since 18.08.09
 * @version 01.00
 */
public interface ISerenityResult {

	public String PACKAGE_NAME = "packageName";
	public String CLASS_NAME = "className";
	public String METHOD_NAME = "methodName";
	public String METHOD_DESCRIPTION = "methodDescription";
	/** We only want a maximum of 10 histories for the trend graph. */
	public int HISTORY = 10;

}
