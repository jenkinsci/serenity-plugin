package com.ikokoon.serenity.process;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.toolkit.Toolkit;

/**
 * Tests the consolidation of the classes. In the case of an inner class the outer class does not get the collected coverage information and the
 * database includes the anonymous classes as well. This is confusing to the user as the source for the inner classes is not available and the
 * statistics for the project are affected by the weighting of the classes and their respective sizes. The consolidator adds the inner class data to
 * the outer classes and removes the inner classes from the database.
 * 
 * @author Michael Couck
 * @since 03.01.10
 * @version 01.00
 */
public class ConsolidatorTest extends ATest implements IConstants {

	@Test
	@SuppressWarnings("unchecked")
	public void execute() {
		String innerClassName = className + "$InnterClass";
		final int innerClassLineNumber = (int) lineNumber + 1000;
		final String methodName = "init";

		Collector.collectCoverage(className, methodName, methodSignature, (int) lineNumber);
		Collector.collectCoverage(innerClassName, methodName + "InnerClassMethod", methodSignature + "InnerClassSignature", innerClassLineNumber);

		new Consolidator(null, dataBase).execute();

		Class<?, ?> klass = dataBase.find(Class.class, Toolkit.hash(innerClassName));
		assertNull(klass);

		DataBaseToolkit.dump(dataBase, null, "Consolidator test dump : ");

		Line<?, ?> line = dataBase.find(Line.class, new ArrayList() {
			{
				Collections.addAll(this, className, methodName, (double) innerClassLineNumber);
			}
		});
		assertNotNull(line);
	}
}
