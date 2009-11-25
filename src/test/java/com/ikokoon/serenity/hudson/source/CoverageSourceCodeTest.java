package com.ikokoon.serenity.hudson.source;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.PerformanceTester;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.process.AccumulatorTest;
import com.ikokoon.toolkit.Toolkit;

public class CoverageSourceCodeTest extends AccumulatorTest {

	@Test
	public void accumulate() {
		// Do nothing
	}

	@Test
	public void highlightSource() throws Exception {
		super.accumulate();
		Class<?, ?> klass = (Class<?, ?>) dataBase.find(Toolkit.hash(Configuration.class.getName()));
		setCovered(klass);
		CoverageSourceCode coverageSourceCode = new CoverageSourceCode(klass);
		String input = "<code><a name=\"29\"><font color=\"#808080\">029</font>&nbsp;</a><font color=\"#ffffff\">&nbsp;&nbsp;</font>"
				+ "<font color=\"#7f0055\"><b>public&nbsp;</b></font><font color=\"#000000\">Logger&nbsp;logger&nbsp;=&nbsp;Logger.getLogger</font>"
				+ "<font color=\"#000000\">(</font><font color=\"#7f0055\"><b>this</b></font><font color=\"#000000\">.getClass</font><font color=\"#000000\">())</font>"
				+ "<font color=\"#000000\">;</font><br /></code>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<code><a name=\"29\"><font color=\"#808080\">029</font>&#160;</a><font color=\"#ffffff\">&#160;&#160;</font>"
				+ "<span style=\"background-color: #C7D6D7;\"><font color=\"#7f0055\"><b>public&#160;</b></font><font color=\"#000000\">Logger&#160;logger&#160;=&#160;Logger.getLogger</font>"
				+ "<font color=\"#000000\">(</font><font color=\"#7f0055\"><b>this</b></font><font color=\"#000000\">.getClass</font><font color=\"#000000\">())</font>"
				+ "<font color=\"#000000\">;</font><br/></span></code>";
		String actual = coverageSourceCode.highlightSource(input);

		Document expectedDocument = getDocument(expected);
		Document actualDocument = getDocument(actual);

		assertTrue(compare(expectedDocument.getRootElement(), actualDocument.getRootElement()));
	}

	private Document getDocument(String string) throws Exception {
		SAXReader reader = new SAXReader(false);
		reader.setStripWhitespaceText(true);
		InputStream inputStream = new ByteArrayInputStream(string.getBytes());
		Document document = reader.read(inputStream);
		return document;
	}

	@SuppressWarnings("unchecked")
	private boolean compare(Element expected, Element actual) {
		if (!expected.getName().equals(actual.getName())) {
			logger.info("Names not equal : " + expected.getName() + ", " + actual.getName());
			return false;
		}
		List<Attribute> expectedAttributes = expected.attributes();
		List<Attribute> actualAttributes = actual.attributes();
		if (expectedAttributes.size() != actualAttributes.size()) {
			logger.info("Attributes not equal");
			return false;
		}
		for (Attribute expectedAttribute : expectedAttributes) {
			Object expectedAttributeValue = expectedAttribute.getValue();
			Object actualAttributeValue = actual.attributeValue(expectedAttribute.getName());
			if (!expectedAttributeValue.equals(actualAttributeValue)) {
				logger.info("Attribute values not equal");
				return false;
			}
		}

		List<Element> expectedChildren = expected.elements();
		List<Element> actualChildren = actual.elements();
		if (expectedChildren.size() != actualChildren.size()) {
			logger.info("Children not equal");
			return false;
		}

		for (int i = 0; i < expectedChildren.size(); i++) {
			boolean equal = compare(expectedChildren.get(i), actualChildren.get(i));
			if (!equal) {
				return false;
			}
		}

		return true;
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

	@Test
	public void getSource() {
		super.accumulate();
		Class<?, ?> klass = (Class<?, ?>) dataBase.find(Toolkit.hash(Configuration.class.getName()));
		final CoverageSourceCode coverageSourceCode = new CoverageSourceCode(klass);
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.IPerform() {
			public void execute() {
				coverageSourceCode.getSource();
			}
		}, "highlight source", 10);
		assertTrue(executionsPerSecond > 10);
	}

}
