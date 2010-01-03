package com.ikokoon.serenity.persistence;

import java.util.List;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.LoggingConfigurator;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.toolkit.Toolkit;

/**
 * Just some useful methods to dump the database and clean it.
 * 
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
public class DataBaseToolkit {

	static {
		LoggingConfigurator.configure();
	}
	private static Logger logger = Logger.getLogger(DataBaseToolkit.class);

	/**
	 * Clears the data in the database.
	 * 
	 * @param dataBase
	 *            the database to truncate
	 */
	@SuppressWarnings("unchecked")
	public static void clear(IDataBase dataBase) {
		Project<?, ?> project = (Project<?, ?>) dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		if (project != null) {
			dataBase.remove(Project.class, project.getId());
		}
		List<Package> packages = dataBase.find(Package.class);
		for (Composite<?, ?> composite : packages) {
			dataBase.remove(composite.getClass(), composite.getId());
		}
		List<Class> classes = dataBase.find(Class.class);
		for (Composite composite : classes) {
			dataBase.remove(composite.getClass(), composite.getId());
		}
		List<Method> methods = dataBase.find(Method.class);
		for (Composite composite : methods) {
			dataBase.remove(composite.getClass(), composite.getId());
		}
		List<Line> lines = dataBase.find(Line.class);
		for (Composite composite : lines) {
			dataBase.remove(composite.getClass(), composite.getId());
		}
	}

	/**
	 * Dumps the database to the output stream.
	 * 
	 * @param dataBase
	 *            the database to dump
	 * @param criteria
	 *            the criteria to match if the data for the composite must be written to the output
	 */
	@SuppressWarnings("unchecked")
	public static void dump(IDataBase dataBase, ICriteria criteria, String message) {
		if (message != null) {
			logger.warn(message);
		}
		try {
			Object object = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
			logger.info(object);
			Project<?, ?> project = (Project<?, ?>) dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
			if (project != null) {
				logger.warn("Project : " + project.getName());
			}
		} catch (Exception e) {
			logger.error("Exception dumping the data for the project object.", e);
		}
		try {
			List<Package> packages = dataBase.find(Package.class);
			for (Package<?, ?> pakkage : packages) {
				// logger.warn("\tPackage : " + pakkage.getId() + " : " + pakkage.getName() + " : " + pakkage.getCoverage());
				log(criteria, pakkage, 1, pakkage.getId() + " : " + pakkage.getName() + " : " + pakkage.getCoverage());
				for (Class<?, ?> klass : ((List<Class<?, ?>>) pakkage.getChildren())) {
					// logger.warn("\t\tClass : " + klass.getId() + " : " + klass.getName() + " : " + klass.getCoverage());
					log(criteria, klass, 2, " : " + klass.getId() + " : " + klass.getName() + " : " + klass.getCoverage());
					List<Efferent> efferents = klass.getEfferent();
					List<Afferent> afferents = klass.getAfferent();
					for (Efferent efferent : efferents) {
						// logger.warn("\t\t\t\t: " + efferent.getName());
						log(criteria, efferent, 4, efferent.getName());
					}
					for (Afferent afferent : afferents) {
						// logger.warn("\t\t\t\t: " + afferent.getName());
						log(criteria, afferent, 4, afferent.getName());
					}
					for (Method<?, ?> method : ((List<Method<?, ?>>) klass.getChildren())) {
						// logger.warn("\t\t\tMethod : " + method.getId() + " : " + method.getName() + " : " + method.getCoverage());
						log(criteria, method, 3, method.getId() + " : " + method.getName() + " : " + method.getCoverage());
						for (Line<?, ?> line : ((List<Line<?, ?>>) method.getChildren())) {
							// logger.warn("\t\t\t\tLine : " + line.getId() + " : " + line.getNumber() + " : " + line.getCounter());
							log(criteria, line, 4, line.getId() + " : " + line.getNumber() + " : " + method.getCoverage());
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception dumping the data for the database.", e);
		}
	}

	private static void log(ICriteria criteria, Composite<?, ?> composite, int tabs, String data) {
		if (criteria == null || (criteria != null && criteria.satisfied(composite))) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < tabs; i++) {
				builder.append("\t");
			}
			builder.append(composite.getClass().getSimpleName());
			builder.append(" : ");
			builder.append(data);
			logger.warn(builder.toString());
		}
	}

	public interface ICriteria {

		public boolean satisfied(Composite<?, ?> composite);

	}

	private static int counter;

	public static void main(String[] args) {
		// C:/Eclipse/workspace/serenity/work/jobs/Findbugs/builds/2009-12-12_21-08-50/serenity/serenity.odb
		// C:/Eclipse/workspace/Findbugs/serenity
		// C:/Eclipse/workspace/Search/modules/Ejb/serenity
		IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class,
				"C:/Eclipse/workspace/Search/modules/Ejb/serenity/serenity.odb", false, null);
		DataBaseToolkit.dump(dataBase, new ICriteria() {
			@SuppressWarnings("unchecked")
			public boolean satisfied(Composite<?, ?> composite) {
				if (composite instanceof Class && ((Class) composite).getName().startsWith("com.ikokoon.search.action.parse.ParserFactory")) {
					logger.warn("");
					return true;
				}
				return false;
			}
		}, null);
		logger.warn("Counter : " + counter);
		dataBase.close();
	}

}