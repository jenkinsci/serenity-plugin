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

import javax.swing.JApplet;

import com.ikokoon.serenity.model.IModel;
import com.ikokoon.serenity.model.Model;

/**
 * This applet displays sets of data.
 * 
 * @author Michael Couck
 * @since 07.11.09
 * @version 01.00
 */
public class TrendApplet extends JApplet {

	/** The width of the drawing area, which is the applet width. */
	private int width;
	/** The height of the drawing area, which is the applet height. */
	private int height;
	/** The y coordinate of the heading for the graph. */
	private final int headingYPosition = 15;
	/** The stroke width of the axis. */
	private final float axisStrokeWidth = 1;
	/** The x coordinate of the origin of the graph. */
	private final int originXPosition = 40;
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
	private Color axisColor = new Color(47, 79, 79);
	private Color columnBorderColor = new Color(0, 0, 0);
	private Color legendBorderColor = new Color(40, 40, 40);
	private Color legendTextColor = new Color(79, 79, 79);
	private Color metricsTextColor = new Color(0, 0, 0);

	private Font headingFont = new Font("Tahoma", Font.BOLD, 16);
	private Font legendFont = new Font("Dialog", Font.PLAIN, 12);
	private Font metricsFont = new Font("Monospaced", Font.PLAIN, 11);

	/** This is a dummy model for testing. */
	private IModel model = getModel();

	/**
	 * Sets the model, presumably from JavaScript in the page.
	 * 
	 * @param matrix
	 *            the model in string form
	 */
	public void setModel(String model) {
		System.out.println("Model : " + model);
		repaint();
	}

	private IModel getModel() {
		String[] legend = { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten" };
		double[][] limits = new double[][] { { 5, 6, 7, 8, 2, 5, 6, 9, 5, 6 }, { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 } };
		double[][] metrics = new double[][] { { 2, 5, 8, 1, 2, 6, 4, 8 }, { 2, 5, 4, 6, 8, 7, 9, 1 }, { 8, 7, 5, 1, 3, 6, 4, 9 },
				{ 5, 4, 7, 8, 3, 6, 2, 4 }, { 2, 4, 1, 5, 9, 6, 4, 7 }, { 1, 2, 5, 3, 6, 4, 1, 9 }, { 9, 8, 9, 7, 4, 5, 2, 1 },
				{ 4, 5, 6, 4, 5, 6, 8, 8 }, { 5, 4, 2, 1, 3, 6, 9, 8 }, { 8, 5, 6, 9, 8, 5, 4, 1 } };
		return new Model(this.getClass().getPackage().getName(), legend, limits, metrics);
	}

	public void init() {
		width = getWidth();
		height = getHeight();
		xAxisLength = width - originXPosition;
		yAxisLength = height - 20;
		legendYPosition = height - legendBoxHeight - 2;
	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		setRenderingHints(g2d);
		// Create the buffered image
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D imageGraphics = bufferedImage.createGraphics();
		setRenderingHints(imageGraphics);
		// Draw the axis
		setAxis(imageGraphics);
		// Draw the columns
		setColumns(imageGraphics);
		// Invert and draw the image
		setImage(g2d, bufferedImage);
		// Write the heading, label and x and y coordinates labels
		setHeading(g2d, "Package : " + model.getName());
		// Draw the legend for the graph
		setLegend(g2d);
	}

	private void setLegend(Graphics2D g2d) {
		// Draw the columns on the graph
		String[] legend = model.getLegend();
		double[][] metrics = model.getMetrics();
		int segments = metrics.length;
		int segmentSize = xAxisLength / segments;
		for (int segment = 0; segment < segments; segment++) {
			// Draw the legend under the columns
			int segmentXPosition = originXPosition + (segment * segmentSize);
			int segmentCentre = segmentXPosition + (segmentSize / 2);

			g2d.setColor(metricsTextColor);
			g2d.setFont(metricsFont);
			Rectangle metricsRectangle = new Rectangle(new Point(segmentXPosition, height - originYPosition), new Dimension(segmentSize, 10));
			setMetrics(g2d, Double.toString(metrics[segment][0]), metricsRectangle);

			int legendXPosition = segmentCentre - (legendBoxWidth / 2);
			g2d.setColor(legendBorderColor);
			g2d.drawRect(legendXPosition, legendYPosition, legendBoxWidth, legendBoxHeight);
			g2d.setColor(legendTextColor);
			g2d.setFont(legendFont);
			Rectangle rectangle = new Rectangle(new Point(legendXPosition, legendYPosition), new Dimension(legendBoxWidth, legendBoxHeight));
			setLegend(g2d, legend[segment], rectangle);
		}
	}

	private void setMetrics(Graphics2D g2d, String string, Rectangle legendBox) {
		FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
		Rectangle2D rectangle = fontMetrics.getStringBounds(string, g2d);
		double textWidth = rectangle.getWidth();
		double textHeight = rectangle.getHeight();
		double textXPosition = legendBox.getCenterX() - (textWidth / 2);
		double textYPosition = legendBox.getCenterY() + (textHeight / 2);
		g2d.drawString(string, (int) textXPosition, (int) textYPosition);
	}

	private void setLegend(Graphics2D g2d, String string, Rectangle legendBox) {
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
		double[][] metrics = model.getMetrics();
		double[][] limits = model.getLimits();
		int segments = metrics.length;
		int segmentSize = xAxisLength / segments;
		for (int segment = 0; segment < segments; segment++) {
			int histories = metrics[segment].length;
			int colorScale = 12;
			int red = 34 + (histories * colorScale), green = 139 + (histories * colorScale), blue = 34 + (histories * colorScale);
			Color columnFillColor = new Color(red, green, blue);
			// Paint from the back to the front
			for (int history = histories - 1; history >= 0; history--) {
				double columnHeight = metrics[segment][history] / limits[1][segment] * (yAxisLength - (yAxisLength / 3));
				g2d.setColor(columnBorderColor);

				// Find the segment start and end position
				int segmentXPosition = originXPosition + (segment * segmentSize) + (history * (columnWidth / 2));
				// Find the column start x position
				int columnXPosition = segmentXPosition + (columnWidth / 2) + 3;
				int columnYPosition = this.columnYPosition + (history * (columnWidth / 3));

				g2d.drawRect(columnXPosition, columnYPosition, columnWidth, (int) columnHeight);
				g2d.setColor(columnFillColor);
				g2d.fillRect(columnXPosition + 1, columnYPosition + 1, columnWidth - 1, (int) columnHeight - 1);

				red = red - colorScale;
				green = green - colorScale;
				blue = blue - colorScale;
				columnFillColor = new Color(red, green, blue);
			}
		}
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
		int segments = model.getLegend().length;
		int segmentSize = xAxisLength / segments;
		for (int i = originXPosition; i < xAxisLength; i = i + segmentSize) {
			g2d.drawLine(i, originYPosition, i, originYPosition - tickMainLength);
		}
	}

	private void setRenderingHints(Graphics2D g2d) {
		// Determine if antialiasing is enabled
		RenderingHints renderingHints = g2d.getRenderingHints();
		boolean antialiasOn = renderingHints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
		if (!antialiasOn) {
			// Enable antialiasing for shapes
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// Enable antialiasing for text
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		} else {

		}
	}

	private void setImage(Graphics2D g2d, BufferedImage image) {
		// Invert the image, we draw inverted as it is easier with the numbers, then flip the image and draw the text
		g2d.drawImage(image, // the specified image to be drawn. This method does nothing if the image is null.
				0, // dx1 the x coordinate of the first corner of the destination rectangle.
				image.getHeight(this), // dy1 the y coordinate of the first corner of the destination rectangle.
				image.getWidth(this), // dx2 the x coordinate of the second corner of the destination rectangle.
				0, // dy2 the y coordinate of the second corner of the destination rectangle.
				0, // sx1 the x coordinate of the first corner of the source rectangle.
				0,// sy1 the y coordinate of the first corner of the source rectangle.
				image.getWidth(this),// sx2 the x coordinate of the second corner of the source rectangle.
				image.getHeight(this),// sy2 the y coordinate of the second corner of the source rectangle.
				this); // observer object to be notified as more of the image is scaled and converted.
	}

	private void setHeading(Graphics2D g2d, String string) {
		// Find the size of string s in font f in the current Graphics context g.
		g2d.setFont(headingFont);
		FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
		Rectangle2D rectangle = fontMetrics.getStringBounds(string, g2d);
		int textWidth = (int) (rectangle.getWidth());
		int panelWidth = this.getWidth();
		// Centre text horizontally and vertically
		int x = (panelWidth - textWidth) / 2;
		g2d.drawString(string, x, headingYPosition); // Draw the string.
	}

	public void update(Graphics g) {
		paint(g);
	}

}
