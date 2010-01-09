package com.ikokoon.serenity.hudson.source;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.PerformanceTester;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;

public class CoverageSourceCodeTest extends ATest {

	private String source = "class Dummy {\n\tprivate String name;\n\n\tpublic Dummy() {\n\t}\n\tpublic String getName() {\n\t\treturn name;\n\t}\n}";

	@Test
	public void getSource() {
		Package<?, ?> pakkage = getPackage();
		dataBase.persist(pakkage);

		Class<?, ?> klass = (Class<?, ?>) dataBase.find(Class.class, pakkage.getChildren().get(0).getId());
		klass.setSource(source);
		setCovered(klass);
		final CoverageSourceCode coverageSourceCode = new CoverageSourceCode(klass);
		String html = coverageSourceCode.getSource();
		logger.info(html);

		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.IPerform() {
			public void execute() {
				coverageSourceCode.getSource();
			}
		}, "highlight source", 10);
		assertTrue(executionsPerSecond > 10);
	}

	private void setCovered(Class<?, ?> klass) {
		List<Method<?, ?>> methods = klass.getChildren();
		for (Method<?, ?> method : methods) {
			List<Line<?, ?>> lines = method.getChildren();
			for (Line<?, ?> line : lines) {
				line.setCounter(1.0);
			}
		}
	}

}
