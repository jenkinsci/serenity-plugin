package com.ikokoon.serenity;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Date;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.Accumulator;
import com.ikokoon.serenity.process.Aggregator;
import com.ikokoon.serenity.process.Cleaner;

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
	 * -javaagent:coverage.jar. This instruction tells the JVM that there is an agent that must be used. In the META-INF directory of the jar
	 * specified there must be a MANIFEST.MF file. In this file the instructions must be something like the following:
	 * 
	 * Manifest-Version: 1.0 <br>
	 * Boot-Class-Path: asm-3.1.jar and so on...<br>
	 * Premain-Class: com.ikokoon.serenity.instrumentation.Transformer
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
					LOGGER.info("Starting accumulation : " + start);
					IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(IConstants.DATABASE_FILE, false);

					new Accumulator(null).execute();
					new Cleaner(null).execute();
					new Aggregator(null, dataBase).execute();

					dataBase.close();

					Date end = new Date();
					long million = 1000 * 1000;
					long duration = end.getTime() - start.getTime();
					LOGGER.info("Finished accumulation : " + end + ", duration : " + duration + " millis");
					LOGGER.info("Total memory : " + (Runtime.getRuntime().totalMemory() / million) + ", max memory : "
							+ (Runtime.getRuntime().maxMemory() / million) + ", free memory : " + (Runtime.getRuntime().freeMemory() / million));
				}
			});
		}
		if (instrumentation != null) {
			instrumentation.addTransformer(INSTANCE);
		}
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

		// JUnit4TestAdapterCache.getDefault().getNotifier(null, null).addListener(new RunListener() {
		// @Override
		// public void testRunStarted(Description description) throws Exception {
		// super.testRunStarted(description);
		// }
		// });

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
			classBytes = writer.toByteArray();
			return classBytes;
		}
		LOGGER.debug("Class : " + className);
		return classBytes;
	}

}