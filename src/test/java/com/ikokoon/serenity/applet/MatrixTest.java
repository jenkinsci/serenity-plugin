package com.ikokoon.serenity.applet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ikokoon.serenity.ATest;

/**
 * Tests the matrix manipulation class.
 * 
 * @author Michael Couck
 * @since 17.11.09
 * @version 01.00
 */
public class MatrixTest extends ATest {

	@Test
	public void inverse() {
		double a[][] = { { 1, 2, 3, 4, 5, 6 }, { 7, 8, 9, 10, 11, 12 }, { 13, 14, 15, 16, 17, 18 } };
		double e[][] = { { 1, 7, 13 }, { 2, 8, 14 }, { 3, 9, 15 }, { 4, 10, 16 }, { 5, 11, 17 }, { 6, 12, 18 } };
		double d[][] = Matrix.inverse(a);
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[i].length; j++) {
				logger.debug(i + ":" + j + ":" + d[i][j]);
				assertEquals(e[i][j], d[i][j]);
			}
		}
	}
}
