package com.ikokoon.serenity.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 03.01.10
 * @version 01.00
 */
public class Consolidator extends AProcess implements IConstants {

	private IDataBase dataBase;

	/**
	 * Constructor takes the parent.
	 * 
	 * @param parent
	 *            the parent process that will chain this process
	 */
	public Consolidator(IProcess parent, IDataBase dataBase) {
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
			for (Class<?, ?> klass : pakkage.getChildren().toArray(new Class[pakkage.getChildren().size()])) {
				if (klass.getName().indexOf("$") > -1) {
					consolidate(klass);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void consolidate(Class<?, ?> klass) {
		logger.info("Class name : " + klass.getName());
		// Find the parent class
		String parentClassName = klass.getName().substring(0, klass.getName().indexOf("$"));
		logger.info("Parent class name : " + parentClassName);
		Class<?, ?> parent = dataBase.find(klass.getClass(), Toolkit.hash(parentClassName));
		logger.info("Parent : " + parent);
		if (parent == null) {
			return;
		}
		// Find the init method and add the lines to it
		Method<?, ?> init = getInit(parent);

		if (init == null) {
			// Interfaces don't have an init method so create one
			Collector.collectLines(parent.getName(), "init", "", 0);
			init = getInit(parent);
		}

		final String parentName = parent.getName();
		final String parentInitMethodname = init.getName();

		for (Method<?, ?> method : klass.getChildren()) {
			for (final Line<?, ?> line : method.getChildren()) {
				Collector.collectCoverage(parent.getName(), init.getName(), init.getDescription(), (int) line.getNumber());
				Line parentLine = dataBase.find(line.getClass(), new ArrayList() {
					{
						Collections.addAll(this, parentName, parentInitMethodname, line.getNumber());
					}
				});
				parentLine.setCounter(line.getCounter());
				logger.info("Collected line : " + parentLine);
			}
		}

		// Remove the inner class from the database as an individual class
		dataBase.remove(Class.class, klass.getId());
	}

	public Method<?, ?> getInit(Class<?, ?> klass) {
		if (klass.getChildren() == null) {
			return null;
		}
		for (Method<?, ?> method : klass.getChildren()) {
			if (method.getName().equals("init")) {
				return method;
			}
		}
		return null;
	}
}