package com.ikokoon.serenity.process;

import java.util.Date;
import java.util.List;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Snapshot;
import com.ikokoon.serenity.persistence.IDataBase;

/**
 * @author Michael Couck
 * @since 27.06.10
 * @version 01.00
 */
public class Snapshooter extends AProcess {

	private IDataBase dataBase;

	public Snapshooter(IProcess parent, IDataBase dataBase) {
		super(parent);
		this.dataBase = dataBase;
	}

	public void execute() {
		takeSnapshot(dataBase);
		super.execute();
	}

	@SuppressWarnings("unchecked")
	private static void takeSnapshot(IDataBase dataBase) {
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
