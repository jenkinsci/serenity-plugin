package com.ikokoon.target.consumer;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.ikokoon.target.consumer.annotation.AnnotationAnnotation;

@Retention(RUNTIME)
@AnnotationAnnotation(name = "Annotation")
@Target(value = { TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, PACKAGE })
public @interface Annotation {

	public String[] fields();

}
