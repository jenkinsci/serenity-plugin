package com.ikokoon.serenity.process;

import java.util.List;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.IDataBase;

/**
 * This class removes the methods and lines from the model as we will not need them further and they form a very large part of the model which hogs
 * memory.
 *
 * @author Michael Couck
 * @since 10.01.10
 * @version 01.00
 */
public class Pruner extends AProcess implements IConstants {

	/** The database to prune. */
	private IDataBase dataBase;

	/**
	 * Constructor takes the parent.
	 *
	 * @param parent
	 *            the parent process that will chain this process
	 */
	public Pruner(IProcess parent, IDataBase dataBase) {
		super(parent);
		this.dataBase = dataBase;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void execute() {
		super.execute();
		List<Package> packages = dataBase.find(Package.class);
		for (Package<?, ?> pakkage : packages.toArray(new Package[packages.size()])) {
			for (Class<?, ?> klass : pakkage.getChildren()) {
				for (Method<?, ?> method : klass.getChildren()) {
					for (Line<?, ?> line : method.getChildren()) {
						line.setParent(null);
					}
					method.setParent(null);
					method.setChildren(null);
				}
				klass.setAfferent(null);
				klass.setEfferent(null);
				// klass.setChildren(null);
			}
			pakkage.setAfferent(null);
			pakkage.setEfferent(null);
			pakkage.setParent(null);
		}
	}

}