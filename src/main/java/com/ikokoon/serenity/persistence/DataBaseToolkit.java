package com.ikokoon.serenity.persistence;

import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseToolkit.class);

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

    @SuppressWarnings({"unchecked", "rawtypes", "UnusedAssignment"})
    private static synchronized void collectEfferentAndAfferent(final Class klass, final List<Package> packages) {
        List<Efferent> efferents = klass.getEfferent();
        for (final Efferent efferent : efferents) {
            String efferentPackage = Toolkit.replaceAll(efferent.getName(), "<e:", "");
            efferentPackage = Toolkit.replaceAll(efferent.getName(), ">", "");
            for (final Package pakkage : packages) {
                List<Class> children = pakkage.getChildren();
                for (final Class child : children) {
                    List<Afferent> afferents = child.getAfferent();
                    for (final Afferent afferent : afferents) {
                        String afferentPackage = Toolkit.replaceAll(afferent.getName(), "<a:", "");
                        afferentPackage = Toolkit.replaceAll(afferent.getName(), ">", "");
                        if (efferentPackage.equals(afferentPackage)) {
                            Collector.collectEfferentAndAfferent(klass.getName(), child.getName());
                        }
                    }
                }
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
            LOGGER.warn(message);
        }
        try {
            Object object = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
            LOGGER.info("" + object);
            Project<?, ?> project = (Project<?, ?>) dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
            if (project != null) {
                LOGGER.warn("Project : " + project.getName());
            }
        } catch (final Exception e) {
            LOGGER.error("Exception dumping the data for the project object.", e);
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
                    /*if (klass.getSource() != null) {
                        log(criteria, klass, 5, klass.getSource());
                    }*/
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
            LOGGER.error("Exception dumping the data for the database.", e);
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
            LOGGER.warn(builder.toString());
        }
    }

}