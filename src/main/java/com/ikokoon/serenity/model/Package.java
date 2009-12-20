package com.ikokoon.serenity.model;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Entity
@Unique(fields = { Composite.NAME })
public class Package<E, F> extends Composite<Project<?, ?>, Class<?, ?>> implements Comparable<Package<?, ?>>, Serializable {

	private String name;

	private double coverage;
	private double complexity;
	private double abstractness;
	private double stability;
	private double distance;

	private double lines;
	private double interfaces;
	private double implementations;
	private double executed;

	private double efference;
	private double afference;

	private Set<Efferent> efferent = new TreeSet<Efferent>();
	private Set<Afferent> afferent = new TreeSet<Afferent>();

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

	public double getExecuted() {
		return executed;
	}

	public void setExecuted(double totalLinesExecuted) {
		this.executed = totalLinesExecuted;
	}

	public double getComplexity() {
		return Toolkit.format(complexity, PRECISION);
	}

	public void setComplexity(double complexity) {
		this.complexity = complexity;
	}

	public double getCoverage() {
		return Toolkit.format(coverage, PRECISION);
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	public double getAbstractness() {
		return Toolkit.format(abstractness, PRECISION);
	}

	public void setAbstractness(double abstractness) {
		this.abstractness = abstractness;
	}

	public double getStability() {
		return Toolkit.format(stability, PRECISION);
	}

	public void setStability(double stability) {
		this.stability = stability;
	}

	public double getDistance() {
		return Toolkit.format(distance, PRECISION);
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getInterfaces() {
		return Toolkit.format(interfaces, PRECISION);
	}

	public void setInterfaces(double interfaces) {
		this.interfaces = interfaces;
	}

	public double getImplementations() {
		return Toolkit.format(implementations, PRECISION);
	}

	public void setImplementations(double implementations) {
		this.implementations = implementations;
	}

	public double getEfference() {
		return Toolkit.format(efference, PRECISION);
	}

	public void setEfference(double efferent) {
		this.efference = efferent;
	}

	public double getAfference() {
		return Toolkit.format(afference, PRECISION);
	}

	public void setAfference(double afferent) {
		this.afference = afferent;
	}

	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	public Set<Efferent> getEfferent() {
		return efferent;
	}

	public void setEfferent(Set<Efferent> efference) {
		this.efferent = efference;
	}

	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	public Set<Afferent> getAfferent() {
		return afferent;
	}

	public void setAfferent(Set<Afferent> afference) {
		this.afferent = afference;
	}

	public String toString() {
		return getId() + ":" + name;
	}

	public int compareTo(Package<?, ?> o) {
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