package com.ikokoon.applet.model;

public class Model implements IModel {

	private String name;
	private String[] legend;
	private double[][] limits;
	private double[][] metrics;

	public Model(String name, String[] legend, double[][] limits, double[][] metrics) {
		this.name = name;
		this.legend = legend;
		this.limits = limits;
		this.metrics = metrics;
	}
	
	public String getName() {
		return name;
	}

	public String[] getLegend() {
		return legend;
	}

	public double[][] getLimits() {
		return limits;
	}

	public double[][] getMetrics() {
		return metrics;
	}

}
