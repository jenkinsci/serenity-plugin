package com.ikokoon.serenity.persistence;

import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.process.Accumulator;
import com.ikokoon.serenity.process.Aggregator;
import com.ikokoon.serenity.process.Cleaner;
import com.ikokoon.serenity.process.Reporter;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Just some useful methods to dump the database and clean it.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09.12.09
 */
public final class DataBaseToolkit {

    public interface Executer {
        void execute(final Object object);
    }

    public interface ICriteria {
        boolean satisfied(final Composite<?, ?> composite);
    }

    static {
        LoggingConfigurator.configure();
    }

    private static final Logger LOGGER = Logger.getLogger(DataBaseToolkit.class.getName());

    public static void accumulateCleanAggregate(final IDataBase dataBase) {
        Date start = new Date();
        LOGGER.info("Starting accumulation : " + start);

        long processStart = System.currentTimeMillis();
        new Accumulator(null).execute();
        LOGGER.info("Accumulator : " + (System.currentTimeMillis() - processStart));

        processStart = System.currentTimeMillis();
        new Cleaner(null, dataBase).execute();
        LOGGER.info("Cleaner : " + (System.currentTimeMillis() - processStart));

        processStart = System.currentTimeMillis();
        new Aggregator(null, dataBase).execute();
        LOGGER.info("Aggregator : " + (System.currentTimeMillis() - processStart));

        processStart = System.currentTimeMillis();
        new Reporter(null, dataBase).execute();
        LOGGER.info("Reporter : " + (System.currentTimeMillis() - processStart));

        String dumpData = Configuration.getConfiguration().getProperty(IConstants.DUMP);
        LOGGER.info("Dump data : " + dumpData + ", " + System.getProperties());
        if (dumpData != null && "true".equals(dumpData.trim())) {
            DataBaseToolkit.dump(dataBase, null, null);
        }

        processStart = System.currentTimeMillis();
        dataBase.close();
        LOGGER.info("Close database : " + (System.currentTimeMillis() - processStart));

        Date end = new Date();
        long million = 1000 * 1000;
        long duration = end.getTime() - start.getTime();
        LOGGER.info("Finished accumulation : " + end + ", duration : " + duration + " millis");
        LOGGER.info("Total memory : " + (Runtime.getRuntime().totalMemory() / million) + ", max memory : "
                + (Runtime.getRuntime().maxMemory() / million) + ", free memory : " + (Runtime.getRuntime().freeMemory() / million));
    }

    /**
     * Clears the data in the database.
     *
     * @param dataBase the database to truncate
     */
    @SuppressWarnings("rawtypes")
    public static synchronized void clear(final IDataBase dataBase) {
        Project<?, ?> project = (Project<?, ?>) dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
        if (project != null) {
            dataBase.remove(Project.class, project.getId());
        }
        List<Package> packages = dataBase.find(Package.class);
        for (final Composite<?, ?> composite : packages) {
            dataBase.remove(composite.getClass(), composite.getId());
        }
        List<Class> classes = dataBase.find(Class.class);
        for (final Composite composite : classes) {
            dataBase.remove(composite.getClass(), composite.getId());
        }
        List<Method> methods = dataBase.find(Method.class);
        for (final Composite composite : methods) {
            dataBase.remove(composite.getClass(), composite.getId());
        }
        List<Line> lines = dataBase.find(Line.class);
        for (final Composite composite : lines) {
            dataBase.remove(composite.getClass(), composite.getId());
        }
        List<Snapshot> snapshots = dataBase.find(Snapshot.class);
        for (final Snapshot snapshot : snapshots) {
            dataBase.remove(Snapshot.class, snapshot.getId());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static synchronized void copyDataBase(final IDataBase sourceDataBase, final IDataBase targetDataBase) {
        Collector.initialize(targetDataBase);
        List<Package> sourcePackages = sourceDataBase.find(Package.class);
        for (final Package sourcePackage : sourcePackages) {
            List<Class> sourceClasses = sourcePackage.getChildren();
            for (final Class sourceClass : sourceClasses) {
                Collector.collectAccess(sourceClass.getName(), sourceClass.getAccess());
                Collector.collectSource(sourceClass.getName(), sourceClass.getSource());
                collectEfferentAndAfferent(sourceClass, sourcePackages);
                List<Class> sourceInnerClasses = sourceClass.getInnerClasses();
                for (final Class sourceInnerClass : sourceInnerClasses) {
                    Collector.collectInnerClass(sourceInnerClass.getName(), sourceClass.getName());
                    Collector.collectSource(sourceInnerClass.getName(), sourceInnerClass.getSource());
                    Method sourceOuterMethod = sourceClass.getOuterMethod();
                    if (sourceOuterMethod != null) {
                        Collector.collectOuterClass(sourceInnerClass.getName(), sourceClass.getName(), sourceOuterMethod.getName(),
                                sourceOuterMethod.getDescription());
                    }
                }
                // Collector.collectSource(sourceClass.getName(), "source");
                List<Method> sourceMethods = sourceClass.getChildren();
                for (final Method sourceMethod : sourceMethods) {
                    Collector.collectComplexity(sourceClass.getName(), sourceMethod.getName(), sourceMethod.getDescription(), sourceMethod.getComplexity());
                    Collector.collectAccess(sourceClass.getName(), sourceMethod.getName(), sourceMethod.getDescription(), sourceMethod.getAccess());
                    List<Line> sourceLines = sourceMethod.getChildren();
                    for (final Line sourceLine : sourceLines) {
                        Collector.collectLine(sourceClass.getName(), sourceMethod.getName(), sourceMethod.getDescription(), (int) sourceLine.getNumber());
                        for (int i = 0; i < sourceLine.getCounter(); i++) {
                            Collector.collectCoverage(sourceClass.getName(), sourceMethod.getName(), sourceMethod.getDescription(),
                                    (int) sourceLine.getNumber());
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes"})
    public static synchronized void execute(final IDataBase dataBase, final Composite composite, final Executer executer) {
        List list = dataBase.find(composite.getClass());
        for (final Object object : list) {
            executer.execute(object);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes", "UnusedAssignment", "WeakerAccess"})
    public static synchronized void collectEfferentAndAfferent(final Class klass, final List<Package> packages) {
        for (final Package pakkage : packages) {
            List<Class> children = pakkage.getChildren();
            for (final Class child : children) {
                Collector.collectEfferentAndAfferent(klass.getName(), child.getName());
            }
        }
    }

    /**
     * Dumps the database to the output stream.
     *
     * @param dataBase the database to dump
     * @param criteria the criteria to match if the data for the composite must be written to the output
     * @param message  the message to set
     */
    @SuppressWarnings("rawtypes")
    public static synchronized void dump(final IDataBase dataBase, final ICriteria criteria, final String message) {
        if (message != null) {
            LOGGER.warning(message);
        }
        try {
            Object object = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
            LOGGER.info("" + object);
            Project<?, ?> project = (Project<?, ?>) dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
            if (project != null) {
                LOGGER.warning("Project : " + project.getName());
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Exception dumping the data for the project object.", e);
        }
        try {
            List<Package> packages = dataBase.find(Package.class);
            for (final Package<?, ?> pakkage : packages) {
                log(criteria, pakkage, 1, pakkage.getId(), " : ", pakkage.getName(), ", coverage : ", pakkage.getCoverage(), ", complexity : ",
                        pakkage.getComplexity(), ", stability : ", pakkage.getStability());
                for (final Class<?, ?> klass : (pakkage.getChildren())) {
                    log(criteria, klass, 2, " : id : ", klass.getId(), " : name : ", klass.getName(), " : coverage : ", klass.getCoverage(), ", complexity : ",
                            klass.getComplexity(), ", outer class : ", klass.getOuterClass(), ", outer method : ", klass.getOuterMethod(), ", lines : ", klass
                                    .getChildren().size(), ", inner classes : ", klass.getInnerClasses());
                    List<Efferent> efferents = klass.getEfferent();
                    List<Afferent> afferents = klass.getAfferent();
                    for (final Efferent efferent : efferents) {
                        log(criteria, efferent, 4, efferent.getName());
                    }
                    for (final Afferent afferent : afferents) {
                        log(criteria, afferent, 4, afferent.getName());
                    }
                    for (final Method<?, ?> method : klass.getChildren()) {
                        log(criteria, method, 3, method.getId().toString(), " : name : ", method.getName(), " : description : ", method.getDescription(),
                                " : coverage : ", method.getCoverage(), ", complexity : ", method.getComplexity(), ", start time : ", method.getStartTime()
                                        + ", end time : ", method.getEndTime());
                        for (final Line<?, ?> line : method.getChildren()) {
                            log(criteria, line, 4, line.getId(), " : number : ", line.getNumber(), ", counter : ", line.getCounter());
                        }
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Exception dumping the data for the database.", e);
        }
    }

    private static synchronized void log(final ICriteria criteria, final Composite<?, ?> composite, final int tabs, final Object... data) {
        if (criteria == null || (criteria.satisfied(composite))) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < tabs; i++) {
                builder.append("    ");
            }
            builder.append(composite.getClass().getSimpleName());
            builder.append(" : ");
            for (final Object datum : data) {
                builder.append(datum);
            }
            LOGGER.warning(builder.toString());
        }
    }

}