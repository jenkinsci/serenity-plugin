package com.ikokoon.hudson.modeller;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

import com.ikokoon.applet.model.IModel;
import com.ikokoon.applet.model.Model;
import com.ikokoon.instrumentation.model.IComposite;
import com.ikokoon.instrumentation.model.Package;
import com.ikokoon.toolkit.Toolkit;

public class PackageModeller implements IModeller {

	private Logger logger = Logger.getLogger(PackageModeller.class);

	private String model;
	private String[] legend = { "coverage", "complexity", "abstractness", "stability", "distance", "interfaces", "implementations", "efferent",
			"afferent", "lines", "totalLinesExecuted" };
	private double[][] limits = new double[][] { { 50, 10, 0.25, 0.5, 0.5, 1000, 1000, 1000, 1000, 1000000, 1000000 } };

	public String getModel() {
		return model;
	}

	public void visit(IComposite<?, ?> composite) {
		Package<?, ?> pakkage = (Package<?, ?>) composite;
		double[][] metrics = new double[legend.length][1];
		for (int i = 0; i < legend.length; i++) {
			metrics[i][0] = (double) Toolkit.getValue(double.class, pakkage, legend[i]);
		}
		try {
			IModel model = new Model(pakkage.getName(), legend, limits, metrics);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(model);
			byteArrayOutputStream.toByteArray();
		} catch (Exception e) {
			logger.error("Exception generating the model for the package : " + composite, e);
		}
	}

}
