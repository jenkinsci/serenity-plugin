package com.ikokoon.serenity.hudson.source;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;

import de.java2html.Version;
import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceIterator;
import de.java2html.javasource.JavaSourceRun;
import de.java2html.javasource.JavaSourceType;
import de.java2html.options.HorizontalAlignment;
import de.java2html.options.IHorizontalAlignmentVisitor;
import de.java2html.options.JavaSourceConversionOptions;
import de.java2html.options.JavaSourceStyleEntry;
import de.java2html.options.JavaSourceStyleTable;
import de.java2html.util.HtmlUtilities;
import de.java2html.util.StringHolder;

/**
 * This class just extends the Java2HTML converter and adds a background colour where the lines were executed. Unfortunately most of the interesting
 * methods were private so there is a lot of copy and paste, pity, se la vie.
 * 
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
public class JavaSource2HTMLConverterExt extends JavaSource2HTMLConverter {

	public static boolean java2HtmlHomepageLinkEnabled = false;
	private static final String HTML_BLOCK_HEADER = "\n\n<!-- ======================================================== -->\n"
			+ "<!-- = Java Sourcecode to HTML automatically converted code = -->\n<!-- =   " + Version.getJava2HtmlConverterTitle() + " "
			+ Version.getBuildDate() + " by Markus Gebhard  markus@jave.de   = -->\n"
			+ "<!-- =     Further information: http://www.java2html.de     = -->\n" + "<div align=\"{0}\" class=\"java\">\n"
			+ "<table border=\"{1}\" cellpadding=\"3\" " + "cellspacing=\"0\" bgcolor=\"{2}\">\n";

	private Class<?, ?> klass;
	private String STYLE = "style=\"background-color: #C7D6D7;\"";

	// private String blue = "#C7D6D7;";
	// private String yellow = "#FBF4B7";
	// private String pink = "#FBC6C6";
	// private String blue = "#C7D6D7";

	private int lineCifferCount;

	public JavaSource2HTMLConverterExt(Class<?, ?> klass) {
		super();
		this.klass = klass;
	}

	public void convert(JavaSource source, JavaSourceConversionOptions options, BufferedWriter writer) throws IOException {
		if (source == null) {
			throw new IllegalStateException("Trying to write out converted code without having source set.");
		}

		String alignValue = getHtmlAlignValue(options.getHorizontalAlignment());
		String bgcolorValue = options.getStyleTable().get(JavaSourceType.BACKGROUND).getHtmlColor();
		String borderValue = (options.isShowTableBorder()) ? "2" : "0";

		writer.write(MessageFormat.format(HTML_BLOCK_HEADER, new Object[] { alignValue, borderValue, bgcolorValue }));

		if ((options.isShowFileName()) && (source.getFileName() != null)) {
			writeFileName(source, writer);
		}

		writer.write("   <tr>");
		writer.newLine();

		writeSourceCode(source, options, writer);

		writer.write("   </tr>");
		writer.newLine();

		if ((options.isShowJava2HtmlLink()) || (java2HtmlHomepageLinkEnabled)) {
			writer
					.write("  <!-- start Java2Html link -->\n   <tr>\n    <td align=\"right\">\n<small>\n<a href=\"http://www.java2html.de\" target=\"_blank\">Java2html</a>\n</small>\n    </td>\n   </tr>\n  <!-- end Java2Html link -->\n");
		}
		writer
				.write("</table>\n</div>\n<!-- =       END of automatically generated HTML code       = -->\n<!-- ======================================================== -->\n\n");
	}

	private String getHtmlAlignValue(HorizontalAlignment alignment) {
		final StringHolder stringHolder = new StringHolder();
		alignment.accept(new IHorizontalAlignmentVisitor() {
			public void visitLeftAlignment(HorizontalAlignment horizontalAlignment) {
				stringHolder.setValue("left");
			}

			public void visitRightAlignment(HorizontalAlignment horizontalAlignment) {
				stringHolder.setValue("right");
			}

			public void visitCenterAlignment(HorizontalAlignment horizontalAlignment) {
				stringHolder.setValue("center");
			}
		});
		return stringHolder.getValue();
	}

	private void writeFileName(JavaSource source, BufferedWriter writer) throws IOException {
		writer.write("  <!-- start headline -->\n   <tr>\n    <td colspan=\"2\">\n     <center><font size=\"+2\">\n      <code><b>\n");
		writer.write(source.getFileName());
		writer.newLine();
		writer.write("      </b></code>\n     </font></center>\n    </td>\n   </tr>\n  <!-- end headline -->\n");
	}

	private void writeSourceCode(JavaSource source, JavaSourceConversionOptions options, BufferedWriter writer) throws IOException {
		writer.write("  <!-- start source code -->\n   <td nowrap=\"nowrap\" valign=\"top\" align=\"left\">\n    <code>\n");

		this.lineCifferCount = String.valueOf(source.getLineCount()).length();

		JavaSourceIterator iterator = source.getIterator();
		int lineNumber = 1;
		while (iterator.hasNext()) {
			JavaSourceRun run = iterator.getNext();
			if (run.isAtStartOfLine()) {
				if (options.isAddLineAnchors()) {
					writeLineAnchorStart(options, writer, lineNumber);
				}
				if (options.isShowLineNumbers()) {
					writeLineNumber(options, writer, lineNumber);
				}
				if (options.isAddLineAnchors()) {
					writeLineAnchorEnd(writer);
				}
				++lineNumber;
			}

			toHTML(options.getStyleTable(), run, writer, lineNumber);
			if ((run.isAtEndOfLine()) && (iterator.hasNext())) {
				writer.write("<br />");
				writer.newLine();
			}
		}
		writer.write("</code>\n    \n   </td>\n  <!-- end source code -->\n");
	}

	private void writeLineAnchorEnd(BufferedWriter writer) throws IOException {
		writer.write("</a>");
	}

	private void writeLineAnchorStart(JavaSourceConversionOptions options, BufferedWriter writer, int lineNumber) throws IOException {
		writer.write("<a name=\"");
		writer.write(options.getLineAnchorPrefix() + lineNumber);
		writer.write("\">");
	}

	private void writeLineNumber(JavaSourceConversionOptions options, BufferedWriter writer, int lineNo) throws IOException {
		JavaSourceStyleEntry styleEntry = options.getStyleTable().get(JavaSourceType.LINE_NUMBERS);
		writeStyleStart(writer, styleEntry, -1);

		String lineNumber = String.valueOf(lineNo);
		int cifferCount = this.lineCifferCount - lineNumber.length();
		while (cifferCount > 0) {
			writer.write(48);
			--cifferCount;
		}

		writer.write(lineNumber);
		writeStyleEnd(writer, styleEntry);
		writer.write("&nbsp;");
	}

	private void toHTML(JavaSourceStyleTable styleTable, JavaSourceRun run, BufferedWriter writer, int lineNumber) throws IOException {
		JavaSourceStyleEntry style = styleTable.get(run.getType());

		writeStyleStart(writer, style, lineNumber);

		String t = HtmlUtilities.encode(run.getCode(), "\n ");

		for (int i = 0; i < t.length(); ++i) {
			char ch = t.charAt(i);
			if (ch == ' ') {
				writer.write("&nbsp;");
			} else {
				writer.write(ch);
			}
		}

		writeStyleEnd(writer, style);
	}

	private void writeStyleStart(BufferedWriter writer, JavaSourceStyleEntry style, int lineNumber) throws IOException {
		if (getCovered(this.klass, lineNumber - 1)) {
			writer.write("<font color=\"" + style.getHtmlColor() + "\" " + STYLE + ">");
		} else {
			writer.write("<font color=\"" + style.getHtmlColor() + "\">");
		}
		if (style.isBold()) {
			writer.write("<b>");
		}
		if (style.isItalic()) {
			writer.write("<i>");
		}
	}

	private void writeStyleEnd(BufferedWriter writer, JavaSourceStyleEntry style) throws IOException {
		if (style.isItalic()) {
			writer.write("</i>");
		}
		if (style.isBold()) {
			writer.write("</b>");
		}
		writer.write("</font>");
	}

	private boolean getCovered(Class<?, ?> klass, double lineNumber) {
		List<Method<?, ?>> methods = klass.getChildren();
		for (Method<?, ?> method : methods) {
			List<Line<?, ?>> lines = method.getChildren();
			for (Line<?, ?> line : lines) {
				if (line.getNumber() == lineNumber) {
					return line.getCounter() > 0;
				}
			}
		}
		for (Class<?, ?> innerKlass : klass.getInnerClasses()) {
			boolean covered = getCovered(innerKlass, lineNumber);
			if (covered) {
				return true;
			}
		}
		return false;
	}
}
