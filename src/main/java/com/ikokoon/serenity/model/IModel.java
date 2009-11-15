package com.ikokoon.serenity.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public interface IModel extends Serializable {

	public String getName();

	public List<String> getLegend();

	public List<ArrayList<Double>> getLimits();

	public List<ArrayList<Double>> getMetrics();

}
