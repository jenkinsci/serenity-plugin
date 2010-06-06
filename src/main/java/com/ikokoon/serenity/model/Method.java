package com.ikokoon.serenity.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Entity
@Unique(fields = { Composite.CLASS_NAME, Composite.NAME, Composite.DESCRIPTION })
public class Method<E, F> extends Composite<Class<?, ?>, Line<?, ?>> implements Comparable<Method<?, ?>>, Serializable {

	/** General. */
	private String name;
	private String className;
	private int access;
	private String description;

	/** Coverage/complexity. */
	private double complexity;
	private double coverage;

	/** Profiling. */
	private int invocations;
	private long startTime;
	private long endTime;
	private long netTime;
	private long totalTime;
	private long startWait;
	private long endWait;
	private long waitTime;

	private List<Snapshot> snapshots = new ArrayList<Snapshot>();

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

	/** Profiling attributes. */

	public int getInvocations() {
		return invocations;
	}

	public void setInvocations(int invocations) {
		this.invocations = invocations;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getNetTime() {
		return netTime;
	}

	public void setNetTime(long netTime) {
		this.netTime = netTime;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public long getStartWait() {
		return startWait;
	}

	public void setStartWait(long startWait) {
		this.startWait = startWait;
	}

	public long getEndWait() {
		return endWait;
	}

	public void setEndWait(long endWait) {
		this.endWait = endWait;
	}

	public long getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}

	public List<Snapshot> getSnapshots() {
		return snapshots;
	}

	public void setSnapshots(List<Snapshot> snapshots) {
		this.snapshots = snapshots;
	}

	public String toString() {
		return getId() + ":" + name;
	}

	public void reset() {
		setEndTime(0);
		setEndWait(0);
		setInvocations(0);
		setNetTime(0);
		setStartTime(0);
		setStartWait(0);
		setTotalTime(0);
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
