package com.ikokoon.serenity;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

public class Reporter {

	private static Logger LOGGER = Logger.getLogger(Reporter.class);
	private static String BORDER_1PX_BLACK = "border : 1px solid black; border-collapse : collapse;";
	private static String TEXT_ALIGN_LEFT = "text-align: left;";
	private static String BORDER_AND_TEXT_ALIGN = BORDER_1PX_BLACK + TEXT_ALIGN_LEFT;

	protected static String METHOD_SERIES = "methodSeries.html";
	protected static String METHOD_NET_SERIES = "methodNetSeries.html";
	protected static String METHOD_CHANGE_SERIES = "methodChangeSeries.html";
	protected static String METHOD_NET_CHANGE_SERIES = "methodNetChangeSeries.html";

	protected static String METHOD_SERIES_FILE = IConstants.SERENITY_DIRECTORY + File.separatorChar + METHOD_SERIES;
	protected static String METHOD_NET_SERIES_FILE = IConstants.SERENITY_DIRECTORY + File.separatorChar + METHOD_NET_SERIES;
	protected static String METHOD_CHANGE_SERIES_FILE = IConstants.SERENITY_DIRECTORY + File.separatorChar + METHOD_CHANGE_SERIES;
	protected static String METHOD_NET_CHANGE_SERIES_FILE = IConstants.SERENITY_DIRECTORY + File.separatorChar + METHOD_NET_CHANGE_SERIES;

	public static void report(IDataBase dataBase) {
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

		Element tableElement = tableElement();

		for (Method method : sortedMethods) {
			Class<?, ?> klass = (Class<?, ?>) method.getParent();
			String className = klass.getName();
			String methodName = method.getName();

			List<Long> methodSeries = Profiler.methodSeries(method);
			String url = buildGraph(methodSeries);
			Element rowElement = addElement(tableElement, "tr", null, BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", className, BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", methodName, BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", Long.toString(Profiler.totalMethodTime(method)), BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", Long.toString(Profiler.totalNetMethodTime(method)), BORDER_AND_TEXT_ALIGN);
			Element dataElement = addElement(rowElement, "td", null, BORDER_AND_TEXT_ALIGN);
			Element imageElement = addElement(dataElement, "img", null, null);
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

		Element tableElement = tableElement();

		for (Method method : sortedMethods) {
			Class<?, ?> klass = (Class<?, ?>) method.getParent();
			String className = klass.getName();
			String methodName = method.getName();

			List<Long> methodSeries = Profiler.methodNetSeries(method);
			String url = buildGraph(methodSeries);
			Element rowElement = addElement(tableElement, "tr", null, BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", className, BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", methodName, BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", Long.toString(Profiler.totalMethodTime(method)), BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", Long.toString(Profiler.totalNetMethodTime(method)), BORDER_AND_TEXT_ALIGN);
			Element dataElement = addElement(rowElement, "td", null, BORDER_AND_TEXT_ALIGN);
			Element imageElement = addElement(dataElement, "img", null, null);
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

		Element tableElement = tableElement();

		for (Method method : sortedMethods) {
			Class<?, ?> klass = (Class<?, ?>) method.getParent();
			String className = klass.getName();
			String methodName = method.getName();

			List<Long> methodSeries = Profiler.methodChangeSeries(method);
			String url = buildGraph(methodSeries);
			Element rowElement = addElement(tableElement, "tr", null, BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", className, BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", methodName, BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", Long.toString(Profiler.averageMethodTime(method)), BORDER_AND_TEXT_ALIGN);
			addElement(rowElement, "td", Long.toString(Profiler.averageMethodNetTime(method)), BORDER_AND_TEXT_ALIGN);
			Element dataElement = addElement(rowElement, "td", null, BORDER_AND_TEXT_ALIGN);
			Element imageElement = addElement(dataElement, "img", null, null);
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

		Element tableElement = tableElement();

		for (Method method : sortedMethods) {
			Class<?, ?> klass = (Class<?, ?>) method.getParent();
			String className = klass.getName();
			String methodName = method.getName();
			List<Long> methodSeries = Profiler.methodNetChangeSeries(method);
			String url = buildGraph(methodSeries);
			Element rowElement = addElement(tableElement, "tr", null, null);
			addElement(rowElement, "td", className, BORDER_1PX_BLACK);
			addElement(rowElement, "td", methodName, BORDER_1PX_BLACK);
			addElement(rowElement, "td", Long.toString(Profiler.averageMethodTime(method)), BORDER_1PX_BLACK);
			addElement(rowElement, "td", Long.toString(Profiler.averageMethodNetTime(method)), BORDER_1PX_BLACK);
			Element dataElement = addElement(rowElement, "td", null, BORDER_1PX_BLACK);
			Element imageElement = addElement(dataElement, "img", null, null);
			addAttributes(imageElement, new String[] { "src" }, new String[] { url });
		}

		Document document = tableElement.getDocument();
		return prettyPrint(document);
	}

	private static Element tableElement() {
		Document document = DocumentHelper.createDocument();
		Element htmlElement = document.addElement("html");
		Element headElement = addElement(htmlElement, "head", null, null);
		Element linkElement = addElement(headElement, "link", null, null);
		addAttributes(linkElement, new String[] { "href", "rel", "type", "media" }, new String[] {
				"http://www.ikokoon.eu/ikokoon/style/style-white.css", "stylesheet", "text/css", "screen" });
		linkElement.addAttribute("href", "http://www.ikokoon.eu/ikokoon/style/style-white.css");

		Element bodyElement = addElement(linkElement, "body", null, null);
		Element tableElement = addElement(bodyElement, "table", null, BORDER_1PX_BLACK);
		Element rowElement = addElement(tableElement, "tr", null, null);
		addElement(rowElement, "th", "Class", BORDER_AND_TEXT_ALIGN);
		addElement(rowElement, "th", "Method", BORDER_AND_TEXT_ALIGN);
		addElement(rowElement, "th", "Time", BORDER_AND_TEXT_ALIGN);
		addElement(rowElement, "th", "Net time", BORDER_AND_TEXT_ALIGN);
		addElement(rowElement, "th", "Graph", BORDER_AND_TEXT_ALIGN);
		return tableElement;
	}

	private static Element addElement(Element parent, String name, String text, String style) {
		Element element = parent.addElement(name);
		if (style != null) {
			addAttributes(element, new String[] { "style" }, new String[] { style });
		}
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
		String url = "http://chart.apis.google.com/chart?cht=lc&chxt=x,y&chxl=0:|0|1|2|3|4|5|6|7|8|9|10&chs=350x100&chd=t:";
		StringBuilder builder = new StringBuilder(url);
		for (int i = 0; i < datas.size(); i++) {
			Long data = datas.get(i);
			builder.append(data);
			if (i + 1 < datas.size()) {
				builder.append(",");
			}
		}
		long min = min(datas);
		long max = max(datas);

		builder.append("&chds=");
		builder.append(min);
		builder.append(",");
		builder.append(max);
		builder.append("&chxr=1,");
		builder.append(min);
		builder.append(",");
		builder.append(max);
		// chxr=1,-241,214&
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