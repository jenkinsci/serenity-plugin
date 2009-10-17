package com.ikokoon.instrumentation.model;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Parent {

	public boolean parent();

}
