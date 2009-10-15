package com.ikokoon.instrumentation.process;

/**
 * @see AProcess for more information on the end processing strategy.
 * 
 * @author Michael Couck
 * @since 23.08.09
 * @version 01.00
 */
public interface IProcess {

	/**
	 * @see AProcess
	 */
	public void execute();

	/**
	 * @see AProcess
	 */
	public void setChild(IProcess child);

}
