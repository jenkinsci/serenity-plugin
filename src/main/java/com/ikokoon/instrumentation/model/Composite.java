package com.ikokoon.instrumentation.model;

import java.util.ArrayList;
import java.util.List;

public class Composite<E> implements IComposite<E> {

	public static final int PRECISION = 2;
	public static final String NAME = "name";
	public static final String CLASS_NAME = "className";
	public static final String METHOD_NAME = "methodName";
	public static final String NUMBER = "number";
	public static final String DESCRIPTION = "description";

	private Long id;
	private IComposite parent;
	private List<E> children = new ArrayList<E>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public IComposite getParent() {
		return parent;
	}

	public void setParent(IComposite parent) {
		this.parent = parent;
	}

	public List<E> getChildren() {
		return children;
	}

	public void setChildren(List<E> children) {
		this.children = children;
	}

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
