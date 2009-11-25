package com.ikokoon.serenity.instrumentation;

import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import com.ikokoon.serenity.instrumentation.dependency.DependencyAnnotationAdapter;
import com.ikokoon.serenity.instrumentation.dependency.DependencyFieldAdapter;
import com.ikokoon.serenity.instrumentation.dependency.DependencySignatureAdapter;
import com.ikokoon.toolkit.ObjectFactory;

public class VisitorFactory {

	private static Logger LOGGER = Logger.getLogger(VisitorFactory.class);

	public static ClassVisitor getClassVisitor(Class<ClassVisitor>[] classAdapterClasses, String className, byte[] classBytes, byte[] sourceBytes) {
		ClassReader reader = new ClassReader(classBytes);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
		ClassVisitor visitor = writer;
		for (Class<ClassVisitor> klass : classAdapterClasses) {
			Object[] parameters = new Object[] { visitor, className, classBytes, sourceBytes };
			visitor = ObjectFactory.getObject(klass, parameters);
			LOGGER.debug("Adding class visitor : " + visitor);
		}
		reader.accept(visitor, 0);
		return writer;
	}

	public static MethodVisitor getMethodVisitor(MethodVisitor visitor, Class<?> klass, String className, String name, String desc) {
		Object[] parameters = new Object[] { visitor, className, name, desc };
		MethodVisitor adapter = (MethodVisitor) ObjectFactory.getObject(klass, parameters);
		return adapter;
	}

	public static FieldVisitor getFieldVisitor(FieldVisitor visitor, String className, String description, String signature) {
		Object[] parameters = new Object[] { visitor, className, description, signature };
		FieldVisitor adapter = ObjectFactory.getObject(DependencyFieldAdapter.class, parameters);
		return adapter;
	}

	public static SignatureVisitor getSignatureVisitor(String className, String signature) {
		SignatureReader reader = new SignatureReader(signature);
		SignatureVisitor adapter = new DependencySignatureAdapter(className);
		reader.accept(adapter);
		return adapter;
	}

	public static AnnotationVisitor getAnnotationVisitor(AnnotationVisitor visitor, String className, String description) {
		AnnotationVisitor adapter = new DependencyAnnotationAdapter(visitor, className, description);
		return adapter;
	}

}
