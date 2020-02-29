package com.ikokoon.toolkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is an abstract factory. Classes are selected for construction according to the best match between the parameters
 * and the solid implementation
 * of the class.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 02.09.08
 */
public abstract class ObjectFactory {

    /**
     * The LOGGER for the class.
     */
    private static Logger LOGGER = Logger.getLogger(ObjectFactory.class.getName());

    /**
     * This method instantiates a class based on the solid implementation class passed as a parameter and the parameters.
     * A best match between the two parameters determines the class to be instantiated.
     *
     * @param <E>           the desired class to be instantiated
     * @param klass         the class to be instantiated
     * @param allParameters the parameters for the constructor, these cannot be primitives and the parameters in the constructor have to be objects as well, not
     *                      primitives
     * @return the class that best matches the desired class and the parameters for constructors
     */
    public static <E> E getObject(final Class<E> klass, final Object... allParameters) {
        List<Object> parameters = new ArrayList<>();
        Constructor<E> constructor = getConstructor(klass, allParameters, parameters);
        if (constructor != null) {
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            try {
                return constructor.newInstance(parameters.toArray());
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, "Exception generating the action for " + klass + ", with parameters : " + Arrays.asList(allParameters), e);
            } catch (InstantiationException e) {
                LOGGER.log(Level.SEVERE, "Exception generating the action for " + klass + ", with parameters : " + Arrays.asList(allParameters), e);
            } catch (IllegalAccessException e) {
                LOGGER.log(Level.SEVERE, "Exception generating the action for " + klass + ", with parameters : " + Arrays.asList(allParameters), e);
            } catch (InvocationTargetException e) {
                LOGGER.log(Level.SEVERE, "Exception generating the action for " + klass + ", with parameters : " + Arrays.asList(allParameters), e);
            }
        }
        return null;
    }

    /**
     * Finds a constructor in a class that has a signature that includes some the parameters in the parameter list on a best match principal. Note
     * that this method will return the first constructor that has all the parameters in one of the permutations even if there is another constructor
     * that has more parameters in another of the permutations.
     *
     * @param klass         the class look for a constructor in
     * @param allParameters all the parameters that are available for the constructor
     * @param parameters    the parameters that were collected for the best match constructor
     * @param <E>           the type to return
     * @return the constructor that has all the parameters
     */
    protected static <E> Constructor<E> getConstructor(Class<E> klass, Object[] allParameters, List<Object> parameters) {
        Constructor<E> constructor = getConstructor(klass, allParameters);
        if (constructor != null) {
            parameters.addAll(Arrays.asList(allParameters));
            LOGGER.fine("Got constructor : " + constructor + ", with parameters : " + parameters);
            return constructor;
        }

        Permutations permutations = new Permutations();
        List<Object[]> permutationsList = new ArrayList<>();
        permutations.getPermutations(allParameters, permutationsList, allParameters.length);
        for (Object[] permutationParameters : permutationsList) {
            LOGGER.fine("Permutations : " + Arrays.asList(permutationParameters));
            constructor = getConstructor(klass, permutationParameters);
            if (constructor != null) {
                parameters.addAll(Arrays.asList(allParameters));
                LOGGER.fine("Got constructor : " + constructor + ", with parameters : " + parameters);
                return constructor;
            }
            // Try every possible permutation of the parameters, we ignore nulls
            for (int first = 0; first < permutationParameters.length; first++) {
                for (int last = first; last < permutationParameters.length; last++) {
                    int size = last - first;
                    LOGGER.fine("First : " + first + ", last : " + last + ", size : " + size);
                    Object[] dest = new Object[size];
                    System.arraycopy(permutationParameters, first, dest, 0, size);
                    constructor = getConstructor(klass, dest);
                    if (constructor != null) {
                        parameters.addAll(Arrays.asList(dest));
                        LOGGER.fine("Got constructor : " + constructor + ", with parameters : " + parameters);
                        return constructor;
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <E> Constructor<E> getConstructor(Class<E> klass, Object[] permutationParameters) {
        LOGGER.fine("Stripped permutations : " + Arrays.asList(permutationParameters));
        Constructor<?>[] constructors = klass.getDeclaredConstructors();
        outer:
        for (Constructor<?> constructor : constructors) {
            LOGGER.fine("Looking at constructor : " + constructor + " for class " + klass);
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes != null && parameterTypes.length != permutationParameters.length) {
                continue;
            }
            int index = -1;
            assert parameterTypes != null;
            for (Class<?> parameterType : parameterTypes) {
                index++;
                if (permutationParameters[index] == null) {
                    continue outer;
                }
                if (!parameterType.isAssignableFrom(permutationParameters[index].getClass())) {
                    continue outer;
                }
            }
            return (Constructor<E>) constructor;
        }
        return null;
    }

    /**
     * Private constructor ensures singularity.
     */
    private ObjectFactory() {
    }

}