package com.ikokoon.serenity.process;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

/**
 * TODO - Profiling:<br>
 * 11) Total time per method<br>
 * 12) % time per method<br>
 * 13) Total time per class<br>
 * 14) % time per class<br>
 * 15) Total time per package<br>
 * 16) % time per package<br>
 * 
 * This class aggregates all the totals for the report. For each method there is a total for the number of lines that the method has and a for each
 * line that is executed there is a line element. So the percentage of lines that were executed is covered lines / total lines * 100. The complexity
 * total is added up for each method along with the coverage total for each method and divided by the total methods in the class to get the class
 * average for coverage and complexity.
 * 
 * Similarly for the package totals of complexity and coverage the class totals for the package are added up and divided by the number of classes in
 * the package.
 * 
 * Metrics are also gathered for the dependency, afferent and efferent and the abstractness and stability calculated from that.
 * 
 * @author Michael Couck
 * @since 18.07.09
 * @version 01.00
 */
public class Aggregator extends AProcess implements IConstants {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(Aggregator.class);
	/** The database to aggregate the data for. */
	private IDataBase dataBase;

	/**
	 * Constructor takes the parent process.
	 * 
	 * @param parent
	 *            the parent process that will call this child. The child process, i.e. this instance, will add it's self to the parent
	 */
	public Aggregator(IProcess parent, IDataBase dataBase) {
		super(parent);
		this.dataBase = dataBase;
	}

	/**
	 * Please refer to the class JavaDoc for more information.
	 */
	public void execute() {
		super.execute();
		logger.info("Running Aggregator: ");
		Project<?, ?> project = (Project<?, ?>) dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		if (project == null) {
			project = new Project<Object, Object>();
			dataBase.persist(project);
		}
		// DataBaseToolkit.dump(dataBase);
		aggregateProject(project);
	}

	@SuppressWarnings("unchecked")
	private void aggregateProject(Project<?, ?> project) {
		List<Package> children = dataBase.find(Package.class);

		aggregatePackages(children);
		project.setTimestamp(new Date());

		double lines = 0d;
		double linesExecuted = 0d;
		double methods = 0d;
		double methodsExecuted = 0d;
		double classes = 0d;
		double classesExecuted = 0d;
		double packages = 0d;
		double packagesExecuted = 0d;

		double complexity = 0;
		double coverage = 0;
		double interfaces = 0;
		double implementations = 0;
		Set<Efferent> efference = new TreeSet<Efferent>();
		Set<Afferent> afference = new TreeSet<Afferent>();

		for (Package<?, ?> pakkage : children) {
			packages++;
			if (pakkage.getExecuted() > 0) {
				packagesExecuted++;
			}
			for (Class<?, ?> klass : ((List<Class<?, ?>>) pakkage.getChildren())) {
				lines += klass.getLines();
				classes++;
				if (klass.getExecuted() > 0) {
					classesExecuted++;
				}
				for (Method<?, ?> method : ((List<Method<?, ?>>) klass.getChildren())) {
					methods++;
					linesExecuted += method.getExecuted();
					if (method.getExecuted() > 0) {
						methodsExecuted++;
					}
				}
			}
		}

		if (lines > 0) {
			for (Package<?, ?> pakkage : children) {
				double packageLines = pakkage.getLines();
				complexity += (packageLines / lines) * pakkage.getComplexity();
				coverage += (packageLines / lines) * pakkage.getCoverage();
				interfaces += pakkage.getInterfaces();
				implementations += pakkage.getImplementations();
				efference.addAll(pakkage.getEfferent());
				afference.addAll(pakkage.getAfferent());
			}
		}

		double abstractness = interfaces / ((interfaces + implementations) == 0 ? 1 : (interfaces + implementations));
		double efferent = efference.size();
		double afferent = afference.size();
		double stability = efferent / ((efferent + afferent) > 0 ? (efferent + afferent) : 1d);
		double a = -1, b = -1;
		double distance = Math.abs(-stability + -abstractness + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));

		project.setComplexity(complexity);
		project.setCoverage(coverage);
		project.setAbstractness(abstractness);
		project.setStability(stability);
		project.setDistance(distance);

		project.setLines(lines);
		project.setLinesExecuted(linesExecuted);
		project.setMethods(methods);
		project.setMethodsExecuted(methodsExecuted);
		project.setClasses(classes);
		project.setClassesExecuted(classesExecuted);
		project.setPackages(packages);
		project.setPackagesExecuted(packagesExecuted);
	}

	@SuppressWarnings("unchecked")
	private void aggregatePackages(List<Package> children) {
		for (Package<?, ?> pakkage : children) {
			List<Class<?, ?>> classes = pakkage.getChildren();
			aggregateClasses(classes);

			double interfaces = 0d;
			double implementations = 0d;
			double lines = 0d;
			double complexity = 0d;
			double coverage = 0d;
			double linesExecuted = 0d;

			Set<Efferent> efference = new TreeSet<Efferent>();
			Set<Afferent> afference = new TreeSet<Afferent>();

			for (Class<?, ?> klass : classes) {
				if (klass.getInterfaze()) {
					interfaces++;
				} else {
					implementations++;
				}
				for (Efferent efferent : klass.getEfferent()) {
					efference.add(efferent);
				}
				for (Afferent afferent : klass.getAfferent()) {
					afference.add(afferent);
				}
				lines += klass.getLines();
				linesExecuted += klass.getExecuted();
			}

			pakkage.setEfferent(efference);
			pakkage.setAfferent(afference);

			pakkage.setLines(lines);
			pakkage.setExecuted(linesExecuted);

			if (lines > 0) {
				for (Class<?, ?> klass : classes) {
					if (klass.getInterfaze()) {
						continue;
					}
					double classLines = klass.getLines();
					complexity += (classLines / lines) * klass.getComplexity();
					coverage += (classLines / lines) * klass.getCoverage();
				}
			}

			double abstractness = interfaces / ((interfaces + implementations) == 0 ? 1 : (interfaces + implementations));
			pakkage.setAbstractness(abstractness);

			double efferent = efference.size();
			double afferent = afference.size();
			pakkage.setEfference(efferent);
			pakkage.setAfference(afferent);

			double stability = efferent / ((efferent + afferent) > 0 ? (efferent + afferent) : 1d);
			pakkage.setStability(stability);

			// 1) u = (x3 - x1)(x2 - x1) + (y3 - y1)(y2 - y1) / ||p2 - p1||²
			// 2) y=mx+c, 0=ax+by+c, d=|am+bn+c|/sqrt(a²+b²) : d=|-stability + -abstractness + 1|/sqrt(-1²+-1²)
			double a = -1, b = -1;
			double distance = Math.abs(-stability + -abstractness + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
			// distance = distance > 0 ? distance : 0; ??
			pakkage.setDistance(distance);

			pakkage.setComplexity(complexity);
			pakkage.setCoverage(coverage);
			pakkage.setDistance(distance);
			pakkage.setImplementations(implementations);
			pakkage.setInterfaces(interfaces);
			pakkage.setStability(stability);
		}
	}

	private void aggregateClasses(List<Class<?, ?>> classes) {
		for (Class<?, ?> klass : classes) {
			List<Method<?, ?>> methods = klass.getChildren();
			aggregateMethods(methods);

			double afferent = klass.getAfferent().size();
			klass.setAfference(afferent);

			double lines = 0d;
			double complexity = 0d;
			double coverage = 0d;
			double linesExecuted = 0d;
			for (Method<?, ?> method : methods) {
				lines += method.getLines();
				linesExecuted += method.getExecuted();
			}
			if (lines > 0) {
				for (Method<?, ?> method : methods) {
					double methodLines = method.getLines();
					if (methodLines == 0) {
						continue;
					}
					double weightedLines = methodLines / lines;
					double methodComplexity = method.getComplexity();
					double methodCoverage = method.getCoverage();
					complexity += methodComplexity * weightedLines;
					coverage += methodCoverage * weightedLines;
				}
			}
			klass.setLines(lines);
			klass.setComplexity(complexity);
			klass.setCoverage(coverage);
			klass.setExecuted(linesExecuted);
			klass.setEfference(klass.getEfferent().size());

			double efference = klass.getEfference();
			double afference = klass.getAfference();
			double stability = efference / ((efference + afference) > 0 ? (efference + afference) : 1);

			klass.setStability(stability);
		}
	}

	private void aggregateMethods(List<Method<?, ?>> methods) {
		for (Method<?, ?> method : methods) {
			try {
				double linesExecuted = 0d;
				// Collect all the lines that were executed
				List<Line<?, ?>> lines = method.getChildren();

				double totalLinesExecuted = 0;
				for (Line<?, ?> line : lines) {
					totalLinesExecuted += line.getCounter();
					if (logger.isDebugEnabled()) {
						logger.debug("Line covered : " + line + ", " + line.getCounter());
					}
					if (line.getCounter() > 0) {
						linesExecuted++;
					}
				}
				double methodLines = method.getLines();
				if (methodLines > 0) {
					double coverage = (linesExecuted / methodLines) * 100d;
					method.setCoverage(coverage);
				}
				method.setExecuted(totalLinesExecuted);
			} catch (Exception e) {
				logger.error("Exception peocessing the method element : " + method.getName(), e);
			}
		}
	}

}