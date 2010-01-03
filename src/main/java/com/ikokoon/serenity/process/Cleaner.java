package com.ikokoon.serenity.process;

import java.util.List;

import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.IDataBase;

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

	private IDataBase dataBase;

	/**
	 * Constructor takes the parent.
	 * 
	 * @param parent
	 *            the parent process that will chain this process
	 */
	public Cleaner(IProcess parent, IDataBase dataBase) {
		super(parent);
		this.dataBase = dataBase;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void execute() {
		super.execute();
		// Clean all the packages that got in the database along the processing
		// that were not included in the packages required
		List<Package> packages = dataBase.find(Package.class);
		for (Package<?, ?> pakkage : packages.toArray(new Package[packages.size()])) {
			// Remove the packages that are not included in the list to process
			if (!Configuration.getConfiguration().included(pakkage.getName())) {
				dataBase.remove(Package.class, pakkage.getId());
			}
		}
	}

}