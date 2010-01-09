package com.ikokoon.serenity.instrumentation.dependency;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.serenity.Collector;
import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.toolkit.Toolkit;

/**
 * This is the entry point for parsing the byte code and collecting the dependency metrics for the class. This class also collects the Java source for
 * the class if it is available.
 * 
 * Dependency metrics consist of the following:<br>
 * 
 * 1) Afferent - the number of packages that rely on this package, i.e. how many times it is referenced by other packages, the number of packages that
 * this class affects<br>
 * 2) Efferent - the number of packages this package relies on, i.e. the opposite of afferent, the number of classes that this class is effected by<br>
 * 3) Abstractness - the ratio of abstract to implementations in a package<br>
 * 4) Entropy - package A relies on package B. Then Package C is introduced and relies on A and B increasing the entropy<br>
 * 5) Stability - Ce / (Ca + Ce), efferent coupling divided by the afferent coupling plus the efferent coupling<br>
 * 6) Distance from main - find the stability distance of the package from the main which is (X=0,Y=1) to (X=1,Y=0) <br>
 * 
 * @author Michael Couck
 * @since 18.07.09
 * @version 01.00
 */
public class DependencyClassAdapter extends ClassAdapter implements Opcodes {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(DependencyClassAdapter.class);
	/** The name of the class to collect dependency metrics on. */
	private String className;
	/** The source for the class if available. */
	private ByteArrayOutputStream source;

	/**
	 * Constructor initialises a {@link DependencyClassAdapter} and takes the parent visitor and the name of the class that will be analysed for
	 * dependency.
	 * 
	 * @param classVisitor
	 *            the parent visitor for the class
	 * @param className
	 *            the name of the class to be analysed
	 * @param source
	 *            the Java source
	 */
	public DependencyClassAdapter(ClassVisitor classVisitor, String className, ByteArrayOutputStream source) {
		super(classVisitor);
		this.className = Toolkit.slashToDot(className);
		this.source = source;
		logger.debug("Class name : " + className + ", source : " + source);
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
		String[] normedInterfaces = new String[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			String normedInterface = Toolkit.slashToDot(interfaces[i]);
			normedInterfaces[i] = normedInterface;
		}
		Collector.collectEfferentAndAfferent(Toolkit.slashToDot(className), Toolkit.slashToDot(superName));
		Collector.collectEfferentAndAfferent(Toolkit.slashToDot(className), normedInterfaces);
		Collector.collectInterface(Toolkit.slashToDot(className), access);
		super.visit(version, access, className, signature, superName, interfaces);
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitAnnotation : " + desc + ", " + visible);
		}
		AnnotationVisitor visitor = super.visitAnnotation(desc, visible);
		AnnotationVisitor adapter = VisitorFactory.getAnnotationVisitor(visitor, className, desc);
		return adapter;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitAttribute(Attribute attr) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitAttribute : " + attr);
		}
		// Attributes are code added that is not standard, and we are not really interested in it are we?
		super.visitAttribute(attr);
	}

	/**
	 * {@inheritDoc}
	 */
	public FieldVisitor visitField(int access, String fieldName, String desc, String signature, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitField : " + access + ", " + fieldName + ", " + desc + ", " + signature + ", " + value);
		}
		FieldVisitor visitor = super.visitField(access, fieldName, desc, signature, value);
		FieldVisitor adapter = VisitorFactory.getFieldVisitor(visitor, DependencyFieldAdapter.class, className, desc, signature);
		return adapter;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitInnerClass(String innerName, String outerName, String innerSimpleName, int access) {
		if (logger.isDebugEnabled()) {
			logger.info("visitInnerClass : inner name : " + innerName + ", outer name : " + outerName + ", inner simple name : " + innerSimpleName);
		}
		if (outerName != null) {
			Collector.collectInnerClass(Toolkit.slashToDot(innerName), Toolkit.slashToDot(outerName));
		}
		super.visitInnerClass(innerName, outerName, innerSimpleName, access);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitOuterClass(String outerName, String outerMethodName, String outerMethodDescription) {
		if (logger.isDebugEnabled()) {
			logger.info("visitOuterClass : class name : " + className + ", owner : " + outerName + ", method name : " + outerMethodName
					+ ", description : " + outerMethodDescription);
		}
		Collector.collectOuterClass(className, Toolkit.slashToDot(outerName), outerMethodName, outerMethodDescription);
		super.visitOuterClass(outerName, outerMethodName, outerMethodDescription);
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
		MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
		if (exceptions != null) {
			for (String exception : exceptions) {
				Collector.collectEfferentAndAfferent(className, Toolkit.slashToDot(exception));
			}
		}
		MethodVisitor adapter = VisitorFactory.getMethodVisitor(visitor, DependencyMethodAdapter.class, className, name, desc);
		return adapter;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitSource(String source, String debug) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitSource : " + source + ", " + debug);
		}
		if (this.source != null && this.source.size() > 0) {
			Collector.collectSource(className, this.source.toString());
		}
		super.visitSource(source, debug);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitEnd() {
		logger.debug("visitEnd : ");
		super.visitEnd();
	}

}