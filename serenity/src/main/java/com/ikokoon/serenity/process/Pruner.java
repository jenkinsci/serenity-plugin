package com.ikokoon.serenity.process;

import java.util.List;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.IDataBase;

/**
 * This class removes the lines and the efferent and afferent from the model as we will not need them further and they form a very large part of the
 * model which hogs memory.
 *
 * @author Michael Couck
 * @since 10.01.10
 * @version 01.00
 */
public class Pruner extends AProcess implements IConstants {

	private Logger logger = Logger.getLogger(this.getClass());
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
		for (Package pakkage : packages) {
			logger.debug("Cleaning package : " + pakkage);
			pakkage.getAfferent().clear();
			pakkage.getEfferent().clear();
			List<Class> classes = pakkage.getChildren();
			for (Class klass : classes) {
				logger.debug("Cleaning class : " + klass);
				klass.getAfferent().clear();
				klass.getEfferent().clear();
				List<Method> methods = klass.getChildren();
				for (Method method : methods) {
					logger.debug("Cleaning method : " + method);
					method.getChildren().clear();
				}
			}
			dataBase.persist(pakkage);
		}

	}
}