package com.ikokoon.report;

import com.ikokoon.instrumentation.model.IComposite;

public class ProjectTrendReport extends AReport {

	public ProjectTrendReport(IComposite<?, ?>... composites) {
		super(composites);
	}

	public void generate() {
		@SuppressWarnings("unused")
		IComposite<?, ?>[] projects = getComposites();
	}

}
