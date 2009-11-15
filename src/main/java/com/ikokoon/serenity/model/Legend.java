package com.ikokoon.serenity.model;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Michael Couck
 * @since 09.11.09
 * @version 01.00
 */
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface Legend {

	/**
	 * The name that will be used in the legend of the graph.
	 * 
	 * @return the test for the graph
	 */
	public String name();

	/**
	 * If the metric is greater than the limit is positive like in the case of coverage this is ascending. In the case where the metric being negative
	 * if greater than the limit like complexity this is false;
	 * 
	 * @return whether the metric being greater than the limit is positive or negative
	 */
	public double positive() default 0.0;

	/**
	 * The limits for the good, ok and bad for the metric.
	 * 
	 * @return the limits for the metric
	 */
	public double[] limits();

}
