package com.ikokoon.serenity.model;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12.08.09
 */
@Unique(fields = {Composite.CLASS_NAME, Composite.NAME, Composite.DESCRIPTION})
public class Method<E, F> extends Composite<Class<?, ?>, Line<?, ?>> implements Comparable<Method<?, ?>>, Serializable {

    /**
     * General.
     */
    private String name;
    private String className;
    private int access;
    private String description;

    /**
     * Coverage/complexity.
     */
    private double complexity;
    private double coverage;

    /**
     * Profiling.
     */
    private int invocations;
    private long startTime;
    private long endTime;
    private long netTime;
    private long totalTime;
    private long startWait;
    private long endWait;
    private long waitTime;

    private List<Snapshot<?, ?>> snapshots = new ArrayList<Snapshot<?, ?>>();

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

    /**
     * Profiling attributes.
     */

    /**
     * @return bla...
     */
    public int getInvocations() {
        return invocations;
    }

    /**
     * @param invocations bla...
     */
    public void setInvocations(int invocations) {
        this.invocations = invocations;
    }

    /**
     * @return bla...
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime bla...
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return bla...
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @param endTime bla...
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * @return bla...
     */
    public long getNetTime() {
        return netTime;
    }

    public void setNetTime(long netTime) {
        this.netTime = netTime;
    }

    /**
     * @return bla...
     */
    public long getTotalTime() {
        return totalTime;
    }

    /**
     * @param totalTime bla...
     */
    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * @return bla...
     */
    public long getStartWait() {
        return startWait;
    }

    /**
     * @param startWait bla...
     */
    public void setStartWait(long startWait) {
        this.startWait = startWait;
    }

    /**
     * @return bla...
     */
    public long getEndWait() {
        return endWait;
    }

    /**
     * @param endWait bla...
     */
    public void setEndWait(long endWait) {
        this.endWait = endWait;
    }

    /**
     * @return bla...
     */
    public long getWaitTime() {
        return waitTime;
    }

    /**
     * @param waitTime bla...
     */
    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    /**
     * @return bla...
     */
    public List<Snapshot<?, ?>> getSnapshots() {
        return snapshots;
    }

    /**
     * @param snapshots bla...
     */
    public void setSnapshots(List<Snapshot<?, ?>> snapshots) {
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

    public int compareTo(@Nonnull final Method<?, ?> o) {
        return getId() != null && o.getId() != null ? getId().compareTo(o.getId()) : 0;
    }

}