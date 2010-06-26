package com.ikokoon.serenity.persistence;

import java.util.List;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.model.Snapshot;
import com.ikokoon.toolkit.LoggingConfigurator;
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
	public static synchronized void clear(IDataBase dataBase) {
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
		List<Snapshot> snapshots = dataBase.find(Snapshot.class);
		for (Snapshot snapshot : snapshots) {
			dataBase.remove(Snapshot.class, snapshot.getId());
		}
	}

	@SuppressWarnings("unchecked")
	public static synchronized void copyDataBase(IDataBase sourceDataBase, IDataBase targetDataBase) {
		Collector.initialize(targetDataBase);
		List<Package> sourcePackages = sourceDataBase.find(Package.class);
		for (Package sourcePackage : sourcePackages) {
			List<Class> sourceClasses = sourcePackage.getChildren();
			for (Class sourceClass : sourceClasses) {
				Collector.collectAccess(sourceClass.getName(), sourceClass.getAccess());
				collectEfferentAndAfferent(sourceClass, sourcePackages);
				List<Class> sourceInnerClasses = sourceClass.getInnerClasses();
				for (Class sourceInnerClass : sourceInnerClasses) {
					Collector.collectInnerClass(sourceInnerClass.getName(), sourceClass.getName());
					Method sourceOuterMethod = sourceClass.getOuterMethod();
					if (sourceOuterMethod != null) {
						Collector.collectOuterClass(sourceInnerClass.getName(), sourceClass.getName(), sourceOuterMethod.getName(), sourceOuterMethod
								.getDescription());
					}
				}
				// Collector.collectSource(sourceClass.getName(), "source");
				List<Method> sourceMethods = sourceClass.getChildren();
				for (Method sourceMethod : sourceMethods) {
					Collector.collectComplexity(sourceClass.getName(), sourceMethod.getName(), sourceMethod.getDescription(), sourceMethod
							.getComplexity());
					Collector.collectAccess(sourceClass.getName(), sourceMethod.getName(), sourceMethod.getDescription(), sourceMethod.getAccess());
					List<Line> sourceLines = sourceMethod.getChildren();
					for (Line sourceLine : sourceLines) {
						Collector.collectLine(sourceClass.getName(), sourceMethod.getName(), sourceMethod.getDescription(), Integer
								.valueOf((int) sourceLine.getNumber()));
						for (int i = 0; i < sourceLine.getCounter(); i++) {
							Collector.collectCoverage(sourceClass.getName(), sourceMethod.getName(), sourceMethod.getDescription(), (int) sourceLine
									.getNumber());
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static synchronized void execute(IDataBase dataBase, Composite composite, Executer executer) {
		List list = dataBase.find(composite.getClass());
		for (Object object : list) {
			executer.execute(object);
		}
	}

	public interface Executer {
		public void execute(Object object);
	}

	@SuppressWarnings("unchecked")
	private static synchronized void collectEfferentAndAfferent(Class klass, List<Package> packages) {
		List<Efferent> efferents = klass.getEfferent();
		for (Efferent efferent : efferents) {
			String efferentPackage = Toolkit.replaceAll(efferent.getName(), "<e:", "");
			efferentPackage = Toolkit.replaceAll(efferent.getName(), ">", "");
			for (Package pakkage : packages) {
				List<Class> children = pakkage.getChildren();
				for (Class child : children) {
					List<Afferent> afferents = child.getAfferent();
					for (Afferent afferent : afferents) {
						String afferentPackage = Toolkit.replaceAll(afferent.getName(), "<a:", "");
						afferentPackage = Toolkit.replaceAll(afferent.getName(), ">", "");
						if (efferentPackage.equals(afferentPackage)) {
							Collector.collectEfferentAndAfferent(klass.getName(), child.getName());
						}
					}
				}
			}
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
	public static synchronized void dump(IDataBase dataBase, ICriteria criteria, String message) {
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
						log(criteria, method, 3, method.getId() + " : name : " + method.getName() + " : description : " + method.getDescription()
								+ " : coverage : " + method.getCoverage() + ", complexity : " + method.getComplexity() + ", start time : "
								+ method.getStartTime() + ", end time : " + method.getEndTime());
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

	private static synchronized void log(ICriteria criteria, Composite<?, ?> composite, int tabs, String data) {
		if (criteria == null || (criteria != null && criteria.satisfied(composite))) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < tabs; i++) {
				builder.append("    ");
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
		// D:/Eclipse/workspace/search/modules/Jar/serenity
		// D:/Eclipse/workspace/Discovery/modules/Jar/serenity
		IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class,
				"D:/Eclipse/workspace/search/modules/Jar/serenity/serenity.odb", null);
		DataBaseToolkit.dump(dataBase, null, "Database dump : ");
		dataBase.close();
	}

}