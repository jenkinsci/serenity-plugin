package com.ikokoon.serenity;

import com.ikokoon.serenity.hudson.source.CoverageSourceCode;
import com.ikokoon.serenity.hudson.source.ISourceCode;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Note to self: Make this class non static? Is this a better option? More OO? Better performance? Will it be easier to understand? In the case of distributing
 * the collector class by putting it in the constant pool of the classes and then calling the instance variable from inside the classes, will this be more
 * difficult to understand?
 * <p>
 * In this static class all the real collection logic is in one place and is called statically. The generation of the instructions to call this class is simple
 * and seemingly not much less performant than an instance variable.
 * <p>
 * This class collects the data from the processing. It adds the metrics to the packages, classes, methods and lines and persists the data in the database. This
 * is the central collection class for the coverage and dependency functionality.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.07.09
 */
@SuppressWarnings("DanglingJavadoc")
public final class Collector implements IConstants {

    /**
     * The LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(Collector.class.getName());
    /**
     * The database/persistence object.
     */
    private static IDataBase DATABASE;

    /** These are the profiler methods. */

    /**
     * Initialises the profiler snapshot taker.
     *
     * @param dataBase the database to use for taking snapshots.
     */
    public static void initialize(final IDataBase dataBase) {
        Collector.DATABASE = dataBase;
    }

    /**
     * This class is called by the byte code injection to increment the allocations of classes on the heap, i.e. when their constructors are called.
     *
     * @param className the name of the class being instantiated
     */
    @SuppressWarnings("unused")
    public static void collectAllocation(final String className, final String methodName, final String methodDescription) {
        Class<Package<?, ?>, Method<?, ?>> klass = getClass(className);
        double allocations = klass.getAllocations();
        allocations++;
        klass.setAllocations(allocations);
    }

    /**
     * This method is called by the byte code injection at the start of a method, i.e. when a thread enters a method.
     *
     * @param className         the name of the class where the thread is entering the method
     * @param methodName        the name of the method in the class that is being executed
     * @param methodDescription the byte code description of the method
     */
    @SuppressWarnings("unused")
    public static void collectStart(final String className, final String methodName, final String methodDescription) {
        Method<?, ?> method = getMethod(className, methodName, methodDescription);
        method.setInvocations(method.getInvocations() + 1);
        method.setStartTime(System.nanoTime());
    }

    /**
     * This method is called by the byte code injection at the end of a method, i.e. when a thread returns from a method. This can happen in a return, or when
     * an exception is thrown.
     *
     * @param className         the name of the class where the thread is entering the method
     * @param methodName        the name of the method in the class that is being executed
     * @param methodDescription the byte code description of the method
     */
    @SuppressWarnings("unused")
    public static void collectEnd(final String className, final String methodName, final String methodDescription) {
        Method<?, ?> method = getMethod(className, methodName, methodDescription);
        method.setEndTime(System.nanoTime());
        long executionTime = method.getEndTime() - method.getStartTime();
        long totalTime = method.getTotalTime() + executionTime;
        method.setTotalTime(totalTime);
    }

    /**
     * This method is called by the byte code injection when there is wait, join, sleep or yield called in a method.
     *
     * @param className         the name of the class where the thread is entering the method
     * @param methodName        the name of the method in the class that is being executed
     * @param methodDescription the byte code description of the method
     */
    @SuppressWarnings("unused")
    public static void collectStartWait(final String className, final String methodName, final String methodDescription) {
        Method<?, ?> method = getMethod(className, methodName, methodDescription);
        method.setStartWait(System.nanoTime());
    }

    /**
     * This method is called by the byte code injection when there is wait, join, sleep or yield that exits in a method.
     *
     * @param className         the name of the class where the thread is entering the method
     * @param methodName        the name of the method in the class that is being executed
     * @param methodDescription the byte code description of the method
     */
    @SuppressWarnings("unused")
    public static void collectEndWait(final String className, final String methodName, final String methodDescription) {
        Method<?, ?> method = getMethod(className, methodName, methodDescription);
        method.setEndWait(System.nanoTime());
        long waitTime = (method.getEndWait() - method.getStartWait()) + method.getWaitTime();
        method.setWaitTime(waitTime);
    }

    /**
     * This method accumulates the number of times a thread goes through each line in a method.
     *
     * @param className         the name of the class that is calling this method
     * @param methodName        the name of the method that the line is in
     * @param methodDescription the description of the method
     * @param lineNumber        the line number of the line that is calling this method
     */
    public static void collectCoverage(final String className, final String methodName, final String methodDescription, final int lineNumber) {
        Line<?, ?> line = getLine(className, methodName, methodDescription, lineNumber);
        line.increment();
    }

    /**
     * This method just collect the line specified in the parameter list.
     *
     * @param className         the name of the class that is calling this method
     * @param lineNumber        the line number of the line that is calling this method
     * @param methodName        the name of the method that the line is in
     * @param methodDescription the description of the method
     */
    public static void collectLine(final String className, final String methodName, final String methodDescription, final Integer lineNumber) {
        getLine(className, methodName, methodDescription, lineNumber);
    }

    /**
     * This method collects the Java source for the class.
     *
     * @param className the name of the class
     * @param source    the source for the class
     */
    public static void collectSource(final String className, final String source) {
        Class<Package<?, ?>, Method<?, ?>> klass = getClass(className);
        // We only set the source if it is null
        if (klass.getSource() == null && source != null && !"".equals(source.trim())) {
            klass.setSource(source);
            LOGGER.fine("Setting source : " + klass.getName());
            LOGGER.fine("                       : " + klass.getSource());
        }
        File file = new File(IConstants.SERENITY_SOURCE, className + ".html");
        if (!file.getParentFile().exists()) {
            boolean madeDirectories = file.getParentFile().mkdirs();
            if (!madeDirectories) {
                LOGGER.warning("Couldn't make directories : " + file.getAbsolutePath());
            }
        }
        // LOGGER.error("Collecting source : " + className + ", " + file.getAbsolutePath());
        // We try to delete the old file first
        boolean deleted = file.delete();
        if (!deleted) {
            LOGGER.warning("Didn't delete source coverage file : " + file);
        }
        if (!file.exists()) {
            if (!Toolkit.createFile(file)) {
                LOGGER.warning("Couldn't create new source file : " + file);
            }
        }
        if (file.exists()) {
            LOGGER.fine("Writing source to file : " + file.getAbsolutePath());
            ISourceCode sourceCode = new CoverageSourceCode(klass, source);
            String htmlSource = sourceCode.getSource();
            Toolkit.setContents(file, htmlSource.getBytes(Charset.defaultCharset()));
        } else {
            LOGGER.warning("Source file does not exist : " + file);
        }
    }

    /**
     * This method is called after each jumps in the method graph. Every time there is a jump the complexity goes up one point. Jumps include if else
     * statements, or just if, throws statements, switch and so on.
     *
     * @param className         the name of the class the method is in
     * @param methodName        the name of the method
     * @param methodDescription the methodDescriptionription of the method
     * @param complexity        the complexity of the method
     */
    public static void collectComplexity(final String className, final String methodName, final String methodDescription, final double complexity) {
        Method<?, ?> method = getMethod(className, methodName, methodDescription);
        method.setComplexity(complexity);
    }

    /**
     * Collects the packages that the class references and adds them to the data.
     *
     * @param sourceClassName        the name of the class in the source application
     * @param targetClassNames the class names that are found in the source class, i.e. the efferent classes
     */
    public static void collectEfferentAndAfferent(final String sourceClassName, final String... targetClassNames) {
        String sourcePackageName = Toolkit.classNameToPackageName(sourceClassName);
        for (String targetClassName : targetClassNames) {
            // Is the target name outside the package for this class
            String targetPackageName = Toolkit.classNameToPackageName(targetClassName);
            if (targetPackageName.trim().equals("")) {
                continue;
            }
            // Is the target and the source the same package name
            if (targetPackageName.equals(sourcePackageName)) {
                continue;
            }
            // Exclude java.lang classes and packages
            if (Configuration.getConfiguration().excluded(sourcePackageName) || Configuration.getConfiguration().excluded(targetPackageName)) {
                continue;
            }
            // Add the target package name to the afferent packages for this package
            Class<Package<?, ?>, Method<?, ?>> sourceClass = getClass(sourceClassName);
            getEfferent(sourceClass, targetPackageName);
            // Add this package to the efferent packages of the target
            Class<Package<?, ?>, Method<?, ?>> targetClass = getClass(targetClassName);
            getAfferent(targetClass, sourcePackageName);
        }
    }

    /**
     * Adds the access attribute to the method object.
     *
     * @param className         the name of the class
     * @param methodName        the name of the method
     * @param methodDescription the description of the method
     * @param access            the access opcode associated with the method
     */
    public static void collectAccess(final String className, final String methodName, final String methodDescription, final Integer access) {
        Method<?, ?> method = getMethod(className, methodName, methodDescription);
        method.setAccess(access);
    }

    /**
     * Adds the access attribute to the class object.
     *
     * @param className the name of the class
     * @param access    the access opcode associated with the class
     */
    public static void collectAccess(final String className, final Integer access) {
        Class<Package<?, ?>, Method<?, ?>> klass = getClass(className);
        if (1537 == access) {
            klass.setInterfaze(true);
        }
        klass.setAccess(access);
    }

    /**
     * Collects the inner class for a class.
     *
     * @param innerName the name of the inner class
     * @param outerName the name of the outer class
     */
    public static void collectInnerClass(final String innerName, final String outerName) {
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
     * @param innerName              the name of the inner class
     * @param outerName              the name of the outer class
     * @param outerMethodName        the method name in the case this is an in-method class definition
     * @param outerMethodDescription the description of the method for anonymous and inline inner classes
     */
    public static void collectOuterClass(final String innerName, final String outerName, final String outerMethodName, final String outerMethodDescription) {
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

    private static Package<Project<?, ?>, Class<?, ?>> getPackage(final String className) {
        String packageName = Toolkit.classNameToPackageName(className);

        long id = Toolkit.hash(packageName);
        @SuppressWarnings("unchecked")
        Package<Project<?, ?>, Class<?, ?>> pakkage = (Package<Project<?, ?>, Class<?, ?>>) DATABASE.find(Package.class, id);

        if (pakkage == null) {
            pakkage = new Package<>();
            pakkage.setName(packageName);
            pakkage.setComplexity(1d);
            pakkage.setCoverage(0d);
            pakkage.setAbstractness(0d);
            pakkage.setStability(0d);
            pakkage.setDistance(0d);
            pakkage.setInterfaces(0d);
            pakkage.setImplementations(0d);
            pakkage = DATABASE.persist(pakkage);
        }
        return pakkage;
    }

    private static Class<Package<?, ?>, Method<?, ?>> getClass(final String className) {
        long id = Toolkit.hash(className);
        Class klass = DATABASE.find(Class.class, id);

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
            //noinspection unchecked
            klass.setParent(pakkage);

            klass = (Class<?, ?>) DATABASE.persist(klass);
        }
        //noinspection unchecked
        return klass;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Method<?, ?> getMethod(final String className, final String methodName, final String methodDescription) {
        String cleanMethodName = methodName.replace('<', ' ').replace('>', ' ').trim();

        long id = Toolkit.hash(className, cleanMethodName, methodDescription);
        Method method = DATABASE.find(Method.class, id);

        if (method == null) {
            method = new Method();

            method.setName(cleanMethodName);
            method.setClassName(className);
            method.setDescription(methodDescription);
            method.setComplexity(0d);
            method.setCoverage(0d);

            Class klass = getClass(className);
            method.setParent(klass);
            if (klass.getChildren() == null) {
                List<Method> children = new ArrayList<>();
                klass.setChildren(children);
            }
            klass.getChildren().add(method);

            DATABASE.persist(method);
        }
        return method;
    }

    @SuppressWarnings({"unchecked", "rawtypes", "WeakerAccess"})
    protected static Line<?, ?> getLine(final String className, final String methodName, final String methodDescription, final double lineNumber) {
        long id = Toolkit.hash(className, methodName, lineNumber);
        Line line = DATABASE.find(Line.class, id);

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

            DATABASE.persist(line);
        }
        return line;
    }

    private static Efferent getEfferent(final Class<?, ?> klass, final String packageName) {
        String name = "<e:" + packageName + ">";
        long id = Toolkit.hash(name);
        Efferent efferent = DATABASE.find(Efferent.class, id);
        if (efferent == null) {
            efferent = new Efferent();
            efferent.setName(name);

            klass.getEfferent().add(efferent);

            DATABASE.persist(efferent);
        }
        return efferent;
    }

    private static Afferent getAfferent(final Class<?, ?> klass, final String packageName) {
        String name = "<a:" + packageName + ">";
        long id = Toolkit.hash(name);
        Afferent afferent = DATABASE.find(Afferent.class, id);
        if (afferent == null) {
            afferent = new Afferent();
            afferent.setName(name);

            klass.getAfferent().add(afferent);

            DATABASE.persist(afferent);
        }
        return afferent;
    }

    /**
     * There can be only one!
     */
    private Collector() {
    }

}