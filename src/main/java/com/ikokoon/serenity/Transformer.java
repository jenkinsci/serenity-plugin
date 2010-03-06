package com.ikokoon.serenity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Date;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.Accumulator;
import com.ikokoon.serenity.process.Aggregator;
import com.ikokoon.serenity.process.Cleaner;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class is the entry point for the Serenity code coverage/complexity/dependency/profiling functionality. This class is called by the JVM on
 * startup. The agent then has first access to the byte code for all classes that are loaded. During this loading the byte code can be enhanced.
 *
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class Transformer implements ClassFileTransformer, IConstants {

	/** The logger. */
	private static Logger LOGGER;
	/** During tests there can be more than one shutdown hook added. */
	private static boolean INITIALISED = false;
	/** The chain of adapters for analysing the classes. */
	private static Class<ClassVisitor>[] CLASS_ADAPTER_CLASSES;

	private static Thread shutdownHook = new Thread() {

	};

	/**
	 * This method is called by the JVM at startup. This method will only be called if the command line for starting the JVM has the following on it:
	 * -javaagent:serenity/serenity.jar. This instruction tells the JVM that there is an agent that must be used. In the META-INF directory of the jar
	 * specified there must be a MANIFEST.MF file. In this file the instructions must be something like the following:
	 *
	 * Manifest-Version: 1.0 <br>
	 * Boot-Class-Path: asm-3.1.jar and so on..., in the case that the required libraries are not on the classpath, which they should be<br>
	 * Premain-Class: com.ikokoon.serenity.Transformer
	 *
	 * Another line in the manifest can start an agent after the JVM has been started, but not for all JVMs. So not very useful.
	 *
	 * These instructions tell the JVM to call this method when loading class files.
	 *
	 * @param args
	 *            a set of arguments that the JVM will call the method with
	 * @param instrumentation
	 *            the instrumentation implementation of the JVM
	 */
	@SuppressWarnings("unchecked")
	public static void premain(String args, Instrumentation instrumentation) {
		if (!INITIALISED) {
			INITIALISED = true;

			// printSystemProperties();

			LoggingConfigurator.configure();
			CLASS_ADAPTER_CLASSES = Configuration.getConfiguration().classAdapters.toArray(new Class[Configuration.getConfiguration().classAdapters
					.size()]);
			LOGGER = Logger.getLogger(Transformer.class);

			if (instrumentation != null) {
				instrumentation.addTransformer(new Transformer());
			}
			String cleanClasses = Configuration.getConfiguration().getProperty(IConstants.CLEAN_CLASSES);
			if (cleanClasses != null && cleanClasses.equals(Boolean.TRUE.toString())) {
				File serenityDirectory = new File(IConstants.SERENITY_DIRECTORY);
				Toolkit.deleteFiles(serenityDirectory, ".class");
				if (!serenityDirectory.exists()) {
					if (!serenityDirectory.mkdirs()) {
						LOGGER.warn("Didn't re-create Serenity directory : " + serenityDirectory.getAbsolutePath());
					}
				}
			}
			File file = new File(IConstants.DATABASE_FILE_ODB);
			Toolkit.deleteFile(file, 3);
			// This is the underlying database that will persist the data to the file system
			IDataBase odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, IConstants.DATABASE_FILE_ODB, null);
			DataBaseToolkit.clear(odbDataBase);
			// This is the ram database that will hold all the data in memory for better performance
			IDataBase ramDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, odbDataBase);
			DataBaseToolkit.clear(ramDataBase);

			addShutdownHook(ramDataBase);

			Collector.setDataBase(ramDataBase);
		}
	}

	private static void addShutdownHook(final IDataBase dataBase) {
		shutdownHook = new Thread() {
			public void run() {
				Date start = new Date();
				LOGGER.info("Starting accumulation : " + start);

				long processStart = System.currentTimeMillis();
				new Accumulator(null).execute();
				LOGGER.info("Accumlulator : " + (System.currentTimeMillis() - processStart));

				processStart = System.currentTimeMillis();
				new Cleaner(null, dataBase).execute();
				LOGGER.info("Cleaner : " + (System.currentTimeMillis() - processStart));

				processStart = System.currentTimeMillis();
				new Aggregator(null, dataBase).execute();
				LOGGER.info("Aggregator : " + (System.currentTimeMillis() - processStart));

				processStart = System.currentTimeMillis();
				dataBase.close();
				LOGGER.info("Close database : " + (System.currentTimeMillis() - processStart));

				Date end = new Date();
				long million = 1000 * 1000;
				long duration = end.getTime() - start.getTime();
				LOGGER.info("Finished accumulation : " + end + ", duration : " + duration + " millis");
				LOGGER.info("Total memory : " + (Runtime.getRuntime().totalMemory() / million) + ", max memory : "
						+ (Runtime.getRuntime().maxMemory() / million) + ", free memory : " + (Runtime.getRuntime().freeMemory() / million));
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	protected static void removeShutdownHook() {
		Runtime.getRuntime().removeShutdownHook(shutdownHook);
	}

	protected static void printSystemProperties() {
		System.out.println("Working directory : " + new File(".").getAbsolutePath());
		System.out.println("Java class path : " + System.getProperty("java.class.path"));
		Enumeration<Object> keys = System.getProperties().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			if (key == null) {
				continue;
			}
			System.out.println("System property : " + key + "=" + System.getProperties().getProperty(key.toString()));
		}
	}

	/**
	 * This method transforms the classes that are specified.
	 */
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBytes)
			throws IllegalClassFormatException {
		// Can we implement a classloader here? Would it make things simpler/more robust/faster?
		// Thread.currentThread().setContextClassLoader(and the custom classloader);
		if (loader != ClassLoader.getSystemClassLoader()) {
			LOGGER.debug("No system classloader : " + className);
			return classBytes;
		}
		if (Configuration.getConfiguration().excluded(className)) {
			LOGGER.debug("Excluded class : " + className);
			return classBytes;
		}
		if (Configuration.getConfiguration().included(className)) {
			LOGGER.debug("Enhancing class : " + className);
			ByteArrayOutputStream source = new ByteArrayOutputStream(0);
			ClassWriter writer = (ClassWriter) VisitorFactory.getClassVisitor(CLASS_ADAPTER_CLASSES, className, classBytes, source);
			byte[] enhancedClassBytes = writer.toByteArray();
			String writeClasses = Configuration.getConfiguration().getProperty(IConstants.WRITE_CLASSES);
			if (writeClasses != null && writeClasses.equals(Boolean.TRUE.toString())) {
				writeClass(className, enhancedClassBytes);
			}
			return enhancedClassBytes;
		} else {
			LOGGER.debug("Class not included : " + className);
		}
		return classBytes;
	}

	private void writeClass(String className, byte[] classBytes) {
		// Write the class so we can check it with JD decompiler visually
		String directoryPath = Toolkit.dotToSlash(Toolkit.classNameToPackageName(className));
		String fileName = className.replaceFirst(Toolkit.classNameToPackageName(className), "") + ".class";
		File directory = new File(IConstants.SERENITY_DIRECTORY + File.separator + directoryPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		LOGGER.debug(directory.getAbsolutePath());

		File file = new File(directory, fileName);
		Toolkit.setContents(file, classBytes);
	}

}