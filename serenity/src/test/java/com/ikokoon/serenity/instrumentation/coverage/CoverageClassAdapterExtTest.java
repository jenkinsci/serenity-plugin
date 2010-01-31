package com.ikokoon.serenity.instrumentation.coverage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.target.Simple;
import com.ikokoon.toolkit.Toolkit;

/**
 * This is the test for the alternative coverage instructions instrumentation.
 *
 * @author Michael Couck
 * @since 19.01.10
 * @version 01.00
 */
public class CoverageClassAdapterExtTest extends ATest {

	@Test
	@SuppressWarnings("unchecked")
	public void instrument() throws Exception {
		String className = Simple.class.getName();
		byte[] classBytes = getClassBytes(className);
		byte[] sourceBytes = getSourceBytes(className);

		writeClass(className, classBytes);

		// Add the coverage instructions
		ByteArrayOutputStream source = new ByteArrayOutputStream();
		source.write(sourceBytes);
		ClassWriter writer = (ClassWriter) VisitorFactory.getClassVisitor(new Class[] { CoverageClassAdapterExt.class }, className, classBytes,
				source);
		classBytes = writer.toByteArray();

		writeClass(className, classBytes);

		// Verify the byte code is valid
		CheckClassAdapter.verify(new ClassReader(classBytes), false, new PrintWriter(System.out));
	}

	private void writeClass(String className, byte[] classBytes) {
		// Write the class so we can check it with JD decompiler visually
		String packageName = Toolkit.classNameToPackageName(className);
		String directoryPath = Toolkit.dotToSlash(packageName);
		String fileName = className.substring(className.indexOf(Toolkit.classNameToPackageName(className)) + packageName.length() + 1) + ".class";
		logger.warn("Directory path : " + directoryPath + ", file name : " + fileName);
		File directory = new File(IConstants.SERENITY_DIRECTORY + File.separator + directoryPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		logger.warn("Absolute directory : " + directory.getAbsolutePath());

		File file = new File(directory, fileName);
		Toolkit.setContents(file, classBytes);
	}

}
