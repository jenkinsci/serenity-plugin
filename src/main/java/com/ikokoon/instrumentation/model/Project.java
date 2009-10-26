package com.ikokoon.instrumentation.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Parent(parent = true)
@Unique(fields = { "name" })
public class Project implements Serializable {

	private Long id;
	private Date timestamp;

	private double totalLines;
	private double totalMethods;

	private double totalLinesExecuted;
	private double totalMethodsExecuted;

	private List<Package> children = new LinkedList<Package>();
	private List<Object> index = new LinkedList<Object>();

	@Id
	public Long getId() {
		return id;
	}

	@Identifier
	public void setId(Long id) {
		this.id = id;
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

	public List<Package> getChildren() {
		return children;
	}

	public void setChildren(List<Package> children) {
		this.children = children;
	}

	public List<Object> getIndex() {
		return index;
	}

	public void setIndex(List<Object> index) {
		this.index = index;
	}

}