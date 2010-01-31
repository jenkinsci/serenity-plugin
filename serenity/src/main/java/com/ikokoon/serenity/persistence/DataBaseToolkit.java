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
				log(criteria, pakkage, 1, pakkage.getId() + " : " + pakkage.getName() + ", coverage : " + pakkage.getCoverage() + ", complexity : "
						+ pakkage.getComplexity() + ", stability : " + pakkage.getStability());
				for (Class<?, ?> klass : ((List<Class<?, ?>>) pakkage.getChildren())) {
					log(criteria, klass, 2, " : id : " + klass.getId() + " : name : " + klass.getName() + " : coverage : " + klass.getCoverage()
							+ ", complexity : " + klass.getComplexity() + ", outer class : " + klass.getOuterClass() + ", outer method : "
							+ klass.getOuterMethod() + ", lines : " + klass.getChildren().size() + ", inner classes : " + klass.getInnerClasses());
					List<Efferent> efferents = klass.getEfferent();
					List<Afferent> afferents = klass.getAfferent();
					for (Efferent efferent : efferents) {
						log(criteria, efferent, 4, efferent.getName());
					}
					for (Afferent afferent : afferents) {
						log(criteria, afferent, 4, afferent.getName());
					}
					for (Method<?, ?> method : ((List<Method<?, ?>>) klass.getChildren())) {
						log(criteria, method, 3, method.getId() + " : name : " + method.getName() + " : coverage : " + method.getCoverage()
								+ ", complexity : " + method.getComplexity());
						for (Line<?, ?> line : ((List<Line<?, ?>>) method.getChildren())) {
							log(criteria, line, 4, line.getId() + " : number : " + line.getNumber() + ", counter : " + line.getCounter());
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

	public static void main(String[] args) {
		// C:/Eclipse/workspace/serenity/work/jobs/Findbugs/builds/2009-12-12_21-08-50/serenity/serenity.odb
		// C:/Eclipse/workspace/Findbugs/serenity
		// C:/Eclipse/workspace/Search/modules/Ejb/serenity
		// C:/Eclipse/workspace/Discovery/serenity
		IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, "C:/Eclipse/workspace/Discovery/serenity/serenity.odb", false,
				null);
		DataBaseToolkit.dump(dataBase, new ICriteria() {
			public boolean satisfied(Composite<?, ?> composite) {
				return true;
			}
		}, "Data base toolkit dump : ");
		Project<?, ?> project = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		logger.info("Project : " + project.getCoverage());
		dataBase.close();
	}

}