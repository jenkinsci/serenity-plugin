package com.ikokoon.serenity.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
@Entity
public abstract class Composite<E, F> {

	public static final int PRECISION = 2;
	public static final String NAME = "name";
	public static final String CLASS_NAME = "className";
	public static final String METHOD_NAME = "methodName";
	public static final String NUMBER = "number";
	public static final String DESCRIPTION = "description";

	private Long id;
	private Composite<E, F> parent;
	private List<F> children = new ArrayList<F>();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Composite<E, F> getParent() {
		return parent;
	}

	public void setParent(Composite<E, F> parent) {
		this.parent = parent;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public List<F> getChildren() {
		return children;
	}

	public void setChildren(List<F> children) {
		this.children = children;
	}

	public static final double COVERAGE_GOOD = 50;
	public static final double COVERAGE_OK = 30;
	public static final double COVERAGE_BAD = 10;

	public static final double COMPLEXITY_GOOD = 10;
	public static final double COMPLEXITY_OK = 30;
	public static final double COMPLEXITY_BAD = 10;

	public static final double ABSTRACTNESS_GOOD = 0.5;
	public static final double ABSTRACTNESS_OK = 0.3;
	public static final double ABSTRACTNESS_BAD = 0.0;

	public static final double STABILITY_GOOD = 0.5;
	public static final double STABILITY_OK = 0.2;
	public static final double STABILITY_BAD = 0.0;

	public static final double DISTANCE_GOOD = 0.5;
	public static final double DISTANCE_OK = 0.3;
	public static final double DISTANCE_BAD = 0.0;

	public static double getCOVERAGE_GOOD() {
		return COVERAGE_GOOD;
	}

	public static double getCOVERAGE_OK() {
		return COVERAGE_OK;
	}

	public static double getCOVERAGE_BAD() {
		return COVERAGE_BAD;
	}

	public static double getCOMPLEXITY_GOOD() {
		return COMPLEXITY_GOOD;
	}

	public static double getCOMPLEXITY_OK() {
		return COMPLEXITY_OK;
	}

	public static double getCOMPLEXITY_BAD() {
		return COMPLEXITY_BAD;
	}

	public static double getABSTRACTNESS_GOOD() {
		return ABSTRACTNESS_GOOD;
	}

	public static double getABSTRACTNESS_OK() {
		return ABSTRACTNESS_OK;
	}

	public static double getABSTRACTNESS_BAD() {
		return ABSTRACTNESS_BAD;
	}

	public static double getSTABILITY_GOOD() {
		return STABILITY_GOOD;
	}

	public static double getSTABILITY_OK() {
		return STABILITY_OK;
	}

	public static double getSTABILITY_BAD() {
		return STABILITY_BAD;
	}

	public static double getDISTANCE_GOOD() {
		return DISTANCE_GOOD;
	}

	public static double getDISTANCE_OK() {
		return DISTANCE_OK;
	}

	public static double getDISTANCE_BAD() {
		return DISTANCE_BAD;
	}

}
