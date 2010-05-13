package com.ikokoon.serenity.model;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is to specify in the persistent entities which fields or combinations of fields are unique, acts something like a compound key
 * declaration for the entities so that unique indexes can be built from the unique value combinations.
 * 
 * @author Michael Couck
 * @since 06.10.09
 * @version 01.00
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Unique {

	/**
	 * The unique fields combination in the object.
	 * 
	 * @return the names of the unique field combination in the object
	 */
	public String[] fields();

}
