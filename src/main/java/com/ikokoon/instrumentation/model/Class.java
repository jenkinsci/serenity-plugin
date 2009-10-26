package com.ikokoon.instrumentation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Unique(fields = { "name" })
public class Class extends Base implements Comparable<Class>, Serializable {

	private Long id;
	private String name;
	private Package parent;
	private double lines;
	private double complexity;
	private double coverage;
	private double stability;
	private double efferent;
	private double afferent;
	private boolean interfaze;
	private Date timestamp;
	private Collection<Method> children = new ArrayList<Method>();
	private Collection<Efferent> efferentPackages = new ArrayList<Efferent>();
	private Collection<Afferent> afferentPackages = new ArrayList<Afferent>();

	@Id
	public Long getId() {
		return id;
	}

	@Identifier
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Package getParent() {
		return parent;
	}

	public void setParent(Package parent) {
		this.parent = parent;
	}

	public double getLines() {
		return lines;
	}

	public void setLines(double lines) {
		this.lines = lines;
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

	public Collection<Method> getChildren() {
		return children;
	}

	public void setChildren(Collection<Method> children) {
		this.children = children;
	}

	public Collection<Efferent> getEfferentPackages() {
		return efferentPackages;
	}

	public void setEfferentPackages(Collection<Efferent> errerent) {
		this.efferentPackages = errerent;
	}

	public Collection<Afferent> getAfferentPackages() {
		return afferentPackages;
	}

	public void setAfferentPackages(Collection<Afferent> afferent) {
		this.afferentPackages = afferent;
	}

	public String toString() {
		return id + ":" + name;
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