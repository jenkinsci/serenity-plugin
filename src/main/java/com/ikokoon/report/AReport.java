package com.ikokoon.report;

import com.ikokoon.instrumentation.model.IComposite;

public abstract class AReport implements IReport {

	private IComposite<?, ?>[] composites;

	public AReport(IComposite<?, ?>... composites) {
		this.composites = composites;
	}

	public abstract void generate();

	public IComposite<?, ?>[] getComposites() {
		return composites;
	}

}
