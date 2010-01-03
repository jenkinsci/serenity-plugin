package com.ikokoon.serenity.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;

import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Entity
@Unique(fields = { Composite.NAME })
public class Project<E, F> extends Composite<Object, Package<?, ?>> implements Serializable {

	private String name = this.getClass().getName();
	private Date timestamp;

	private double coverage;
	private double complexity;
	private double abstractness;
	private double stability;
	private double distance;

	private double lines;
	private double methods;
	private double classes;
	private double packages;

	private double linesExecuted;
	private double methodsExecuted;
	private double classesExecuted;
	private double packagesExecuted;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getComplexity() {
		return Toolkit.format(complexity, 2);
	}

	public void setComplexity(double complex) {
		this.complexity = complex;
	}

	public double getCoverage() {
		return Toolkit.format(coverage, 2);
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
		return Toolkit.format(stability, 2);
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

}