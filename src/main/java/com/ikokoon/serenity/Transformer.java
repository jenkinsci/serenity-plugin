package com.ikokoon.serenity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Date;

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
import com.ikokoon.serenity.process.Listener;
import com.ikokoon.serenity.process.Reporter;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class is the entry point for the Serenity code coverage/complexity/dependency/profiling functionality. This class is called by the JVM on startup. The
 * agent then has first access to the byte code for all classes that are loaded. During this loading the byte code can be enhanced.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class Transformer implements ClassFileTransformer, IConstants {

	/** The LOGGER. */
	private static Logger LOGGER;
	/** During tests there can be more than one shutdown hook added. */
	private static boolean INITIALISED = false;
	/** The chain of adapters for analysing the classes. */
	private static Class<ClassVisitor>[] CLASS_ADAPTER_CLASSES;
	/** The shutdown hook will clean, accumulate and aggregate the data. */
	private static Thread shutdownHook;

	/**
	 * This method is called by the JVM at startup. This method will only be called if the command line for starting the JVM has the following on it:
	 * -javaagent:serenity/serenity.jar. This instruction tells the JVM that there is an agent that must be used. In the META-INF directory of the jar specified
	 * there must be a MANIFEST.MF file. In this file the instructions must be something like the following:
	 * 
	 * Manifest-Version: 1.0 <br>
	 * Boot-Class-Path: asm-3.1.jar and so on..., in the case that the required libraries are not on the classpath, which they should be<br>
	 * Premain-Class: com.ikokoon.serenity.Transformer
	 * 
	 * Another line in the manifest can start an agent after the JVM has been started, but not for all JVMs. So not very useful.
	 * 
	 * These instructions tell the JVM to call this method when loading class files.
	 * 
	 * @param args a set of arguments that the JVM will call the method with
	 * @param instrumentation the instrumentation implementation of the JVM
	 */
	@SuppressWarnings("unchecked")
	public static void premain(final String args, final Instrumentation instrumentation) {
		if (!INITIALISED) {
			INITIALISED = true;
			LoggingConfigurator.configure();
			CLASS_ADAPTER_CLASSES = Configuration.getConfiguration().classAdapters.toArray(new Class[Configuration.getConfiguration().classAdapters.size()]);
			LOGGER = Logger.getLogger(Transformer.class);
			LOGGER.error("Starting Serenity : ");
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
			Collector.initialize(ramDataBase);
			Profiler.initialize(ramDataBase);
			new Listener(null, ramDataBase).execute();
			addShutdownHook(ramDataBase);
			LOGGER.error("Finished initializing Serenity : ");
		}
	}

	/**
	 * This method adds the shutdown hook that will clean and accumulate the data when the Jvm shuts down.
	 * 
	 * @param dataBase the database to get the data from
	 */
	private static void addShutdownHook(final IDataBase dataBase) {
		shutdownHook = new Thread() {
			public void run() {
				Date start = new Date();
				LOGGER.warn("Starting accumulation : " + start);

				long processStart = System.currentTimeMillis();
				new Accumulator(null).execute();
				LOGGER.warn("Accumlulator : " + (System.currentTimeMillis() - processStart));

				processStart = System.currentTimeMillis();
				new Cleaner(null, dataBase).execute();
				LOGGER.warn("Cleaner : " + (System.currentTimeMillis() - processStart));

				processStart = System.currentTimeMillis();
				new Aggregator(null, dataBase).execute();
				LOGGER.warn("Aggregator : " + (System.currentTimeMillis() - processStart));

				processStart = System.currentTimeMillis();
				new Reporter(null, dataBase).execute();
				LOGGER.warn("Reporter : " + (System.currentTimeMillis() - processStart));

				processStart = System.currentTimeMillis();
				dataBase.close();
				LOGGER.warn("Close database : " + (System.currentTimeMillis() - processStart));

				String dumpData = Configuration.getConfiguration().getProperty(IConstants.DUMP);
				if (dumpData != null && "true".equals(dumpData.trim())) {
					DataBaseToolkit.dump(dataBase, null, null);
				}

				Date end = new Date();
				long million = 1000 * 1000;
				long duration = end.getTime() - start.getTime();
				LOGGER.warn("Finished accumulation : " + end + ", duration : " + duration + " millis");
				LOGGER.warn("Total memory : " + (Runtime.getRuntime().totalMemory() / million) + ", max memory : "
						+ (Runtime.getRuntime().maxMemory() / million) + ", free memory : " + (Runtime.getRuntime().freeMemory() / million));
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	protected static void removeShutdownHook() {
		Runtime.getRuntime().removeShutdownHook(shutdownHook);
	}

	/**
	 * This method transforms the classes that are specified.
	 */
	public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain,
			final byte[] classBytes) throws IllegalClassFormatException {
		if (Configuration.getConfiguration().included(className) && !Configuration.getConfiguration().excluded(className)) {
			LOGGER.debug("Enhancing class : " + className);
			ByteArrayOutputStream source = new ByteArrayOutputStream(0);
			ClassWriter writer = (ClassWriter) VisitorFactory.getClassVisitor(CLASS_ADAPTER_CLASSES, className, classBytes, source);
			byte[] enhancedClassBytes = writer.toByteArray();
			String writeClasses = Configuration.getConfiguration().getProperty(IConstants.WRITE_CLASSES);
			if (writeClasses != null && writeClasses.equals(Boolean.TRUE.toString())) {
				writeClass(className, enhancedClassBytes);
			}
			// Return the injected bytes for the class, i.e. with the coverage instructions
			return enhancedClassBytes;
		} else {
			LOGGER.debug("Class not included : " + className);
		}
		// Return the original bytes for the class
		return classBytes;
	}

	/**
	 * This method writes the transformed classes to the file system so they can be viewed later.
	 * 
	 * @param className the name of the class file
	 * @param classBytes the bytes of byte code to write
	 */
	private void writeClass(final String className, final byte[] classBytes) {
		// Write the class so we can check it with JD decompiler visually
		String directoryPath = Toolkit.dotToSlash(Toolkit.classNameToPackageName(className));
		String fileName = className.replaceFirst(Toolkit.classNameToPackageName(className), "") + ".class";
		File directory = new File(IConstants.SERENITY_DIRECTORY + File.separator + directoryPath);
		if (!directory.exists()) {
			directory.mkdirs();
			LOGGER.debug(directory.getAbsolutePath());
		}
		File file = new File(directory, fileName);
		Toolkit.setContents(file, classBytes);
	}

}