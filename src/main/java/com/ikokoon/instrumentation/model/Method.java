package com.ikokoon.instrumentation.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Unique(fields = { Composite.CLASS_NAME, Composite.NAME, Composite.DESCRIPTION })
public class Method<E, F> extends Composite<Class<?, ?>, Line<?, ?>> implements Comparable<Method<?, ?>>, Serializable {

	private String name;
	private String className;
	private String description;
	private double complexity;
	private double lines;
	private double totalLinesExecuted;
	private double coverage;
	private Date timestamp;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getComplexity() {
		return format(complexity, PRECISION);
	}

	public void setComplexity(double complexity) {
		this.complexity = complexity;
	}

	public double getLines() {
		return format(lines, PRECISION);
	}

	public void setLines(double lines) {
		this.lines = lines;
	}

	public double getTotalLinesExecuted() {
		return totalLinesExecuted;
	}

	public void setTotalLinesExecuted(double totalLinesExecuted) {
		this.totalLinesExecuted = totalLinesExecuted;
	}

	public double getCoverage() {
		return format(coverage, PRECISION);
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
