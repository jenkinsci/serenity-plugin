package com.ikokoon.instrumentation.process;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.ikokoon.IConstants;
import com.ikokoon.instrumentation.Configuration;
import com.ikokoon.instrumentation.model.Afferent;
import com.ikokoon.instrumentation.model.Class;
import com.ikokoon.instrumentation.model.Efferent;
import com.ikokoon.instrumentation.model.Line;
import com.ikokoon.instrumentation.model.Method;
import com.ikokoon.instrumentation.model.Package;
import com.ikokoon.persistence.IDataBase;

/**
 * TODO - add the project metrics aggregation
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

	/**
	 * Constructor takes the parent process.
	 * 
	 * @param parent
	 *            the parent process that will call this child. The child process, i.e. this instance, will add it's self to the parent
	 */
	public Aggregator(IProcess parent) {
		super(parent);
	}

	/**
	 * Please refer to the class JavaDoc for more information.
	 * 
	 * @param document
	 *            the document to aggregate the totals for
	 */
	public void execute() {
		IDataBase dataBase = IDataBase.DataBase.getDataBase();
		logger.info("Running Aggregator dump: ");
		List<Package> packages = dataBase.find(Package.class, 0, Integer.MAX_VALUE);
		for (Package pakkage : packages) {
			// Remove the packages that are not included in the packages that we want coverage on
			logger.debug("Processing package : " + pakkage.getName());
			if (!Configuration.getConfiguration().included(pakkage.getName())) {
				dataBase.remove(Package.class, pakkage.getId());
				logger.debug("Removed : " + pakkage.getName() + "-" + !Configuration.getConfiguration().included(pakkage.getName()));
				continue;
			}
			try {
				// Iterate through all the method elements in each class, add them up for the class
				// then divide them by the number of methods in the class to get the average for method complexity
				// for the class
				Collection<Class> classes = pakkage.getChildren();
				double totalPackageComplexity = 0;
				double totalPackageLines = 0;
				double totalPackageCovered = 0;
				double totalPackageInterfaces = 0;
				double totalPackageImplementations = 0;
				double totalLinesExecuted = 0;
				Set<Efferent> totalPackageEfference = new TreeSet<Efferent>();
				Set<Afferent> totalPackageAfference = new TreeSet<Afferent>();
				for (Class klass : classes) {
					try {
						String className = klass.getName();
						if (Configuration.getConfiguration().excluded(className)) {
							continue;
						}
						if (klass.getInterfaze()) {
							totalPackageInterfaces++;
						} else {
							totalPackageImplementations++;
						}
						Collection<Method> methods = klass.getChildren();
						double totalClassComplexity = 0;
						double totalClassCoverage = 0;
						for (Method method : methods) {
							try {
								// Add up all the method complexities
								double methodComplexity = method.getComplexity();
								// Set the coverage equal to the number of lines covered, by the number of lines in the method
								double methodLines = method.getLines();
								// Lines covered include the initilisation lines in the constructors which is more than the actual
								// lines in the method, which results in a more than 100% coverage of course, wo if this is more
								// then set it to the lines in the method not the lines covered
								double linesCovered = method.getChildren().size();
								linesCovered = linesCovered > methodLines ? methodLines : linesCovered;
								double coverage = (linesCovered / (methodLines != 0 ? methodLines : 1)) * 100d;
								if (logger.isDebugEnabled()) {
									logger.debug("Method : " + method.getName() + ", lines : " + methodLines + ", lines covered : " + linesCovered
											+ ", coverage : " + coverage);
								}
								if (coverage > 0) {
									method.setCoverage(coverage);
									totalClassCoverage += coverage;
								}

								// Collect all the lines that were executed
								Collection<Line> lines = method.getChildren();
								for (Line line : lines) {
									totalLinesExecuted += line.getCounter();
								}

								totalClassComplexity += methodComplexity;
								totalPackageLines += methodLines;
								totalPackageCovered += linesCovered;
							} catch (Exception e) {
								logger.error("Exception peocessing the method element : " + method.getName(), e);
							}
						}
						// Divide the added method complexities by the number of methods in the class to get the class average
						double averageClassComplexity = totalClassComplexity / methods.size() > 0 ? methods.size() : 1d;
						klass.setComplexity(averageClassComplexity);
						double averageClassCoverage = totalClassCoverage / methods.size() > 0 ? methods.size() : 1d;
						klass.setCoverage(averageClassCoverage);

						// The stability I = Ce/(Ce+Ca)
						double classEfference = klass.getEfferentPackages().size();
						double classAfference = klass.getAfferentPackages().size();
						double classStability = classEfference / ((classEfference + classAfference) > 0 ? (classEfference + classAfference) : 1);
						classStability = classStability > 0 ? classStability : 0;
						klass.setEfferent(classEfference);
						klass.setAfferent(classAfference);
						klass.setStability(classStability);

						totalPackageComplexity += averageClassComplexity;

						totalPackageEfference.addAll(klass.getEfferentPackages());
						totalPackageAfference.addAll(klass.getAfferentPackages());
					} catch (Exception e) {
						logger.error("Exception processing the class element : " + klass, e);
					}
				}

				// Add the package complexity to the package tag
				double averagePackageComplexity = totalPackageComplexity / classes.size() > 0 ? classes.size() : 1d;
				pakkage.setComplexity(averagePackageComplexity);
				// Add the package coverage to the package tag
				double averagePackageCoverage = (totalPackageCovered / (totalPackageLines > 0 ? totalPackageLines : 1)) * 100d;
				pakkage.setCoverage(averagePackageCoverage);
				// How many interfaces in the package for abstractness
				pakkage.setInterfaces(totalPackageInterfaces);
				// How many classes in the package for abstractness
				pakkage.setImplementations(totalPackageImplementations);
				// The abstractness is the A=AC/(AC+CC)
				double packageAbstractness = Math.max(1, totalPackageInterfaces / (totalPackageInterfaces + totalPackageImplementations));
				pakkage.setAbstractness(packageAbstractness);

				// Set the afferent and efferent metrics for the package
				double packageEfference = totalPackageEfference.size();
				double packageAfference = totalPackageAfference.size();
				pakkage.setEfferent(packageEfference);
				pakkage.setAfferent(packageAfference);

				// Calculate the average stability for the package
				double packageStability = packageEfference / ((packageEfference + packageAfference) > 0 ? (packageEfference + packageAfference) : 1d);
				pakkage.setStability(packageStability);

				// The distance to the sequence:
				// 1) u = (x3 - x1)(x2 - x1) + (y3 - y1)(y2 - y1) / ||p2 - p1||²
				// 2) y=mx+c, 0=ax+by+c, d=|am+bn+c|/sqrt(a²+b²) : d=|-stability + -abstractness + 1|/sqrt(-1²+-1²)
				double a = -1, b = -1;
				double distance = Math.abs(-packageStability + -packageAbstractness + 1) / Math.sqrt(Math.exp(a) + Math.exp(b));
				// distance = distance > 0 ? distance : 0; ??
				pakkage.setDistance(distance);
			} catch (Exception e) {
				logger.error("Exception processing the package element : " + pakkage, e);
			}
			dataBase.merge(pakkage);
		}
		super.execute();
	}

}