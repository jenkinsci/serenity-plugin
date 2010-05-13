package com.ikokoon.serenity.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Entity
@Unique(fields = { Composite.NAME })
public class Class<E, F> extends Composite<Package<?, ?>, Method<?, ?>> implements Comparable<Class<?, ?>>, Serializable {

	private String name;
	private int access;
	private Class<?, ?> outerClass;
	private Method<?, ?> outerMethod;
	private List<Class<?, ?>> innerClasses = new ArrayList<Class<?, ?>>();

	private double coverage;
	private double complexity;
	private double stability;

	private double efference;
	private double afference;

	private boolean interfaze;

	private List<Efferent> efferent = new ArrayList<Efferent>();
	private List<Afferent> afferent = new ArrayList<Afferent>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAccess() {
		return access;
	}

	public void setAccess(int access) {
		this.access = access;
	}

	public Class<?, ?> getOuterClass() {
		return outerClass;
	}

	public void setOuterClass(Class<?, ?> outerClass) {
		this.outerClass = outerClass;
	}

	public Method<?, ?> getOuterMethod() {
		return outerMethod;
	}

	public void setOuterMethod(Method<?, ?> outerMethod) {
		this.outerMethod = outerMethod;
	}

	public List<Class<?, ?>> getInnerClasses() {
		return innerClasses;
	}

	public void setInnerClasses(List<Class<?, ?>> innerClasses) {
		this.innerClasses = innerClasses;
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

	public double getStability() {
		return stability;
	}

	public void setStability(double stability) {
		this.stability = stability;
	}

	public double getEfference() {
		return efference;
	}

	public void setEfference(double efferent) {
		this.efference = efferent;
	}

	public double getAfference() {
		return afference;
	}

	public void setAfference(double afferent) {
		this.afference = afferent;
	}

	public boolean getInterfaze() {
		return interfaze;
	}

	public void setInterfaze(boolean interfaze) {
		this.interfaze = interfaze;
	}

	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	public List<Efferent> getEfferent() {
		return efferent;
	}

	public void setEfferent(List<Efferent> efferent) {
		this.efferent = efferent;
	}

	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	public List<Afferent> getAfferent() {
		return afferent;
	}

	public void setAfferent(List<Afferent> afferent) {
		this.afferent = afferent;
	}

	public String toString() {
		return getId() + ":" + name;
	}

	public int compareTo(Class<?, ?> o) {
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