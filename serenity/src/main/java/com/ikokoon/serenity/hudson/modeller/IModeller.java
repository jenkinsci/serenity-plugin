package com.ikokoon.serenity.hudson.modeller;

import com.ikokoon.serenity.model.Composite;

/**
 * This is the interface for classes that can generate the model that gets sent to the front end.
 * 
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
public interface IModeller {

	/**
	 * Accesses the model in a base64 string representation.
	 * 
	 * @return the IModel serialised to a base 64 string
	 */
	public String getModel();

	/**
	 * Visits a composite and builds a model from the composite.
	 * 
	 * @param klass
	 *            the class of the composite
	 * @param composites
	 *            the list of composites to generate the model from
	 */
	public void visit(Class<?> klass, Composite<?, ?>... composites);

}
