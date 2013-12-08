package com.ikokoon.serenity.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Unique(fields = { Composite.NAME })
public class Class<E, F> extends Composite<Package<?, ?>, Method<?, ?>> implements Comparable<Class<?, ?>>, Serializable {

	private int access;
	private String name;
	private Class<?, ?> outerClass;
	private Method<?, ?> outerMethod;	
	private List<Class<?, ?>> innerClasses = new ArrayList<Class<?, ?>>();

	private double coverage;
	private double complexity;
	private double stability;

	private double efference;
	private double afference;

	private boolean interfaze;

	private double allocations;

	private List<Efferent> efferent = new ArrayList<Efferent>();
	private List<Afferent> afferent = new ArrayList<Afferent>();

	private List<Snapshot<?, ?>> snapshots = new ArrayList<Snapshot<?, ?>>();

	private String source;

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

	public double getAllocations() {
		return allocations;
	}

	public void setAllocations(double allocations) {
		this.allocations = allocations;
	}

	public List<Efferent> getEfferent() {
		return efferent;
	}

	public void setEfferent(List<Efferent> efferent) {
		this.efferent = efferent;
	}

	public List<Afferent> getAfferent() {
		return afferent;
	}

	public void setAfferent(List<Afferent> afferent) {
		this.afferent = afferent;
	}

	public List<Snapshot<?, ?>> getSnapshots() {
		return snapshots;
	}

	public void setSnapshots(List<Snapshot<?, ?>> snapshots) {
		this.snapshots = snapshots;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
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