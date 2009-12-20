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
import com.ikokoon.serenity.instrumentation.dependency.DependencySignatureAdapter;
import com.ikokoon.toolkit.ObjectFactory;

/**
 * This class instantiates visitors for classes, methods, field, signature and annotations.
 * 
 * @author Michael Couck
 * @since 09.12.09
 * @version 01.00
 */
public class VisitorFactory {

	private static Logger LOGGER = Logger.getLogger(VisitorFactory.class);

	/**
	 * Instantiates a chain of class visitors. Each visitor can modify or add code to the class as it is parsed and the writer will output the new
	 * class byte code.
	 * 
	 * @param classAdapterClasses
	 *            the class visitor classes
	 * @param className
	 *            the name of the class to be visited
	 * @param classBytes
	 *            the byte array of the byte code
	 * @param sourceBytes
	 *            the byte array of the source code for the class
	 * @return the class visitor/writer
	 */
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

	/**
	 * This method constructs a method visitor chain based on the class of the method adapter passed as a parameter.
	 * 
	 * @param visitor
	 *            the parent method visitor
	 * @param klass
	 *            the class of the method visitor to initialise
	 * @param className
	 *            the name of the class that the method visitor will visit
	 * @param name
	 *            the name of the method
	 * @param desc
	 *            the description or signature of the method in byte code format
	 * @return the method visitor
	 */
	public static MethodVisitor getMethodVisitor(MethodVisitor visitor, Class<?> klass, String className, String name, String desc) {
		Object[] parameters = new Object[] { visitor, className, name, desc };
		MethodVisitor adapter = (MethodVisitor) ObjectFactory.getObject(klass, parameters);
		return adapter;
	}

	/**
	 * This method constructs a field visitor chain that will visit the byte code for a field in a class.
	 * 
	 * @param visitor
	 *            the parent field visitor
	 * @param klass
	 *            the field visitor class type
	 * @param className
	 *            the name of the class the field is in
	 * @param description
	 *            the description of the field in byte code format
	 * @param signature
	 *            the signature of the field in byte code
	 * @return the field visitor
	 */
	public static FieldVisitor getFieldVisitor(FieldVisitor visitor, Class<?> klass, String className, String description, String signature) {
		Object[] parameters = new Object[] { visitor, className, description, signature };
		FieldVisitor adapter = (FieldVisitor) ObjectFactory.getObject(klass, parameters);
		return adapter;
	}

	/**
	 * This method constructs a signature visitor that will visit a signature for something, could be an annotation or a method.
	 * 
	 * @param className
	 *            the name of the class the signature is in
	 * @param signature
	 *            the signature in byte code to visit
	 * @return the signature to be visited
	 */
	public static SignatureVisitor getSignatureVisitor(String className, String signature) {
		SignatureReader reader = new SignatureReader(signature);
		SignatureVisitor adapter = new DependencySignatureAdapter(className);
		reader.accept(adapter);
		return adapter;
	}

	/**
	 * This method constructs an annotation visitor to visit annotations.
	 * 
	 * @param visitor
	 *            the parent annotation visitor
	 * @param className
	 *            the name of the class with the annotation
	 * @param description
	 *            the description or signature of the annotation
	 * @return the annotation visitor
	 */
	public static AnnotationVisitor getAnnotationVisitor(AnnotationVisitor visitor, String className, String description) {
		AnnotationVisitor adapter = new DependencyAnnotationAdapter(visitor, className, description);
		return adapter;
	}

}
