package com.ikokoon.serenity.process.aggregator;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 07.03.10
 * @version 01.00
 */
public class ProjectAggregator extends AAggregator {

	public ProjectAggregator(IDataBase dataBase) {
		super(dataBase);
	}

	@SuppressWarnings("rawtypes")
	public void aggregate() {
		// First do the packages
		List<Package> packages = dataBase.find(Package.class);
		for (Package pakkage : packages) {
			new PackageAggregator(dataBase, pakkage).aggregate();
		}
		Project<?, ?> project = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		if (project == null) {
			project = new Project();
			project.setName(Project.class.getName());
		}
		aggregate(project);
		setPrecision(project);
		dataBase.persist(project);
	}

	@SuppressWarnings("rawtypes")
	protected void aggregate(Project<?, ?> project) {
		List<Package> packages = dataBase.find(Package.class);

		project.setTimestamp(new Date());

		List<Line<?, ?>> lines = getLines(packages);
		List<Method<?, ?>> methods = getMethods(packages);

		double classes = 0d;

		double totalComplexity = 0;
		double executed = 0d;
		double interfaces = 0;
		double implementations = 0;
		Set<Efferent> efference = new TreeSet<Efferent>();
		Set<Afferent> afference = new TreeSet<Afferent>();

		for (Method<?, ?> method : methods) {
			totalComplexity += method.getComplexity();
		}

		for (Line<?, ?> line : lines) {
			if (line.getCounter() > 0) {
				executed++;
			}
		}

		if (lines.size() > 0) {
			for (Package<?, ?> pakkage : packages) {
				interfaces += pakkage.getInterfaces();
				implementations += pakkage.getImplementations();
				efference.addAll(pakkage.getEfferent());
				afference.addAll(pakkage.getAfferent());
				classes += pakkage.getChildren().size();
			}
		}

		// lines.size() > 0 ? (executed / lines.size()) * 100d : 0;
		double coverage = getCoverage(lines.size(), executed);
		// methods.size() > 0 ? totalComplexity / methods.size() : 0;
		double complexity = getComplexity(methods.size(), totalComplexity);

		// (efferent + afferent) > 0 ? efferent / (efferent + afferent) : 1d;
		double stability = getStability(efference.size(), afference.size());
		// (interfaces + implementations) > 0 ? interfaces / (interfaces + implementations) : 1d;
		double abstractness = getAbstractness(interfaces, implementations);
		// Math.abs(-stability + -abstractness + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		double distance = getDistance(stability, abstractness);

		project.setComplexity(complexity);
		project.setCoverage(coverage);
		project.setAbstractness(abstractness);
		project.setStability(stability);
		project.setDistance(distance);

		project.setLines(lines.size());
		project.setMethods(methods.size());
		project.setClasses(classes);
		project.setPackages(packages.size());

		setPrecision(project);
	}
}
