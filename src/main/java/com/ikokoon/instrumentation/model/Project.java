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
@Unique(fields = { "name" })
public class Project extends Composite implements Serializable {

	private String name = this.getClass().getName();
	private Date timestamp;

	private double totalLines;
	private double totalMethods;

	private double totalLinesExecuted;
	private double totalMethodsExecuted;

	private List<IComposite> index = new ArrayList<IComposite>();

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

	public List<IComposite> getIndex() {
		return index;
	}

	public void setIndex(List<IComposite> index) {
		this.index = index;
	}

}