package com.ikokoon.serenity.model;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Unique(fields = { Composite.CLASS_NAME, Composite.METHOD_NAME, Composite.NUMBER })
public class Line<E, F> extends Composite<Class<?, ?>, Object> implements Comparable<Line<?, ?>>, Serializable {

	private String className;
	private String methodName;
	private double number;
	private double counter;

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public double getNumber() {
		return number;
	}

	public void setNumber(double number) {
		this.number = number;
	}

	public double getCounter() {
		return counter;
	}

	public void setCounter(double counter) {
		this.counter = counter;
	}

	public void increment() {
		this.counter++;
	}

	public String toString() {
		return "Id : " + getId() + ", class name : " + className + ", method name : " + methodName + ", number : " + number + ", counter : "
				+ counter;
	}

	public int compareTo(@Nonnull final Line<?, ?> o) {
		return getId() != null && o.getId() != null ? getId().compareTo(o.getId()) : 0;
	}

}
