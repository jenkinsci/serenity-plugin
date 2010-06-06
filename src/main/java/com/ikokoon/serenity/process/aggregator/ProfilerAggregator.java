package com.ikokoon.serenity.process.aggregator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Snapshot;
import com.ikokoon.serenity.persistence.IDataBase;

public class ProfilerAggregator extends AAggregator {

	@SuppressWarnings("unchecked")
	private Comparator<Class> classComparator = new Comparator<Class>() {
		public int compare(Class o1, Class o2) {
			List<Snapshot> o1Snapshots = o1.getSnapshots();
			List<Snapshot> o2Snapshots = o2.getSnapshots();
			if (o1Snapshots.size() == 0 || o2Snapshots.size() == 0) {
				return 0;
			}
			Snapshot o1Snapshot = o1Snapshots.get(o1Snapshots.size() - 1);
			Snapshot o2Snapshot = o2Snapshots.get(o2Snapshots.size() - 1);
			Long o1Total = o1Snapshot.getTotal();
			Long o2Total = o2Snapshot.getTotal();
			return o2Total.compareTo(o1Total);
		}
	};

	@SuppressWarnings("unchecked")
	private Comparator<Method> methodComparator = new Comparator<Method>() {
		public int compare(Method o1, Method o2) {
			List<Snapshot> o1Snapshots = o1.getSnapshots();
			List<Snapshot> o2Snapshots = o2.getSnapshots();
			if (o1Snapshots.size() == 0 || o2Snapshots.size() == 0) {
				return 0;
			}
			Snapshot o1Snapshot = o1Snapshots.get(o1Snapshots.size() - 1);
			Snapshot o2Snapshot = o2Snapshots.get(o2Snapshots.size() - 1);
			Long o1Total = o1Snapshot.getTotal();
			Long o2Total = o2Snapshot.getTotal();
			return o2Total.compareTo(o1Total);
		}
	};

	private Map<Class<?, ?>, List<Long>> netClassTimes = new HashMap<Class<?, ?>, List<Long>>();
	private Map<Class<?, ?>, List<Long>> totalClassTimes = new HashMap<Class<?, ?>, List<Long>>();
	private Map<Class<?, ?>, List<Long>> performanceClassDeltas = new HashMap<Class<?, ?>, List<Long>>();
	private Map<Method<?, ?>, List<Long>> netMethodTimes = new HashMap<Method<?, ?>, List<Long>>();
	private Map<Method<?, ?>, List<Long>> totalMethodTimes = new HashMap<Method<?, ?>, List<Long>>();
	private Map<Method<?, ?>, List<Long>> performanceMethodDeltas = new HashMap<Method<?, ?>, List<Long>>();
	@SuppressWarnings("unchecked")
	private List<Class> sortedClasses = new ArrayList<Class>();
	@SuppressWarnings("unchecked")
	private List<Method> sortedMethods = new ArrayList<Method>();

	public ProfilerAggregator(IDataBase dataBase) {
		super(dataBase);
	}

	public void aggregate() {
		aggregate(dataBase);
	}

	@SuppressWarnings("unchecked")
	private void aggregate(IDataBase dataBase) {
		// 1) Top most expensive for each class and method time taken
		// 2) A time series for each class. We end up with something like: {Adapter, {2, 5, 8, 7, 5, 9, 6, 8, 8}} for both the classes and the methods
		// 3) A series of changes between the performance for snapshots of classes and methods
		List<Class> classes = dataBase.find(Class.class);
		sortedClasses.addAll(classes);
		for (Class klass : classes) {
			List<Snapshot> snapshots = klass.getSnapshots();
			long previousClassTotalTime = 0;
			for (Snapshot snapshot : snapshots) {
				List<Long> netTimeSeries = netClassTimes.get(klass);
				if (netTimeSeries == null) {
					netTimeSeries = new ArrayList<Long>();
					netClassTimes.put(klass, netTimeSeries);
				}
				netTimeSeries.add(snapshot.getNet());
				List<Long> totalTimeSeries = totalClassTimes.get(klass);
				if (totalTimeSeries == null) {
					totalTimeSeries = new ArrayList<Long>();
					totalClassTimes.put(klass, totalTimeSeries);
				}
				totalTimeSeries.add(snapshot.getTotal());
				List<Long> performanceClassDeltaSeries = performanceClassDeltas.get(klass);
				if (performanceClassDeltaSeries == null) {
					performanceClassDeltaSeries = new ArrayList<Long>();
					performanceClassDeltas.put(klass, performanceClassDeltaSeries);
				}
				long deltaTotalTime = snapshot.getTotal() - previousClassTotalTime;
				performanceClassDeltaSeries.add(deltaTotalTime);
				previousClassTotalTime = snapshot.getTotal();
			}
			// A time series for each method.
			List<Method> methods = klass.getChildren();
			sortedMethods.addAll(methods);
			for (Method method : methods) {
				snapshots = method.getSnapshots();
				long previousMethodTotalTime = 0;
				for (Snapshot snapshot : snapshots) {
					List<Long> netTimeSeries = netMethodTimes.get(method);
					if (netTimeSeries == null) {
						netTimeSeries = new ArrayList<Long>();
						netMethodTimes.put(method, netTimeSeries);
					}
					netTimeSeries.add(snapshot.getNet());
					List<Long> totalTimeSeries = totalMethodTimes.get(method);
					if (totalTimeSeries == null) {
						totalTimeSeries = new ArrayList<Long>();
						totalMethodTimes.put(method, totalTimeSeries);
					}
					totalTimeSeries.add(snapshot.getTotal());
					List<Long> performanceMethodDeltaSeries = performanceMethodDeltas.get(method);
					if (performanceMethodDeltaSeries == null) {
						performanceMethodDeltaSeries = new ArrayList<Long>();
						performanceMethodDeltas.put(method, performanceMethodDeltaSeries);
					}
					long deltaTotalTime = snapshot.getTotal() - previousMethodTotalTime;
					performanceMethodDeltaSeries.add(deltaTotalTime);
					previousMethodTotalTime = snapshot.getTotal();
				}
			}
		}
		Collections.sort(sortedClasses, classComparator);
		Collections.sort(sortedMethods, methodComparator);
	}

	public Map<Class<?, ?>, List<Long>> getNetClassTimes() {
		return netClassTimes;
	}

	public Map<Class<?, ?>, List<Long>> getTotalClassTimes() {
		return totalClassTimes;
	}

	public Map<Class<?, ?>, List<Long>> getPerformanceClassDeltas() {
		return performanceClassDeltas;
	}

	public Map<Method<?, ?>, List<Long>> getNetMethodTimes() {
		return netMethodTimes;
	}

	public Map<Method<?, ?>, List<Long>> getTotalMethodTimes() {
		return totalMethodTimes;
	}

	public Map<Method<?, ?>, List<Long>> getPerformanceMethodDeltas() {
		return performanceMethodDeltas;
	}

	@SuppressWarnings("unchecked")
	public Collection<Class> getSortedClasses() {
		return sortedClasses;
	}

	@SuppressWarnings("unchecked")
	public Collection<Method> getSortedMethods() {
		return sortedMethods;
	}

}