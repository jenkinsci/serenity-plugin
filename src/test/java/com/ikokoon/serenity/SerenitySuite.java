/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ikokoon.serenity;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.ikokoon.serenity.hudson.modeller.ModellerTest;
import com.ikokoon.serenity.hudson.source.CoverageSourceCodeTest;
import com.ikokoon.serenity.instrumentation.InstrumentationTest;
import com.ikokoon.serenity.instrumentation.VisitorFactoryTest;
import com.ikokoon.serenity.instrumentation.complexity.ComplexityTest;
import com.ikokoon.serenity.instrumentation.coverage.CoverageTest;
import com.ikokoon.serenity.instrumentation.dependency.DependencyTest;
import com.ikokoon.serenity.persistence.DataBaseJpaTest;
import com.ikokoon.serenity.persistence.DataBaseManagerTest;
import com.ikokoon.serenity.persistence.DataBaseOdbTest;
import com.ikokoon.serenity.persistence.DataBaseRamTest;
import com.ikokoon.serenity.persistence.DataBaseToolkitTest;
import com.ikokoon.serenity.process.AccumulatorTest;
import com.ikokoon.serenity.process.AggregatorTest;
import com.ikokoon.serenity.process.ListenerTest;
import com.ikokoon.serenity.process.ReporterTest;
import com.ikokoon.toolkit.ObjectFactoryTest;
import com.ikokoon.toolkit.PermutationsTest;
import com.ikokoon.toolkit.ToolkitTest;
import com.ikokoon.toolkit.TypeTest;

/**
 * @author Michael Couck
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( {
		// Adapter functionality tests
		ComplexityTest.class, // Tests the complexity functionality
		CoverageTest.class, // Tests that the coverage instructions have been added
		DependencyTest.class, // Tests that the dependency adapters are collecting the dependency data correctly
		InstrumentationTest.class,

		// Processors tests
		AccumulatorTest.class, // Accumulates all the classes on the classpath
		AggregatorTest.class, // Aggregates the data when finished

		// Top level tests, functional rather than unit
		CollectorTest.class, // Tests that the collector is collecting the data
		TransformerTest.class, // Tests that the transformer is transforming the classes
		ListenerTest.class, // For the listener that listens to dump the reports
		ProfilerTest.class, // Tests the profiler in a couple of ways
		ReporterTest.class, // Tests the reporting functionality

		// The factory tests
		ObjectFactoryTest.class, // Instantiates objects based on the class and parameters
		VisitorFactoryTest.class, // Instantiates the visitors

		// Toolkit and assorted common functionality tests
		TypeTest.class, ToolkitTest.class, ModellerTest.class, PermutationsTest.class, CoverageSourceCodeTest.class,
		InstrumentationTest.class,

		// Some of the many database/persistence tests
		DataBaseManagerTest.class, // The test for opening and closing the databases
		DataBaseToolkitTest.class, // Testing the clear and copy functions
		DataBaseRamTest.class, // The in memory database
		DataBaseOdbTest.class, // The Neodatis database
		DataBaseJpaTest.class // The JPA database(experimental)

})
public class SerenitySuite {
	// DataBaseDb4oTest.class
}
