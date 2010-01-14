package com.ikokoon.serenity.hudson.modeller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.IModel;
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
	private final int PRECISION = 2;

	/** The string representation in base 64 of the serialised model object. */
	private String model;
	private Set<String> fields = new TreeSet<String>();
	{
		fields.add("coverage");
		fields.add("complexity");
		fields.add("abstractness");
		fields.add("stability");
		fields.add("distance");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getModel() {
		return model;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(Class<?> klass, Composite<?, ?>... composites) {
		String name = null;
		List<String> legend = new ArrayList<String>();
		List<ArrayList<Double>> metrics = new ArrayList<ArrayList<Double>>();
		// First build the legend and the metrics
		Field[] fields = klass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (!this.fields.contains(field.getName())) {
				continue;
			}
			String fieldName = field.getName();
			String firstLetter = fieldName.substring(0, 1);
			String firstLetterCapital = firstLetter.toUpperCase();
			legend.add(fieldName.replaceFirst(firstLetter, firstLetterCapital));
		}
		for (Composite<?, ?> composite : composites) {
			name = (String) Toolkit.getValue(klass, composite, "name");
			for (int i = 0, index = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (!this.fields.contains(field.getName())) {
					continue;
				}
				String fieldName = field.getName();
				double value = 0;
				if (composite != null) {
					value = Toolkit.getValue(Double.class, composite, fieldName);
				}

				if (fieldName.equals("abstractness") || fieldName.equals("stability") || fieldName.equals("distance")) {
					value *= 100;
				}

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
		IModel model = new Model(name, legend, metrics);
		this.model = Toolkit.serializeToBase64(model);
	}

}