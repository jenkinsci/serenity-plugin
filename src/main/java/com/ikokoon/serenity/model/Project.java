package com.ikokoon.serenity.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Unique(fields = { Composite.NAME })
public class Project<E, F> extends Composite<Object, Package<?, ?>> implements Serializable {

	private String name = this.getClass().getName();
	private Date timestamp;

	@Legend(name = COVERAGE, limits = { COVERAGE_GOOD, COVERAGE_OK, COVERAGE_BAD }, positive = 1.0)
	private double coverage;
	@Legend(name = COMPLEXITY, limits = { COMPLEXITY_GOOD, COMPLEXITY_OK, COMPLEXITY_BAD })
	private double complexity;
	@Legend(name = ABSTRACTNESS, limits = { ABSTRACTNESS_GOOD, ABSTRACTNESS_OK, ABSTRACTNESS_BAD }, positive = 1.0)
	private double abstractness;
	@Legend(name = STABILITY, limits = { STABILITY_GOOD, STABILITY_OK, STABILITY_BAD }, positive = 1.0)
	private double stability;
	@Legend(name = DISTANCE, limits = { DISTANCE_GOOD, DISTANCE_OK, DISTANCE_BAD }, positive = 1.0)
	private double distance;
	@Legend(name = LINES, limits = { NO_LIMIT, NO_LIMIT, NO_LIMIT })
	private double lines;
	@Legend(name = METHODS, limits = { NO_LIMIT, NO_LIMIT, NO_LIMIT })
	private double methods;
	@Legend(name = CLASSES, limits = { NO_LIMIT, NO_LIMIT, NO_LIMIT })
	private double classes;
	@Legend(name = PACKAGES, limits = { NO_LIMIT, NO_LIMIT, NO_LIMIT })
	private double packages;

	private double linesExecuted;
	private double methodsExecuted;
	private double classesExecuted;
	private double packagesExecuted;

	private List<IComposite<?, ?>> index = new ArrayList<IComposite<?, ?>>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getComplexity() {
		return complexity;
	}

	public void setComplexity(double complex) {
		this.complexity = complex;
	}

	public double getCoverage() {
		return coverage;
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	public double getAbstractness() {
		return abstractness;
	}

	public void setAbstractness(double abstrakt) {
		this.abstractness = abstrakt;
	}

	public double getStability() {
		return stability;
	}

	public void setStability(double stability) {
		this.stability = stability;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public double getLines() {
		return lines;
	}

	public void setLines(double totalLines) {
		this.lines = totalLines;
	}

	public double getMethods() {
		return methods;
	}

	public void setMethods(double totalMethods) {
		this.methods = totalMethods;
	}

	public double getLinesExecuted() {
		return linesExecuted;
	}

	public void setLinesExecuted(double totalLinesExecuted) {
		this.linesExecuted = totalLinesExecuted;
	}

	public double getMethodsExecuted() {
		return methodsExecuted;
	}

	public void setMethodsExecuted(double totalMethodsExecuted) {
		this.methodsExecuted = totalMethodsExecuted;
	}

	public double getClasses() {
		return classes;
	}

	public void setClasses(double totalClasses) {
		this.classes = totalClasses;
	}

	public double getClassesExecuted() {
		return classesExecuted;
	}

	public void setClassesExecuted(double totalClassesExecuted) {
		this.classesExecuted = totalClassesExecuted;
	}

	public double getPackages() {
		return packages;
	}

	public void setPackages(double totalPackages) {
		this.packages = totalPackages;
	}

	public double getPackagesExecuted() {
		return packagesExecuted;
	}

	public void setPackagesExecuted(double totalPackagesExecuted) {
		this.packagesExecuted = totalPackagesExecuted;
	}

	public List<IComposite<?, ?>> getIndex() {
		return index;
	}

	public void setIndex(List<IComposite<?, ?>> index) {
		this.index = index;
	}

}