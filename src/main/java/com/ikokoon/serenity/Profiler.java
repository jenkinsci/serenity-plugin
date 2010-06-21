package com.ikokoon.serenity;

import java.util.List;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Snapshot;

/**
 * This class generates the reports for the profiled classes.
 *
 * <pre>
 * Model:
 * Class class
 *     List<Snapshot> snapshots
 *         long net
 *         long total
 *         long wait
 *         Date start
 *         Date end
 *     List<Method> methods
 *         List<Snapshot> snapshots
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
 * @since 12.06.10
 * @version 01.00
 */
public class Profiler {

	protected static Logger LOGGER = Logger.getLogger(Profiler.class);

	/**
	 * Calculate the series for the total times for the method<br>
	 * methodSeries()<br>
	 * Snapshots {2, 5, 3, 6, 5}
	 */
	public static List<Long> methodSeries(Method<?, ?> method) {
		if (method.getSeries().size() == 0) {
			List<Snapshot<?, ?>> snapshots = method.getSnapshots();
			for (Snapshot<?, ?> snapshot : snapshots) {
				method.getSeries().add(snapshot.getTotal());
			}
		}
		return method.getSeries();
	}

	/**
	 * Calculate the series for the net times for the method<br>
	 * methodNetSeries()<br>
	 * Snapshot {1, 2, 4, 3, 2}
	 */
	public static List<Long> methodNetSeries(Method<?, ?> method) {
		if (method.getNetSeries().size() == 0) {
			List<Snapshot<?, ?>> snapshots = method.getSnapshots();
			for (Snapshot<?, ?> snapshot : snapshots) {
				long netTime = snapshot.getTotal() - snapshot.getWait();
				snapshot.setNet(netTime);
				method.getNetSeries().add(snapshot.getNet());
			}
		}
		return method.getNetSeries();
	}

	/**
	 * Calculate the total time for each method:<br>
	 * totalMethodTime()<br>
	 * Snapshots {2, 5, 3, 6, 5} = 2 + 5 + 3 + 6 + 5 = 21
	 */
	public static long totalMethodTime(Method<?, ?> method) {
		List<Long> methodSeries = methodSeries(method);
		long totalTime = 0;
		for (Long time : methodSeries) {
			totalTime += time;
		}
		return totalTime;
	}

	/**
	 * Calculate the total net time for each method<br>
	 * totalNetMethodTime()<br>
	 * Snapshot {1, 2, 4, 3, 2 } = 1 + 2 + 4 + 3 + 2 = 12
	 */
	public static long totalNetMethodTime(Method<?, ?> method) {
		List<Long> methodNetSeries = methodNetSeries(method);
		long totalNetTime = 0;
		for (Long netTime : methodNetSeries) {
			totalNetTime += netTime;
		}
		return totalNetTime;
	}

	/**
	 * Calculate the series for the change in total time for the method<br>
	 * methodChangeSeries()<br>
	 * Snapshots {2, 5, 3, 6, 5} = {3, -2, 3, -1}
	 */
	public static List<Long> methodChangeSeries(Method<?, ?> method) {
		if (method.getSeriesChange().size() == 0) {
			List<Long> methodSeries = methodSeries(method);
			long previousTime = 0;
			for (Long time : methodSeries) {
				long change = time - previousTime;
				method.getSeriesChange().add(change);
				previousTime = time;
			}
		}
		return method.getSeriesChange();
	}

	/**
	 * Calculate the series for the change in net time for the method<br>
	 * methodNetChangeSeries()<br>
	 * Snapshot {1, 2, 4, 3, 2} = {1, 2, -1, -1}
	 */
	public static List<Long> methodNetChangeSeries(Method<?, ?> method) {
		if (method.getSeriesChangeNet().size() == 0) {
			List<Long> methodNetSeries = methodNetSeries(method);
			long previousTime = 0;
			for (Long time : methodNetSeries) {
				long change = time - previousTime;
				method.getSeriesChangeNet().add(change);
				previousTime = time;
			}
		}
		return method.getSeriesChangeNet();
	}

	/**
	 * Calculate the average total time for each method<br>
	 * averageMethodTime()<br>
	 * Snapshot {2, 5, 3, 6, 5} = (2 + 5 + 3 + 6 + 5)/5 = 21/5 = 4.2
	 */
	public static long averageMethodTime(Method<?, ?> method) {
		List<Long> methodSeries = methodSeries(method);
		long totalTime = 0;
		for (Long time : methodSeries) {
			totalTime += time;
		}
		long denominator = methodSeries.size() > 0 ? methodSeries.size() : 1;
		return totalTime / denominator;
	}

	/**
	 * Calculate the average net time for each method<br>
	 * averageMethodNetTime()<br>
	 * Snapshot {1, 2, 4, 3, 2} = (1 + 2 + 4 + 3 + 2)/5 = 12/5 = 2.4
	 */
	public static long averageMethodNetTime(Method<?, ?> method) {
		List<Long> methodNetSeries = methodNetSeries(method);
		long totalTime = 0;
		for (Long time : methodNetSeries) {
			totalTime += time;
		}
		long denominator = methodNetSeries.size() > 0 ? methodNetSeries.size() : 1;
		return totalTime / denominator;
	}

	/**
	 * Calculate the total change for the methods<br>
	 * methodChange()<br>
	 * Snapshots {2, 5, 3, 6, 5} = 3
	 */
	public static long methodChange(Method<?, ?> method) {
		List<Long> methodSeries = methodSeries(method);
		long totalChange = 0;
		long previousTime = 0;
		for (Long time : methodSeries) {
			long change = time - previousTime;
			totalChange += change;
			previousTime = time;
		}
		return totalChange;
	}

	/**
	 * Calculate the net change for the methods<br>
	 * methodNetChange()<br>
	 * Snapshot {1, 2, 4, 3, 2} = 1
	 */
	public static long methodNetChange(Method<?, ?> method) {
		List<Long> methodNetSeries = methodNetSeries(method);
		long totalChange = 0;
		long previousTime = 0;
		for (Long time : methodNetSeries) {
			long change = time - previousTime;
			totalChange += change;
			previousTime = time;
		}
		return totalChange;
	}

}