package com.ikokoon.serenity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Snapshot;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class takes a database and produces reports based on the snapshots for each method.
 *
 * @author Michael Couck
 * @since 19.06.10
 * @version 01.00
 */
public class Reporter {

	private static Logger LOGGER = Logger.getLogger(Reporter.class);

	protected static String STYLE_SHEET = "profiler-report-style.css";
	protected static String METHOD_SERIES = "methodSeries.html";
	protected static String METHOD_NET_SERIES = "methodNetSeries.html";
	protected static String METHOD_CHANGE_SERIES = "methodChangeSeries.html";
	protected static String METHOD_NET_CHANGE_SERIES = "methodNetChangeSeries.html";

	protected static String STYLE_SHEET_FILE = IConstants.SERENITY_DIRECTORY + File.separatorChar + STYLE_SHEET;
	protected static String METHOD_SERIES_FILE = IConstants.SERENITY_DIRECTORY + File.separatorChar + METHOD_SERIES;
	protected static String METHOD_NET_SERIES_FILE = IConstants.SERENITY_DIRECTORY + File.separatorChar + METHOD_NET_SERIES;
	protected static String METHOD_CHANGE_SERIES_FILE = IConstants.SERENITY_DIRECTORY + File.separatorChar + METHOD_CHANGE_SERIES;
	protected static String METHOD_NET_CHANGE_SERIES_FILE = IConstants.SERENITY_DIRECTORY + File.separatorChar + METHOD_NET_CHANGE_SERIES;

	public static void report(IDataBase dataBase) {
		try {
			// Write the style sheet first
			InputStream inputStream = Reporter.class.getResourceAsStream(File.separatorChar + STYLE_SHEET);
			String styleSheetString = Toolkit.getContents(inputStream).toString();
			File file = new File(STYLE_SHEET_FILE);
			if (!file.exists()) {
				Toolkit.setContents(file, styleSheetString.getBytes());
			}
		} catch (Exception e) {
			LOGGER.error("Exception writing the stype sheet : ", e);
		}

		String html = Reporter.methodSeries(dataBase);
		writeReport(METHOD_SERIES_FILE, html);
		html = Reporter.methodNetSeries(dataBase);
		writeReport(METHOD_NET_SERIES_FILE, html);
		html = Reporter.methodChangeSeries(dataBase);
		writeReport(METHOD_CHANGE_SERIES_FILE, html);
		html = Reporter.methodNetChangeSeries(dataBase);
		writeReport(METHOD_NET_CHANGE_SERIES_FILE, html);
	}

	/**
	 * Writes the report data to the file system.
	 *
	 * @param name
	 *            the name of the report
	 * @param html
	 *            the html to write in the file
	 */
	private static void writeReport(String name, String html) {
		try {
			File file = new File(name);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Writing report : " + file.getAbsolutePath());
			}
			Toolkit.setContents(file, html.getBytes());
		} catch (Exception e) {
			LOGGER.error("Exception writing report : " + name, e);
		}
	}

	/**
	 * This method generates the time series for the methods and puts it in an HTML string. The methods are sorted according to the greatest average
	 * time for each method.
	 */
	@SuppressWarnings("unchecked")
	public static String methodSeries(final IDataBase dataBase) {
		Comparator<Method> comparator = new Comparator<Method>() {
			public int compare(Method o1, Method o2) {
				Long o1Average = new Long(Profiler.averageMethodTime(o1));
				Long o2Average = new Long(Profiler.averageMethodTime(o2));
				// We want a descending table, i.e. the most expensive at the top
				return o2Average.compareTo(o1Average);
			}
		};
		Set<Method> sortedMethods = new TreeSet<Method>(comparator);
		List<Method> methods = dataBase.find(Method.class);
		sortedMethods.addAll(methods);

		List<Snapshot<?, ?>> snapshots = methods.size() > 0 ? methods.get(0).getSnapshots() : new ArrayList<Snapshot<?, ?>>();
		Element tableElement = tableElement(snapshots);

		for (Method method : sortedMethods) {
			Class<?, ?> klass = (Class<?, ?>) method.getParent();
			String className = klass.getName();
			String methodName = method.getName();

			List<Long> methodSeries = Profiler.methodSeries(method);
			String url = buildGraph(methodSeries);
			Element rowElement = addElement(tableElement, "tr", null);
			addElement(rowElement, "td", className);
			addElement(rowElement, "td", methodName);
			addElement(rowElement, "td", Long.toString(Profiler.totalMethodTime(method)));
			addElement(rowElement, "td", Long.toString(Profiler.totalNetMethodTime(method)));
			Element dataElement = addElement(rowElement, "td", null);
			Element imageElement = addElement(dataElement, "img", null);
			addAttributes(imageElement, new String[] { "src" }, new String[] { url });
		}

		Document document = tableElement.getDocument();
		return prettyPrint(document);

	}

	@SuppressWarnings("unchecked")
	public static String methodNetSeries(final IDataBase dataBase) {
		Comparator<Method> comparator = new Comparator<Method>() {
			public int compare(Method o1, Method o2) {
				Long o1Average = new Long(Profiler.averageMethodNetTime(o1));
				Long o2Average = new Long(Profiler.averageMethodNetTime(o2));
				// We want a descending table, i.e. the most expensive at the top
				return o2Average.compareTo(o1Average);
			}
		};
		Set<Method> sortedMethods = new TreeSet<Method>(comparator);
		List<Method> methods = dataBase.find(Method.class);
		sortedMethods.addAll(methods);

		List<Snapshot<?, ?>> snapshots = methods.size() > 0 ? methods.get(0).getSnapshots() : new ArrayList<Snapshot<?, ?>>();
		Element tableElement = tableElement(snapshots);

		for (Method method : sortedMethods) {
			Class<?, ?> klass = (Class<?, ?>) method.getParent();
			String className = klass.getName();
			String methodName = method.getName();

			List<Long> methodSeries = Profiler.methodNetSeries(method);
			String url = buildGraph(methodSeries);
			Element rowElement = addElement(tableElement, "tr", null);
			addElement(rowElement, "td", className);
			addElement(rowElement, "td", methodName);
			addElement(rowElement, "td", Long.toString(Profiler.totalMethodTime(method)));
			addElement(rowElement, "td", Long.toString(Profiler.totalNetMethodTime(method)));
			Element dataElement = addElement(rowElement, "td", null);
			Element imageElement = addElement(dataElement, "img", null);
			addAttributes(imageElement, new String[] { "src" }, new String[] { url });
		}

		Document document = tableElement.getDocument();
		return prettyPrint(document);
	}

	@SuppressWarnings("unchecked")
	public static String methodChangeSeries(final IDataBase dataBase) {
		Comparator<Method> comparator = new Comparator<Method>() {
			public int compare(Method o1, Method o2) {
				Long o1Average = new Long(Profiler.averageMethodTime(o1));
				Long o2Average = new Long(Profiler.averageMethodTime(o2));
				return o2Average.compareTo(o1Average);
			}
		};
		Set<Method> sortedMethods = new TreeSet<Method>(comparator);
		List<Method> methods = dataBase.find(Method.class);
		sortedMethods.addAll(methods);

		List<Snapshot<?, ?>> snapshots = methods.size() > 0 ? methods.get(0).getSnapshots() : new ArrayList<Snapshot<?, ?>>();
		Element tableElement = tableElement(snapshots);

		for (Method method : sortedMethods) {
			Class<?, ?> klass = (Class<?, ?>) method.getParent();
			String className = klass.getName();
			String methodName = method.getName();

			List<Long> methodSeries = Profiler.methodChangeSeries(method);
			String url = buildGraph(methodSeries);
			Element rowElement = addElement(tableElement, "tr", null);
			addElement(rowElement, "td", className);
			addElement(rowElement, "td", methodName);
			addElement(rowElement, "td", Long.toString(Profiler.averageMethodTime(method)));
			addElement(rowElement, "td", Long.toString(Profiler.averageMethodNetTime(method)));
			Element dataElement = addElement(rowElement, "td", null);
			Element imageElement = addElement(dataElement, "img", null);
			addAttributes(imageElement, new String[] { "src" }, new String[] { url });
		}

		Document document = tableElement.getDocument();
		return prettyPrint(document);
	}

	@SuppressWarnings("unchecked")
	public static String methodNetChangeSeries(final IDataBase dataBase) {
		Comparator<Method> comparator = new Comparator<Method>() {
			public int compare(Method o1, Method o2) {
				Long o1Average = new Long(Profiler.averageMethodTime(o1));
				Long o2Average = new Long(Profiler.averageMethodTime(o2));
				return o2Average.compareTo(o1Average);
			}
		};
		Set<Method> sortedMethods = new TreeSet<Method>(comparator);
		List<Method> methods = dataBase.find(Method.class);
		sortedMethods.addAll(methods);

		List<Snapshot<?, ?>> snapshots = methods.size() > 0 ? methods.get(0).getSnapshots() : new ArrayList<Snapshot<?, ?>>();
		Element tableElement = tableElement(snapshots);

		for (Method method : sortedMethods) {
			Class<?, ?> klass = (Class<?, ?>) method.getParent();
			String className = klass.getName();
			String methodName = method.getName();
			List<Long> methodSeries = Profiler.methodNetChangeSeries(method);
			String url = buildGraph(methodSeries);
			Element rowElement = addElement(tableElement, "tr", null);
			addElement(rowElement, "td", className);
			addElement(rowElement, "td", methodName);
			addElement(rowElement, "td", Long.toString(Profiler.averageMethodTime(method)));
			addElement(rowElement, "td", Long.toString(Profiler.averageMethodNetTime(method)));
			Element dataElement = addElement(rowElement, "td", null);
			Element imageElement = addElement(dataElement, "img", null);
			addAttributes(imageElement, new String[] { "src" }, new String[] { url });
		}

		Document document = tableElement.getDocument();
		return prettyPrint(document);
	}

	private static Element tableElement(List<Snapshot<?, ?>> snapshots) {
		Document document = DocumentHelper.createDocument();
		Element htmlElement = document.addElement("html");
		Element headElement = addElement(htmlElement, "head", null);
		Element linkElement = addElement(headElement, "link", null);
		addAttributes(linkElement, new String[] { "href", "rel", "type", "media" }, new String[] { STYLE_SHEET, "stylesheet", "text/css", "screen" });

		Element bodyElement = addElement(linkElement, "body", null);
		Element tableElement = addElement(bodyElement, "table", null);

		Element headerRowElement = addElement(tableElement, "tr", null);
		String periods = "no periods";
		if (snapshots.size() > 0) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
			Snapshot<?, ?> firstSnapshot = snapshots.get(0);
			Snapshot<?, ?> lastSnapshot = snapshots.get(snapshots.size() - 1);

			String start = dateFormat.format(firstSnapshot.getStart());
			String end = dateFormat.format(lastSnapshot.getStart());

			StringBuilder builder = new StringBuilder(start);
			builder.append(" to ");
			builder.append(end);

			long intervals = firstSnapshot.getEnd().getTime() - firstSnapshot.getStart().getTime();
			builder.append(", at intervals of : ");
			builder.append(intervals);
			builder.append(" ms.");

			periods = builder.toString();
		}
		Element headerElement = addElement(headerRowElement, "th", "Period from : " + periods);
		addAttributes(headerElement, new String[] { "colspan" }, new String[] { "5" });

		Element rowElement = addElement(tableElement, "tr", null);
		addElement(rowElement, "th", "Class");
		addElement(rowElement, "th", "Method");
		addElement(rowElement, "th", "Time");
		addElement(rowElement, "th", "Net time");
		addElement(rowElement, "th", "Graph");
		return tableElement;
	}

	private static Element addElement(Element parent, String name, String text) {
		Element element = parent.addElement(name);
		if (text != null) {
			element.addText(text);
		}
		return element;
	}

	private static void addAttributes(Element element, String[] names, String[] values) {
		for (int i = 0; i < names.length; i++) {
			element.addAttribute(names[i], values[i]);
		}
	}

	private static String prettyPrint(Document document) {
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(System.out, format);
			writer.write(document);

			format = OutputFormat.createCompactFormat();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			writer = new XMLWriter(byteArrayOutputStream, format);
			writer.write(document);
			return byteArrayOutputStream.toString();
		} catch (Exception e) {
			LOGGER.error("Exception pretty printing the output", e);
		}
		return document.asXML();
	}

	private static String buildGraph(List<Long> datas) {
		String url = "http://chart.apis.google.com/chart?cht=lc&chxt=x,y";
		StringBuilder builder = new StringBuilder(url);
		// Set the size of the image
		// &chs=600x100
		builder.append("&chs=");
		int width = datas.size() * 20;
		int height = 100;
		builder.append(width);
		builder.append("x");
		builder.append(height);

		// Add the data
		builder.append("&chd=t:");
		for (int i = 0; i < datas.size(); i++) {
			Long data = datas.get(i);
			builder.append(data);
			if (i + 1 < datas.size()) {
				builder.append(",");
			}
		}
		// Add the periods
		// chxl=0:|0|1|2|3|4|5|6|7|8|9|10&
		builder.append("&chxl=0:|");
		for (int i = 0; i < datas.size(); i++) {
			builder.append(i);
			if (i + 1 < datas.size()) {
				builder.append("|");
			}
		}

		// Minimum and maximum data size
		long min = min(datas);
		long max = max(datas);
		builder.append("&chds=");
		builder.append(min);
		builder.append(",");
		builder.append(max);
		// Minimum and maximum data size again?
		builder.append("&chxr=1,");
		builder.append(min);
		builder.append(",");
		builder.append(max);
		return builder.toString();
	}

	private static long min(List<Long> datas) {
		long min = 0;
		for (Long data : datas) {
			if (data < min) {
				min = data;
			}
		}
		return min;
	}

	private static long max(List<Long> datas) {
		long max = 0;
		for (Long data : datas) {
			if (data > max) {
				max = data;
			}
		}
		return max;
	}

}