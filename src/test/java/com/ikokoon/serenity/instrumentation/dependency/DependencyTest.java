package com.ikokoon.serenity.instrumentation.dependency;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.target.TargetAccess;
import com.ikokoon.target.consumer.Annotation;
import com.ikokoon.target.consumer.TargetConsumer;
import com.ikokoon.toolkit.Toolkit;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DependencyTest extends ATest {

    // private IDataBase dataBase;

    @Before
    public void clear() {
        // dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, mockInternalDataBase);
        // DataBaseToolkit.clear(dataBase);
        // Collector.initialize(dataBase);
    }

    @Test
    public void visitInner() throws Exception {
        // LOGGER.info("***************************************************");
        // visitClass(DependencyClassAdapter.class, Discovery.class.getName());
        // LOGGER.info("***************************************************");
        // visitClass(DependencyClassAdapter.class, Discovery.InnerClass.class.getName());
        // LOGGER.info("***************************************************");
        // visitClass(DependencyClassAdapter.class, Discovery.InnerClass.InnerInnerClass.class.getName());
    }

    @Test
    public void visit() throws Exception {
        // DataBaseToolkit.clear(dataBase);
        Configuration.getConfiguration().includedPackages.add(className);
        Configuration.getConfiguration().includedPackages.add(packageName);
        Configuration.getConfiguration().includedPackages.add(Logger.class.getPackage().getName());

        visitClass(DependencyClassAdapter.class, className);

        Package<?, ?> pakkage = (Package<?, ?>) dataBase.find(Package.class, Toolkit.hash(packageName));
        assertNotNull(pakkage);
        Class<?, ?> klass = (Class<?, ?>) dataBase.find(Class.class, Toolkit.hash(className));
        assertNotNull(klass);

        List<Afferent> afferent = klass.getAfferent();
        List<Efferent> efferent = klass.getEfferent();
        assertTrue(containsEfferentPackage(efferent, Logger.class.getPackage().getName()));
        assertTrue(containsEfferentPackage(efferent, Serializable.class.getPackage().getName()));
        assertTrue(containsEfferentPackage(efferent, Annotation.class.getPackage().getName()));

        visitClass(DependencyClassAdapter.class, TargetConsumer.class.getName());
        assertTrue(containsEfferentPackage(efferent, TargetConsumer.class.getPackage().getName()));

        dataBase.remove(Package.class, Toolkit.hash(packageName));

        // Test that Annotation has a reference to AnnotationAnnotation and visa versa
        // Test for field annotations and method annotations
        // Test for cyclic references?
    }

    @Test
    public void access() {
        String className = TargetAccess.class.getName();
        visitClass(DependencyClassAdapter.class, className);
        Class<?, ?> klass = (Class<?, ?>) dataBase.find(Class.class, Toolkit.hash(className));
        assertNotNull(klass);

        List<Class<?, ?>> innerClasses = klass.getInnerClasses();
        for (Class<?, ?> innerClass : innerClasses) {
            LOGGER.warning("Inner class : " + innerClass.getName() + ", " + innerClass.getAccess());
        }

        List<Method<?, ?>> methods = klass.getChildren();
        for (Method<?, ?> method : methods) {
            LOGGER.warning("Method : " + method.getName() + ", " + method.getAccess());
        }

        // printOpCodes();
    }

    @SuppressWarnings("unused")
    protected void printOpCodes() {
        // <j:set var="publicAccess" value="1" />
        Field[] fields = Opcodes.class.getDeclaredFields();
        Opcodes opcodes = new Opcodes() {
        };
        for (Field field : fields) {
            StringBuilder builder = new StringBuilder("<j:set var=\"");
            builder.append(field.getName());
            builder.append("\" value=\"");
            try {
                field.setAccessible(true);
                builder.append(field.get(opcodes));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "", e);
            }
            builder.append("\" />");

            System.out.println(builder);
        }
    }

    @SuppressWarnings("unused")
    private boolean containsAfferentPackage(List<Afferent> afferent, String name) {
        for (final Afferent aff : afferent) {
            LOGGER.fine("Afferent : " + aff);
            if (aff.getName().contains(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsEfferentPackage(List<Efferent> efferent, String name) {
        for (final Efferent eff : efferent) {
            if (eff.getName().contains(name)) {
                return true;
            }
        }
        return false;
    }

}