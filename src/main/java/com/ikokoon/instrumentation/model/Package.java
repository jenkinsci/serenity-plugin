package com.ikokoon.instrumentation.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Unique(fields = { Composite.NAME })
public class Package extends Composite implements Comparable<Package>, Serializable {

	private String name;
	private double lines;
	private double totalLinesExecuted;
	private double complexity;
	private double coverage;
	private double abstractness;
	private double stability;
	private double distance;
	private double interfaces;
	private double implementations;
	private double efferent;
	private double afferent;
	private Date timestamp;
	private Set<Efferent> efference = new TreeSet<Efferent>();
	private Set<Afferent> afference = new TreeSet<Afferent>();

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

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
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

	public double getAbstractness() {
		return format(abstractness, PRECISION);
	}

	public void setAbstractness(double abstractness) {
		this.abstractness = abstractness;
	}

	public double getStability() {
		return format(stability, PRECISION);
	}

	public void setStability(double stability) {
		this.stability = stability;
	}

	public double getDistance() {
		return format(distance, PRECISION);
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getInterfaces() {
		return format(interfaces, PRECISION);
	}

	public void setInterfaces(double interfaces) {
		this.interfaces = interfaces;
	}

	public double getImplementations() {
		return format(implementations, PRECISION);
	}

	public void setImplementations(double implementations) {
		this.implementations = implementations;
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

	public Set<Efferent> getEfference() {
		return efference;
	}

	public void setEfference(Set<Efferent> efference) {
		this.efference = efference;
	}

	public Set<Afferent> getAfference() {
		return afference;
	}

	public void setAfference(Set<Afferent> afference) {
		this.afference = afference;
	}

	public String toString() {
		return getId() + ":" + name;
	}

	public int compareTo(Package o) {
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