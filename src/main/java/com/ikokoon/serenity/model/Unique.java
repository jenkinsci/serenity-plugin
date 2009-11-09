package com.ikokoon.serenity.model;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is to specify in the persisten entities which fields or combinations of fields are unique, acts somthing like a compound key
 * declaration for the entities so that unique indexes can be built from the unique value combinations.
 * 
 * @author Michael Couck
 * @since 06.10.09
 * @version 01.00
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Unique {

	public String[] fields();

}
