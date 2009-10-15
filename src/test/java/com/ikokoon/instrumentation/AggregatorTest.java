package com.ikokoon.instrumentation;

import org.junit.Test;

import com.ikokoon.ATest;
import com.ikokoon.IConstants;
import com.ikokoon.instrumentation.process.Aggregator;
import com.ikokoon.target.Target;

/**
 * This is the test for the aggregator. The aggregator takes the collected data on the methods, classes and packages and calculates the metrics like
 * the abstractness the stability and so on.
 * 
 * @author Michael Couck
 * @since 02.08.09
 * @version 01.00
 */
public class AggregatorTest extends ATest implements IConstants {

	@Test
	public void execute() {
		Configuration.getConfiguration().includedPackages.add(Target.class.getPackage().getName());
		Aggregator aggregator = new Aggregator(null);
		aggregator.execute();
		// TODO - how to implement me? A functional test too?
		logger.warn("Implement me please!");
	}

}
