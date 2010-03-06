package com.ikokoon.serenity.process;

import java.util.List;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
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
		List<Line> lines = dataBase.find(Line.class);
		for (Line line : lines) {
			dataBase.remove(Line.class, line.getId());
		}
		List<Efferent> efferents = dataBase.find(Efferent.class);
		for (Efferent efferent : efferents) {
			dataBase.remove(Efferent.class, efferent.getId());
		}
		List<Afferent> afferents = dataBase.find(Afferent.class);
		for (Afferent afferent : afferents) {
			dataBase.remove(Afferent.class, afferent.getId());
		}
	}
}