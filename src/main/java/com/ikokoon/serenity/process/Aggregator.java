package com.ikokoon.serenity.process;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.aggregator.IAggregator;
import com.ikokoon.serenity.process.aggregator.ProjectAggregator;

/**
 * This class aggregates all the totals for the report. For each method there is a total for the number of lines that the method has and a for each
 * line that is executed there is a line element. So the percentage of lines that were executed is covered lines / total lines * 100. The complexity
 * total is added up for each method along with the coverage total for each method and divided by the total methods in the class to get the class
 * average for coverage and complexity.
 *
 * Similarly for the package totals of complexity and coverage the class totals for the package are added up and divided by the number of classes in
 * the package.
 *
 * Metrics are also gathered for the dependency, afferent and efferent and the abstractness and stability calculated from that.
 *
 * @author Michael Couck
 * @since 18.07.09
 * @version 01.00
 */
public class Aggregator extends AProcess implements IConstants {

	/** The database to aggregate the data for. */
	private IDataBase dataBase;

	/**
	 * Constructor takes the parent process.
	 *
	 * @param parent
	 *            the parent process that will call this child. The child process, i.e. this instance, will add it's self to the parent
	 */
	public Aggregator(IProcess parent, IDataBase dataBase) {
		super(parent);
		logger.warn("Aggregator");
		this.dataBase = dataBase;
	}

	/**
	 * Please refer to the class JavaDoc for more information.
	 */
	public void execute() {
		super.execute();
		logger.info("Running Aggregator: ");
		IAggregator aggregator = new ProjectAggregator(dataBase);
		aggregator.aggregate();
	}

}