package com.ikokoon.toolkit;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

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
		List<Object> parameters = new ArrayList<Object>();
		Constructor<E> constructor = getConstructor(klass, allParameters, parameters);
		LOGGER.debug("Got constructor : " + constructor);
		if (constructor != null) {
			try {
				if (!constructor.isAccessible()) {
					constructor.setAccessible(true);
				}
				E e = constructor.newInstance(parameters.toArray());
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
	@SuppressWarnings("unchecked")
	private static <E> Constructor<E> getConstructor(Class<E> klass, Object[] allParameters, List<Object> parameters) {
		List<Object> bestParameters = new ArrayList<Object>();
		// Look for a constructor that has a parameter for all the types
		Constructor<?>[] constructors = klass.getDeclaredConstructors();
		Constructor<?> bestConstructor = null;
		double bestMatch = 0;
		for (Constructor<?> constructor : constructors) {
			LOGGER.debug("Looking at constructor : " + constructor + " for class " + klass);
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			int parameterCounter = 0;
			for (Class<?> parameterType : parameterTypes) {
				for (Object parameter : allParameters) {
					LOGGER.debug("Looking for parameter : " + parameter);
					if (parameter == null || parameterType.isAssignableFrom(parameter.getClass())
							|| parameterType.getName().equals(parameter.getClass().getName())) {
						parameterCounter++;
						bestParameters.add(parameter);
					}
				}
			}
			double match = 0;
			if (parameterTypes.length > 0) {
				match = (parameterCounter / parameterTypes.length) * 100;
			}
			if (match >= bestMatch) {
				bestMatch = match;
				bestConstructor = constructor;
				parameters.clear();
				parameters.addAll(bestParameters);
			}
			LOGGER.debug("Constructor : " + constructor + ", parameters : " + parameters + ", for class : " + klass);
		}
		return (Constructor<E>) bestConstructor;
	}

	/**
	 * Private constructor ensures singularity.
	 */
	private ObjectFactory() {
	}

}