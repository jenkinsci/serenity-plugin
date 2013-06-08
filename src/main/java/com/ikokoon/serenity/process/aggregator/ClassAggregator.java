package com.ikokoon.serenity.process.aggregator;

import java.util.ArrayList;
import java.util.List;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.persistence.IDataBase;

/**
 * @author Michael Couck
 * @since 07.03.10
 * @version 01.00
 */
public class ClassAggregator extends AAggregator {

	private Class<?, ?> klass;

	public ClassAggregator(IDataBase dataBase, Class<?, ?> klass) {
		super(dataBase);
		this.klass = klass;
	}

	@SuppressWarnings("rawtypes")
	public void aggregate() {
		// First do the methods
		List<Method<?, ?>> methods = klass.getChildren();
		for (final Method method : methods) {
			new MethodAggregator(dataBase, method).aggregate();
		}
		aggregate(klass);
		setPrecision(klass);
		dataBase.persist(klass);
	}

	protected void aggregate(final Class<?, ?> klass) {
		logger.debug("Processing class : " + klass);

		List<Line<?, ?>> lines = getLines(klass, new ArrayList<Line<?, ?>>());
		List<Method<?, ?>> methods = getMethods(klass, new ArrayList<Method<?, ?>>());

		double executed = 0d;
		double totalComplexity = 0d;

		for (Line<?, ?> line : lines) {
			if (line.getCounter() > 0d) {
				executed++;
			}
		}
		for (Method<?, ?> method : methods) {
			if (lines.size() > 0) {
				totalComplexity += method.getComplexity();
			} else {
				totalComplexity++;
			}
		}

		// lines.size() > 0 ? (executed / (double) lines.size()) * 100 : 0;
		double coverage = getCoverage(lines.size(), executed);
		// methods.size() > 0 ? totalComplexity / methods.size() : 1;
		double complexity = getComplexity(methods.size(), totalComplexity);

		klass.setCoverage(coverage);
		klass.setComplexity(complexity);
		klass.setAfference(klass.getAfferent().size());
		klass.setEfference(klass.getEfferent().size());

		// (efference + afference) > 0 ? efference / (efference + afference) : 1;
		double stability = getStability(klass.getEfferent().size(), klass.getAfferent().size());

		klass.setStability(stability);
	}

}
