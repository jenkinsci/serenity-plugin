package com.ikokoon.serenity.persistence;

import com.ikokoon.serenity.model.Composite;
import com.ikokoon.toolkit.Toolkit;

/**
 * This is the top level class in the heirachy for the database classes. Any common methods that are used in the database classes can be added to this
 * class.
 * 
 * @author Michael Couck
 * @since 01.12.09
 * @version 01.00
 */
public abstract class DataBase implements IDataBase {

	/**
	 * Sets the id for a composite.
	 * 
	 * @param composite
	 *            the composite to set the id for
	 */
	synchronized final void setId(Composite<?, ?> composite) {
		if (composite == null) {
			return;
		}
		if (composite.getId() == null) {
			Object[] uniqueValues = Toolkit.getUniqueValues(composite);
			Long id = Toolkit.hash(uniqueValues);
			composite.setId(id);
		}
	}
}