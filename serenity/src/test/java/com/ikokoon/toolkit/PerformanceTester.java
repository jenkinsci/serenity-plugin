package com.ikokoon.toolkit;

import com.ikokoon.serenity.ATest;

public class PerformanceTester extends ATest {

	public interface IPerform {
		public void execute();
	}

	public static double execute(IPerform perform, String type, double iterations) {
		double start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			perform.execute();
		}
		double end = System.currentTimeMillis();
		double duration = (end - start) / 1000d;
		double executionsPerSecond = (iterations / duration);
		logger.info("Duration : " + duration + ", " + type + " per second : " + executionsPerSecond);
		return executionsPerSecond;
	}

}
