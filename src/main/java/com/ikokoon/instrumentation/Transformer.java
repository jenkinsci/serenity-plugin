package com.ikokoon.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.ikokoon.IConstants;
import com.ikokoon.instrumentation.process.Accumulator;
import com.ikokoon.instrumentation.process.Aggregator;
import com.ikokoon.instrumentation.process.Cleaner;
import com.ikokoon.persistence.IDataBase;
import com.ikokoon.toolkit.ObjectFactory;

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
	/** The aggregated flag. */
	private static boolean accumulated = false;

	/** The chain of adapters for analysing the classes. */
	private Class<ClassVisitor>[] classAdapterClasses;

	/**
	 * This method is called by the JVM at startup. This method will only be called if the command line for starting the JVM has the following on it:
	 * -javaagent:coverage.jar. This instruction tells the JVM that there is an agent that must be used. In the META-INF directory of the jar
	 * specified there must be a MANIFEST.MF file. In this file the instructions must be something like the following:
	 * 
	 * Manifest-Version: 1.0 <br>
	 * Boot-Class-Path: asm-3.1.jar and so on...<br>
	 * Premain-Class: com.ikokoon.instrumentation.CoverageTransformer
	 * 
	 * These instructions tell the JVM to call this method when loading class files.
	 * 
	 * @param args
	 *            a set of arguments that the JVM will call the method with
	 * @param instrumentation
	 *            the instrumentation implementation of the JVM
	 */
	public static void premain(String args, Instrumentation instrumentation) {
		URL url = Transformer.class.getResource(LOG_4_J_PROPERTIES);
		if (url != null) {
			PropertyConfigurator.configure(url);
		}
		LOGGER = Logger.getLogger(Transformer.class);
		LOGGER.error("Loaded logging properties from : " + url);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				LOGGER.info("Writing and finalizing the persistence");
				IDataBase dataBase = IDataBase.DataBase.getDataBase(IConstants.DATABASE_FILE, false);
				new Cleaner(null).execute();
				new Aggregator(null, dataBase).execute();
				dataBase.close();
				long million = 1000 * 1000;
				LOGGER.info("Total memory : " + (Runtime.getRuntime().totalMemory() / million) + ", max memory : "
						+ (Runtime.getRuntime().maxMemory() / million) + ", free memory : " + (Runtime.getRuntime().freeMemory() / million));
			}
		});
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
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		// Only classes from the system classloader
		if (loader != ClassLoader.getSystemClassLoader()) {
			LOGGER.debug("No system classloader : " + className);
			return classfileBuffer;
		}
		// Can't enhance yourself or native etc. classes
		if (Configuration.getConfiguration().excluded(className)) {
			LOGGER.debug("Excluded class : " + className);
			return classfileBuffer;
		}
		// Check for packages that we need to enhance
		if (Configuration.getConfiguration().included(className)) {
			LOGGER.debug("Enhancing class : " + className);
			ClassWriter writer = getVisitorChain(classfileBuffer, new byte[0], classAdapterClasses, className);
			byte[] result = writer.toByteArray();
			return result;
		}
		if (!accumulated) {
			accumulated = true;
			Date start = new Date();
			LOGGER.info("Starting accumulation : " + start);
			new Accumulator(null).execute();
			Date end = new Date();
			long duration = end.getTime() - start.getTime();
			LOGGER.info("Finished accumulation : " + end + ", duration : " + duration + " millis");
		}
		return classfileBuffer;
	}

	/**
	 * Builds the visitor chain that will gather the code metrics while visiting the byte code for the class.
	 * 
	 * @param classVisitor
	 *            the top level visitor, typically this is a ClassWriter instance
	 * @param classAdapterClasses
	 *            the class adapters that will be chained to the writer
	 * @param className
	 *            the constructor parameter for the visitor
	 * @return the bottom visitor in the chain of visitors
	 */
	public ClassWriter getVisitorChain(byte[] classfileBuffer, byte[] sourcefileBuffer, Class<ClassVisitor>[] classAdapterClasses, String className) {
		ClassReader reader = new ClassReader(classfileBuffer);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
		ClassVisitor visitor = writer;
		for (Class<ClassVisitor> klass : classAdapterClasses) {
			Object[] parameters = new Object[] { visitor, className, classfileBuffer, sourcefileBuffer };
			visitor = ObjectFactory.getObject(klass, parameters);
			LOGGER.debug("Adding class visitor : " + visitor);
		}
		reader.accept(visitor, 0);
		// ASMifierClassVisitor classVisitor = new ASMifierClassVisitor(new PrintWriter(System.out));
		// CheckClassAdapter checkClassAdapter = new CheckClassAdapter(classAdapter);
		return writer;
	}

}