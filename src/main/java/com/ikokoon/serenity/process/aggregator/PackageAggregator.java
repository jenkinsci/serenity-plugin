package com.ikokoon.serenity.process.aggregator;

import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.IDataBase;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 07.03.10
 */
public class PackageAggregator extends AAggregator {

    private Package<?, ?> pakkage;

    public PackageAggregator(final IDataBase dataBase, final Package<?, ?> pakkage) {
        super(dataBase);
        this.pakkage = pakkage;
    }

    @SuppressWarnings("rawtypes")
    public void aggregate() {
        // First do the classes
        List<Class<?, ?>> classes = pakkage.getChildren();
        for (final Class klass : classes) {
            new ClassAggregator(dataBase, klass).aggregate();
        }
        aggregate(pakkage);
        setPrecision(pakkage);
        dataBase.persist(pakkage);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void aggregate(final Package pakkage) {
        List<Line<?, ?>> lines = getLines(pakkage);
        List<Method<?, ?>> methods = getMethods(pakkage);

        double interfaces = 0d;
        double implementations = 0d;
        double executed = 0d;
        double methodAccumulatedComplexity = 0d;

        if (methods != null) {
            for (Method<?, ?> method : methods) {
                methodAccumulatedComplexity += method.getComplexity();
            }
        }

        if (lines != null) {
            for (Line<?, ?> line : lines) {
                if (line.getCounter() > 0) {
                    executed++;
                }
            }
        }

        Set<Efferent> efferent = new TreeSet<>();
        Set<Afferent> afferent = new TreeSet<>();

        List<Class<?, ?>> classes = pakkage.getChildren();
        for (Class klass : classes) {
            if (klass.getInterfaze()) {
                interfaces++;
            } else {
                implementations++;
            }
            List<Efferent> efferents = klass.getEfferent();
            for (Efferent e : efferents) {
                efferent.add(e);
            }
            List<Afferent> afferents = klass.getAfferent();
            for (Afferent a : afferents) {
                afferent.add(a);
            }
        }

        pakkage.setEfferent(efferent);
        pakkage.setAfferent(afferent);

        pakkage.setLines(lines == null ? 0 : lines.size());
        pakkage.setExecuted(executed);

        // lines.size() > 0 ? (linesExecuted / lines.size()) * 100d : 0;
        double coverage = getCoverage(lines == null ? 0 : lines.size(), executed);
        // methods.size() > 0 ? methodAccumulatedComplexity / methods.size() : 1;
        double complexity = getComplexity(methods == null ? 0 : methods.size(), methodAccumulatedComplexity);
        // (interfaces + implementations) > 0 ? interfaces / (interfaces + implementations) : 1;
        double abstractness = getAbstractness(interfaces, implementations);
        // (efferent + afferent) > 0 ? efferent / (efferent + afferent) : 1d;
        double stability = getStability(efferent.size(), afferent.size());
        // Math.abs(-stability + -abstractness + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
        double distance = getDistance(stability, abstractness);

        logger.fine("Package aggregates : " + pakkage.getName() + ", cov:com:sta:abs:dis : " + coverage + ":" + complexity + ":" + stability + ":" + abstractness + ":" + distance);

        pakkage.setInterfaces(interfaces);
        pakkage.setImplementations(implementations);
        pakkage.setEfference(efferent.size());
        pakkage.setAfference(afferent.size());

        pakkage.setCoverage(coverage);
        pakkage.setComplexity(complexity);
        pakkage.setStability(stability);
        pakkage.setAbstractness(abstractness);
        pakkage.setDistance(distance);
    }

}
