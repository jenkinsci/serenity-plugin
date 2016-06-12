package com.ikokoon.serenity.hudson.modeller;

import com.ikokoon.serenity.model.Composite;

/**
 * This is the interface for classes that can generate the model that gets sent to the front end.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09.12.09
 */
public interface IModeller {

    /**
     * Accesses the model in a base64 string representation.
     *
     * @return the IModel serialised to a base 64 string
     */
    String getModel();

    /**
     * Visits a composite and builds a model from the composite.
     *
     * @param klass      the class of the composite
     * @param composites the list of composites to generate the model from
     */
    void visit(final Class<?> klass, final Composite<?, ?>... composites);

    /**
     * Sets the numbers of each build in Jenkins.
     *
     * @param buildNumbers the build numbers in Jenkins, i.e. 23, 26, 89 etc, do not have to be consecutive
     */
    void setBuildNumbers(final Integer... buildNumbers);

}
