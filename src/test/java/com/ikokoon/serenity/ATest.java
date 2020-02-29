package com.ikokoon.serenity;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.target.Target;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

/**
 * Base class for the tests.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 30.07.09
 */
@SuppressWarnings("FieldCanBeLocal")
@Ignore
@RunWith(MockitoJUnitRunner.class)
public abstract class ATest implements IConstants {

    protected static Logger LOGGER;

    protected static IDataBase mockInternalDataBase = mock(IDataBase.class);
    protected static IDataBase dataBase;

    private Type stringType = Type.getType(String.class);
    private Type integerType = Type.getType(Integer.class);
    private Type[] types = new Type[]{stringType, stringType, stringType, integerType, integerType};

    protected String packageName = Target.class.getPackage().getName();
    protected String className = Target.class.getName();
    protected String methodName = "complexMethod";
    protected String methodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, types);
    protected double lineNumber = 70;
    double complexity = 10d;
    int access = 1537;

    private String efferentName = "efferentName";
    private String afferentName = "afferentName";

    @BeforeClass
    public static void beforeClass() {
        LoggingConfigurator.configure();
        LOGGER = Logger.getLogger(ATest.class.getName());
        System.setProperty(IConstants.INCLUDED_ADAPTERS_PROPERTY, "profiling;coverage;complexity;dependency");
        Configuration.getConfiguration().includedPackages.add(IConstants.class.getPackage().getName());
        Configuration.getConfiguration().includedPackages.add(Target.class.getPackage().getName());
        Configuration.getConfiguration().includedPackages.add(Configuration.class.getPackage().getName());
        Configuration.getConfiguration().includedPackages.add("com.ikokoon");
        Configuration.getConfiguration().excludedPackages.add(Object.class.getPackage().getName());

        dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, mockInternalDataBase);
        Collector.initialize(dataBase);
    }

    @AfterClass
    public static void afterClass() {
        dataBase.close();
    }

    protected void visitClass(java.lang.Class<?> visitorClass, String className) {
        byte[] classBytes = getClassBytes(className);
        byte[] sourceBytes = getSourceBytes(className);
        visitClass(visitorClass, className, classBytes, sourceBytes);
    }

    @SuppressWarnings("unchecked")
    protected ClassWriter visitClass(java.lang.Class<?> visitorClass, String className, byte[] classBytes, byte[] sourceBytes) {
        ByteArrayOutputStream source = new ByteArrayOutputStream();
        try {
            source.write(sourceBytes);
        } catch (final IOException e) {
            LOGGER.severe(e.getMessage());
        }
        return (ClassWriter) VisitorFactory.getClassVisitor(new java.lang.Class[]{visitorClass}, className, classBytes, source);
    }

    protected byte[] getClassBytes(String className) {
        String classPath = Toolkit.dotToSlash(className) + ".class";
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(classPath);
        return Toolkit.getContents(inputStream).toByteArray();
    }

    protected byte[] getSourceBytes(String className) {
        String classPath = Toolkit.dotToSlash(className) + ".java";
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(classPath);
        return Toolkit.getContents(inputStream).toByteArray();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Package<?, ?> getPackage() {
        Package pakkage = new Package();
        pakkage.setAbstractness(1d);
        pakkage.setAfference(1d);
        pakkage.setChildren(new ArrayList<Class>());
        pakkage.setComplexity(1d);
        pakkage.setCoverage(1d);
        pakkage.setDistance(1d);
        pakkage.setEfference(1d);
        pakkage.setImplementations(1d);
        pakkage.setInterfaces(1d);
        pakkage.setName(packageName);
        pakkage.setStability(1d);
        getClass(pakkage);
        return pakkage;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
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

        klass.setInterfaze(false);
        klass.setName(className);
        klass.setStability(1d);
        getMethod(klass);
        return klass;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Method<?, ?> getMethod(Class<?, ?> klass) {
        Method method = new Method();
        method.setParent(klass);
        method.setClassName(klass.getName());
        klass.getChildren().add(method);
        method.setComplexity(1d);
        method.setCoverage(1d);
        method.setDescription(methodDescription);
        method.setName(methodName);
        getLine(method);
        return method;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
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
