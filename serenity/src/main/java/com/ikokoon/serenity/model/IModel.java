package com.ikokoon.serenity.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @see Model
 * 
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
public interface IModel extends Serializable {

	/**
	 * Access to the name of the composite.
	 * 
	 * @return the name of the composites or set of composites
	 */
	public String getName();

	/**
	 * Access to the legend for the graph.
	 * 
	 * @return the list of strings that represent the legend of the graph
	 */
	public List<String> getLegend();

	/**
	 * Access to the matrix of the data for the composites.
	 * 
	 * @return the matrix of data for the graph
	 */
	public List<ArrayList<Double>> getMetrics();

}
