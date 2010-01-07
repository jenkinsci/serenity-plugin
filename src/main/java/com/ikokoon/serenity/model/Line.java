package com.ikokoon.serenity.model;

import java.io.Serializable;

import javax.persistence.Entity;

import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Entity
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
		return Toolkit.format(number, PRECISION);
	}

	public void setNumber(double number) {
		this.number = number;
	}

	public double getCounter() {
		return Toolkit.format(counter, PRECISION);
	}

	public void setCounter(double counter) {
		// System.out.println("Set counter : " + counter + ", " + number);
		// Thread.dumpStack();
		this.counter = counter;
	}

	public void increment() {
		this.counter++;
	}

	public String toString() {
		return "Id : " + getId() + ", class name : " + className + ", method name : " + methodName + ", number : " + number + ", counter : "
				+ counter;
	}

	public int compareTo(Line<?, ?> o) {
		int comparison = 0;
		if (this.getId() != null && o.getId() != null) {
			comparison = this.getId().compareTo(o.getId());
		}
		return comparison;
	}

}
