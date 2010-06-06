package com.ikokoon.serenity;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Snapshot;
import com.ikokoon.serenity.persistence.IDataBase;

public class Profiler {

	private static Logger LOGGER = Logger.getLogger(Profiler.class);

	public static void initialize(final IDataBase dataBase) {
		long snapshptInterval = Configuration.getConfiguration().getSnapshotInterval();
		LOGGER.warn("Profiler initialize : " + dataBase + ", " + snapshptInterval);
		if (snapshptInterval > 0) {
			Timer timer = new Timer();
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					LOGGER.warn("Taking snapshot at : " + new Date());
					takeSnapshot(dataBase);
				}
			};
			timer.schedule(timerTask, snapshptInterval, snapshptInterval);
		}
	}

	@SuppressWarnings("unchecked")
	private static void takeSnapshot(IDataBase dataBase) {
		long time = System.currentTimeMillis();
		List<Class> classes = dataBase.find(Class.class);
		for (Class klass : classes) {
			List<Method> methods = klass.getChildren();
			long netClassTime = 0;
			long totalClassTime = 0;
			for (Method method : methods) {
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
					netClassTime += method.getNetTime();
					totalClassTime += method.getTotalTime();
				}
				Snapshot snapshot = new Snapshot();
				snapshot.setStart(new Date(time));
				snapshots.add(snapshot);
			}
			List<Snapshot> snapshots = klass.getSnapshots();
			int size = snapshots.size();
			if (size > 0) {
				// Finalise the last snapshot
				Snapshot snapshot = snapshots.get(size - 1);
				snapshot.setEnd(new Date(time));
				snapshot.setNet(netClassTime);
				snapshot.setTotal(totalClassTime);
			}
			Snapshot snapshot = new Snapshot();
			snapshot.setStart(new Date(time));
			snapshots.add(snapshot);
		}
	}

}