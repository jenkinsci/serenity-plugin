package com.ikokoon.instrumentation;

import org.junit.Test;

import com.ikokoon.ATest;
import com.ikokoon.IConstants;

/**
 * This is the test for the accumulator the looks through all the classes on the classpath that were not loaded at runtime and does the dependency,
 * coverage and so on for them.
 * 
 * @author Michael Couck
 * @since 24.07.09
 * @version 01.00
 */
public class AccumulatorTest extends ATest implements IConstants {

	@Test
	public void accumulate() {
		// TODO - how can we test this method? A functional test then?
		logger.warn("Implement me please!");
	}
}
