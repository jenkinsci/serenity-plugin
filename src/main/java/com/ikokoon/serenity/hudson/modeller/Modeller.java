package com.ikokoon.serenity.hudson.modeller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.ikokoon.serenity.model.IComposite;
import com.ikokoon.serenity.model.IModel;
import com.ikokoon.serenity.model.Legend;
import com.ikokoon.serenity.model.Model;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class takes a composite and produces a string base64 representation of the serialised model object for the composite.
 * 
 * @author Michael Couck
 * @since 09.11.09
 * @version 01.00
 */
public class Modeller implements IModeller {

	/** To avoid having long numbers we make the precision 2 after the decimal. */
	private static final int PRECISION = 2;

	/** The string representation in base 64 of the serialised model object. */
	private String model;

	public String getModel() {
		return model;
	}

	public void visit(Class<?> klass, IComposite<?, ?>... composites) {
		String name = null;
		List<String> legend = new ArrayList<String>();
		List<ArrayList<Double>> limits = new ArrayList<ArrayList<Double>>();
		List<ArrayList<Double>> metrics = new ArrayList<ArrayList<Double>>();
		// First build the legend and the metrics
		Field[] fields = klass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Legend annotation = field.getAnnotation(Legend.class);
			if (annotation == null) {
				continue;
			}
			String legendName = annotation.name();
			legend.add(legendName);
			ArrayList<Double> limit = new ArrayList<Double>();
			for (double d : annotation.limits()) {
				limit.add(d);
			}
			limit.add(annotation.positive());
			limits.add(limit);
		}
		for (IComposite<?, ?> composite : composites) {
			name = (String) Toolkit.getValue(klass, composite, "name");
			for (int i = 0, index = 0; i < fields.length; i++) {
				Field field = fields[i];
				Legend annotation = field.getAnnotation(Legend.class);
				if (annotation == null) {
					continue;
				}
				String fieldName = field.getName();
				double value = Toolkit.getValue(Double.class, composite, fieldName);
				value = Toolkit.format(value, PRECISION);

				ArrayList<Double> metric = null;
				if (metrics.size() > index) {
					metric = (ArrayList<Double>) metrics.get(index);
				} else {
					metric = new ArrayList<Double>();
					metrics.add(metric);
				}
				metric.add(value);
				index++;
			}
		}
		IModel model = new Model(name, legend, limits, metrics);
		this.model = Toolkit.serializeToBase64(model);
	}

}
