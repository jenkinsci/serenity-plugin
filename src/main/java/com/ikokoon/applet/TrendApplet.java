package com.ikokoon.applet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JApplet;

public class TrendApplet extends JApplet {

	public void init() {
		setBackground(Color.white);
	}

	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;
		// Create the buffered image
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D imageGraphics = image.createGraphics();

		// Determine if antialiasing is enabled
		RenderingHints rhints = imageGraphics.getRenderingHints();
		boolean antialiasOn = rhints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
		if (!antialiasOn) {
			// Enable antialiasing for shapes
			imageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// Enable antialiasing for text
			imageGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

		imageGraphics.setColor(Color.black);
		imageGraphics.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

		imageGraphics.fillRect(10, 10, 10, 10);
		imageGraphics.fillOval(30, 30, 40, 50);
		imageGraphics.drawOval(50, 10, 40, 50);
		int X = 150, Y = 50;
		int r = 25;
		imageGraphics.drawOval(X - r, Y - r, r * 2, r * 2);

		g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);
	}

	public void update(Graphics g) {
		paint(g);
	}

}
