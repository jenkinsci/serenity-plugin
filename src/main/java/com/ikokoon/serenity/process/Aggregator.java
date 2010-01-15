package com.ikokoon.serenity.process;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

/**
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

	private static final int PRECISION = 2;

	/** The database to aggregate the data for. */
	private IDataBase dataBase;
	/** The maps of composites and lines and composites and methods. Makes the processing faster. */
	private Map<Composite<?, ?>, Set<Line<?, ?>>> compositeLines;
	private Map<Composite<?, ?>, Set<Method<?, ?>>> compositeMethods;

	/**
	 * Constructor takes the parent process.
	 * 
	 * @param parent
	 *            the parent process that will call this child. The child process, i.e. this instance, will add it's self to the parent
	 */
	@SuppressWarnings("unchecked")
	public Aggregator(IProcess parent, IDataBase dataBase) {
		super(parent);
		this.dataBase = dataBase;
		compositeLines = new HashMap<Composite<?, ?>, Set<Line<?, ?>>>();
		compositeMethods = new HashMap<Composite<?, ?>, Set<Method<?, ?>>>();

		Project<?, ?> project = (Project<?, ?>) dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		if (project == null) {
			project = new Project<Object, Object>();
			dataBase.persist(project);
		}

		List<Package> packages = dataBase.find(Package.class);
		Set<Line<?, ?>> lines = getLines(packages);
		Set<Method<?, ?>> methods = getMethods(packages);

		compositeLines.put(project, lines);
		compositeMethods.put(project, methods);
	}

	/**
	 * Please refer to the class JavaDoc for more information.
	 */
	public void execute() {
		super.execute();
		logger.debug("Running Aggregator: ");
		Project<?, ?> project = (Project<?, ?>) dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		// DataBaseToolkit.dump(dataBase);
		aggregateProject(project);
	}

	@SuppressWarnings("unchecked")
	protected void aggregateProject(Project<?, ?> project) {
		List<Package> packages = dataBase.find(Package.class);
		Set<Line<?, ?>> lines = compositeLines.get(project);
		Set<Method<?, ?>> methods = compositeMethods.get(project);

		aggregatePackages(packages);
		project.setTimestamp(new Date());

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

		if (logger.isDebugEnabled()) {
			logger.debug("Lines : " + lines.size() + ", executed : " + executed);
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

		double coverage = getCoverage(lines.size(), executed);// lines.size() > 0 ? (executed / lines.size()) * 100d : 0;
		double complexity = getComplexity(methods.size(), totalComplexity); // methods.size() > 0 ? totalComplexity / methods.size() : 0;

		double stability = getStability(efference.size(), afference.size()); // (efferent + afferent) > 0 ? efferent / (efferent + afferent) : 1d;
		double abstractness = getAbstractness(interfaces, implementations); // (interfaces + implementations) > 0 ? interfaces / (interfaces +
		// implementations) : 1d;
		double distance = getDistance(stability, abstractness); // Math.abs(-stability + -abstractness + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b,
		// 2));

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

	/**
	 * 1) u = (x3 - x1)(x2 - x1) + (y3 - y1)(y2 - y1) / ||p2 - p1||² <br>
	 * 2) y = mx + c, 0 = ax + by + c, d = |am + bn + c| / sqrt(a² + b²) : d= |-stability + -abstractness + 1| / sqrt(-1² + -1²)
	 * 
	 * @param stability
	 * @param abstractness
	 * @return
	 */
	private double getDistance(double stability, double abstractness) {
		double a = -1, b = -1;
		double distance = Math.abs(-stability + -abstractness + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		return distance;
	}

	private double getAbstractness(double interfaces, double implementations) {
		double abstractness = (interfaces + implementations) > 0 ? interfaces / (interfaces + implementations) : 1d;
		return abstractness;
	}

	private double getStability(double efferent, double afferent) {
		double stability = (efferent + afferent) > 0 ? efferent / (efferent + afferent) : 1d;
		return stability;
	}

	private double getComplexity(double methods, double totalComplexity) {
		double complexity = methods > 0 ? totalComplexity / methods : 0;
		return complexity;
	}

	private double getCoverage(double lines, double executed) {
		double coverage = lines > 0 ? (executed / lines) * 100d : 0;
		return coverage;
	}

	@SuppressWarnings("unchecked")
	protected void aggregatePackages(List<Package> children) {
		for (Package<?, ?> pakkage : children) {
			List<Class<?, ?>> classes = pakkage.getChildren();
			aggregateClasses(classes);
			aggregatePackage(pakkage);
			setPrecision(pakkage);
		}
	}

	protected void aggregatePackage(Package<?, ?> pakkage) {
		Set<Line<?, ?>> lines = compositeLines.get(pakkage);
		Set<Method<?, ?>> methods = compositeMethods.get(pakkage);

		double interfaces = 0d;
		double implementations = 0d;
		double executed = 0d;
		double methodAccumulatedComplexity = 0d;

		for (Line<?, ?> line : lines) {
			if (line.getCounter() > 0) {
				executed++;
			}
		}

		for (Method<?, ?> method : methods) {
			methodAccumulatedComplexity += method.getComplexity();
		}

		Set<Efferent> efference = new TreeSet<Efferent>();
		Set<Afferent> afference = new TreeSet<Afferent>();

		for (Class<?, ?> klass : pakkage.getChildren()) {
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
		}

		pakkage.setEfferent(efference);
		pakkage.setAfferent(afference);

		pakkage.setLines(lines.size());
		pakkage.setExecuted(executed);

		double coverage = getCoverage(lines.size(), executed); // lines.size() > 0 ? (linesExecuted / lines.size()) * 100d : 0;
		double complexity = getComplexity(methods.size(), methodAccumulatedComplexity); // methods.size() > 0 ? methodAccumulatedComplexity /
		// methods.size() : 1;

		double abstractness = getAbstractness(interfaces, implementations); // (interfaces + implementations) > 0 ? interfaces / (interfaces +
		// implementations) : 1;

		double stability = getStability(efference.size(), afference.size()); // (efferent + afferent) > 0 ? efferent / (efferent + afferent) : 1d;

		double distance = getDistance(stability, abstractness); // Math.abs(-stability + -abstractness + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b,
		// 2));

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

	protected void aggregateClasses(List<Class<?, ?>> classes) {
		for (Class<?, ?> klass : classes) {
			List<Method<?, ?>> methods = klass.getChildren();
			aggregateMethods(methods);
			aggregateClass(klass);
			setPrecision(klass);
		}
	}

	protected void aggregateClass(Class<?, ?> klass) {
		Set<Line<?, ?>> lines = compositeLines.get(klass);
		Set<Method<?, ?>> methods = compositeMethods.get(klass);

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

		double coverage = getCoverage(lines.size(), executed);// lines.size() > 0 ? (executed / (double) lines.size()) * 100 : 0;
		double complexity = getComplexity(methods.size(), totalComplexity); // methods.size() > 0 ? totalComplexity / methods.size() : 1;

		klass.setCoverage(coverage);
		klass.setComplexity(complexity);
		klass.setAfference(klass.getAfferent().size());
		klass.setEfference(klass.getEfferent().size());

		double stability = getStability(klass.getEfferent().size(), klass.getAfferent().size()); // (efference + afference) > 0 ? efference /
		// (efference + afference) : 1;

		klass.setStability(stability);
	}

	@SuppressWarnings("unchecked")
	protected Set<Line<?, ?>> getLines(Collection<Package> pakkages) {
		Set<Line<?, ?>> projectLines = new TreeSet<Line<?, ?>>();
		for (Package<?, ?> pakkage : pakkages) {
			Set<Line<?, ?>> lines = getLines(pakkage);
			compositeLines.put(pakkage, lines);
			if (logger.isDebugEnabled()) {
				logger.debug("Package : " + pakkage + ", line size : " + lines.size());
			}
			projectLines.addAll(lines);
		}
		return projectLines;
	}

	@SuppressWarnings("unchecked")
	protected Set<Method<?, ?>> getMethods(Collection<Package> pakkages) {
		Set<Method<?, ?>> projectMethods = new TreeSet<Method<?, ?>>();
		for (Package<?, ?> pakkage : pakkages) {
			Set<Method<?, ?>> methods = getMethods(pakkage);
			compositeMethods.put(pakkage, methods);
			if (logger.isDebugEnabled()) {
				logger.debug("Package : " + pakkage + ", method size : " + methods.size());
			}
			projectMethods.addAll(methods);
		}
		return projectMethods;
	}

	protected Set<Line<?, ?>> getLines(Package<?, ?> pakkage) {
		Set<Line<?, ?>> packageLines = new TreeSet<Line<?, ?>>();
		for (Class<?, ?> klass : pakkage.getChildren()) {
			Set<Line<?, ?>> lines = getLines(klass, new TreeSet<Line<?, ?>>());
			compositeLines.put(klass, lines);
			packageLines.addAll(lines);
		}
		return packageLines;
	}

	protected Set<Method<?, ?>> getMethods(Package<?, ?> pakkage) {
		Set<Method<?, ?>> packageMethods = new TreeSet<Method<?, ?>>();
		for (Class<?, ?> klass : pakkage.getChildren()) {
			Set<Method<?, ?>> methods = new TreeSet<Method<?, ?>>();
			getMethods(klass, methods);
			compositeMethods.put(klass, methods);
			packageMethods.addAll(methods);
		}
		return packageMethods;
	}

	protected Set<Line<?, ?>> getLines(Class<?, ?> klass, Set<Line<?, ?>> lines) {
		for (Class<?, ?> innerKlass : klass.getInnerClasses()) {
			getLines(innerKlass, lines);
		}
		for (Method<?, ?> method : klass.getChildren()) {
			for (Line<?, ?> line : method.getChildren()) {
				if (!containsLine(lines, line)) {
					lines.add(line);
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.info("Class : " + klass + ", line size : " + lines.size());
		}
		return lines;
	}

	protected Set<Method<?, ?>> getMethods(Class<?, ?> klass, Set<Method<?, ?>> methods) {
		for (Class<?, ?> innerKlass : klass.getInnerClasses()) {
			getMethods(innerKlass, methods);
		}
		for (Method<?, ?> method : klass.getChildren()) {
			methods.add(method);
		}
		if (logger.isDebugEnabled()) {
			logger.info("Class : " + klass + ", method size : " + methods.size());
		}
		return methods;
	}

	private boolean containsLine(Set<Line<?, ?>> lines, Line<?, ?> line) {
		for (Line<?, ?> setLine : lines) {
			if (setLine.getNumber() == line.getNumber()) {
				return true;
			}
		}
		return false;
	}

	protected void aggregateMethods(List<Method<?, ?>> methods) {
		for (Method<?, ?> method : methods) {
			try {
				double executed = 0d;
				// Collect all the lines that were executed
				for (Line<?, ?> line : method.getChildren()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Line covered : " + line + ", " + line.getCounter());
					}
					if (line.getCounter() > 0) {
						executed++;
					}
				}
				if (method.getChildren().size() > 0) {
					double coverage = getCoverage(method.getChildren().size(), executed); // (executed / method.getChildren().size()) * 100d;
					method.setCoverage(coverage);
				}
			} catch (Exception e) {
				logger.error("Exception peocessing the method element : " + method.getName(), e);
			}
		}
	}

	private void setPrecision(Composite<?, ?> composite) {
		Field[] fields = composite.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (double.class.isAssignableFrom(field.getType()) || Double.class.isAssignableFrom(field.getDeclaringClass())) {
				try {
					field.setAccessible(true);
					double value = field.getDouble(composite);
					value = Toolkit.format(value, PRECISION);
					field.setDouble(composite, value);
				} catch (Exception e) {
					logger.error("Exception accessing the field : " + field, e);
				}
			}
		}
	}

}