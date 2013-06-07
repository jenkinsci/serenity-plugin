package com.ikokoon.serenity.process;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
 * @version 01.00
 */
public class Accumulator extends AProcess {

	/** The set of jars that are processed so we don't do the same jar more than once. */
	private Set<String> jarsProcessed = new TreeSet<String>();
	/** The set of classes that are processed so we don't process the files more than once. */
	private Set<String> filesProcessed = new TreeSet<String>();
	/** The chain of adapters for analysing the classes. */
	private java.lang.Class<ClassVisitor>[] CLASS_ADAPTER_CLASSES;

	/**
	 * Constructor takes the parent process.
	 */
	@SuppressWarnings("unchecked")
	public Accumulator(final IProcess parent) {
		super(parent);
		CLASS_ADAPTER_CLASSES = Configuration.getConfiguration().classAdapters.toArray(new java.lang.Class[Configuration.getConfiguration().classAdapters
				.size()]);
	}

	/**
	 * @inheritDoc
	 */
	public void execute() {
		super.execute();
		String classpath = Configuration.getConfiguration().getClassPath();
		logger.error("Class path : " + File.pathSeparator + ", " + classpath);
		StringTokenizer stringTokenizer = new StringTokenizer(classpath, File.pathSeparator, false);
		while (stringTokenizer.hasMoreTokens()) {
			String token = stringTokenizer.nextToken();
			File file = new File(token);
			if (!file.exists() || !file.canRead()) {
				logger.warn("Can't read file : " + file.getAbsolutePath());
				continue;
			}
			if (file.isFile()) {
				if (token.endsWith(".jar") || token.endsWith(".zip") || token.endsWith(".war") || token.endsWith(".ear")) {
					logger.error("Processing jar : " + file.getAbsolutePath());
					processJar(file);
				}
			} else if (file.isDirectory()) {
				processDir(file);
			}
		}
		// Look for all jars below this directory to find some source
		List<File> list = new ArrayList<File>();
		File baseDirectory = new File(".");
		Toolkit.findFiles(baseDirectory, new Toolkit.IFileFilter() {
			public boolean matches(final File file) {
				if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
					return true;
				}
				return false;
			}
		}, list);
		for (final File file : list) {
			logger.error("Processing jar : " + file.getAbsolutePath());
			processJar(file);
		}
	}

	/**
	 * Processes a directory on a file system, looks for class files and feeds the byte code into the adapter chain for collecting the metrics for the class.
	 * 
	 * @param file the directory or file to look in for the class data
	 */
	void processDir(final File file) {
		// Iteratively go through the directories
		if (file == null || !file.exists() || !file.canWrite()) {
			return;
		}
		if (file.isDirectory()) {
			File files[] = file.listFiles();
			for (int j = 0; j < files.length; j++) {
				File child = files[j];
				processDir(child);
			}
		} else if (file.isFile() && file.canRead()) {
			String filePath = file.getAbsolutePath();
			filePath = Toolkit.slashToDot(filePath);
			if (excluded(filePath)) {
				return;
			}
			byte[] classBytes = Toolkit.getContents(file).toByteArray();
			ByteArrayOutputStream source = new ByteArrayOutputStream();

			String className = null;

			// Strip the beginning of the path off the name
			for (String packageName : Configuration.getConfiguration().includedPackages) {
				if (filePath.indexOf(packageName) > -1) {
					int indexOfPackageName = filePath.indexOf(packageName);
					int classIndex = filePath.lastIndexOf(".class");
					try {
						if (classIndex > -1) {
							className = filePath.substring(indexOfPackageName, classIndex);
							break;
						}
					} catch (Exception e) {
						logger.error("Exception reading the class files in a directory", e);
					}
				}
			}
			if (!filesProcessed.add(className)) {
				return;
			}
			processClass(className, classBytes, source);
		}
	}

	/**
	 * Processes a jar or zip file or something like it, looks for class and feeds the byte code into the adapter chain for collecting the metrics for the
	 * class.
	 * 
	 * @param file the file to look in for the class data
	 */
	private void processJar(final File file) {
		// Don't process the jars more than once
		if (!jarsProcessed.add(file.getName())) {
			return;
		}

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
			String className = Toolkit.slashToDot(entryName);
			if (excluded(className)) {
				continue;
			}
			logger.error("Processsing entry : " + className);
			try {
				InputStream inputStream = jarFile.getInputStream(jarEntry);
				byte[] classFileBytes = Toolkit.getContents(inputStream).toByteArray();
				ByteArrayOutputStream source = null;
				if (jarEntry.getName().indexOf("$") == -1) {
					source = getSource(jarFile, entryName);
				} else {
					source = new ByteArrayOutputStream();
				}
				processClass(Toolkit.slashToDot(entryName), classFileBytes, source);
			} catch (IOException e) {
				logger.error("Exception reading entry : " + jarEntry + ", from file : " + jarFile, e);
			}
		}
	}

	protected ByteArrayOutputStream getSource(final JarFile jarFile, final String entryName) throws IOException {
		// Look for the source
		final String javaEntryName = entryName.substring(0, entryName.lastIndexOf('.')) + ".java";
		logger.error("Looking for source : " + javaEntryName + ", " + entryName + ", " + jarFile.getName());
		ZipEntry javaEntry = jarFile.getEntry(javaEntryName);
		if (javaEntry != null) {
			logger.warn("Got source : " + javaEntry);
			InputStream inputStream = jarFile.getInputStream(javaEntry);
			return Toolkit.getContents(inputStream);
		} else {
			List<File> files = new ArrayList<File>();
			// Look on the file system below the dot directory for the Java file
			Toolkit.findFiles(new File("."), new Toolkit.IFileFilter() {
				public boolean matches(final File file) {
					String filePath = file.getAbsolutePath();
					// Could be on windows
					filePath = Toolkit.replaceAll(filePath, "\\", "/");
					boolean isSourceFile = filePath.contains(javaEntryName);
					if (isSourceFile) {
						logger.error("Is source file : " + isSourceFile + ", " + filePath);
					}
					return isSourceFile;
				}
			}, files);
			if (!files.isEmpty()) {
				return Toolkit.getContents(files.get(0));
			}
		}
		return new ByteArrayOutputStream();
	}

	private void processClass(final String name, final byte[] classBytes, final ByteArrayOutputStream source) {
		String strippedName = name;
		if (strippedName != null && strippedName.endsWith(".class")) {
			strippedName = strippedName.substring(0, strippedName.lastIndexOf('.'));
		}
		try {
			logger.error("Adding class : " + strippedName);
			VisitorFactory.getClassVisitor(CLASS_ADAPTER_CLASSES, strippedName, classBytes, source);
		} catch (Exception e) {
			int sourceLength = source != null ? source.size() : 0;
			logger.warn("Class name : " + strippedName + ", length : " + classBytes.length + ", source : " + sourceLength);
			logger.error("Exception accululating data on class " + strippedName, e);
		}
	}

	private boolean excluded(final String name) {
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
		// Check that the class is not excluded in the excluded packages
		if (Configuration.getConfiguration().excluded(name)) {
			logger.debug("Excluded file : " + name);
			return true;
		}
		// Don't process the same class twice
		if (!filesProcessed.add(name)) {
			logger.debug("Already done file : " + name);
			return true;
		}
		return false;
	}

}