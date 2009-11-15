package com.ikokoon.serenity.model;

public abstract class AComposite<E, F> implements IComposite<E, F> {

	public static final double NO_LIMIT = 0;

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

	public static double getNO_LIMIT() {
		return NO_LIMIT;
	}

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
