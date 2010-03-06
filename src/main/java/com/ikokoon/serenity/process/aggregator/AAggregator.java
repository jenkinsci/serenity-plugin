package com.ikokoon.serenity.process.aggregator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

public abstract class AAggregator implements IAggregator {

	private static final int PRECISION = 2;

	protected Logger logger = Logger.getLogger(this.getClass());
	protected IDataBase dataBase;
	private Map<Object, List<?>> lines = new HashMap<Object, List<?>>();
	private Map<Object, List<?>> methods = new HashMap<Object, List<?>>();

	public AAggregator(IDataBase dataBase) {
		this.dataBase = dataBase;
	}

	/**
	 * Returns a list of lines in the packages, i.e. all the lines in the packages.
	 *
	 * @param pakkages
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<Line<?, ?>> getLines(List<Package> pakkages) {
		List<Line<?, ?>> projectLines = new ArrayList<Line<?, ?>>();
		for (Package<?, ?> pakkage : pakkages) {
			List<Line<?, ?>> lines = (List<Line<?, ?>>) this.lines.get(pakkage);
			if (lines == null) {
				lines = getLines(pakkage);
			}
			projectLines.addAll(lines);
		}
		return projectLines;
	}

	/**
	 * Returns a list of lines in the package.
	 *
	 * @param pakkage
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<Line<?, ?>> getLines(Package<?, ?> pakkage) {
		List<Line<?, ?>> packageLines = new ArrayList<Line<?, ?>>();
		for (Class<?, ?> klass : pakkage.getChildren()) {
			List<Line<?, ?>> lines = (List<Line<?, ?>>) this.lines.get(klass);
			if (lines == null) {
				lines = getLines(klass, new ArrayList<Line<?, ?>>());
			}
			packageLines.addAll(lines);
		}
		return packageLines;
	}

	/**
	 * Returns a list of lines in the class.
	 *
	 * @param klass
	 * @param lines
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<Line<?, ?>> getLines(Class<?, ?> klass, List<Line<?, ?>> lines) {
		for (Class<?, ?> innerKlass : klass.getInnerClasses()) {
			getLines(innerKlass, lines);
		}
		List<Line<?, ?>> setLines = (List<Line<?, ?>>) this.lines.get(klass);
		if (setLines == null) {
			for (Method<?, ?> method : klass.getChildren()) {
				for (Line<?, ?> line : method.getChildren()) {
					if (!containsLine(lines, line)) {
						lines.add(line);
					}
				}
			}
		} else {
			lines.addAll(setLines);
		}
		return lines;
	}

	/**
	 * Returns true if the specified set contains the line.
	 *
	 * @param lines
	 * @param line
	 * @return
	 */
	private boolean containsLine(List<Line<?, ?>> lines, Line<?, ?> line) {
		for (Line<?, ?> setLine : lines) {
			if (setLine.getNumber() == line.getNumber()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a list of methods that are in the packages, i.e. all the methods in the package.
	 *
	 * @param pakkages
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<Method<?, ?>> getMethods(Collection<Package> pakkages) {
		List<Method<?, ?>> projectMethods = new ArrayList<Method<?, ?>>();
		for (Package<?, ?> pakkage : pakkages) {
			List<Method<?, ?>> methods = (List<Method<?, ?>>) this.methods.get(pakkage);
			if (methods == null) {
				methods = getMethods(pakkage);
			}
			projectMethods.addAll(methods);
		}
		return projectMethods;
	}

	@SuppressWarnings("unchecked")
	protected List<Method<?, ?>> getMethods(Package<?, ?> pakkage) {
		List<Method<?, ?>> packageMethods = (List<Method<?, ?>>) methods.get(pakkage);
		if (packageMethods == null) {
			packageMethods = new ArrayList<Method<?, ?>>();
			for (Class<?, ?> klass : pakkage.getChildren()) {
				List<Method<?, ?>> methods = new ArrayList<Method<?, ?>>();
				getMethods(klass, methods);
				packageMethods.addAll(methods);
			}
		}
		return packageMethods;
	}

	protected List<Method<?, ?>> getMethods(Class<?, ?> klass, List<Method<?, ?>> methods) {
		for (Class<?, ?> innerKlass : klass.getInnerClasses()) {
			getMethods(innerKlass, methods);
		}
		for (Method<?, ?> method : klass.getChildren()) {
			methods.add(method);
		}
		return methods;
	}

	protected void setPrecision(Composite<?, ?> composite) {
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

	/**
	 * Distance from the Main Sequence (D): The perpendicular distance of a package from the idealised line A + I = 1. This metric is an indicator of
	 * the package's balance between abstractness and stability. A package squarely on the main sequence is optimally balanced with respect to its
	 * abstractness and stability. Ideal packages are either completely abstract and stable (x=0, y=1) or completely concrete and unstable (x=1, y=0).
	 * The range for this metric is 0 to 1, with D=0 indicating a package that is coincident with the main sequence and D=1 indicating a package that
	 * is as far from the main sequence as possible.
	 *
	 * 1) u = (x3 - x1)(x2 - x1) + (y3 - y1)(y2 - y1) / ||p2 - p1||² <br>
	 * 2) y = mx + c, 0 = ax + by + c, d = |am + bn + c| / sqrt(a² + b²) : d= |-stability + -abstractness + 1| / sqrt(-1² + -1²)
	 *
	 * @param stability
	 * @param abstractness
	 * @return
	 */
	protected double getDistance(double stability, double abstractness) {
		double a = -1, b = -1;
		double distance = Math.abs(-stability + -abstractness + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		return distance;
	}

	/**
	 * Abstractness (A): The ratio of the number of abstract classes (and interfaces) in the analyzed package to the total number of classes in the
	 * analyzed package. The range for this metric is 0 to 1, with A=0 indicating a completely concrete package and A=1 indicating a completely
	 * abstract package.
	 *
	 * @param interfaces
	 * @param implementations
	 * @return
	 */
	protected double getAbstractness(double interfaces, double implementations) {
		double abstractness = (interfaces + implementations) > 0 ? interfaces / (interfaces + implementations) : 1d;
		return abstractness;
	}

	/**
	 * Instability (I): The ratio of efferent coupling (Ce) to total coupling (Ce + Ca) such that I = Ce / (Ce + Ca). This metric is an indicator of
	 * the package's resilience to change. The range for this metric is 0 to 1, with I=0 indicating a completely stable package and I=1 indicating a
	 * completely instable package.
	 *
	 * @param efferent
	 * @param afferent
	 * @return
	 */
	protected double getStability(double efferent, double afferent) {
		double stability = (efferent + afferent) > 0 ? efferent / (efferent + afferent) : 1d;
		return stability;
	}

	/**
	 * Calculates the complexity for a class.
	 *
	 * @param methods
	 * @param totalComplexity
	 * @return
	 */
	protected double getComplexity(double methods, double totalComplexity) {
		double complexity = methods > 0 ? totalComplexity / methods : 1;
		return Math.max(1, complexity);
	}

	/**
	 * Calculates the coverage for a method, class or package.
	 *
	 * @param lines
	 * @param executed
	 * @return
	 */
	protected double getCoverage(double lines, double executed) {
		double coverage = lines > 0 ? (executed / lines) * 100d : 0;
		return coverage;
	}
}
