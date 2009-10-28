package com.ikokoon.instrumentation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Unique(fields = { Composite.NAME })
public class Class extends Composite implements Comparable<Class>, Serializable {

	private String name;
	private double lines;
	private double totalLinesExecuted;
	private double complexity;
	private double coverage;
	private double stability;
	private double efferent;
	private double afferent;
	private boolean interfaze;
	private Date timestamp;
	private List<Efferent> efferentPackages = new ArrayList<Efferent>();
	private List<Afferent> afferentPackages = new ArrayList<Afferent>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLines() {
		return lines;
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

	public String getNameTrimmed() {
		if (getName() != null) {
			int index = getName().lastIndexOf('.');
			if (index > -1) {
				return getName().substring(index + 1, getName().length());
			}
		}
		return null;
	}

	public double getComplexity() {
		return format(complexity, PRECISION);
	}

	public void setComplexity(double complexity) {
		this.complexity = complexity;
	}

	public double getCoverage() {
		return format(coverage, PRECISION);
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	public double getStability() {
		return format(stability, PRECISION);
	}

	public void setStability(double stability) {
		this.stability = stability;
	}

	public double getEfferent() {
		return format(efferent, PRECISION);
	}

	public void setEfferent(double efferent) {
		this.efferent = efferent;
	}

	public double getAfferent() {
		return format(afferent, PRECISION);
	}

	public void setAfferent(double afferent) {
		this.afferent = afferent;
	}

	public boolean getInterfaze() {
		return interfaze;
	}

	public void setInterfaze(boolean interfaze) {
		this.interfaze = interfaze;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Collection<Efferent> getEfferentPackages() {
		return efferentPackages;
	}

	public void setEfferentPackages(List<Efferent> efferent) {
		this.efferentPackages = efferent;
	}

	public List<Afferent> getAfferentPackages() {
		return afferentPackages;
	}

	public void setAfferentPackages(List<Afferent> afferent) {
		this.afferentPackages = afferent;
	}

	public String toString() {
		return getId() + ":" + name;
	}

	public int compareTo(Class o) {
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