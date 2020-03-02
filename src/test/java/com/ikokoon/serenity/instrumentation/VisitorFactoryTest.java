package com.ikokoon.serenity.instrumentation;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;

import com.ikokoon.serenity.instrumentation.profiling.ProfilingClassAdviceAdapter;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.instrumentation.complexity.ComplexityClassAdapter;
import com.ikokoon.serenity.instrumentation.complexity.ComplexityMethodAdapter;
import com.ikokoon.serenity.instrumentation.coverage.CoverageClassAdapter;
import com.ikokoon.serenity.instrumentation.dependency.DependencyClassAdapter;

public class VisitorFactoryTest extends ATest {

    // private IDataBase dataBase;

    @Before
    public void clear() {
        // dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, mockInternalDataBase);
        // DataBaseToolkit.clear(dataBase);
        // Collector.initialize(dataBase);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void getClassVisitor() throws Exception {
        byte[] classBytes = getClassBytes(className);
        byte[] sourceBytes = getSourceBytes(className);
        Class[] adapters = new Class[]{CoverageClassAdapter.class, ComplexityClassAdapter.class, DependencyClassAdapter.class,
                ProfilingClassAdviceAdapter.class};

        ByteArrayOutputStream source = new ByteArrayOutputStream();
        source.write(sourceBytes);
        ClassVisitor visitor = VisitorFactory.getClassVisitor(adapters, className, classBytes, source);
        assertNotNull(visitor);

        MethodVisitor methodVisitor = VisitorFactory.getMethodVisitor(new MethodVisitor(Opcodes.ASM5) {
                                                                          @Override
                                                                          public void visitParameter(String name, int access) {
                                                                              super.visitParameter(name, access);
                                                                          }
                                                                      }, ComplexityMethodAdapter.class, Opcodes.ACC_PUBLIC,
                className, methodName, "methodDescription");
        assertNotNull(methodVisitor);
    }

}
