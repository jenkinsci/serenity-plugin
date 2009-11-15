package com.ikokoon.serenity.model;

import java.util.ArrayList;
import java.util.List;

public class Model implements IModel {

	private String name;
	private List<String> legend;
	private List<ArrayList<Double>> limits;
	private List<ArrayList<Double>> metrics;

	public Model(String name, List<String> legend, List<ArrayList<Double>> limits, List<ArrayList<Double>> metrics) {
		this.name = name;
		this.legend = legend;
		this.limits = limits;
		this.metrics = metrics;
	}

	public String getName() {
		return name;
	}

	public List<String> getLegend() {
		return legend;
	}

	public List<ArrayList<Double>> getLimits() {
		return limits;
	}

	public List<ArrayList<Double>> getMetrics() {
		return metrics;
	}

}
