package com.ikokoon.serenity.hudson.source;

/**
 * This is the interface for classes that take the Java source and convert it into HTML that can be displayed by the browser.
 * 
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
public interface ISourceCode {

	/**
	 * Accesses to the HTML that has been generated from the Java source.
	 * 
	 * @return the HTML from the Java source
	 */
	public String getSource();

}
