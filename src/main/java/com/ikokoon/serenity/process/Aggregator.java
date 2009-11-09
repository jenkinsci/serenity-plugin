package com.ikokoon.serenity.process;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.ikokoon.IConstants;
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
 * TODO:<br>
 * 
 * Metrics:<br>
 * 1) Total lines executed<br>
 * 2) Total lines of code<br>
 * 3) Total methods executed<br>
 * 4) Project abstractness, stability, efference and afference<br>
 * 5) Lines per method<br>
 * 6) Lines per class<br>
 * 7) Lines per package<br>
 * 8) % code executed per package, class and method<br>
 * 9) Weighted average of above<br>
 * 10) Maven support<br>
 * 
 * Profiling:<br>
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
		logger.info("Running Aggregator dump: ");
		Project<?, ?> project = (Project<?, ?>) dataBase.find(Toolkit.hash(Project.class.getName()));
		if (project != null) {
			aggregateProject(project);
		}
	}

	private void aggregateProject(Project<?, ?> project) {
		List<Package<?, ?>> packages = project.getChildren();
		aggregatePackages(packages);
		project.setTimestamp(new Date());

		double totalLines = 0d;
		double totalLinesExecuted = 0d;
		double totalMethods = 0d;
		double totalMethodsExecuted = 0d;
		double totalClasses = 0d;
		double totalClassesExecuted = 0d;
		double totalPackages = 0d;
		double totalPackagesExecuted = 0d;

		for (Package<?, ?> pakkage : packages) {
			totalPackages++;
			if (pakkage.getTotalLinesExecuted() > 0) {
				totalPackagesExecuted++;
			}
			for (Class<?, ?> klass : ((List<Class<?, ?>>) pakkage.getChildren())) {
				totalLines += klass.getLines();
				totalClasses++;
				if (klass.getTotalLinesExecuted() > 0) {
					totalClassesExecuted++;
				}
				for (Method<?, ?> method : ((List<Method<?, ?>>) klass.getChildren())) {
					totalMethods++;
					totalLinesExecuted += method.getTotalLinesExecuted();
					if (method.getTotalLinesExecuted() > 0) {
						totalMethodsExecuted++;
					}
				}
			}
		}

		project.setTotalLines(totalLines);
		project.setTotalLinesExecuted(totalLinesExecuted);
		project.setTotalMethods(totalMethods);
		project.setTotalMethodsExecuted(totalMethodsExecuted);
		project.setTotalClasses(totalClasses);
		project.setTotalClassesExecuted(totalClassesExecuted);
		project.setTotalPackages(totalPackages);
		project.setTotalPackagesExecuted(totalPackagesExecuted);
	}

	private void aggregatePackages(List<Package<?, ?>> packages) {
		for (Package<?, ?> pakkage : packages) {
			logger.debug("Processing package : " + pakkage.getName());
			List<Class<?, ?>> classes = pakkage.getChildren();
			aggregateClasses(classes);

			double interfaces = 0d;
			double implementations = 0d;
			double packageLines = 0d;
			double complexity = 0d;
			double coverage = 0d;
			double totalLinesExecuted = 0d;

			Set<Efferent> efference = new TreeSet<Efferent>();
			Set<Afferent> afference = new TreeSet<Afferent>();

			for (Class<?, ?> klass : classes) {
				if (klass.getInterfaze()) {
					interfaces++;
				} else {
					implementations++;
				}
				for (Efferent efferent : klass.getEfferentPackages()) {
					efference.add(efferent);
				}
				for (Afferent afferent : klass.getAfferentPackages()) {
					afference.add(afferent);
				}
				packageLines += klass.getLines();
				totalLinesExecuted += klass.getTotalLinesExecuted();
			}

			pakkage.setEfference(efference);
			pakkage.setAfference(afference);

			pakkage.setLines(packageLines);
			pakkage.setTotalLinesExecuted(totalLinesExecuted);

			if (packageLines > 0) {
				for (Class<?, ?> klass : classes) {
					double classLines = klass.getLines();
					complexity += (classLines / packageLines) * klass.getComplexity();
					coverage += (classLines / packageLines) * klass.getCoverage();
				}
			}

			double abstractness = interfaces / ((interfaces + implementations) == 0 ? 1 : (interfaces + implementations));
			pakkage.setAbstractness(abstractness);

			double efferent = efference.size();
			double afferent = afference.size();
			pakkage.setEfferent(efferent);
			pakkage.setAfferent(afferent);

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

			double afferent = klass.getAfferentPackages().size();
			klass.setAfferent(afferent);

			double classLines = 0d;
			double complexity = 0d;
			double coverage = 0d;
			double totalLinesExecuted = 0d;
			for (Method<?, ?> method : methods) {
				classLines += method.getLines();
			}
			if (classLines > 0) {
				for (Method<?, ?> method : methods) {
					double methodLines = method.getLines();
					if (methodLines == 0) {
						continue;
					}
					double weightedLines = methodLines / classLines;
					double methodComplexity = method.getComplexity();
					double methodCoverage = method.getCoverage();
					complexity += methodComplexity * weightedLines;
					coverage += methodCoverage * weightedLines;
					totalLinesExecuted += method.getTotalLinesExecuted();
				}
			}
			klass.setLines(classLines);
			klass.setComplexity(complexity);
			klass.setCoverage(coverage);
			klass.setTotalLinesExecuted(totalLinesExecuted);
			klass.setEfferent(klass.getEfferentPackages().size());

			double efference = klass.getEfferent();
			double afference = klass.getAfferent();
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
					if (line.getCounter() > 0) {
						linesExecuted++;
					}
				}
				double methodLines = method.getLines();
				double coverage = (linesExecuted / methodLines) * 100d;
				method.setCoverage(coverage);
				method.setTotalLinesExecuted(totalLinesExecuted);
			} catch (Exception e) {
				logger.error("Exception peocessing the method element : " + method.getName(), e);
			}
		}
	}

}