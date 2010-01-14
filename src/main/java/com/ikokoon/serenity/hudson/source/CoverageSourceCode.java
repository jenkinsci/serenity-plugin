package com.ikokoon.serenity.hudson.source;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.model.Class;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;

/**
 * This class takes the source for a Java file and generates HTML from the source that can be displayed in a browser.
 * 
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
public class CoverageSourceCode implements ISourceCode {

	private Logger logger = Logger.getLogger(CoverageSourceCode.class);
	/** The class from the model that has the source code in it. */
	private Class<?, ?> klass;
	private String source;
	private JavaSourceParser javaSourceParser;

	class JavaSourceParserExt extends JavaSourceParser {
		public JavaSourceParserExt() {
			super(JavaSourceConversionOptions.getRawDefault());
		}
	}

	private JavaSourceConversionOptions options = JavaSourceConversionOptions.getRawDefault();
	{
		options.setAddLineAnchors(true);
		options.setShowLineNumbers(true);
	}

	/**
	 * Constructor takes the class that contains the source for the conversion.
	 * 
	 * @param klass
	 *            the class that contains the source
	 */
	public CoverageSourceCode(Class<?, ?> klass, String source) {
		this.klass = klass;
		this.source = source;
		try {
			javaSourceParser = new JavaSourceParserExt();
		} catch (Exception e) {
			logger.info("Exception initialising the Java source parser", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSource() {
		if (this.klass != null && this.source != null) {
			try {
				// Convert the Java source to HTML
				InputStream inputStream = new ByteArrayInputStream(source.getBytes());
				JavaSource javaSource = javaSourceParser.parse(new InputStreamReader(inputStream));
				JavaSource2HTMLConverter converter = new JavaSource2HTMLConverterExt(klass);
				StringWriter writer = new StringWriter();
				converter.convert(javaSource, options, writer);
				String html = writer.toString();
				return html;
			} catch (Exception e) {
				logger.error("Exception generating the HTML for the class source : " + klass, e);
			}
		}
		return "No source";
	}

}