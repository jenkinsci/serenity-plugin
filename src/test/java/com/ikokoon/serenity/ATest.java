package com.ikokoon.serenity;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.objectweb.asm.Type;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.serenity.instrumentation.complexity.ComplexityClassAdapter;
import com.ikokoon.serenity.instrumentation.coverage.CoverageClassAdapter;
import com.ikokoon.serenity.instrumentation.dependency.DependencyClassAdapter;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.target.Target;
import com.ikokoon.toolkit.Toolkit;

/**
 * Base class for the tests.
 * 
 * @author Michael Couck
 * @since 30.07.09
 * @version 01.00
 */
public abstract class ATest implements IConstants {

	protected static Logger logger;

	protected IDataBase dataBase;

	protected String packageName = Target.class.getPackage().getName();
	protected String className = Target.class.getName();
	protected String methodName = "complexMethod";
	protected Type stringType = Type.getType(String.class);
	protected Type integerType = Type.getType(Integer.class);
	protected Type[] types = new Type[] { stringType, stringType, stringType, integerType, integerType };
	protected String methodSignature = Type.getMethodDescriptor(Type.VOID_TYPE, types);
	protected double lineNumber = 70;
	protected double complexity = 10d;
	protected int access = 1537;

	protected String efferentName = "efferentName";
	protected String afferentName = "afferentName";

	@BeforeClass
	public static void setup() {
		LoggingConfigurator.configure();
		logger = Logger.getLogger(ATest.class);
		StringBuilder builder = new StringBuilder(CoverageClassAdapter.class.getName());
		builder.append(";");
		builder.append(DependencyClassAdapter.class.getName());
		builder.append(";");
		builder.append(ComplexityClassAdapter.class.getName());
		System.setProperty(IConstants.INCLUDED_ADAPTERS_PROPERTY, builder.toString());
		Configuration.getConfiguration().includedPackages.add(IConstants.class.getPackage().getName());
		Configuration.getConfiguration().includedPackages.add(Transformer.class.getPackage().getName());
	}

	@Before
	public void initilize() {
		if (dataBase == null) {
			dataBase = IDataBase.DataBaseManager.getDataBase(IConstants.DATABASE_FILE, true);
		}
	}

	protected void visitClass(java.lang.Class<?> visitorClass, String className) {
		byte[] classBytes = getClassBytes(className);
		byte[] sourceBytes = getSourceBytes(className);
		visitClass(visitorClass, classBytes, sourceBytes);
	}

	@SuppressWarnings("unchecked")
	protected void visitClass(java.lang.Class<?> visitorClass, byte[] classBytes, byte[] sourceBytes) {
		VisitorFactory.getClassVisitor(new java.lang.Class[] { visitorClass }, className, classBytes, sourceBytes);
	}

	protected byte[] getClassBytes(String className) {
		String classPath = Toolkit.dotToSlash(className) + ".class";
		InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(classPath);
		byte[] classBytes = Toolkit.getContents(inputStream).toByteArray();
		return classBytes;
	}

	protected byte[] getSourceBytes(String className) {
		String classPath = Toolkit.dotToSlash(className) + ".java";
		InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(classPath);
		byte[] sourceBytes = Toolkit.getContents(inputStream).toByteArray();
		return sourceBytes;
	}

	@SuppressWarnings("unchecked")
	protected Package<?, ?> getPackage() {
		Package pakkage = new Package();
		pakkage.setAbstractness(1d);
		pakkage.setAfference(1d);
		pakkage.setChildren(new ArrayList<Class>());
		pakkage.setComplexity(1d);
		pakkage.setCoverage(1d);
		pakkage.setDistance(1d);
		pakkage.setEfference(1d);
		pakkage.setImplement(1d);
		pakkage.setInterfaces(1d);
		pakkage.setName(packageName);
		pakkage.setStability(1d);
		getClass(pakkage);
		return pakkage;
	}

	@SuppressWarnings("unchecked")
	protected Class<?, ?> getClass(Package<?, ?> pakkage) {
		Class klass = new Class();
		klass.setParent(pakkage);
		pakkage.getChildren().add(klass);
		klass.setAfference(1d);

		klass.setComplexity(1d);
		klass.setCoverage(1d);
		klass.setEfference(1d);

		Efferent efferent = new Efferent();
		efferent.setName(efferentName);
		klass.getEfferent().add(efferent);

		Afferent afferent = new Afferent();
		afferent.setName(afferentName);
		klass.getAfferent().add(afferent);

		klass.setInterfaze(true);
		klass.setName(className);
		klass.setStability(1d);
		getMethod(klass);
		return klass;
	}

	@SuppressWarnings("unchecked")
	protected Method<?, ?> getMethod(Class<?, ?> klass) {
		Method method = new Method();
		method.setParent(klass);
		method.setClassName(klass.getName());
		klass.getChildren().add(method);
		method.setComplexity(1d);
		method.setCoverage(1d);
		method.setDescription(methodSignature);
		method.setLines(1d);
		method.setName(methodName);
		getLine(method);
		return method;
	}

	@SuppressWarnings("unchecked")
	protected Line<?, ?> getLine(Method<?, ?> method) {
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
