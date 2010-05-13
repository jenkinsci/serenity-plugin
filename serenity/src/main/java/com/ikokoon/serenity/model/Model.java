package com.ikokoon.serenity.model;

import java.util.ArrayList;
import java.util.List;

/**
 * NOT USED, used to the the model for the chart applet.
 * 
 * This model is a collection of metrics for a composite. In the case of several packages that have been analysed over time, the metrics for the
 * packages are accumulated and represented in a list of lists that can be seen as a matrix of data. This data is then displayed in the front end in
 * the Applet.
 * 
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
public class Model implements IModel {

	/** The name of the composite. */
	private String name;
	/** The legend for the graph. */
	private List<String> legend;
	/** The metrics for the composite over time. */
	private List<ArrayList<Double>> metrics;

	/**
	 * Constructor takes all the data to initialise the model.
	 * 
	 * @param name
	 *            the name of the composite
	 * @param legend
	 *            the legend for the graph
	 * @param metrics
	 *            the data for the composites
	 */
	public Model(String name, List<String> legend, List<ArrayList<Double>> metrics) {
		this.name = name;
		this.legend = legend;
		this.metrics = metrics;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getLegend() {
		return legend;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ArrayList<Double>> getMetrics() {
		return metrics;
	}

}
