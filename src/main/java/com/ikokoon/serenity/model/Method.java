package com.ikokoon.serenity.model;

import java.io.Serializable;

import javax.persistence.Entity;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Entity
@Unique(fields = { Composite.CLASS_NAME, Composite.NAME, Composite.DESCRIPTION })
public class Method<E, F> extends Composite<Class<?, ?>, Line<?, ?>> implements Comparable<Method<?, ?>>, Serializable {

	private String name;
	private double access;
	private String className;
	private String description;
	private double complexity;
	private double coverage;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getAccess() {
		return access;
	}

	public void setAccess(double access) {
		this.access = access;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getComplexity() {
		return complexity;
	}

	public void setComplexity(double complexity) {
		this.complexity = complexity;
	}

	public double getCoverage() {
		return coverage;
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	public String toString() {
		return getId() + ":" + name;
	}

	public int compareTo(Method<?, ?> o) {
		int comparison = 0;
		if (this.getId() != null && o.getId() != null) {
			comparison = this.getId().compareTo(o.getId());
		} else {
			if (this.getName() != null && o.getName() != null) {
				comparison = this.getName().compareTo(o.getName());
			}
		}
		return comparison;
	}

}
