package com.ikokoon.serenity.model;

import java.util.List;

public interface IComposite<E, F> {

	public static final int PRECISION = 2;
	public static final String NAME = "name";
	public static final String CLASS_NAME = "className";
	public static final String METHOD_NAME = "methodName";
	public static final String NUMBER = "number";
	public static final String DESCRIPTION = "description";

	public static final String LINES = "Lines";
	public static final String METHODS = "Methods";
	public static final String CLASSES = "Classes";
	public static final String PACKAGES = "Packages";
	public static final String EXECUTED = "Executed";
	public static final String COMPLEXITY = "Complexity";
	public static final String COVERAGE = "Coverage %";
	public static final String ABSTRACTNESS = "Abstract < 1.0";
	public static final String STABILITY = "Stability < 1.0";
	public static final String DISTANCE = "Distance < 1.0";
	public static final String INTERFACES = "Interfaces";
	public static final String IMPLEMENTATIONS = "Implementations";
	public static final String EFFERENCE = "Efference";
	public static final String AFFERENCE = "Afference";

	public Long getId();

	public void setId(Long id);

	public IComposite<E, F> getParent();

	public void setParent(IComposite<E, F> parent);

	public List<F> getChildren();

	public void setChildren(List<F> children);

}
