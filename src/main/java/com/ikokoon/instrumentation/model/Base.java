package com.ikokoon.instrumentation.model;

public class Base {

	public static final int PRECISION = 2;

	/**
	 * Formats a double to the required precision.
	 * 
	 * @param d
	 *            the double to format
	 * @param precision
	 *            the precision for the result
	 * @return the double formatted to the required precision
	 */
	public double format(double d, int precision) {
		String doubleString = Double.toString(d);
		doubleString = format(doubleString, precision);
		try {
			d = Double.parseDouble(doubleString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return d;
	}

	/**
	 * Formats a string to the desired precision.
	 * 
	 * @param string
	 *            the string to format to a precision
	 * @param precision
	 *            the precision of the result
	 * @return the string formatted to the required precision
	 */
	public String format(String string, int precision) {
		if (string == null) {
			return string;
		}
		char[] chars = string.trim().toCharArray();
		StringBuilder builder = new StringBuilder();
		int decimal = 1;
		int state = 0;
		int decimals = 0;
		for (char c : chars) {
			switch (c) {
			case '.':
				state = decimal;
				builder.append(c);
				break;
			case ',':
				state = decimal;
				builder.append(c);
				break;
			default:
				if (state == decimal) {
					if (decimals++ >= precision) {
						break;
					}
				}
				builder.append(c);
				break;
			}
		}
		return builder.toString();
	}
}
