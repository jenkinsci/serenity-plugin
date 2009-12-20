package com.ikokoon.serenity.applet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import com.ikokoon.serenity.model.IModel;
import com.ikokoon.toolkit.Base64;

/**
 * This applet displays sets of data.
 * 
 * @author Michael Couck
 * @since 07.11.09
 * @version 01.00
 */
public class ChartApplet extends JApplet {

	private static final int WIDTH = 750;
	private static final int HEIGHT = 210;
	private static final int HISTORY = 10;

	/** The width of the drawing area, which is the applet width. */
	private int width;
	/** The height of the drawing area, which is the applet height. */
	private int height;
	/** The y coordinate of the heading for the graph. */
	private final int headingYPosition = 15;
	/** The stroke width of the axis. */
	private final float axisStrokeWidth = 1f;
	/** The x coordinate of the origin of the graph. */
	private final int originXPosition = 20;
	/** The y coordinate of the origin of the graph. */
	private final int originYPosition = 40;
	/** The length of the x axis. */
	private int xAxisLength;
	/** The length of the y axis. */
	private int yAxisLength;
	/** The pixels between the ticks on the graph. */
	private final int tick = 5;
	/** The length of a tick. */
	private final int tickLength = 3;
	/** The length of a main tick on the graph. */
	private final int tickMainLength = tickLength * 2 + 2;
	/** The starting point to draw columns, 1 pixel above the origin so we can see the border. */
	private final int columnYPosition = originYPosition + (int) axisStrokeWidth;
	/** The width of a column. */
	private final int columnWidth = 15;
	/** The width of the legend box. */
	private final int legendBoxWidth = 65;
	/** The height of the legend box. */
	private final int legendBoxHeight = 20;
	/** The y coordinate of the legend boxes. */
	private int legendYPosition;

	private Stroke axisStroke = new BasicStroke(axisStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);

	private int colorScale = 8;
	private int goodRed = 0 + (HISTORY * colorScale), goodGreen = 160 + (HISTORY * colorScale), goodBlue = 0 + (HISTORY * colorScale);
	private int okRed = 80 + (HISTORY * colorScale), okGreen = 40 + (HISTORY * colorScale), okBlue = 40 + (HISTORY * colorScale);
	private int badRed = 160 + (HISTORY * colorScale), badGreen = 0 + (HISTORY * colorScale), badBlue = 0 + (HISTORY * colorScale);

	private Color axisColor = new Color(47, 79, 79);
	private Color columnBorderColor = new Color(0, 0, 0);
	private Color legendTextColor = new Color(21, 21, 21);
	private Color metricsTextColor = new Color(0, 0, 0);
	private Color headingTextColor = new Color(0, 0, 0);

	private Font headingFont = new Font("Tahoma", Font.BOLD, 16);
	private Font metricsFont = new Font("Arial", Font.PLAIN, 12);
	private Font legendFont = new Font("Dialog", Font.PLAIN, 11);

	/** This is a dummy model for testing. */
	private IModel model = getModel();

	/**
	 * Sets the model, presumably from JavaScript in the page.
	 * 
	 * @param matrix
	 *            the model in string form
	 */
	public void setModel(final String uri) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					int counter = 0;
					model = null;
					while (counter++ < 50 && model == null) {
						Thread.sleep(10);
						getModel(uri);
					}
					repaint();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void getModel(String uri) {
		try {
			// System.out.println("Base : " + getDocumentBase());
			URL url = new URL(getDocumentBase() + uri);
			// System.out.println("URL : " + url);
			InputStream inputStream = url.openStream();
			// System.out.println("Input stream : " + inputStream);
			String base64 = getContents(inputStream).toString();
			// System.out.println("Base 64 : " + base64);
			this.model = (IModel) deserializeFromBase64(base64);
			// System.out.println("Model : " + this.model);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * De-serializes an object from a base 64 string to an object.
	 * 
	 * @param base64
	 *            the base 64 string representation of the object
	 * @return the object de-serialised from the string or null if an exception is thrown
	 */
	public Object deserializeFromBase64(String base64) {
		byte[] bytes = Base64.decode(base64);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			return objectInputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Reads the contents of the file and returns the contents in a byte array form.
	 * 
	 * @param inputStream
	 *            the file to read the contents from
	 * @return the file contents in a byte array output stream
	 * @throws Exception
	 */
	private ByteArrayOutputStream getContents(InputStream inputStream) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (inputStream == null) {
			return bos;
		}
		try {
			byte[] bytes = new byte[1024];
			int read;
			while ((read = inputStream.read(bytes)) > -1) {
				bos.write(bytes, 0, read);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bos;
	}

	public synchronized void init() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		width = getWidth();
		height = getHeight();
		xAxisLength = width - originXPosition;
		yAxisLength = height - 40;
		legendYPosition = height - legendBoxHeight - 5;
	}

	public synchronized void paint(final Graphics g) {
		try {
			Graphics2D g2d = (Graphics2D) g;
			setRenderingHints(g2d);
			// Create the buffered image
			BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			setRenderingHints(bufferedImage.createGraphics());
			// Paint the background white and set the border
			setCanvas(bufferedImage.createGraphics());
			// Draw the axis
			setAxis(bufferedImage.createGraphics());
			// Draw the columns
			setColumns(bufferedImage.createGraphics());
			// Invert the image with the columns
			bufferedImage = invertImage(bufferedImage);
			setRenderingHints(bufferedImage.createGraphics());
			// Write the heading, label and x and y coordinates labels
			setHeading(bufferedImage.createGraphics(), model.getName());
			// Draw the legend for the graph
			setLegend(bufferedImage.createGraphics());
			// And finally draw the image on the screen
			g2d.drawImage(bufferedImage, 0, 0, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setCanvas(Graphics2D g2d) {
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, width, height);
		g2d.setColor(Color.black);
		g2d.drawRect(0, 0, width - 1, height - 1);
	}

	private void setLegend(Graphics2D g2d) {
		// Draw the columns on the graph
		List<String> legend = model.getLegend();
		List<ArrayList<Double>> metrics = model.getMetrics();
		int segments = metrics.size();
		int segmentSize = xAxisLength / segments;
		for (int segment = 0; segment < segments; segment++) {
			// Draw the legend under the columns
			int segmentXPosition = originXPosition + (segment * segmentSize);

			Rectangle metricsRectangle = new Rectangle(new Point(segmentXPosition, height - originYPosition), new Dimension(segmentSize, 10));
			setMetrics(g2d, Double.toString(metrics.get(segment).get(0)), metricsRectangle);

			int segmentCentre = segmentXPosition + (segmentSize / 2);
			int legendXPosition = segmentCentre - (legendBoxWidth / 2);
			Rectangle rectangle = new Rectangle(new Point(legendXPosition, legendYPosition), new Dimension(legendBoxWidth, legendBoxHeight));
			setLegend(g2d, legend.get(segment), rectangle);
		}
	}

	private void setMetrics(Graphics2D g2d, String string, Rectangle legendBox) {
		g2d.setColor(metricsTextColor);
		g2d.setFont(metricsFont);
		FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
		Rectangle2D rectangle = fontMetrics.getStringBounds(string, g2d);
		double textWidth = rectangle.getWidth();
		double textHeight = rectangle.getHeight();
		double textXPosition = legendBox.getCenterX() - (textWidth / 2);
		double textYPosition = legendBox.getCenterY() + (textHeight / 2);
		g2d.drawString(string, (int) textXPosition, (int) textYPosition);
	}

	private void setLegend(Graphics2D g2d, String string, Rectangle legendBox) {
		g2d.setColor(legendTextColor);
		g2d.setFont(legendFont);
		FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
		Rectangle2D rectangle = fontMetrics.getStringBounds(string, g2d);
		double textWidth = rectangle.getWidth();
		double textHeight = rectangle.getHeight();
		double textXPosition = legendBox.getCenterX() - (textWidth / 2);
		double textYPosition = legendBox.getCenterY() + (textHeight / 2);
		g2d.drawString(string, (int) textXPosition, (int) textYPosition);
	}

	private void setColumns(Graphics2D g2d) {
		// Draw the columns on the graph
		List<ArrayList<Double>> limits = null; // model.getLimits();
		List<ArrayList<Double>> metrics = model.getMetrics();
		int segments = metrics.size();
		int segmentSize = xAxisLength / segments;
		for (int segment = 0; segment < segments && segment < HISTORY; segment++) {
			int histories = Math.min(metrics.get(segment).size(), HISTORY);

			int goodRed = this.goodRed, goodGreen = this.goodGreen, goodBlue = this.goodBlue;
			int okRed = this.okRed, okGreen = this.okGreen, okBlue = this.okBlue;
			int badRed = this.badRed, badGreen = this.badGreen, badBlue = this.badBlue;

			Color columnGoodFillColor = new Color(goodRed, goodGreen, goodBlue);
			Color columnOkFillColor = new Color(okRed, okGreen, okBlue);
			Color columnBadFillColor = new Color(badRed, badGreen, badBlue);
			// Paint from the back to the front
			for (int history = histories - 1; history >= 0; history--) {
				double metric = metrics.get(segment).get(history);
				double scale = getScale(metric, 1);
				double percentage = (metric / scale);
				double columnHeight = percentage * (yAxisLength - (yAxisLength / 3));

				// Find the segment start and end position
				int segmentXPosition = originXPosition + (segment * segmentSize) + (history * (columnWidth / 2));
				// Find the column start x position
				int columnXPosition = segmentXPosition + (columnWidth / 2) + 3;
				int columnYPosition = this.columnYPosition + (history * (columnWidth / 3));

				int borderXPosition = columnXPosition;
				int borderYPosition = columnYPosition;
				int borderHeight = (int) columnHeight;

				double goodLimit = limits.get(segment).get(0);
				double okLimit = limits.get(segment).get(1);
				double badLimit = limits.get(segment).get(2);
				double positiveLimit = limits.get(segment).get(3);

				double goodColumnHeight = 0;
				double okColumnHeight = 0;
				double badColumnHeight = 0;

				boolean raised = true;

				goodColumnHeight = Math.min(columnHeight, (goodLimit / metric) * columnHeight);
				okColumnHeight = Math.min(columnHeight, ((okLimit / metric) * columnHeight) + goodColumnHeight);
				badColumnHeight = Math.min(columnHeight, ((badLimit / metric) * columnHeight) + okColumnHeight);

				if (goodLimit == 0 && okLimit == 0 && badLimit == 0) {
					goodColumnHeight = columnHeight;
				}

				if (positiveLimit == 1) {
					double tempColumnHeight = goodColumnHeight;
					goodColumnHeight = badColumnHeight;
					badColumnHeight = tempColumnHeight;
				}
				// else {
				// fillColumn(g2d, columnBadFillColor, columnXPosition, columnYPosition, (int) badColumnHeight, raised);
				// fillColumn(g2d, columnOkFillColor, columnXPosition, columnYPosition, (int) okColumnHeight, raised);
				// fillColumn(g2d, columnGoodFillColor, columnXPosition, columnYPosition, (int) goodColumnHeight, raised);
				// }

				// fillColumn(g2d, columnBadFillColor, columnXPosition, columnYPosition, (int) badColumnHeight, raised);
				// fillColumn(g2d, columnOkFillColor, columnXPosition, columnYPosition, (int) okColumnHeight, raised);
				// fillColumn(g2d, columnGoodFillColor, columnXPosition, columnYPosition, (int) goodColumnHeight, raised);

				fillColumn(g2d, columnGoodFillColor, columnXPosition, columnYPosition, (int) goodColumnHeight, raised);
				fillColumn(g2d, columnOkFillColor, columnXPosition, columnYPosition, (int) okColumnHeight, raised);
				fillColumn(g2d, columnBadFillColor, columnXPosition, columnYPosition, (int) badColumnHeight, raised);

				// g2d.setColor(columnGoodFillColor);
				// g2d.fill3DRect(columnXPosition, columnYPosition, columnWidth, (int) goodColumnHeight, raised);
				// g2d.setColor(columnOkFillColor);
				// g2d.fill3DRect(columnXPosition, columnYPosition, columnWidth, (int) okColumnHeight, raised);
				// g2d.setColor(columnBadFillColor);
				// g2d.fill3DRect(columnXPosition, columnYPosition, columnWidth, (int) badColumnHeight, raised);

				g2d.setColor(columnBorderColor);
				g2d.draw3DRect(borderXPosition, borderYPosition, columnWidth, borderHeight, raised);

				goodRed = getNextColor(colorScale, goodRed, this.goodRed);
				goodGreen = getNextColor(colorScale, goodGreen, this.goodGreen);
				goodBlue = getNextColor(colorScale, goodBlue, this.goodBlue);

				okRed = getNextColor(colorScale, okRed, this.okRed);
				okGreen = getNextColor(colorScale, okGreen, this.okGreen);
				okBlue = getNextColor(colorScale, okBlue, this.okBlue);

				badRed = getNextColor(colorScale, badRed, this.badRed);
				badGreen = getNextColor(colorScale, badGreen, this.badGreen);
				badBlue = getNextColor(colorScale, badBlue, this.badBlue);

				columnGoodFillColor = new Color(goodRed, goodGreen, goodBlue);
				columnOkFillColor = new Color(okRed, okGreen, okBlue);
				columnBadFillColor = new Color(badRed, badGreen, badBlue);
			}
		}
	}

	private void fillColumn(Graphics2D g2d, Color color, int columnXPosition, int columnYPosition, int height, boolean raised) {
		g2d.setColor(color);
		g2d.fill3DRect(columnXPosition, columnYPosition, columnWidth, height, raised);
	}

	private int getNextColor(int colorScale, int color, int original) {
		color = color - colorScale;
		if (color < 0) {
			color = original;
		}
		return color;
	}

	private double getScale(double d, int multiplier) {
		if (d <= 1) {
			return 1;
		}
		if (d < multiplier * 10) {
			return multiplier * 10;
		}
		return getScale(d, multiplier * 10);
	}

	private void setAxis(Graphics2D g2d) {
		// Draw the x and y axis
		g2d.setStroke(axisStroke);
		g2d.setColor(axisColor);
		g2d.drawLine(originXPosition, originYPosition, xAxisLength, originYPosition); // x
		g2d.drawLine(originXPosition, originYPosition, originXPosition, yAxisLength); // y
		// Draw the marks on the y axis
		for (int i = originYPosition, index = 0; i < yAxisLength; i = i + tick, index++) {
			if (index % 5 == 0) {
				g2d.drawLine(originXPosition - tickMainLength, i, originXPosition, i);
			} else {
				g2d.drawLine(originXPosition - tickLength, i, originXPosition, i);
			}
		}
		// Draw marks on the x axis
		int segments = model.getLegend().size();
		int segmentSize = xAxisLength / segments;
		for (int i = originXPosition; i < xAxisLength; i = i + segmentSize) {
			g2d.drawLine(i, originYPosition, i, originYPosition - tickMainLength);
		}
	}

	private void setHeading(Graphics2D g2d, String string) {
		// Find the size of string s in font f in the current Graphics context g.
		g2d.setFont(headingFont);
		g2d.setColor(headingTextColor);
		FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
		Rectangle2D rectangle = fontMetrics.getStringBounds(string, g2d);
		int textWidth = (int) (rectangle.getWidth());
		int panelWidth = this.getWidth();
		// Centre text horizontally and vertically
		int x = (panelWidth - textWidth) / 2;
		g2d.drawString(string, x, headingYPosition); // Draw the string.
	}

	private void setRenderingHints(Graphics2D g2d) {
		// Determine if antialiasing is enabled
		RenderingHints renderingHints = g2d.getRenderingHints();
		boolean antialiasOn = renderingHints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
		if (!antialiasOn) {
			// Enable antialiasing for shapes
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// Enable antialiasing for text
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
	}

	private BufferedImage invertImage(BufferedImage bufferedImage) {
		BufferedImage invertedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D invertedImageGraphics = invertedImage.createGraphics();
		// Invert the image, we draw inverted as it is easier with the numbers, then flip the image and draw the text
		invertedImageGraphics.drawImage(bufferedImage, // the specified image to be drawn. This method does nothing if the image is null.
				0, // dx1 the x coordinate of the first corner of the destination rectangle.
				bufferedImage.getHeight(this), // dy1 the y coordinate of the first corner of the destination rectangle.
				bufferedImage.getWidth(this), // dx2 the x coordinate of the second corner of the destination rectangle.
				0, // dy2 the y coordinate of the second corner of the destination rectangle.
				0, // sx1 the x coordinate of the first corner of the source rectangle.
				0,// sy1 the y coordinate of the first corner of the source rectangle.
				bufferedImage.getWidth(this),// sx2 the x coordinate of the second corner of the source rectangle.
				bufferedImage.getHeight(this),// sy2 the y coordinate of the second corner of the source rectangle.
				this); // observer object to be notified as more of the image is scaled and converted.
		return invertedImage;
	}

	public void update(Graphics g) {
		paint(g);
	}

	protected static String projectBase64 = "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";
	protected static String packageBase64 = "rO0ABXNyACBjb20uaWtva29vbi5zZXJlbml0eS5tb2RlbC5Nb2RlbGSMpEB/c+h5AgAETAAGbGVnZW5kdAAQTGphdmEvdXRpbC9MaXN0O0wABmxpbWl0c3EAfgABTAAHbWV0cmljc3EAfgABTAAEbmFtZXQAEkxqYXZhL2xhbmcvU3RyaW5nO3hwc3IAE2phdmEudXRpbC5BcnJheUxpc3R4gdIdmcdhnQMAAUkABHNpemV4cAAAAAl3BAAAAAp0AApDb3ZlcmFnZSAldAAKQ29tcGxleGl0eXQADkFic3RyYWN0IDwgMS4wdAAPU3RhYmlsaXR5IDwgMS4wdAAORGlzdGFuY2UgPCAxLjB0AAVMaW5lc3QACkludGVyZmFjZXN0AA9JbXBsZW1lbnRhdGlvbnN0AAhFeGVjdXRlZHhzcQB+AAQAAAAJdwQAAAAKc3EAfgAEAAAABHcEAAAACnNyABBqYXZhLmxhbmcuRG91YmxlgLPCSilr+wQCAAFEAAV2YWx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cEBJAAAAAAAAc3EAfgARQD4AAAAAAABzcQB+ABFAJAAAAAAAAHNxAH4AET/wAAAAAAAAeHNxAH4ABAAAAAR3BAAAAApzcQB+ABFAJAAAAAAAAHNxAH4AEUA+AAAAAAAAc3EAfgARQCQAAAAAAABzcQB+ABEAAAAAAAAAAHhzcQB+AAQAAAAEdwQAAAAKc3EAfgARP+AAAAAAAABzcQB+ABE/0zMzMzMzM3NxAH4AEQAAAAAAAAAAc3EAfgARP/AAAAAAAAB4c3EAfgAEAAAABHcEAAAACnNxAH4AET/gAAAAAAAAc3EAfgARP8mZmZmZmZpzcQB+ABEAAAAAAAAAAHNxAH4AET/wAAAAAAAAeHNxAH4ABAAAAAR3BAAAAApzcQB+ABE/4AAAAAAAAHNxAH4AET/TMzMzMzMzc3EAfgARAAAAAAAAAABzcQB+ABE/8AAAAAAAAHhzcQB+AAQAAAAEdwQAAAAKc3EAfgARAAAAAAAAAABzcQB+ABEAAAAAAAAAAHNxAH4AEQAAAAAAAAAAc3EAfgARAAAAAAAAAAB4c3EAfgAEAAAABHcEAAAACnNxAH4AEQAAAAAAAAAAc3EAfgARAAAAAAAAAABzcQB+ABEAAAAAAAAAAHNxAH4AEQAAAAAAAAAAeHNxAH4ABAAAAAR3BAAAAApzcQB+ABEAAAAAAAAAAHNxAH4AEQAAAAAAAAAAc3EAfgARAAAAAAAAAABzcQB+ABEAAAAAAAAAAHhzcQB+AAQAAAAEdwQAAAAKc3EAfgARAAAAAAAAAABzcQB+ABEAAAAAAAAAAHNxAH4AEQAAAAAAAAAAc3EAfgARAAAAAAAAAAB4eHNxAH4ABAAAAAl3BAAAAApzcQB+AAQAAAADdwQAAAAKc3EAfgARQCNrhR64UexzcQB+ABFAI2uFHrhR7HNxAH4AEUAja4UeuFHseHNxAH4ABAAAAAN3BAAAAApzcQB+ABFANKuFHrhR7HNxAH4AEUA0q4UeuFHsc3EAfgARQDSrhR64Uex4c3EAfgAEAAAAA3cEAAAACnNxAH4AET/B64UeuFHsc3EAfgARP8HrhR64UexzcQB+ABE/weuFHrhR7HhzcQB+AAQAAAADdwQAAAAKc3EAfgARP9hR64UeuFJzcQB+ABE/2FHrhR64UnNxAH4AET/YUeuFHrhSeHNxAH4ABAAAAAN3BAAAAApzcQB+ABE/1R64UeuFH3NxAH4AET/VHrhR64Ufc3EAfgARP9UeuFHrhR94c3EAfgAEAAAAA3cEAAAACnNxAH4AEUDDfYAAAAAAc3EAfgARQMN9gAAAAABzcQB+ABFAw32AAAAAAHhzcQB+AAQAAAADdwQAAAAKc3EAfgARQDsAAAAAAABzcQB+ABFAOwAAAAAAAHNxAH4AEUA7AAAAAAAAeHNxAH4ABAAAAAN3BAAAAApzcQB+ABFAY6AAAAAAAHNxAH4AEUBjoAAAAAAAc3EAfgARQGOgAAAAAAB4c3EAfgAEAAAAA3cEAAAACnNxAH4AEUD9zHAAAAAAc3EAfgARQP3McAAAAABzcQB+ABFA/cxwAAAAAHh4dAATZWR1LnVtZC5jcy5maW5kYnVncw==";
	protected static String classBase64 = "rO0ABXNyACBjb20uaWtva29vbi5zZXJlbml0eS5tb2RlbC5Nb2RlbGSMpEB/c+h5AgAETAAGbGVnZW5kdAAQTGphdmEvdXRpbC9MaXN0O0wABmxpbWl0c3EAfgABTAAHbWV0cmljc3EAfgABTAAEbmFtZXQAEkxqYXZhL2xhbmcvU3RyaW5nO3hwc3IAE2phdmEudXRpbC5BcnJheUxpc3R4gdIdmcdhnQMAAUkABHNpemV4cAAAAAd3BAAAAAp0AApDb3ZlcmFnZSAldAAKQ29tcGxleGl0eXQAD1N0YWJpbGl0eSA8IDEuMHQABUxpbmVzdAAIRXhlY3V0ZWR0AAlFZmZlcmVuY2V0AAlBZmZlcmVuY2V4c3EAfgAEAAAAB3cEAAAACnNxAH4ABAAAAAR3BAAAAApzcgAQamF2YS5sYW5nLkRvdWJsZYCzwkopa/sEAgABRAAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHBASQAAAAAAAHNxAH4AD0A+AAAAAAAAc3EAfgAPQCQAAAAAAABzcQB+AA8/8AAAAAAAAHhzcQB+AAQAAAAEdwQAAAAKc3EAfgAPQCQAAAAAAABzcQB+AA9APgAAAAAAAHNxAH4AD0AkAAAAAAAAc3EAfgAPAAAAAAAAAAB4c3EAfgAEAAAABHcEAAAACnNxAH4ADz/gAAAAAAAAc3EAfgAPP8mZmZmZmZpzcQB+AA8AAAAAAAAAAHNxAH4ADz/wAAAAAAAAeHNxAH4ABAAAAAR3BAAAAApzcQB+AA8AAAAAAAAAAHNxAH4ADwAAAAAAAAAAc3EAfgAPAAAAAAAAAABzcQB+AA8AAAAAAAAAAHhzcQB+AAQAAAAEdwQAAAAKc3EAfgAPAAAAAAAAAABzcQB+AA8AAAAAAAAAAHNxAH4ADwAAAAAAAAAAc3EAfgAPAAAAAAAAAAB4c3EAfgAEAAAABHcEAAAACnNxAH4ADwAAAAAAAAAAc3EAfgAPAAAAAAAAAABzcQB+AA8AAAAAAAAAAHNxAH4ADwAAAAAAAAAAeHNxAH4ABAAAAAR3BAAAAApzcQB+AA8AAAAAAAAAAHNxAH4ADwAAAAAAAAAAc3EAfgAPAAAAAAAAAABzcQB+AA8AAAAAAAAAAHh4c3EAfgAEAAAAB3cEAAAACnNxAH4ABAAAAAN3BAAAAApzcQB+AA9AVW1wo9cKPXNxAH4AD0BVbXCj1wo9c3EAfgAPQFVtcKPXCj14c3EAfgAEAAAAA3cEAAAACnNxAH4ADz/vrhR64Ueuc3EAfgAPP++uFHrhR65zcQB+AA8/764UeuFHrnhzcQB+AAQAAAADdwQAAAAKc3EAfgAPP/AAAAAAAABzcQB+AA8/8AAAAAAAAHNxAH4ADz/wAAAAAAAAeHNxAH4ABAAAAAN3BAAAAApzcQB+AA9AHAAAAAAAAHNxAH4AD0AcAAAAAAAAc3EAfgAPQBwAAAAAAAB4c3EAfgAEAAAAA3cEAAAACnNxAH4AD0AYAAAAAAAAc3EAfgAPQBgAAAAAAABzcQB+AA9AGAAAAAAAAHhzcQB+AAQAAAADdwQAAAAKc3EAfgAPP/AAAAAAAABzcQB+AA8/8AAAAAAAAHNxAH4ADz/wAAAAAAAAeHNxAH4ABAAAAAN3BAAAAApzcQB+AA8AAAAAAAAAAHNxAH4ADwAAAAAAAAAAc3EAfgAPAAAAAAAAAAB4eHQAJGVkdS51bWQuY3MuZmluZGJ1Z3MuY2xhc3NmaWxlLkdsb2JhbA==";

	/**
	 * This generates a dummy model for testing and the initial data displayed rather than nothing.
	 * 
	 * @return a generated dummy model
	 */
	private IModel getModel() {
		IModel model = (IModel) deserializeFromBase64(classBase64);
		return model;
	}

}
