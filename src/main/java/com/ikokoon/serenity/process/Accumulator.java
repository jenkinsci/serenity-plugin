package com.ikokoon.serenity.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.objectweb.asm.ClassVisitor;

import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class looks through the classpath and collects metrics on the classes that were not instanciated by the classloader during the unit tests and
 * creates a visitor chain for the class that will collect the complexity and dependency metrics for the class.
 * 
 * @author Michael Couck
 * @since 24.07.09
 * @version 01.00
 */
public class Accumulator extends AProcess {

	/** The set of jars that are processed so we don't do the same jar more than once. */
	private Set<String> jars = new TreeSet<String>();
	/** The set of classes that are processed so we don't process the files more than once. */
	private Set<String> files = new TreeSet<String>();
	/** The chain of adapters for analysing the classes. */
	private Class<ClassVisitor>[] CLASS_ADAPTER_CLASSES;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Accumulator(IProcess parent) {
		super(parent);
		CLASS_ADAPTER_CLASSES = Configuration.getConfiguration().classAdapters.toArray(new Class[Configuration.getConfiguration().classAdapters
				.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public void execute() {
		super.execute();
		String classpath = Configuration.getConfiguration().getClassPath();
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
	}

	/**
	 * Processes a directory on a file system, looks for class files and feeds the byte code into the adapter chain for collecting the metrics for the
	 * class.
	 * 
	 * @param file
	 *            the directory or file to look in for the class data
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
			String filePath = file.getAbsolutePath();
			filePath = Toolkit.slashToDot(filePath);
			if (excluded(filePath)) {
				return;
			}
			if (filePath.endsWith(".class")) {
				byte[] classBytes = Toolkit.getContents(file).toByteArray();
				byte[] sourceBytes = null;

				String classFileName = file.getName();
				String javaFileName = classFileName.substring(0, classFileName.lastIndexOf('.')) + ".java";
				File javaFile = new File(file.getParent(), javaFileName);
				if (javaFile.exists() && javaFile.isFile() && javaFile.canRead()) {
					sourceBytes = Toolkit.getContents(javaFile).toByteArray();
				}
				processClass(file.getAbsolutePath(), classBytes, sourceBytes);
			}
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
		// Don't process the jars more than once
		if (jars.contains(file.getName())) {
			return;
		}
		jars.add(file.getName());

		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
		} catch (Exception e) {
			logger.error("Exeption accessing the jar : " + file, e);
			return;
		}
		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			JarEntry jarEntry = jarEntries.nextElement();
			String entryName = jarEntry.getName();
			if (excluded(Toolkit.slashToDot(entryName))) {
				continue;
			}
			logger.debug("Processsing file : " + entryName);
			try {
				if (entryName.endsWith(".class")) {
					InputStream inputStream = jarFile.getInputStream(jarEntry);
					byte[] classFileBytes = Toolkit.getContents(inputStream).toByteArray();

					// Look for the source
					String javaEntryName = entryName.substring(0, entryName.lastIndexOf('.')) + ".java";
					logger.debug("Looking for source : " + javaEntryName + ", " + entryName);
					ZipEntry javaEntry = jarFile.getEntry(javaEntryName);
					byte[] sourceFileBytes = new byte[0];
					if (javaEntry != null) {
						inputStream = jarFile.getInputStream(javaEntry);
						sourceFileBytes = Toolkit.getContents(inputStream).toByteArray();
					}
					processClass(Toolkit.slashToDot(entryName), classFileBytes, sourceFileBytes);
				}
			} catch (IOException e) {
				logger.error("Exception reading entry : " + jarEntry + ", from file : " + jarFile, e);
			}
		}
	}

	private void processClass(String name, byte[] classBytes, byte[] sourceBytes) {
		if (name != null && name.endsWith(".class")) {
			name = name.substring(0, name.lastIndexOf('.'));
		}
		logger.debug("Class name : " + name + ", length : " + classBytes.length);
		try {
			VisitorFactory.getClassVisitor(CLASS_ADAPTER_CLASSES, name, classBytes, sourceBytes);
		} catch (Exception e) {
			logger.error("Exception generating complexity and dependency statistics on class " + name, e);
		}
	}

	private boolean excluded(String name) {
		// Don't process anything that is not a class file or a Java file
		if (!name.endsWith(".class")) {
			logger.debug("Not processing file : " + name);
			return true;
		}
		// Check that the class is included in the included packages
		if (!Configuration.getConfiguration().included(name)) {
			logger.debug("File not included : " + name);
			return true;
		}
		// Don't do excluded classes and packages
		if (Configuration.getConfiguration().excluded(name)) {
			logger.debug("Excluded file : " + name);
			return true;
		}
		// Don't process the same class twice
		if (files.contains(name)) {
			logger.debug("Already done file : " + name);
			return true;
		}
		files.add(name);
		return false;
	}

}