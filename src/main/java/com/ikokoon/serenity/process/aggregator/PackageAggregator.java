package com.ikokoon.serenity.process.aggregator;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.IDataBase;

/**
 * @author Michael Couck
 * @since 07.03.10
 * @version 01.00
 */
public class PackageAggregator extends AAggregator {

	private Package<?, ?> pakkage;

	public PackageAggregator(IDataBase dataBase, Package<?, ?> pakkage) {
		super(dataBase);
		this.pakkage = pakkage;
	}

	@SuppressWarnings("rawtypes")
	public void aggregate() {
		// First do the classes
		List<Class<?, ?>> classes = pakkage.getChildren();
		for (Class klass : classes) {
			new ClassAggregator(dataBase, klass).aggregate();
		}
		aggregate(pakkage);
		setPrecision(pakkage);
		dataBase.persist(pakkage);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void aggregate(Package pakkage) {
		List<Line<?, ?>> lines = getLines(pakkage);
		List<Method<?, ?>> methods = getMethods(pakkage);

		double interfaces = 0d;
		double implementations = 0d;
		double executed = 0d;
		double methodAccumulatedComplexity = 0d;

		if (methods != null) {
			for (Method<?, ?> method : methods) {
				methodAccumulatedComplexity += method.getComplexity();
			}
		}

		if (lines != null) {
			for (Line<?, ?> line : lines) {
				if (line.getCounter() > 0) {
					executed++;
				}
			}
		}

		Set<Efferent> efference = new TreeSet<Efferent>();
		Set<Afferent> afference = new TreeSet<Afferent>();

		List<Class<?, ?>> classes = pakkage.getChildren();
		for (Class klass : classes) {
			if (klass.getInterfaze()) {
				interfaces++;
			} else {
				implementations++;
			}
			List<Efferent> efferents = klass.getEfferent();
			for (Efferent efferent : efferents) {
				efference.add(efferent);
			}
			List<Afferent> afferents = klass.getAfferent();
			for (Afferent afferent : afferents) {
				afference.add(afferent);
			}
		}

		pakkage.setEfferent(efference);
		pakkage.setAfferent(afference);

		pakkage.setLines(lines.size());
		pakkage.setExecuted(executed);

		// lines.size() > 0 ? (linesExecuted / lines.size()) * 100d : 0;
		double coverage = getCoverage(lines.size(), executed);
		// methods.size() > 0 ? methodAccumulatedComplexity / methods.size() : 1;
		double complexity = getComplexity(methods.size(), methodAccumulatedComplexity);
		// (interfaces + implementations) > 0 ? interfaces / (interfaces + implementations) : 1;
		double abstractness = getAbstractness(interfaces, implementations);
		// (efferent + afferent) > 0 ? efferent / (efferent + afferent) : 1d;
		double stability = getStability(efference.size(), afference.size());
		// Math.abs(-stability + -abstractness + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		double distance = getDistance(stability, abstractness);

		pakkage.setInterfaces(interfaces);
		pakkage.setImplementations(implementations);
		pakkage.setEfference(efference.size());
		pakkage.setAfference(afference.size());

		pakkage.setCoverage(coverage);
		pakkage.setComplexity(complexity);
		pakkage.setStability(stability);
		pakkage.setAbstractness(abstractness);
		pakkage.setDistance(distance);
	}

}
