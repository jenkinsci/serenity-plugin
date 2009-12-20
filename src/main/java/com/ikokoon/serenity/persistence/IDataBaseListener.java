package com.ikokoon.serenity.persistence;

/**
 * This interface is the listener that should be implemented by objects that are interested in events fired by the databases.
 * 
 * @author Michael Couck
 * @since 10.12.09
 * @version 01.00
 */
public interface IDataBaseListener {

	/**
	 * Fires the event by the database.
	 * 
	 * @param dataBaseEvent
	 *            the event that the database fired
	 */
	public void fireDataBaseEvent(IDataBaseEvent dataBaseEvent);

}
