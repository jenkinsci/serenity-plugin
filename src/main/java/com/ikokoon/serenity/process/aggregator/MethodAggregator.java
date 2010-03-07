package com.ikokoon.serenity.process.aggregator;

import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.persistence.IDataBase;

/**
 * @author Michael Couck
 * @since 07.03.10
 * @version 01.00
 */
public class MethodAggregator extends AAggregator {

	private Method<?, ?> method;

	public MethodAggregator(IDataBase dataBase, Method<?, ?> method) {
		super(dataBase);
		this.method = method;
	}

	public void aggregate() {
		aggregate(method);
		setPrecision(method);
		dataBase.persist(method);
	}

	protected void aggregate(Method<?, ?> method) {
		logger.debug("Processing method : " + method);
		try {
			double executed = 0d;
			// Collect all the lines that were executed
			for (Line<?, ?> line : method.getChildren()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Line covered : " + line + ", " + line.getCounter());
				}
				if (line.getCounter() > 0) {
					executed++;
				}
			}
			if (method.getChildren().size() > 0) {
				// (efference + afference) > 0 ? efference / (efference + afference) : 1;
				double coverage = getCoverage(method.getChildren().size(), executed);
				method.setCoverage(coverage);
			}
		} catch (Exception e) {
			logger.error("Exception peocessing the method element : " + method.getName(), e);
		}
	}

}
