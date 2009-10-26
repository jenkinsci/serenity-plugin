package com.ikokoon.instrumentation.process;

import java.util.List;

import com.ikokoon.IConstants;
import com.ikokoon.instrumentation.Configuration;
import com.ikokoon.instrumentation.model.Package;
import com.ikokoon.instrumentation.model.Project;
import com.ikokoon.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

/**
 * During the collection of the data packages are collected along with the data so we have references to the packages. For example if a class relies
 * on 'org.logj4' then this package will be added to the database but is not included in the packages that the user wants. This class will clean the
 * unwanted packages from the database when the processing is finished.
 * 
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
public class Cleaner extends AProcess implements IConstants {

	/**
	 * Constructor takes the parent.
	 * 
	 * @param parent
	 *            the parent process that will chain this process
	 */
	public Cleaner(IProcess parent) {
		super(parent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void execute() {
		// Clean all the packages that got in the database along the processing
		// that were not included in the packages required and all the packages and classes
		// that have been deleted that are still hanging around
		IDataBase dataBase = IDataBase.DataBase.getDataBase();
		Long id = Toolkit.hash(Project.class.getName());
		Project project = dataBase.find(Project.class, id);
		List<Package> packages = project.getChildren();
		for (Package pakkage : packages) {
			// Remove the packages that are not included in the list to process
			if (!Configuration.getConfiguration().included(pakkage.getName())) {
				dataBase.remove(Package.class, pakkage.getId());
				continue;
			}
			// Remove the packages that have no classes, not interesting
			if (pakkage.getChildren() != null && pakkage.getChildren().size() == 0) {
				dataBase.remove(Package.class, pakkage.getId());
				continue;
			}
		}
	}

}