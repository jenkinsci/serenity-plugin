package com.ikokoon.serenity.persistence;

/**
 * This is an event that is fired by the database on events that can be useful to the database manager or any other interested objects.
 * 
 * @author Michael Couck
 * @since 10.12.09
 * @version 01.00
 */
public interface IDataBaseEvent {

	/** The types of events that the database can fire. */
	public enum Type {
		DATABASE_OPEN, DATABASE_CLOSE,
	}

	/**
	 * Access to the type of event the database is firing.
	 * 
	 * @return the event type
	 */
	public Type getEventType();

	/**
	 * Access to the database that fired the event.
	 * 
	 * @return the database that fired the event
	 */
	public IDataBase getDataBase();
}
