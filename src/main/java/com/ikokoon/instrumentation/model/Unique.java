package com.ikokoon.instrumentation.model;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Michael Couck
 * @since 06.10.09
 * @version 01.00
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Unique {

	public String[] fields();

	public String discriminator() default "";

}
