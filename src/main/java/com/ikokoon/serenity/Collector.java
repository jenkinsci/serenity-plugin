package com.ikokoon.serenity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.hudson.source.CoverageSourceCode;
import com.ikokoon.serenity.hudson.source.ISourceCode;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.model.Snapshot;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

/**
 * TODO - make this class non static? Is this a better option? More OO? Better performance? Will it be easier to understand? In the case of
 * distributing the collector class by putting it in the constant pool of the classes and then calling the instance variable from inside the classes,
 * will this be more difficult to understand?
 *
 * In this static class all the real collection logic is in one place and is called statically. The generation of the instructions to call this class
 * is simple and seemingly not much less performant than an instance variable.
 *
 * This class collects the data from the processing. It adds the metrics to the packages, classes, methods and lines and persists the data in the
 * database. This is the central collection class for the coverage and dependency functionality.
 *
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class Collector implements IConstants {

	/** The logger. */
	private static final Logger LOGGER = Logger.getLogger(Collector.class);
	/** The database/persistence object. */
	private static IDataBase dataBase;

	/** These are the profiler methods. */

	/**
	 * Initialises the profiler snapshot taker.
	 *
	 * @param dataBase
	 *            the database to use for taking snapshots.
	 */
	public static void initialize(final IDataBase dataBase) {
		Collector.dataBase = dataBase;
		long snapshptInterval = Configuration.getConfiguration().getSnapshotInterval();
		LOGGER.info("Profiler initialize : " + dataBase + ", " + snapshptInterval);
		if (snapshptInterval > 0) {
			Timer timer = new Timer();
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					try {
						LOGGER.info("Taking snapshot at : " + new Date());
						takeSnapshot();
					} catch (Exception e) {
						LOGGER.error("Exception taking the snapshot : ", e);
					}
				}
			};
			timer.schedule(timerTask, snapshptInterval, snapshptInterval);
		}
	}

	/**
	 * This class is called by the byte code injection to increment the allocations of classes on the heap, i.e. when their constructors are called.
	 *
	 * @param className
	 *            the name of the class being instantiated
	 * @param methodName
	 *            the name of the method, typically this will be 'init'
	 * @param methodDescription
	 *            the byte code description of the method
	 */
	public static final void collectAllocation(String className, String methodName, String methodDescription) {
		Class<Package<?, ?>, Method<?, ?>> klass = getClass(className);
		double allocations = klass.getAllocations();
		allocations++;
		klass.setAllocations(allocations);
	}

	/**
	 * This method is called by the byte code injection at the start of a method, i.e. when a thread enters a method.
	 *
	 * @param className
	 *            the name of the class where the thread is entering the method
	 * @param methodName
	 *            the name of the method in the class that is being executed
	 * @param methodDescription
	 *            the byte code description of the method
	 */
	public static final void collectStart(String className, String methodName, String methodDescription) {
		Method<?, ?> method = getMethod(className, methodName, methodDescription);
		method.setInvocations(method.getInvocations() + 1);
		method.setStartTime(System.nanoTime());
	}

	/**
	 * This method is called by the byte code injection at the end of a method, i.e. when a thread returns from a method. This can happen in a return,
	 * or when an exception is thrown.
	 *
	 * @param className
	 *            the name of the class where the thread is entering the method
	 * @param methodName
	 *            the name of the method in the class that is being executed
	 * @param methodDescription
	 *            the byte code description of the method
	 */
	public static final void collectEnd(String className, String methodName, String methodDescription) {
		Method<?, ?> method = getMethod(className, methodName, methodDescription);
		method.setEndTime(System.nanoTime());
		long executionTime = method.getEndTime() - method.getStartTime();
		long totalTime = method.getTotalTime() + executionTime;
		method.setTotalTime(totalTime);
	}

	/**
	 * This method is called by the byte code injection when there is wait, join, sleep or yield called in a method.
	 *
	 * @param className
	 *            the name of the class where the thread is entering the method
	 * @param methodName
	 *            the name of the method in the class that is being executed
	 * @param methodDescription
	 *            the byte code description of the method
	 */
	public static final void collectStartWait(String className, String methodName, String methodDescription) {
		Method<?, ?> method = getMethod(className, methodName, methodDescription);
		method.setStartWait(System.nanoTime());
	}

	/**
	 * This method is called by the byte code injection when there is wait, join, sleep or yield that exits in a method.
	 *
	 * @param className
	 *            the name of the class where the thread is entering the method
	 * @param methodName
	 *            the name of the method in the class that is being executed
	 * @param methodDescription
	 *            the byte code description of the method
	 */
	public static final void collectEndWait(String className, String methodName, String methodDescription) {
		Method<?, ?> method = getMethod(className, methodName, methodDescription);
		method.setEndWait(System.nanoTime());
		long waitTime = (method.getEndWait() - method.getStartWait()) + method.getWaitTime();
		method.setWaitTime(waitTime);
	}

	/**
	 * This method accumulates the number of times a thread goes through each line in a method.
	 *
	 * @param className
	 *            the name of the class that is calling this method
	 * @param methodName
	 *            the name of the method that the line is in
	 * @param methodDescription
	 *            the description of the method
	 * @param lineNumber
	 *            the line number of the line that is calling this method
	 */
	public static final void collectCoverage(String className, String methodName, String methodDescription, int lineNumber) {
		Line<?, ?> line = getLine(className, methodName, methodDescription, lineNumber);
		line.increment();
	}

	/**
	 * This method just collect the line specified in the parameter list.
	 *
	 * @param className
	 *            the name of the class that is calling this method
	 * @param lineNumber
	 *            the line number of the line that is calling this method
	 * @param methodName
	 *            the name of the method that the line is in
	 * @param methodDescription
	 *            the description of the method
	 */
	public static final void collectLine(String className, String methodName, String methodDescription, Integer lineNumber) {
		getLine(className, methodName, methodDescription, lineNumber);
	}

	/**
	 * This method collects the Java source for the class.
	 *
	 * @param className
	 *            the name of the class
	 * @param source
	 *            the source for the class
	 */
	public static final void collectSource(String className, String source) {
		Class<Package<?, ?>, Method<?, ?>> klass = getClass(className);
		File file = new File(IConstants.SERENITY_SOURCE, className + ".html");
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if (!file.exists()) {
			if (!Toolkit.createFile(file)) {
				LOGGER.info("Couldn't create new source file : " + file);
			}
		}
		if (file.exists()) {
			ISourceCode sourceCode = new CoverageSourceCode(klass, source);
			String htmlSource = sourceCode.getSource();
			Toolkit.setContents(file, htmlSource.getBytes());
		}
	}

	/**
	 * This method is called after each jumps in the method graph. Every time there is a jump the complexity goes up one point. Jumps include if else
	 * statements, or just if, throws statements, switch and so on.
	 *
	 * @param className
	 *            the name of the class the method is in
	 * @param methodName
	 *            the name of the method
	 * @param methodDescription
	 *            the methodDescriptionription of the method
	 * @param complexity
	 *            the complexity of the method
	 */
	public static final void collectComplexity(String className, String methodName, String methodDescription, double complexity) {
		Method<?, ?> method = getMethod(className, methodName, methodDescription);
		method.setComplexity(complexity);
	}

	/**
	 * Collects the packages that the class references and adds them to the document.
	 *
	 * @param className
	 *            the name of the classes
	 * @param targetClassNames
	 *            the referenced class names
	 */
	public static final void collectEfferentAndAfferent(String className, String... targetClassNames) {
		String packageName = Toolkit.classNameToPackageName(className);
		for (String targetClassName : targetClassNames) {
			// Is the target name outside the package for this class
			String targetPackageName = Toolkit.classNameToPackageName(targetClassName);
			if (targetPackageName.trim().equals("")) {
				continue;
			}
			// Is the target and the source the same package name
			if (targetPackageName.equals(packageName)) {
				continue;
			}
			// Exclude java.lang classes and packages
			if (Configuration.getConfiguration().excluded(packageName) || Configuration.getConfiguration().excluded(targetPackageName)) {
				continue;
			}
			// Add the target package name to the afferent packages for this package
			Class<Package<?, ?>, Method<?, ?>> klass = getClass(className);
			Afferent afferent = getAfferent(klass, targetPackageName);
			if (!klass.getAfferent().contains(afferent)) {
				klass.getAfferent().add(afferent);
			}
			// Add this package to the efferent packages of the target
			Class<Package<?, ?>, Method<?, ?>> targetClass = getClass(targetClassName);
			Efferent efferent = getEfferent(targetClass, packageName);
			if (!targetClass.getEfferent().contains(efferent)) {
				targetClass.getEfferent().add(efferent);
			}
		}
	}

	/**
	 * Adds the access attribute to the method object.
	 *
	 * @param className
	 *            the name of the class
	 * @param methodName
	 *            the name of the method
	 * @param methodDescription
	 *            the description of the method
	 * @param access
	 *            the access opcode associated with the method
	 */
	public static final void collectAccess(String className, String methodName, String methodDescription, Integer access) {
		Method<?, ?> method = getMethod(className, methodName, methodDescription);
		method.setAccess(access);
	}

	/**
	 * Adds the access attribute to the class object.
	 *
	 * @param className
	 *            the name of the class
	 * @param access
	 *            the access opcode associated with the class
	 */
	public static final void collectAccess(String className, Integer access) {
		Class<Package<?, ?>, Method<?, ?>> klass = getClass(className);
		if (access.intValue() == 1537) {
			klass.setInterfaze(true);
		}
		klass.setAccess(access);
	}

	/**
	 * Collects the inner class for a class.
	 *
	 * @param innerName
	 *            the name of the inner class
	 * @param outerName
	 *            the name of the outer class
	 */
	public static final void collectInnerClass(String innerName, String outerName) {
		Class<?, ?> innerClass = getClass(innerName);
		Class<?, ?> outerClass = getClass(outerName);
		if (innerClass.getOuterClass() == null) {
			innerClass.setOuterClass(outerClass);
		}
		if (!outerClass.getInnerClasses().contains(innerClass)) {
			outerClass.getInnerClasses().add(innerClass);
		}
	}

	/**
	 * Collects the outer class of an inner class.
	 *
	 * @param innerName
	 *            the name of the inner class
	 * @param outerName
	 *            the name of the outer class
	 * @param outerMethodName
	 *            the method name in the case this is an in-method class definition
	 * @param outerMethodDescription
	 *            the description of the method for anonymous and inline inner classes
	 */
	public static final void collectOuterClass(String innerName, String outerName, String outerMethodName, String outerMethodDescription) {
		Class<?, ?> innerClass = getClass(innerName);
		Class<?, ?> outerClass = getClass(outerName);
		if (innerClass.getOuterClass() == null) {
			innerClass.setOuterClass(outerClass);
		}
		if (!outerClass.getInnerClasses().contains(innerClass)) {
			outerClass.getInnerClasses().add(innerClass);
		}
		if (innerClass.getOuterMethod() == null) {
			if (outerMethodName != null) {
				Method<?, ?> outerMethod = getMethod(outerName, outerMethodName, outerMethodDescription);
				innerClass.setOuterMethod(outerMethod);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static final Package<Project<?, ?>, Class<?, ?>> getPackage(String className) {
		String packageName = Toolkit.classNameToPackageName(className);

		long id = Toolkit.hash(packageName);
		Package<Project<?, ?>, Class<?, ?>> pakkage = (Package<Project<?, ?>, Class<?, ?>>) dataBase.find(Package.class, id);

		if (pakkage == null) {
			pakkage = new Package<Project<?, ?>, Class<?, ?>>();
			pakkage.setName(packageName);
			pakkage.setComplexity(1d);
			pakkage.setCoverage(0d);
			pakkage.setAbstractness(0d);
			pakkage.setStability(0d);
			pakkage.setDistance(0d);
			pakkage.setInterfaces(0d);
			pakkage.setImplementations(0d);
			pakkage = (Package<Project<?, ?>, Class<?, ?>>) dataBase.persist(pakkage);

			LOGGER.debug("Added package : " + pakkage);
		}
		return pakkage;
	}

	@SuppressWarnings("unchecked")
	private static final Class<Package<?, ?>, Method<?, ?>> getClass(String className) {
		long id = Toolkit.hash(className);
		Class klass = (Class) dataBase.find(Class.class, id);

		if (klass == null) {
			klass = new Class();

			klass.setName(className);
			klass.setComplexity(1d);
			klass.setCoverage(0d);
			klass.setStability(0d);
			klass.setEfference(0d);
			klass.setAfference(0d);
			klass.setInterfaze(false);

			Package<Project<?, ?>, Class<?, ?>> pakkage = getPackage(className);
			pakkage.getChildren().add(klass);
			klass.setParent(pakkage);

			klass = (Class<?, ?>) dataBase.persist(klass);
			LOGGER.debug("Added class  : " + klass);
		}
		return klass;
	}

	@SuppressWarnings("unchecked")
	private static final Method<?, ?> getMethod(String className, String methodName, String methodDescription) {
		methodName = methodName.replace('<', ' ').replace('>', ' ').trim();

		long id = Toolkit.hash(className, methodName, methodDescription);
		Method method = (Method) dataBase.find(Method.class, id);

		if (method == null) {
			method = new Method();

			method.setName(methodName);
			method.setClassName(className);
			method.setDescription(methodDescription);
			method.setComplexity(0d);
			method.setCoverage(0d);

			Class klass = getClass(className);
			method.setParent(klass);
			if (klass.getChildren() == null) {
				List<Method> children = new ArrayList<Method>();
				klass.setChildren(children);
			}
			klass.getChildren().add(method);

			dataBase.persist(method);
		}
		return method;
	}

	@SuppressWarnings("unchecked")
	protected static final Line<?, ?> getLine(String className, String methodName, String methodDescription, double lineNumber) {
		long id = Toolkit.hash(className, methodName, lineNumber);
		Line line = (Line) dataBase.find(Line.class, id);

		if (line == null) {
			line = new Line();

			line.setNumber(lineNumber);
			line.setCounter(0d);
			line.setClassName(className);
			line.setMethodName(methodName);

			Method method = getMethod(className, methodName, methodDescription);
			Collection<Composite> lines = method.getChildren();
			line.setParent(method);
			lines.add(line);

			dataBase.persist(line);
		}
		return line;
	}

	private static final Efferent getEfferent(Class<?, ?> klass, String packageName) {
		StringBuilder builder = new StringBuilder("<e:");
		builder.append(packageName);
		builder.append(">");
		String name = builder.toString();

		long id = Toolkit.hash(name);
		Efferent efferent = (Efferent) dataBase.find(Efferent.class, id);
		if (efferent == null) {
			efferent = new Efferent();
			efferent.setName(name);

			klass.getEfferent().add(efferent);

			dataBase.persist(efferent);
		}
		return efferent;
	}

	private static final Afferent getAfferent(Class<?, ?> klass, String packageName) {
		StringBuilder builder = new StringBuilder("<a:");
		builder.append(packageName);
		builder.append(">");
		String name = builder.toString();

		long id = Toolkit.hash(name);
		Afferent afferent = (Afferent) dataBase.find(Afferent.class, id);
		if (afferent == null) {
			afferent = new Afferent();
			afferent.setName(name);

			klass.getAfferent().add(afferent);

			dataBase.persist(afferent);
		}
		return afferent;
	}

	@SuppressWarnings("unchecked")
	private static void takeSnapshot() {
		long time = System.currentTimeMillis();
		List<Class> classes = dataBase.find(Class.class);
		for (Class klass : classes) {
			takeSnapshot(time, klass);
		}
	}

	@SuppressWarnings("unchecked")
	private static Class takeSnapshot(long time, Class klass) {
		List<Method> methods = klass.getChildren();
		long netClassTime = 0;
		long totalClassTime = 0;
		long totalWaitTime = 0;
		for (Method method : methods) {
			takeSnapshot(time, method);
			netClassTime += method.getNetTime();
			totalClassTime += method.getTotalTime();
			totalWaitTime += method.getTotalTime();
		}
		List<Snapshot> snapshots = klass.getSnapshots();
		int size = snapshots.size();
		if (size > 0) {
			// Finalise the last snapshot
			Snapshot snapshot = snapshots.get(size - 1);
			snapshot.setEnd(new Date(time));
			snapshot.setNet(netClassTime);
			snapshot.setTotal(totalClassTime);
			snapshot.setWait(totalWaitTime);
		}
		Snapshot snapshot = new Snapshot();
		snapshot.setStart(new Date(time));
		snapshots.add(snapshot);
		return klass;
	}

	@SuppressWarnings("unchecked")
	private static Method takeSnapshot(long time, Method method) {
		List<Snapshot> snapshots = method.getSnapshots();
		int size = snapshots.size();
		if (size > 0) {
			// Finalise the last snapshot
			Snapshot snapshot = snapshots.get(size - 1);
			snapshot.setEnd(new Date(time));
			snapshot.setNet(method.getNetTime());
			snapshot.setTotal(method.getTotalTime());
			// Reset the method data
			method.reset();
		}
		Snapshot snapshot = new Snapshot();
		snapshot.setStart(new Date(time));
		snapshots.add(snapshot);
		return method;
	}

}