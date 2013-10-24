package com.ikokoon.serenity.process;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
 * This class looks through the classpath and collects metrics on the classes that were not instantiated by the classloader during the unit tests and creates a
 * visitor chain for the class that will collect the complexity and dependency metrics for the class.
 * 
 * @author Michael Couck
 * @since 24.07.09
 * @version 01.10
 */
public class Accumulator extends AProcess {

	/** The chain of adapters for analysing the classes. */
	private java.lang.Class<ClassVisitor>[] CLASS_ADAPTER_CLASSES;

	/**
	 * Constructor takes the parent process.
	 */
	@SuppressWarnings("unchecked")
	public Accumulator(final IProcess parent) {
		super(parent);
		Configuration configuration = Configuration.getConfiguration();
		CLASS_ADAPTER_CLASSES = configuration.classAdapters.toArray(new java.lang.Class[Configuration.getConfiguration().classAdapters.size()]);
	}

	/**
	 * @inheritDoc
	 */
	public void execute() {
		super.execute();
		// All the files that we are interested in
		Set<File> files = new TreeSet<File>();

		// Look for all jars below this directory to find some source
		File dotDirectory = new File(".");
		walkFileSystem(dotDirectory, files);
		// logger.error("Files : " + files);

		// Walk the class path looking for files that are included
		String classpath = Configuration.getConfiguration().getClassPath();
		// logger.error("Class path : " + File.pathSeparator + ", " + classpath);
		StringTokenizer stringTokenizer = new StringTokenizer(classpath, ";:", false);
		while (stringTokenizer.hasMoreTokens()) {
			String token = stringTokenizer.nextToken();
			File file = new File(token);
			walkFileSystem(file, files);
		}
		processFiles(files);
	}

	private void walkFileSystem(final File file, final Set<File> files) {
		try {
			// logger.error("Walking file : " + file);
			if (file.isDirectory()) {
				File[] childFiles = file.listFiles();
				if (childFiles != null && childFiles.length > 0) {
					for (final File childFile : childFiles) {
						walkFileSystem(childFile, files);
					}
				}
			} else {
				if (file.canRead()) {
					files.add(file);
				}
			}
		} catch (Exception e) {
			logger.error("Exception walking the file tree : ", e);
		}
	}

	void processFiles(final Set<File> files) {
		for (final File file : files) {
			processFile(file, files);
		}
	}

	/**
	 * Processes a directory on a file system, looks for class files and feeds the byte code into the adapter chain for collecting the metrics for the class.
	 * 
	 * @param file the directory or file to look in for the class data
	 */
	void processFile(final File file, final Set<File> files) {
		String filePath = file.getAbsolutePath();
		if (filePath.endsWith("jar") || filePath.endsWith("zip")) {
			processJar(file);
		} else {
			if (filePath.endsWith(".class")) {
				processClass(file, files);
			}
		}
	}

	void processClass(final File file, final Set<File> files) {
		String filePath = Toolkit.slashToDot(Toolkit.cleanFilePath(file.getAbsolutePath()));
		byte[] classBytes = Toolkit.getContents(file).toByteArray();
		// Strip the beginning of the path off the name
		for (String packageName : Configuration.getConfiguration().includedPackages) {
			if (filePath.indexOf(packageName) > -1) {
				int indexOfPackageName = filePath.lastIndexOf(packageName);
				int classIndex = filePath.lastIndexOf(".class");
				if (classIndex > -1) {
					final String className = filePath.substring(indexOfPackageName, classIndex);
					if (isExcluded(className + ".class")) {
						continue;
					}
					ByteArrayOutputStream source = new ByteArrayOutputStream();
					// Find the source in the files set
					File sourceFile = getSourceFile(className, files);
					if (sourceFile != null) {
						InputStream inputStream = null;
						try {
							inputStream = new FileInputStream(sourceFile);
							source = Toolkit.getContents(inputStream);
						} catch (IOException e) {
							logger.error("Exception processing source file : " + sourceFile, e);
						} finally {
							try {
								if (inputStream != null) {
									inputStream.close();
								}
							} catch (Exception e) {
								logger.error("Exception closing the input stream : " + sourceFile, e);
							}
						}
					}
					processClass(className, classBytes, source);
					break;
				}
			}
		}
	}

	File getSourceFile(final String className, final Set<File> files) {
		if (className.indexOf('$') > -1) {
			return null;
		}
		for (final File file : files) {
			if (!file.getName().endsWith(".java")) {
				continue;
			}
			String sourceFilePath = Toolkit.slashToDot(Toolkit.cleanFilePath(file.getAbsolutePath()));
			if (sourceFilePath.contains(className)) {
				// logger.error("Got source file : " + sourceFilePath);
				return file;
			}
		}
		return null;
	}

	/**
	 * Processes a jar or zip file or something like it, looks for class and feeds the byte code into the adapter chain for collecting the metrics for the
	 * class.
	 * 
	 * @param file the file to look in for the class data
	 */
	private void processJar(final File file) {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
		} catch (Exception e) {
			// logger.error("Exeption accessing the jar : " + file, e);
			return;
		}
		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			JarEntry jarEntry = jarEntries.nextElement();
			String entryName = jarEntry.getName();
			String className = Toolkit.slashToDot(entryName);
			if (!className.endsWith(".class") || isExcluded(className)) {
				continue;
			}
			try {
				// logger.error("Processsing entry : " + className);
				InputStream inputStream = jarFile.getInputStream(jarEntry);
				byte[] classFileBytes = Toolkit.getContents(inputStream).toByteArray();
				ByteArrayOutputStream source = null;
				if (jarEntry.getName().indexOf("$") == -1) {
					source = getSource(jarFile, entryName);
				} else {
					source = new ByteArrayOutputStream();
				}
				processClass(Toolkit.slashToDot(entryName), classFileBytes, source);
			} catch (Exception e) {
				logger.error("Exception reading entry : " + jarEntry + ", from file : " + file, e);
			}
		}
	}

	protected ByteArrayOutputStream getSource(final JarFile jarFile, final String entryName) throws IOException {
		// Look for the source
		final String javaEntryName = entryName.substring(0, entryName.lastIndexOf('.')) + ".java";
		// logger.error("Looking for source : " + javaEntryName + ", " + entryName + ", " + jarFile.getName());
		ZipEntry javaEntry = jarFile.getEntry(javaEntryName);
		if (javaEntry != null) {
			// logger.error("Got source : " + javaEntry);
			InputStream inputStream = jarFile.getInputStream(javaEntry);
			return Toolkit.getContents(inputStream);
		}
		return new ByteArrayOutputStream();
	}

	private void processClass(final String name, final byte[] classBytes, final ByteArrayOutputStream source) {
		String strippedName = name;
		if (strippedName != null && strippedName.endsWith("class")) {
			strippedName = strippedName.substring(0, strippedName.lastIndexOf('.'));
		}
		// logger.error("Adding class : " + strippedName);
		VisitorFactory.getClassVisitor(CLASS_ADAPTER_CLASSES, strippedName, classBytes, source);
	}

	boolean isExcluded(final String name) {
		// Don't process anything that is not a class file or a Java file
		if (name.contains("svn")) {
			return true;
		}
		if (!name.endsWith("class") && !name.endsWith("java") && !name.endsWith("jar") && !name.endsWith("zip")) {
			// logger.error("Not processing file : " + name);
			return true;
		}
		// Check that the class is included in the included packages
		if (!Configuration.getConfiguration().included(name)) {
			// logger.error("File not included : " + name);
			return true;
		} else {
			// Check that the class is not excluded in the excluded packages
			if (Configuration.getConfiguration().excluded(name)) {
				// logger.error("Excluded file : " + name);
				return true;
			}
		}
		return false;
	}

}