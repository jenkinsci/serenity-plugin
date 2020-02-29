package com.ikokoon.serenity.process;

import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.Profiler;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Snapshot;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * This class takes a database and produces reports based on the snapshots for each method, for the profiler.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19.06.10
 */
public class Reporter extends AProcess {

    private IDataBase dataBase;

    public Reporter(IProcess parent, IDataBase dataBase) {
        super(parent);
        this.dataBase = dataBase;
    }

    public void execute() {
        try {
            // Only execute the reports for the profiler if the snapshot interval is set
            long snapshotInterval = Configuration.getConfiguration().getSnapshotInterval();
            logger.log(Level.SEVERE, "Reporter : ");
            if (snapshotInterval < 0) {
                return;
            }
            try {
                // Write the style sheet first
                File file = new File("./" + IConstants.STYLE_SHEET_FILE);
                if (!file.exists()) {
                    InputStream inputStream = Reporter.class.getResourceAsStream(IConstants.REPORT_STYLE_SHEET);
                    Toolkit.setContents(file, Toolkit.getContents(inputStream).toByteArray());
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception writing the style sheet : ", e);
            }

            String html = methodSeries(dataBase);
            writeReport(IConstants.METHOD_SERIES_FILE, html);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception writing the reports", e);
        }
        super.execute();
    }

    /**
     * Writes the report data to the file system.
     *
     * @param name the name of the report
     * @param html the html to write in the file
     */
    private void writeReport(String name, String html) {
        try {
            File file = new File(name);
            if (!file.getParentFile().exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            logger.info("Writing report : " + file.getAbsolutePath());
            Toolkit.setContents(file, html.getBytes(IConstants.ENCODING));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception writing report : " + name, e);
        }
    }

    /**
     * This method generates the time series for the methods and puts it in an HTML string. The methods are sorted according to the greatest
     * average time for each method.
     *
     * @param dataBase the database to aggregate the statistics for
     * @return the html representing the series of timings for the method
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    String methodSeries(final IDataBase dataBase) {
        Comparator<Method> comparator = new Comparator<Method>() {
            public int compare(Method o1, Method o2) {
                Double o1Average = Profiler.averageMethodTime(o1);
                Double o2Average = Profiler.averageMethodTime(o2);
                // We want a descending table, i.e. the most expensive at the top
                return o2Average.compareTo(o1Average);
            }
        };
        Set<Method> sortedMethods = new TreeSet<>(comparator);
        List<Method> methods = dataBase.find(Method.class);
        sortedMethods.addAll(methods);

        List<Snapshot<?, ?>> snapshots = methods.size() > 0 ? methods.get(0).getSnapshots() : new ArrayList<Snapshot<?, ?>>();
        Element tableElement = tableElement(snapshots);

        for (Method method : sortedMethods) {
            Class<?, ?> klass = (Class<?, ?>) method.getParent();
            String className = klass.getName();
            String methodName = method.getName();

            Element rowElement = addElement(tableElement, "tr", null);
            addElement(rowElement, "td", className);
            addElement(rowElement, "td", methodName);
            addElement(rowElement, "td", Double.toString(Profiler.averageMethodTime(method)));
            addElement(rowElement, "td", Double.toString(Profiler.averageMethodNetTime(method)));
            addElement(rowElement, "td", Double.toString(Profiler.totalMethodTime(method)));
            addElement(rowElement, "td", Double.toString(Profiler.totalNetMethodTime(method)));
            addElement(rowElement, "td", Integer.toString(method.getInvocations()));

            Element dataElement = addElement(rowElement, "td", null);
            Element imageElement = addElement(dataElement, "img", null);
            // Add the method series graph for the average and total time for the method
            List<Double> methodSeries = Profiler.methodSeries(method);
            String url = buildGraph(IConstants.METHOD_SERIES, method, methodSeries);
            addAttributes(imageElement, new String[]{"src"}, new String[]{url});

            dataElement = addElement(rowElement, "td", null);
            imageElement = addElement(dataElement, "img", null);
            // Add the method change graph, i.e. the change in the average time for the method
            List<Double> methodChangeSeries = Profiler.methodChangeSeries(method);
            url = buildGraph(IConstants.METHOD_CHANGE_SERIES, method, methodChangeSeries);
            addAttributes(imageElement, new String[]{"src"}, new String[]{url});
        }

        Document document = tableElement.getDocument();
        return prettyPrint(document);

    }

    private Element tableElement(List<Snapshot<?, ?>> snapshots) {
        Document document = DocumentHelper.createDocument();
        Element htmlElement = document.addElement("html");
        Element headElement = addElement(htmlElement, "head", null);
        Element linkElement = addElement(headElement, "link", null);
        addAttributes(linkElement, new String[]{"href", "rel", "type", "media"}, new String[]{IConstants.STYLE_SHEET, "stylesheet",
                "text/css", "screen"});

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

            //noinspection ConstantConditions
            if (firstSnapshot != null && firstSnapshot.getEnd() != null && firstSnapshot.getStart() != null) {
                long intervals = firstSnapshot.getEnd().getTime() - firstSnapshot.getStart().getTime();
                builder.append(", at intervals of : ");
                builder.append(intervals);
                builder.append(" ms.");
            }

            periods = builder.toString();
        }
        Element headerElement = addElement(headerRowElement, "th", "Period from : " + periods);
        addAttributes(headerElement, new String[]{"colspan"}, new String[]{"9"});

        Element rowElement = addElement(tableElement, "tr", null);
        addElement(rowElement, "th", "Class");
        addElement(rowElement, "th", "Method");
        addElement(rowElement, "th", "Time");
        addElement(rowElement, "th", "Net time");
        addElement(rowElement, "th", "Delta time");
        addElement(rowElement, "th", "Net delta time");
        addElement(rowElement, "th", "Invocations");
        addElement(rowElement, "th", "Graph");
        addElement(rowElement, "th", "Delta draph");
        return tableElement;
    }

    private Element addElement(Element parent, String name, String text) {
        Element element = parent.addElement(name);
        if (text != null) {
            element.addText(text);
        }
        return element;
    }

    private void addAttributes(Element element, String[] names, String[] values) {
        for (int i = 0; i < names.length; i++) {
            element.addAttribute(names[i], values[i]);
        }
    }

    private String prettyPrint(Document document) {
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            XMLWriter writer = new XMLWriter(byteArrayOutputStream, format);
            writer.write(document);
            return byteArrayOutputStream.toString(IConstants.ENCODING);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception pretty printing the output", e);
        }
        return document.asXML();
    }

    String buildGraph(String seriesDirectory, Method<?, ?> method, List<Double> datas) {
        XYSeries series = new XYSeries("XYGraph", false, false);

        double snapshot = 0;
        for (Double data : datas) {
            double seconds = TimeUnit.NANOSECONDS.toSeconds(data.intValue());
            series.add(snapshot++, seconds);
        }

        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(series);
        JFreeChart chart = ChartFactory.createXYLineChart(null, "Snapshots", "Time", seriesCollection, PlotOrientation.VERTICAL, false,
                false, false);
        chart.setTitle(new TextTitle(method.getName(), new Font("Arial", Font.BOLD, 11)));

        XYPlot xyPlot = chart.getXYPlot();
        NumberAxis yAxis = (NumberAxis) xyPlot.getRangeAxis();
        yAxis.setAutoRange(true);
        yAxis.setAutoRangeIncludesZero(true);

        NumberAxis xAxis = (NumberAxis) xyPlot.getDomainAxis();
        xAxis.setAutoRange(true);
        xAxis.setAutoRangeIncludesZero(true);
        // xAxis.setTickUnit(new NumberTickUnit(1));

        StringBuilder builder = new StringBuilder(method.getClassName());
        builder.append(method.getName());
        builder.append(method.getDescription());

        String fileName = Long.toString(Toolkit.hash(builder.toString()));
        fileName += ".jpeg";

        File chartSeriesDirectory = new File(IConstants.chartDirectory, seriesDirectory);
        File chartFile = new File(chartSeriesDirectory, fileName);
        try {
            if (!IConstants.chartDirectory.exists()) {
                //noinspection ResultOfMethodCallIgnored
                IConstants.chartDirectory.mkdirs();
            }
            if (!chartSeriesDirectory.exists()) {
                //noinspection ResultOfMethodCallIgnored
                chartSeriesDirectory.mkdirs();
            }
            if (!chartFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                chartFile.createNewFile();
            }
            ChartUtilities.saveChartAsJPEG(chartFile, chart, 450, 150);
            builder = new StringBuilder(IConstants.CHARTS);
            builder.append(File.separatorChar);
            builder.append(seriesDirectory);
            builder.append(File.separatorChar);
            builder.append(fileName);

            return builder.toString();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception generating the graph", e);
        }
        return null;
    }

}