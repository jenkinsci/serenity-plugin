package com.ikokoon.serenity.process;

import java.util.Collection;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.aggregator.ProfilerAggregator;

public class ProfilerAggregatorTest extends ATest implements IConstants {

	private IDataBase dataBase;

	@Before
	public void before() {
		String dataBaseFile = "./src/test/resources/isearch/serenity.odb";
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, mockInternalDataBase);
	}

	@Test
	public void aggregate() {
		ProfilerAggregator aggregator = new ProfilerAggregator(dataBase);
		aggregator.aggregate();

		logger.warn("");
		logger.warn("");
		logger.warn("**** Printing the net class times ****");
		printData(aggregator.getNetClassTimes());

		logger.warn("");
		logger.warn("");
		logger.warn("**** Printing the net method times ****");
		printData(aggregator.getNetMethodTimes());

		logger.warn("");
		logger.warn("");
		logger.warn("**** Printing the class deltas ****");
		printData(aggregator.getPerformanceClassDeltas());

		logger.warn("");
		logger.warn("");
		logger.warn("**** Printing the method deltas ****");
		printData(aggregator.getPerformanceMethodDeltas());

		logger.warn("");
		logger.warn("");
		logger.warn("**** Printing the class times ****");
		printData(aggregator.getTotalClassTimes());

		logger.warn("");
		logger.warn("");
		logger.warn("**** Printing the method times ****");
		printData(aggregator.getTotalMethodTimes());

		logger.warn("");
		logger.warn("");
		logger.warn("**** Printing the sorted classes ****");
		printData(aggregator.getSortedClasses());

		logger.warn("");
		logger.warn("");
		logger.warn("**** Printing the sorted methods ****");
		printData(aggregator.getSortedMethods());
	}

	@SuppressWarnings("unchecked")
	private void printData(Collection<? extends Composite> data) {
		for (Composite composite : data) {
			logger.warn(composite.getParent() + " : " + composite);
		}
	}

	private void printData(Map<?, ?> data) {
		for (Object object : data.keySet()) {
			logger.warn(object + " : " + data.get(object));
		}
	}

}
