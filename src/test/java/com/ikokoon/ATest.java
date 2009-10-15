package com.ikokoon;

import java.net.URL;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;

import com.ikokoon.instrumentation.complexity.ComplexityClassAdapter;
import com.ikokoon.instrumentation.coverage.CoverageClassAdapter;
import com.ikokoon.instrumentation.dependency.DependencyClassAdapter;
import com.ikokoon.instrumentation.model.Afferent;
import com.ikokoon.instrumentation.model.Class;
import com.ikokoon.instrumentation.model.Efferent;
import com.ikokoon.instrumentation.model.Line;
import com.ikokoon.instrumentation.model.Method;
import com.ikokoon.instrumentation.model.Package;
import com.ikokoon.persistence.ADataBase;
import com.ikokoon.persistence.DataBaseXml;
import com.ikokoon.persistence.IDataBase;
import com.ikokoon.target.one.One;

/**
 * Base class for the tests.
 * 
 * @author Michael Couck
 * @since 30.07.09
 * @version 01.00
 */
public abstract class ATest implements IConstants {

	protected static Logger logger;

	protected ADataBase dataBase;

	protected String packageName = One.class.getPackage().getName();
	protected String className = One.class.getName();
	protected String methodName = "complexMethod";
	protected String methodDescription = "methodDescription";
	protected double lineNumber = 70;
	protected double complexity = 10d;
	protected int access = 1537;

	protected String efferentName = "efferentName";
	protected String afferentName = "afferentName";

	@BeforeClass
	public static void setup() {
		URL url = ATest.class.getResource(LOG_4_J_PROPERTIES);
		if (url != null) {
			PropertyConfigurator.configure(url);
		}
		logger = Logger.getLogger(ATest.class);
		logger.info("Loaded logging properties from : " + url);
		StringBuilder builder = new StringBuilder(CoverageClassAdapter.class.getName());
		builder.append(";");
		builder.append(DependencyClassAdapter.class.getName());
		builder.append(";");
		builder.append(ComplexityClassAdapter.class.getName());
		System.setProperty(IConstants.INCLUDED_ADAPTERS_PROPERTY, builder.toString());
	}

	@Before
	public void initilize() {
		dataBase = (ADataBase) IDataBase.DataBase.getDataBase();
		delete(Efferent.class);
		delete(Afferent.class);
		delete(Line.class);
		delete(Method.class);
		delete(Class.class);
		delete(Package.class);
	}

	protected <T> void delete(java.lang.Class<T> klass) {
		if (dataBase instanceof DataBaseXml) {
			List<T> objects = dataBase.find(klass, 0, Integer.MAX_VALUE);
			for (T t : objects) {
				if (t != null) {
					Long id = (Long) dataBase.getId(klass, t);
					if (id != null) {
						dataBase.remove(klass, id);
					}
				}
			}
		}
		// if (dataBase instanceof DataBaseDb4o) {
		// try {
		// Query query = ((DataBaseDb4o) dataBase).objectContainer.query();
		// query.constrain(klass);
		// List list = query.execute();
		// for (Object object : list) {
		// ((DataBaseDb4o) dataBase).objectContainer.delete(object);
		// }
		// } catch (Exception e) {
		// logger.warn("Exception deleting the data", e);
		// }
		// }
		// if (dataBase instanceof DataBaseJpa) {
		// try {
		// dataBase.execute("delete from " + klass.getSimpleName() + " a");
		// } catch (Exception e) {
		// logger.warn("Exception deleting the data", e);
		// }
		// }
		// if (dataBase instanceof DataBaseNeodatis) {
		// List<T> objects = dataBase.find(klass, 0, Integer.MAX_VALUE);
		// for (T t : objects) {
		// if (t != null) {
		// Long id = (Long) dataBase.getId(klass, t);
		// if (id != null) {
		// dataBase.remove(klass, id);
		// }
		// }
		// }
		// }
	}

	protected void dumpData(java.lang.Class klass) {
		logger.error("Dumping data : ");
		try {
			List<Object> objects = dataBase.find(klass, 0, Integer.MAX_VALUE);
			for (Object object : objects) {
				logger.error(klass.getSimpleName() + " : " + object);
			}
		} catch (Exception e) {
			logger.error("Exception dumping data", e);
		}
	}

	protected Package getPackage() {
		Package pakkage = new Package();
		pakkage.setAbstractness(1d);
		pakkage.setAfferent(1d);
		pakkage.setChildren(new TreeSet<Class>());
		pakkage.setComplexity(1d);
		pakkage.setCoverage(1d);
		pakkage.setDistance(1d);
		pakkage.setEfferent(1d);
		pakkage.setImplementations(1d);
		pakkage.setInterfaces(1d);
		pakkage.setName(packageName);
		pakkage.setStability(1d);
		getClass(pakkage);
		return pakkage;
	}

	protected Class getClass(Package pakkage) {
		Class klass = new Class();
		klass.setParent(pakkage);
		pakkage.getChildren().add(klass);
		klass.setAfferent(1d);

		klass.setComplexity(1d);
		klass.setCoverage(1d);
		klass.setEfferent(1d);

		Efferent efferent = new Efferent();
		efferent.setName(efferentName);
		klass.getEfferentPackages().add(efferent);
		efferent.getBases().add(klass);

		Afferent afferent = new Afferent();
		afferent.setName(afferentName);
		klass.getAfferentPackages().add(afferent);
		afferent.getBases().add(klass);

		klass.setInterfaze(true);
		klass.setName(className);
		klass.setStability(1d);
		getMethod(klass);
		return klass;
	}

	protected Method getMethod(Class klass) {
		Method method = new Method();
		method.setParent(klass);
		method.setClassName(klass.getName());
		klass.getChildren().add(method);
		method.setComplexity(1d);
		method.setCoverage(1d);
		method.setDescription(methodDescription);
		method.setLines(1d);
		method.setName(methodName);
		getLine(method);
		return method;
	}

	protected Line getLine(Method method) {
		Line line = new Line();
		line.setCounter(1d);
		line.setNumber(lineNumber);
		line.setParent(method);
		line.setMethodName(method.getName());
		line.setClassName(method.getClassName());
		method.getChildren().add(line);
		return line;
	}

}
