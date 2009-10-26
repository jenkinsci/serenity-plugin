package com.ikokoon.toolkit;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

/**
 * This is an abstract factory. Classes are selected for construction according to the best match between the parameters and the solid implementation
 * of the class.
 * 
 * @author Michael Couck
 * @since 02.09.08
 * @version 01.00
 */
public abstract class ObjectFactory {

	/** The logger for the class. */
	private static Logger LOGGER = Logger.getLogger(ObjectFactory.class);

	/**
	 * This method instantiates a class based on the solid implementation class passed as a parameter and the parameters. A best match between the two
	 * parameters determines the class to be instanciated.
	 * 
	 * @param <E>
	 *            the desired class to be instanciated
	 * @param klass
	 *            the class to be instanciated
	 * @param parameters
	 *            the parameters for the constructor
	 * @return the class that best matches the desired class and the parameters for constructors
	 */
	public static <E> E getObject(Class<E> klass, Object[] allParameters) {
		Constructor<E> constructor = getConstructor(klass, allParameters);
		LOGGER.debug("Got constructor : " + constructor);
		if (constructor != null) {
			try {
				if (!constructor.isAccessible()) {
					constructor.setAccessible(true);
				}
				E e = constructor.newInstance(allParameters);
				LOGGER.debug("Instanciated : " + e);
				return e;
			} catch (Exception e) {
				LOGGER.error("Exception generating the action for " + klass + ", with parameters : ", e);
				for (Object parameter : allParameters) {
					LOGGER.error("		: " + parameter);
				}
			}
		}
		return null;
	}

	/**
	 * Finds a constructor in a class that has a signature that includes some the parameters in the parameter list on a best match principal.
	 * 
	 * @param klass
	 *            the class look for a constructor in
	 * @param parameters
	 *            the parameters for the constructor
	 * @param parameters
	 *            the parameters that were collected for the best match constructor
	 * @return the constructor that has all the parameters
	 */
	protected static <E> Constructor<E> getConstructor(Class<E> klass, Object[] allParameters) {
		// Look for a constructor that has a parameter for all the types
		Constructor<?>[] constructors = klass.getDeclaredConstructors();
		for (Constructor<?> constructor : constructors) {
			LOGGER.debug("Looking at constructor : " + constructor + " for class " + klass);
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes != null && parameterTypes.length != allParameters.length) {
				continue;
			}
			int index = 0;
			boolean hasAllParameters = true;
			for (Class<?> parameterType : parameterTypes) {
				if (allParameters[index] == null) {
					continue;
				}
				if (!parameterType.isAssignableFrom(allParameters[index++].getClass())) {
					hasAllParameters = false;
				}
			}
			if (hasAllParameters) {
				return (Constructor<E>) constructor;
			}
		}
		return null;
	}

	/**
	 * Private constructor ensures singularity.
	 */
	private ObjectFactory() {
	}

}