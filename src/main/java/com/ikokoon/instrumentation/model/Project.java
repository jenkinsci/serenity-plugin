package com.ikokoon.instrumentation.model;

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

	private double totalLines;
	private double totalLinesExecuted;

	private double totalMethods;
	private double totalMethodsExecuted;

	private double totalClasses;
	private double totalClassesExecuted;

	private double totalPackages;
	private double totalPackagesExecuted;

	private List<IComposite<?, ?>> index = new ArrayList<IComposite<?, ?>>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public double getTotalLines() {
		return totalLines;
	}

	public void setTotalLines(double totalLines) {
		this.totalLines = totalLines;
	}

	public double getTotalMethods() {
		return totalMethods;
	}

	public void setTotalMethods(double totalMethods) {
		this.totalMethods = totalMethods;
	}

	public double getTotalLinesExecuted() {
		return totalLinesExecuted;
	}

	public void setTotalLinesExecuted(double totalLinesExecuted) {
		this.totalLinesExecuted = totalLinesExecuted;
	}

	public double getTotalMethodsExecuted() {
		return totalMethodsExecuted;
	}

	public void setTotalMethodsExecuted(double totalMethodsExecuted) {
		this.totalMethodsExecuted = totalMethodsExecuted;
	}

	public double getTotalClasses() {
		return totalClasses;
	}

	public void setTotalClasses(double totalClasses) {
		this.totalClasses = totalClasses;
	}

	public double getTotalClassesExecuted() {
		return totalClassesExecuted;
	}

	public void setTotalClassesExecuted(double totalClassesExecuted) {
		this.totalClassesExecuted = totalClassesExecuted;
	}

	public double getTotalPackages() {
		return totalPackages;
	}

	public void setTotalPackages(double totalPackages) {
		this.totalPackages = totalPackages;
	}

	public double getTotalPackagesExecuted() {
		return totalPackagesExecuted;
	}

	public void setTotalPackagesExecuted(double totalPackagesExecuted) {
		this.totalPackagesExecuted = totalPackagesExecuted;
	}

	public List<IComposite<?, ?>> getIndex() {
		return index;
	}

	public void setIndex(List<IComposite<?, ?>> index) {
		this.index = index;
	}

}