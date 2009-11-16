package com.ikokoon.target;

import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * This is the test class for the coverage functionality.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class Target implements ITarget, Serializable {

	/** The logger for the class. */
	private transient Logger logger = Logger.getLogger(Target.class);

	@SuppressWarnings("unused")
	private String name;

	/**
	 * Constructor.
	 */
	public Target() {
	}

	/**
	 * Another constructor.
	 * 
	 * @param name
	 */
	public Target(String name) {
		this.name = name;
		logger.debug(name);
	}

	/**
	 * A simple method that does nothing.
	 * 
	 * @param name
	 * @return
	 */
	public String getName(String name) {
		int a = 5;
		int b = a;
		int c = a + b;
		logger.debug(c);
		InnerTarget innerTarget = new InnerTarget();
		logger.debug(innerTarget.helloWorld("Hello World"));
		// See what happens with an anon class
		class InlineClass {
			public String helloWorldAgain(String helloWorldAgain) {
				return helloWorldAgain;
			}
		}
		InlineClass inlineClass = new InlineClass();
		inlineClass.helloWorldAgain("Hello World Again");
		return name;
	}

	/**
	 * A complex method that does nothing.
	 * 
	 * @param s1
	 * @param s2
	 * @param s3
	 * @param i1
	 * @param i2
	 */
	public void complexMethod(String s1, String s2, String s3, Integer i1, Integer i2) throws Exception {
		if (s1.equals(s2)) {
			if (s2.equals(s3)) {
			}
		}
		if (s3.equals(i1)) {
		}
		if (i1.equals(i2)) {
		} else {
			if (s3.equals(s1)) {
			} else {
			}
		}
		if (i2.equals(s2)) {
			if (s3.equals(i1)) {
				if (s3.equals(i1)) {
				}
			}
		}
		if (s3.equals(i1)) {
			if (s3.equals(i1)) {
			}
		}
		int x = (int) (Math.random() * 100d);
		if (x > 50) {
		}
		if (x < 50) {
		}
		for (int i = 0; i < 10; i++) {
		}

		int a = 0;
		do {
			a++;
		} while (a < 10);

		switch (i1) {
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		case 5:
			break;
		default:
			break;
		}
		if (x < -1) {
			throw new Exception("Oops...");
		}
	}

	public String methodName(String s1, String s2) {
		return s1;
	}

	/**
	 * An inner class for shits and giggles.
	 * 
	 * @author Michael Couck
	 */
	public static class InnerTarget {
		public String helloWorld(String helloWorld) {
			return helloWorld;
		}
	}

}
