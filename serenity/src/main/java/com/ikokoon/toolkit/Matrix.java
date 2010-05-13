package com.ikokoon.toolkit;

/**
 * Matrix manipulation class.
 * 
 * @author Michael Couck
 * @since 17.11.09
 * @version 01.00
 */
public class Matrix {

	/**
	 * Inverts a matrix from a m by n to a n by m matrix swapping the indexes.
	 * 
	 * @param a
	 *            the matrix to invert
	 * @return the inverted matrix
	 */
	public static double[][] inverse(double[][] a) {
		int n = a.length;
		int m = a[n - 1].length;
		double[][] b = new double[m][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				b[j][i] = a[i][j];
			}
		}
		return b;
	}

}
