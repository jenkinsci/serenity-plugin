package com.ikokoon.serenity.hudson.source;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.toolkit.Toolkit;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;

public class CoverageSourceCode implements ISourceCode {

	private Logger logger = Logger.getLogger(CoverageSourceCode.class);
	private Class<?, ?> klass;
	private String yellow = "#FBF4B7";
	private String pink = "#FBC6C6";
	private String blue = "#C7D6D7";

	public CoverageSourceCode(Class<?, ?> klass) {
		this.klass = klass;
	}

	public String getSource() {
		if (this.klass != null && this.klass.getSource() != null) {
			try {
				// Convert the Java source to HTML
				InputStream inputStream = new ByteArrayInputStream(klass.getSource().getBytes());
				JavaSource javaSource = new JavaSourceParser().parse(new InputStreamReader(inputStream));
				JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
				StringWriter writer = new StringWriter();
				JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
				options.setShowLineNumbers(true);
				options.setAddLineAnchors(true);
				converter.convert(javaSource, options, writer);

				// Highlight the source where the lines are covered
				String source = writer.toString();
				source = highlightSource(source);
				return source;
			} catch (Exception e) {
				logger.error("Exception generating the HTML for the class source", e);
			}
		}
		return "No source";
	}

	String highlightSource(String html) throws Exception {
		html = Toolkit.replaceOld(html, "&nbsp;", "#####");

		SAXReader reader = new SAXReader(false);
		reader.setStripWhitespaceText(true);
		InputStream inputStream = new ByteArrayInputStream(html.getBytes());
		Document document = reader.read(inputStream);
		Element element = document.getRootElement();
		highlightSource(element);

		html = document.asXML();
		html = Toolkit.replaceOld(html, "#####", "&#160;");

		return html;
	}

	@SuppressWarnings("unchecked")
	void highlightSource(Element element) {
		if (element.getName().equals(CODE)) {
			Element codeElement = element;
			List<Element> codeChildren = codeElement.elements();
			int SPACES = 0, INSERT = 1;
			for (int i = 0, state = SPACES; i < codeChildren.size(); i++) {
				Element codeChild = codeChildren.get(i);
				// See that we are in an 'a' tag, indicating a line of code
				if (codeChild.getName().equals(A) && codeChild.attributeValue(NAME) != null) {
					double lineNumber = Double.parseDouble(codeChild.attributeValue(NAME));
					if (getCovered(lineNumber)) {
						// Create a span tag and add all the siblings up to the next 'a' tag to the span tag
						Element spanElement = new DefaultElement(SPAN);
						// Add all the children up to the next 'a' tag to the span tag
						if (++i < codeChildren.size()) {
							do {
								codeChild = codeChildren.get(i);
								if (state == SPACES) { // Spaces before the first character in the code
									state++;
								} else if (state == INSERT) {
									state++;
									// spanElement.setParent(codeElement);
									spanElement.addAttribute(STYLE, STYLE_VALUE);
									codeChildren.add(i, spanElement);
								} else {
									codeElement.remove(codeChild);
									// codeChild.setParent(spanElement);
									spanElement.elements().add(codeChild);
								}
							} while (++i < codeChildren.size() && !codeChild.getName().equals(A));
						}
						state = SPACES;
					}
				}
			}
			return;
		}
		// We haven't got to the code element yet in the tree, try the children
		List<Element> children = element.elements();
		for (Element child : children) {
			highlightSource(child);
		}
	}

	private boolean getCovered(double lineNumber) {
		List<Method<?, ?>> methods = this.klass.getChildren();
		for (Method<?, ?> method : methods) {
			List<Line<?, ?>> lines = method.getChildren();
			for (Line<?, ?> line : lines) {
				if (line.getNumber() == lineNumber) {
					return line.getCounter() > 0;
				}
			}
		}
		return false;
	}

}
