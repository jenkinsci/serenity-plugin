package com.ikokoon.serenity;

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
	/** The static instance of this transformer. */
	public static final Transformer INSTANCE = new Transformer();
	/** During tests there can be more than one shutdown hook added. */
	private static boolean shutdownHookAdded = false;

	/** The chain of adapters for analysing the classes. */
	private Class<ClassVisitor>[] classAdapterClasses;

	/**
	 * This method is called by the JVM at startup. This method will only be called if the command line for starting the JVM has the following on it:
	 * -javaagent:serenity.jar. This instruction tells the JVM that there is an agent that must be used. In the META-INF directory of the jar
	 * specified there must be a MANIFEST.MF file. In this file the instructions must be something like the following:
	 * 
	 * Manifest-Version: 1.0 <br>
	 * Boot-Class-Path: asm-3.1.jar and so on..., in the case that the required libraries are not on the classpath, which they should be<br>
	 * Premain-Class: com.ikokoon.serenity.instrumentation.Transformer
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
	public static void premain(String args, Instrumentation instrumentation) {
		LoggingConfigurator.configure();
		LOGGER = Logger.getLogger(Transformer.class);
		if (!shutdownHookAdded) {
			shutdownHookAdded = true;
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					Date start = new Date();
					LOGGER.error("Starting accumulation : " + start);
					IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, false, null);

					// Execute the processing chain, child first
					new Accumulator(null).execute();
					new Cleaner(null).execute();
					new Aggregator(null, dataBase).execute();

					dataBase.close();

					Date end = new Date();
					long million = 1000 * 1000;
					long duration = end.getTime() - start.getTime();
					LOGGER.error("Finished accumulation : " + end + ", duration : " + duration + " millis");
					LOGGER.error("Total memory : " + (Runtime.getRuntime().totalMemory() / million) + ", max memory : "
							+ (Runtime.getRuntime().maxMemory() / million) + ", free memory : " + (Runtime.getRuntime().freeMemory() / million));
				}
			});
		}
		if (instrumentation != null) {
			instrumentation.addTransformer(INSTANCE);
		}
		String cleanClasses = Configuration.getConfiguration().getProperty(IConstants.CLEAN_CLASSES);
		if (cleanClasses != null && cleanClasses.equals(Boolean.TRUE.toString())) {
			File serenityDirectory = new File(IConstants.SERENITY_DIRECTORY);
			Toolkit.deleteFile(serenityDirectory);
			if (!serenityDirectory.exists()) {
				if (!serenityDirectory.mkdirs()) {
					LOGGER.warn("Didn't re-create Serenity directory : " + serenityDirectory.getAbsolutePath());
				}
			}
		}
		IDataBase iDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, IConstants.DATABASE_FILE_ODB, true, null);
		IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, true, iDataBase);
		DataBaseToolkit.clear(dataBase);
	}

	@SuppressWarnings("unchecked")
	public Transformer() {
		classAdapterClasses = Configuration.getConfiguration().classAdapters
				.toArray(new Class[Configuration.getConfiguration().classAdapters.size()]);
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
			ClassWriter writer = (ClassWriter) VisitorFactory.getClassVisitor(classAdapterClasses, className, classBytes, new byte[0]);
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
		File directory = new File(IConstants.SERENITY_DIRECTORY + directoryPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		LOGGER.debug(directory.getAbsolutePath());

		File file = new File(directory, fileName);
		Toolkit.setContents(file, classBytes);
	}

}