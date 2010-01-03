package com.ikokoon.serenity.hudson.modeller;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.toolkit.Toolkit;

public class HighchartsModeller implements IModeller {

	public static final int HISTORY = 5;

	private Logger logger = Logger.getLogger(this.getClass());
	private String model;

	public String getModel() {
		return model;
	}

	public void visit(java.lang.Class<?> klass, Composite<?, ?>... composites) {
		// coverageData = [49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]
		// complexityData - [1016, 1016, 1015.9, 1015.5, 1012.3, 1009.5, 1009.6, 1010.2, 1013.1, 1016.9, 1018.2, 1016.7]
		// stabilityData - [7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6]
		// categoryData - ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12']

		StringBuilder coverageData = new StringBuilder("[0.0,");
		StringBuilder complexityData = new StringBuilder("[0.0,");
		StringBuilder stabilityData = new StringBuilder("[0.0,");
		StringBuilder categoryData = new StringBuilder("['0',");

		Composite<?, ?> composite = null;
		for (int i = 0; i < composites.length; i++) {
			composite = composites[i];
			if (composite instanceof Class) {
				coverageData.append(((Class<?, ?>) composite).getCoverage());
				complexityData.append(((Class<?, ?>) composite).getComplexity());
				stabilityData.append(((Class<?, ?>) composite).getStability());
			} else if (composite instanceof Package) {
				coverageData.append(((Package<?, ?>) composite).getCoverage());
				complexityData.append(((Package<?, ?>) composite).getComplexity());
				stabilityData.append(((Package<?, ?>) composite).getStability());
			} else if (composite instanceof Project) {
				coverageData.append(((Project<?, ?>) composite).getCoverage());
				complexityData.append(((Project<?, ?>) composite).getComplexity());
				stabilityData.append(((Project<?, ?>) composite).getStability());
			}
			categoryData.append("'");
			categoryData.append(i + 1);
			categoryData.append("'");
			if (i + 1 < composites.length) {
				coverageData.append(",");
				complexityData.append(",");
				stabilityData.append(",");
				categoryData.append(",");
			}
		}
		coverageData.append("]");
		complexityData.append("]");
		stabilityData.append("]");
		categoryData.append("]");

		String compositeName = composite instanceof Project ? ((Project<?, ?>) composite).getName()
				: composite instanceof Package ? ((Package<?, ?>) composite).getName() : composite instanceof Class ? ((Class<?, ?>) composite)
						.getName() : "What am I?";

		model = Toolkit.getContents(this.getClass().getResourceAsStream("model.jquery")).toString();

		model = Toolkit.replaceAll(model, "compositeName", compositeName);
		model = Toolkit.replaceAll(model, "coverageData", coverageData.toString());
		model = Toolkit.replaceAll(model, "complexityData", complexityData.toString());
		model = Toolkit.replaceAll(model, "stabilityData", stabilityData.toString());
		model = Toolkit.replaceAll(model, "categoryData", categoryData.toString());

		logger.warn("Composite name : " + compositeName + ", coverage data : " + coverageData + ", complexity data : " + complexityData
				+ ", stability data : " + stabilityData + ", category data : " + categoryData);
		logger.warn("Model : " + model);
	}

}
