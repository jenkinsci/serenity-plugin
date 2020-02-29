package com.ikokoon.serenity.instrumentation;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.instrumentation.profiling.ProfilingClassAdapter;
import com.ikokoon.serenity.instrumentation.profiling.ProfilingClassAdviceAdapter;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.toolkit.Toolkit;
import org.junit.Test;
import org.mockito.asm.Type;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class InstrumentationTest extends ATest {

    interface Interface {
        void method();
    }

    @Test(expected = RuntimeException.class)
    public void checkNoProfilingInstructions() throws IOException {
        byte[] classBytes = getClassBytes(this.className);
        byte[] sourceBytes = getSourceBytes(this.className);
        // Verify that the profiling instructions are not in the byte code
        ByteArrayOutputStream source = new ByteArrayOutputStream();
        source.write(sourceBytes);
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new InstrumentationClassAdapterChecker(writer, IConstants.COLLECT_START, IConstants.PROFILING_METHOD_DESCRIPTION);
        LOGGER.fine("Adding class visitor : " + visitor);
        reader.accept(visitor, 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkProfilingAdviceInstructions() throws Exception {
        byte[] classBytes = getClassBytes(this.className);
        byte[] sourceBytes = getSourceBytes(this.className);

        // Add the profiling instructions
        ByteArrayOutputStream source = new ByteArrayOutputStream();
        source.write(sourceBytes);
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ProfilingClassAdviceAdapter profilingClassAdviceAdapter = new ProfilingClassAdviceAdapter(writer, className);

        reader.accept(profilingClassAdviceAdapter, ClassReader.EXPAND_FRAMES);
        classBytes = writer.toByteArray();

        String classPath = Toolkit.dotToSlash(this.className);
        File file = new File("./" + classPath + ".class");
        if (!file.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
        }
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        Toolkit.setContents(file, classBytes);
        LOGGER.warning("Wrote class to : " + file.getAbsolutePath());

        // Verify that the profiling instructions are in the byte code
        reader = new ClassReader(classBytes);
        writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        InstrumentationClassAdapterChecker instrumentationClassAdapterChecker = new InstrumentationClassAdapterChecker(writer,
                IConstants.COLLECT_START, IConstants.PROFILING_METHOD_DESCRIPTION);
        reader.accept(instrumentationClassAdapterChecker, ClassReader.EXPAND_FRAMES);

        final byte[] finalClassBytes = classBytes;
        final String finalClassName = this.className;
        // Call methods on the target and verify that the various methods
        // were called in the correct order including the wait, join and so on
        ClassLoader loader = new ClassLoader() {
            public java.lang.Class<?> loadClass(String className) throws ClassNotFoundException {
                if (className.equals(finalClassName)) {
                    return this.defineClass(className, finalClassBytes, 0, finalClassBytes.length);
                }
                return super.loadClass(className);
            }
        };

        String joinMethodName = "join";
        String joinMethodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[]{Type.getType(Long.class), Type.getType(Integer.class)});

        Thread.currentThread().setContextClassLoader(loader);
        Object target = loader.loadClass(className).newInstance();

        Long joinTime = 1000l;
        Toolkit.executeMethod(target, joinMethodName, new Object[]{joinTime, 0});
        DataBaseToolkit.dump(dataBase, null, "Dump after profiling");

        Method<Class<?, ?>, Line<?, ?>> method = dataBase.find(Method.class, Arrays.asList(className, joinMethodName, joinMethodDescription));
        LOGGER.warning("Method : " + method);
        assertNotNull(method);

        LOGGER.warning("Invocations : " + method.getInvocations());

        LOGGER.warning("Start time : " + method.getStartTime());
        LOGGER.warning("End time   : " + method.getEndTime());

        LOGGER.warning("Start wait : " + method.getStartWait());
        LOGGER.warning("End wait   : " + method.getEndWait());
        LOGGER.warning("Wait time  : " + method.getWaitTime());

        LOGGER.warning("Net time : " + method.getNetTime());
        LOGGER.warning("Total time : " + method.getTotalTime());

        assertTrue(method.getInvocations() > 0);
        assertTrue(method.getStartTime() > 0);
        assertTrue(method.getEndTime() > 0);
        assertTrue(method.getStartWait() > 0);
        assertTrue(method.getEndWait() > 0);
        assertTrue(method.getWaitTime() > 0);
        // assertTrue(method.getNetTime() > 0);
        assertTrue(method.getTotalTime() > 0);
    }

    @Test(expected = RuntimeException.class)
    public void interfaceTest() throws Exception {
        String className = Interface.class.getName();
        byte[] classBytes = getClassBytes(className);
        byte[] sourceBytes = getSourceBytes(className);

        // Add the profiling instructions
        ByteArrayOutputStream source = new ByteArrayOutputStream();
        source.write(sourceBytes);
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ProfilingClassAdviceAdapter profilingClassAdviceAdapter = new ProfilingClassAdviceAdapter(writer, className);

        reader.accept(profilingClassAdviceAdapter, 0);
        classBytes = writer.toByteArray();

        String classPath = Toolkit.dotToSlash(className);
        File file = new File("./" + classPath + ".class");
        if (!file.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
        }
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        Toolkit.setContents(file, classBytes);
        LOGGER.warning("Wrote class to : " + file.getAbsolutePath());

        // Verify that the profiling instructions are not in the byte code
        reader = new ClassReader(classBytes);
        writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        InstrumentationClassAdapterChecker instrumentationClassAdapterChecker = new InstrumentationClassAdapterChecker(writer,
                IConstants.COLLECT_START, IConstants.PROFILING_METHOD_DESCRIPTION);
        reader.accept(instrumentationClassAdapterChecker, 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkProfilingInstructions() throws Exception {
        byte[] classBytes = getClassBytes(this.className);
        byte[] sourceBytes = getSourceBytes(this.className);

        // Add the profiling instructions
        ByteArrayOutputStream source = new ByteArrayOutputStream();
        source.write(sourceBytes);
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ProfilingClassAdapter profilingClassAdapter = new ProfilingClassAdapter(writer, className);

        reader.accept(profilingClassAdapter, 0);
        classBytes = writer.toByteArray();

        String classPath = Toolkit.dotToSlash(this.className);
        File file = new File("./" + classPath + ".class");
        if (!file.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
        }
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        Toolkit.setContents(file, classBytes);
        LOGGER.warning("Wrote class to : " + file.getAbsolutePath());

        // Verify that the profiling instructions are not in the byte code
        reader = new ClassReader(classBytes);
        writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        // ClassVisitor classVisitor, Class<?> methodVisitorClass, String collectorMethodName, String collectorMethodDescription
        InstrumentationClassAdapterChecker instrumentationClassAdapterChecker = new InstrumentationClassAdapterChecker(writer,
                IConstants.COLLECT_START, IConstants.PROFILING_METHOD_DESCRIPTION);
        reader.accept(instrumentationClassAdapterChecker, 0);

        final byte[] finalClassBytes = classBytes;
        final String finalClassName = this.className;
        // Call the complex method on the target and verify that the Collector was called with the new instructions
        ClassLoader loader = new ClassLoader() {
            public java.lang.Class<?> loadClass(String className) throws ClassNotFoundException {
                if (className.equals(finalClassName)) {
                    return this.defineClass(className, finalClassBytes, 0, finalClassBytes.length);
                }
                return super.loadClass(className);
            }
        };
        Thread.currentThread().setContextClassLoader(loader);
        Object target = loader.loadClass(className).newInstance();
        Toolkit.executeMethod(target, methodName, new Object[]{"", "", "", 1, 2});
        DataBaseToolkit.dump(dataBase, null, "Dump after profiling");

        Method<Class<?, ?>, Line<?, ?>> method = dataBase.find(Method.class, Arrays.asList(className, methodName, methodDescription));
        LOGGER.warning("Method : " + method);
    }

}