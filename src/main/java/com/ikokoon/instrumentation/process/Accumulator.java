package com.ikokoon.instrumentation.process;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassVisitor;

import com.ikokoon.instrumentation.Configuration;
import com.ikokoon.instrumentation.Transformer;
import com.ikokoon.instrumentation.complexity.ComplexityClassAdapter;
import com.ikokoon.instrumentation.dependency.DependencyClassAdapter;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class looks through the classpath and collects metrics on the classes that were not instanciated by the classloader.
 * 
 * @author Michael Couck
 * @since 24.07.09
 * @version 01.00
 */
public class Accumulator extends AProcess {

	private Logger logger = Logger.getLogger(Accumulator.class);
	/** The map of jars that are processed so we don't do the same jar more than once.. */
	private Map<String, Long> jars = new HashMap<String, Long>();
	/** The map of classes that are processed so we don't process the classes more than once. */
	private Map<Long, String> classes = new HashMap<Long, String>();

	/**
	 * {@inheritDoc}
	 */
	public Accumulator(IProcess parent) {
		super(parent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void execute() {
		String classpath = System.getProperty("java.class.path");
		String surefireClasspath = System.getProperty("surefire.test.class.path");
		if (surefireClasspath != null) {
			classpath += File.pathSeparator;
			classpath += surefireClasspath;
		}
		StringTokenizer stringTokenizer = new StringTokenizer(classpath, File.pathSeparator);
		while (stringTokenizer.hasMoreTokens()) {
			String token = stringTokenizer.nextToken();
			logger.debug("Processing jar : " + token);
			File file = new File(token);
			if (!file.exists() || !file.canRead()) {
				logger.warn("Can't read file : " + file);
				continue;
			}
			if (file.isFile()) {
				if (token.endsWith(".jar") || token.endsWith(".zip") || token.endsWith(".war") || token.endsWith(".ear")) {
					processJar(file);
				}
			} else if (file.isDirectory()) {
				processDir(file);
			}
		}
		super.execute();
	}

	/**
	 * Processes a directory on a file system, looks for class fiels and feeds the byte code into the adapter chain for collecting the metrics for the
	 * class.
	 * 
	 * @param file
	 *            the directory to look in for the class data
	 */
	private void processDir(File file) {
		// Iteratively go through the directories
		if (file == null || !file.exists() || !file.canWrite()) {
			return;
		}
		if (file.isDirectory()) {
			File files[] = file.listFiles();
			for (int j = 0; j < files.length; j++) {
				file = files[j];
				processDir(file);
			}
		} else if (file.isFile() && file.canRead()) {
			processClass(file.getAbsolutePath());
		}
	}

	/**
	 * Processes a class file in a zip or jar file, looks for class files and feeds the byte code into the adapter chain for collecting the metrics
	 * for the class.
	 * 
	 * @param entryName
	 *            the entry in the zip or jar file
	 */
	@SuppressWarnings("unchecked")
	private void processClass(String entryName) {
		// Don't process anything that is not a class file
		if (!entryName.endsWith(".class")) {
			logger.debug("Not processing file : " + entryName);
			return;
		}
		// Don't process the same class twice
		Long hash = Toolkit.hash(entryName);
		if (classes.get(hash) != null) {
			logger.debug("Already done class : " + entryName);
			return;
		}
		classes.put(hash, entryName);
		// Check that the class is included in the included packages
		if (!Configuration.getConfiguration().included(entryName)) {
			logger.debug("Class not included : " + entryName + " - not included");
			return;
		}
		// Don't do excluded classes and packages
		if (Configuration.getConfiguration().excluded(entryName)) {
			logger.debug("Excluded class : " + entryName);
			return;
		}
		logger.debug("Accumulating class : " + entryName);
		byte[] classfileBuffer = loadBytes(entryName);
		entryName = entryName.replaceAll(".class", "");
		logger.debug("Class name : " + entryName + ", length : " + classfileBuffer.length);
		try {
			Class<ClassVisitor>[] classAdapterClasses = new Class[] { DependencyClassAdapter.class, ComplexityClassAdapter.class };
			Transformer.INSTANCE.getVisitorChain(classfileBuffer, classAdapterClasses, entryName);
		} catch (Exception e) {
			logger.error("Exception generating complexity and dependency statistics on class " + entryName, e);
		}
	}

	/**
	 * Processes a jar or zip file or something like it, looks for class and feeds the byte code into the adapter chain for collecting the metrics for
	 * the class.
	 * 
	 * @param file
	 *            the file to look in for the class data
	 */
	private void processJar(File file) {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
		} catch (Exception e) {
			logger.error("Exeption accessing the jar : " + file, e);
			return;
		}
		// Don't process the jars more than once
		if (jars.containsKey(file.getName())) {
			Long length = jars.get(file.getName());
			if (length.equals(file.length())) {
				return;
			}
		}
		jars.put(file.getName(), file.length());
		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			JarEntry jarEntry = jarEntries.nextElement();
			String entryName = jarEntry.getName();
			processClass(entryName);
		}
	}

	/**
	 * Reads the bytes of the file specified and returns the array.
	 * 
	 * @param name
	 *            the name of the resource to read the byte data for
	 * @return the byte array from the file/resource
	 */
	public byte[] loadBytes(String name) {
		InputStream inputStream = ClassLoader.getSystemResourceAsStream(name);
		ByteArrayOutputStream byteArrayOutputStream = Toolkit.getContents(inputStream);
		return byteArrayOutputStream.toByteArray();
	}

}