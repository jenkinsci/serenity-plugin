package com.ikokoon.serenity;

import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Snapshot;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.Reporter;
import com.ikokoon.serenity.process.Snapshooter;

import java.util.*;
import java.util.logging.Logger;

/**
 * This class generates the reports for the profiled classes.
 * <pre>
 * Model:
 * Class class
 *     List snapshots
 *         long net
 *         long total
 *         long wait
 *         Date start
 *         Date end
 *     List methods
 *         List snapshots
 *             long net
 *             long total
 *             long wait
 *             Date start
 *             Date end
 *
 * Calculations:
 * 1) Calculate the total time for each method - totalMethodTime()
 *     Snapshots {2, 5, 3, 6, 5} = 2 + 5 + 3 + 6 + 5 = 21
 * 2) Calculate the total net time for each method - totalNetMethodTime()
 *     Snapshot {1, 2, 4, 3, 2 } = 1 + 2 + 4 + 3 + 2 = 12
 * 3) Calculate the series for the total times for the method - methodSeries()
 *     Snapshots {2, 5, 3, 6, 5}
 * 4) Calculate the series for the net times for the method - methodNetSeries()
 *     Snapshot {1, 2, 4, 3, 2}
 * 5) Calculate the series for the change in total time for the method - methodChangeSeries()
 *     Snapshots {2, 5, 3, 6, 5} = {3, -2, 3, -1}
 * 6) Calculate the series for the change in net time for the method - methodNetChangeSeries()
 *     Snapshot {1, 2, 4, 3, 2} = {1, 2, -1, -1}
 * 7) Calculate the average total time for each method - averageMethodTime()
 *     Snapshot {2, 5, 3, 6, 5} = (2 + 5 + 3 + 6 + 5)/5 = 21/5 = 4.2
 * 8) Calculate the average net time for each method - averageMethodNetTime()
 *     Snapshot {1, 2, 4, 3, 2} = (1 + 2 + 4 + 3 + 2)/5 = 12/5 = 2.4
 * 9) Calculate the total change for the methods - methodChange()
 *     Snapshots {2, 5, 3, 6, 5} = 3
 * 10) Calculate the net change for the methods - methodNetChange()
 *     Snapshot {1, 2, 4, 3, 2} = 1
 *
 * Does this make sense?
 * x) Calculate the average total change for the methods - averageMethodChange()
 *     Snapshots {2, 5, 3, 6, 5} = 3, -2, 3, -1 = 3/5 = 0.66
 * xx) Calculate the average net change for the methods - averageNetMethodChange()
 * 	    Snapshot {1, 2, 4, 3, 2} = 1, 2, -1, -1  = 1/5 = 0.20
 * xxx) Calculate the average change in total time for each class - averageClassTimeChange()
 * xxxx) Calculate the average change in net time for each class - averageClassNetTimeChange()
 *
 * 1) Calculate the total time for each class - totalClassTime()
 * 2) Calculate the total net time for each class - totalNetClassTime()
 *
 * 5) Calculate the highest average total change for the classes - highestAverageClassChange()
 * 6) Calculate the highest average net change for the classes - highestAverageNetClassChange()
 * 7) Calculate the highest total change for the classes - highestClassChange()
 * 8) Calculate the highest net change for the classes - highestClassNetChange()
 * 9) Calculate the series for the total times for the class - classSeries()
 * 10) Calculate the series for the net times for the class - classNetSeries()
 * 11) Calculate the series for the change in total time for the class -
 * 12) Calculate the series for the change in net time for the class
 *
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.06.10
 */
public class Profiler {

    protected static final Logger LOGGER = Logger.getLogger(Profiler.class.getName());

    private static double TIME_UNIT_DENOMINATOR = 1d;

    public static void initialize(final IDataBase dataBase) {
        long snapshptInterval = Configuration.getConfiguration().getSnapshotInterval();
        if (snapshptInterval > 0) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        LOGGER.severe("Taking snapshot at : " + new Date());
                        new Snapshooter(null, dataBase).execute();
                    } catch (Exception e) {
                        LOGGER.severe("Exception taking the snapshot : " + e);
                    }
                }
            };
            timer.schedule(timerTask, snapshptInterval, snapshptInterval);
        }
        long reportInterval = Configuration.getConfiguration().getReportInterval();
        if (reportInterval > 0) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        LOGGER.severe("Writing report at : " + new Date());
                        new Reporter(null, dataBase).execute();
                    } catch (Exception e) {
                        LOGGER.severe("Exception taking the snapshot : " + e);
                    }
                }
            };
            timer.schedule(timerTask, snapshptInterval, snapshptInterval);
        }
        // LOGGER.info("Profiler initialize : " + dataBase + ", " + snapshptInterval + ", " + reportInterval);
        TIME_UNIT_DENOMINATOR = Configuration.getConfiguration().getTimeUnitDenominator();
    }

    private static double getValue(final double value) {
        return value / TIME_UNIT_DENOMINATOR;
    }

    /**
     * Calculate the series for the total times for the method<br>
     * methodSeries()
     * Snapshots {2, 5, 3, 6, 5}
     *
     * @param method the method series of timeings
     * @return a list of the times for the method
     */
    public static List<Double> methodSeries(Method<?, ?> method) {
        List<Double> series = new ArrayList<Double>();
        List<Snapshot<?, ?>> snapshots = method.getSnapshots();
        for (Snapshot<?, ?> snapshot : snapshots) {
            series.add(getValue(snapshot.getTotal()));
        }
        return series;
    }

    /**
     * Calculate the series for the net times for the method<br>
     * methodNetSeries()<br>
     * Snapshot {1, 2, 4, 3, 2}
     *
     * @param method the method to get the net time for
     * @return the list of timings for the method
     */
    public static List<Double> methodNetSeries(Method<?, ?> method) {
        List<Double> series = new ArrayList<Double>();
        List<Snapshot<?, ?>> snapshots = method.getSnapshots();
        for (Snapshot<?, ?> snapshot : snapshots) {
            double netTime = snapshot.getTotal() - snapshot.getWait();
            snapshot.setNet(getValue(netTime));
            series.add(snapshot.getNet());
        }
        return series;
    }

    /**
     * Calculate the total time for each method:<br>
     * totalMethodTime()<br>
     * Snapshots {2, 5, 3, 6, 5} = 2 + 5 + 3 + 6 + 5 = 21
     *
     * @param method the method to get the total time for
     * @return the total time for the method
     */
    public static double totalMethodTime(Method<?, ?> method) {
        List<Double> methodSeries = methodSeries(method);
        long totalTime = 0;
        for (Double time : methodSeries) {
            totalTime += time;
        }
        return getValue(totalTime);
    }

    /**
     * Calculate the total net time for each method<br>
     * totalNetMethodTime()<br>
     * Snapshot {1, 2, 4, 3, 2 } = 1 + 2 + 4 + 3 + 2 = 12
     *
     * @param method the method to get the total net time for
     * @return the total net for the method
     */
    public static double totalNetMethodTime(Method<?, ?> method) {
        List<Double> methodNetSeries = methodNetSeries(method);
        long totalNetTime = 0;
        for (Double netTime : methodNetSeries) {
            totalNetTime += netTime;
        }
        return getValue(totalNetTime);
    }

    /**
     * Calculate the series for the change in total time for the method<br>
     * methodChangeSeries()<br>
     * Snapshots {2, 5, 3, 6, 5} = {3, -2, 3, -1}
     *
     * @param method the method to get change in time for
     * @return the list of changes in timings for the method
     */
    public static List<Double> methodChangeSeries(Method<?, ?> method) {
        List<Double> series = new ArrayList<Double>();
        List<Double> methodSeries = methodSeries(method);
        double previousTime = 0;
        for (Double time : methodSeries) {
            double change = getValue(time - previousTime);
            series.add(change);
            previousTime = time;
        }
        return series;
    }

    /**
     * Calculate the series for the change in net time for the method<br>
     * methodNetChangeSeries()<br>
     * Snapshot {1, 2, 4, 3, 2} = {1, 2, -1, -1}
     *
     * @param method bla...
     * @return bla...
     */
    public static List<Double> methodNetChangeSeries(Method<?, ?> method) {
        List<Double> series = new ArrayList<Double>();
        List<Double> methodNetSeries = methodNetSeries(method);
        double previousTime = 0;
        for (Double time : methodNetSeries) {
            double change = getValue(time - previousTime);
            series.add(change);
            previousTime = time;
        }
        return series;
    }

    /**
     * Calculate the average total time for each method<br>
     * averageMethodTime()<br>
     * Snapshot {2, 5, 3, 6, 5} = (2 + 5 + 3 + 6 + 5)/5 = 21/5 = 4.2
     *
     * @param method bla...
     * @return bla...
     */
    public static double averageMethodTime(Method<?, ?> method) {
        List<Double> methodSeries = methodSeries(method);
        double totalTime = 0;
        for (Double time : methodSeries) {
            totalTime += getValue(time);
        }
        long denominator = methodSeries.size() > 0 ? methodSeries.size() : 1;
        return totalTime / denominator;
    }

    /**
     * Calculate the average net time for each method<br>
     * averageMethodNetTime()<br>
     * Snapshot {1, 2, 4, 3, 2} = (1 + 2 + 4 + 3 + 2)/5 = 12/5 = 2.4
     *
     * @param method bla...
     * @return bla...
     */
    public static double averageMethodNetTime(Method<?, ?> method) {
        List<Double> methodNetSeries = methodNetSeries(method);
        double totalTime = 0;
        for (Double time : methodNetSeries) {
            totalTime += getValue(time);
        }
        long denominator = methodNetSeries.size() > 0 ? methodNetSeries.size() : 1;
        return totalTime / denominator;
    }

    /**
     * Calculate the total change for the methods<br>
     * methodChange()<br>
     * Snapshots {2, 5, 3, 6, 5} = 3
     *
     * @param method bla...
     * @return bla...
     */
    public static double methodChange(Method<?, ?> method) {
        List<Double> methodSeries = methodSeries(method);
        double totalChange = 0;
        double previousTime = 0;
        for (Double time : methodSeries) {
            double change = getValue(time - previousTime);
            totalChange += change;
            previousTime = time;
        }
        return totalChange;
    }

    /**
     * Calculate the net change for the methods<br>
     * methodNetChange()<br>
     * Snapshot {1, 2, 4, 3, 2} = 1
     *
     * @param method bla...
     * @return bla...
     */
    public static double methodNetChange(Method<?, ?> method) {
        List<Double> methodNetSeries = methodNetSeries(method);
        double totalChange = 0;
        double previousTime = 0;
        for (Double time : methodNetSeries) {
            double change = getValue(time - previousTime);
            totalChange += change;
            previousTime = time;
        }
        return totalChange;
    }

}