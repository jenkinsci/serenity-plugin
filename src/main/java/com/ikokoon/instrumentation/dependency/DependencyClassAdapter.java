package com.ikokoon.instrumentation.dependency;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.instrumentation.Collector;
import com.ikokoon.toolkit.Toolkit;

/**
 * Dependency metrics consist of the following:<br>
 * 
 * 1) Afferent - the number of packages that rely on this package, i.e. how many times it is referenced by other packages<br>
 * 2) Efferent - the number of packages this package relies on, i.e. the opposite of afferent<br>
 * 3) Abstractness - the ratio of abstract to implementations in a package<br>
 * 4) Entropy - package A relies on package B. Then Package C is introduced and relies on A and B increasing the entropy<br>
 * 5) Stability - Ce / (Ca + Ce), efferent coupling divided by the afferent coupling plus the efferent coupling<br>
 * 6) Distance from main - find the stability distance of the package from the main which is (X=0,Y=1) to (X=1,Y=0) <br>
 * 
 * This class collects all the dependency data for a class while visiting the byte code.
 * 
 * @author Michael Couck
 * @since 18.07.09
 * @version 01.00
 */
public class DependencyClassAdapter extends ClassAdapter implements Opcodes {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(DependencyClassAdapter.class);
	/** The name of the class that is being instrumented. */
	private String className;
	/** The byte array of the byte code for the class being parsed.. */
	@SuppressWarnings("unused")
	private byte[] classfileBuffer;
	/** The source for the class if available. */
	private byte[] sourcefileBuffer;

	/**
	 * Constructor takes the parent visitor and the name of the class that will be analysed for dependency.
	 * 
	 * @param visitor
	 *            the parent visitor for the class
	 * @param className
	 *            the name of the class to be analysed
	 */
	public DependencyClassAdapter(ClassVisitor visitor, String className, byte[] classfileBuffer, byte[] sourcefileBuffer) {
		super(visitor);
		this.className = className;
		this.classfileBuffer = classfileBuffer;
		this.sourcefileBuffer = sourcefileBuffer;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
		if (logger.isDebugEnabled()) {
			logger.debug("visit : " + version + ", " + access + ", " + className + ", " + signature + ", " + superName);
			if (interfaces != null) {
				logger.debug(Arrays.asList(interfaces).toString());
			}
		}
		Collector.collectMetrics(className, superName);
		Collector.collectMetrics(className, interfaces);
		Collector.collectMetrics(className, access);
		super.visit(version, access, className, signature, superName, interfaces);
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitAnnotation : " + desc + ", " + visible);
		}
		String[] annotationClasses = Toolkit.byteCodeSignatureToClassNameArray(desc);
		Collector.collectMetrics(className, annotationClasses);
		return super.visitAnnotation(desc, visible);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitAttribute(Attribute attr) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitAttribute : " + attr);
		}
		// TODO - what is an attribute?
		super.visitAttribute(attr);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitEnd() {
		logger.debug("visitEnd : ");
		super.visitEnd();
	}

	/**
	 * {@inheritDoc}
	 */
	public FieldVisitor visitField(int access, String fieldName, String desc, String signature, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitField : " + access + ", " + fieldName + ", " + desc + ", " + signature + ", " + value);
		}
		String[] fieldClasses = Toolkit.byteCodeSignatureToClassNameArray(desc);
		Collector.collectMetrics(className, fieldClasses);
		fieldClasses = Toolkit.byteCodeSignatureToClassNameArray(signature);
		Collector.collectMetrics(className, fieldClasses);
		return super.visitField(access, fieldName, desc, signature, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitInnerClass : " + name + ", " + outerName + ", " + innerName);
		}
		super.visitInnerClass(name, outerName, innerName, access);
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitMethod : " + access + ", " + name + ", " + desc + ", " + signature);
			if (exceptions != null) {
				logger.debug(Arrays.asList(exceptions).toString());
			}
		}
		MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
		MethodAdapter methodAdapter = new DependencyMethodAdapter(methodVisitor, className, name, desc);
		return methodAdapter;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitOuterClass(String owner, String methodName, String desc) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitOuterClass : " + owner + ", " + methodName + ", " + desc);
		}
		String[] outerClassClasses = Toolkit.byteCodeSignatureToClassNameArray(desc);
		Collector.collectMetrics(owner, outerClassClasses);
		if (methodName != null) {
			Collector.collectMetrics(methodName, outerClassClasses);
		}
		super.visitOuterClass(owner, methodName, desc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitSource(String source, String debug) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitSource : " + source + ", " + debug);
		}
		// ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(classfileBuffer);
		// DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sourcefileBuffer);
		Collector.collectSource(className, byteArrayInputStream.toString());
		super.visitSource(source, debug);
	}

}