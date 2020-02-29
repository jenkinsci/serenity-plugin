package com.ikokoon.serenity.process.aggregator;

import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.persistence.IDataBase;

import java.util.logging.Level;

/**
 * @author Michael Couck
 * @since 07.03.10
 * @version 01.00
 */
public class MethodAggregator extends AAggregator {

	private Method<?, ?> method;

	public MethodAggregator(final IDataBase dataBase, final Method<?, ?> method) {
		super(dataBase);
		this.method = method;
	}

	public void aggregate() {
		aggregate(method);
		setPrecision(method);
		dataBase.persist(method);
	}

	protected void aggregate(final Method<?, ?> method) {
		try {
			double executed = 0d;
			// Collect all the lines that were executed
			for (Line<?, ?> line : method.getChildren()) {
				if (line.getCounter() > 0) {
					executed++;
				}
			}
			if (method.getChildren().size() > 0) {
				double coverage = getCoverage(method.getChildren().size(), executed);
				method.setCoverage(coverage);
			}
		} catch (final Exception e) {
			logger.log(Level.SEVERE, "Exception processing the method element : " + method.getName(), e);
		}
	}

}
