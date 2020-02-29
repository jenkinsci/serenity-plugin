package com.ikokoon.toolkit;

import com.ikokoon.serenity.ATest;
import com.ikokoon.target.Target;
import org.junit.Test;
import org.objectweb.asm.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This is the test for the utility class.
 *
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class ToolkitTest extends ATest {

	@Test
	public void slashToDot() {
		String name = "com/ikokoon/target/Target";
		name = Toolkit.slashToDot(name);
		assertEquals(Target.class.getName(), name);
	}

	@Test
	public void dotToSlash() {
		String name = Target.class.getName();
		name = Toolkit.dotToSlash(name);
		assertEquals("com/ikokoon/target/Target", name);
	}

	@Test
	public void classNameToPackageName() {
		Type[] types = Type.getArgumentTypes("(Ljava/lang/Integer;Ljava/math/BigDecimal;)Ljava/lang/String;");
		for (Type argumentType : types) {
			LOGGER.fine("Type : " + argumentType.getClassName() + ", " + argumentType.getDescriptor() + ", " + argumentType.getInternalName() + ", "
					+ argumentType.getSize() + ", " + argumentType.getSort());
		}

		Type returnType = Type.getReturnType("(Ljava/lang/Integer;Ljava/math/BigDecimal;)Ljava/lang/String;");
		LOGGER.fine("Type : " + returnType.getClassName() + ", " + returnType.getDescriptor() + ", " + returnType.getInternalName() + ", "
				+ returnType.getSize() + ", " + returnType.getSort());

		String name = Toolkit.classNameToPackageName(Toolkit.dotToSlash(Target.class.getName()));
		assertEquals("com.ikokoon.target", name);
	}

	@Test
	public void formatString() {
		int precision = 3;
		String string = Toolkit.format("123456789,123456789", precision);
		assertEquals(precision, string.substring(string.indexOf(',') + 1, string.length()).length());
		string = Toolkit.format("123456789.123456789", precision);
		assertEquals(precision, string.substring(string.indexOf('.') + 1, string.length()).length());
	}

	@Test
	public void formatDouble() {
		int precision = 3;
		double d = 123456.8755135d;
		d = Toolkit.format(d, precision);
		assertTrue(123456.875 == d);
		assertFalse(123456.8755 == d);
	}

	@Test
	public void hash() {
		String string = ToolkitTest.class.getName();
		Long stringHash = Toolkit.hash(string);
		List<Object> objects = new ArrayList<Object>();
		objects.add(string);
		Long arrayHash = Toolkit.hash(objects.toArray());
		assertEquals(stringHash, arrayHash);
	}

	@Test
	public void deleteFile() throws Exception {
		File folder = createFolderAndOneFile("target/folder", "file.txt");
		Toolkit.deleteFile(folder, 3);
		LOGGER.fine("Deleted folder and file : " + folder.exists());
	}

	private File createFolderAndOneFile(String folderName, String fileName) throws Exception {
		File folder = new File(".", folderName);
		folder.mkdir();
		assertTrue(folder.exists());

		File file = new File(folder, fileName);
		file.createNewFile();
		assertTrue(file.exists());

		return folder;
	}

	@Test
	public void copyFiles() throws Exception {
		File source = createFolderAndOneFile("target/folder", "serenity.odb");
		File destination = new File("/tmp/Eclipse/workspace/serenity/work/jobs/Discovery/builds/2010-02-26_16-12-15/serenity");
		if (destination.exists()) {
			Toolkit.deleteFile(destination, 3);
		}
		LOGGER.fine("Deleted destination : " + destination.exists());
		Toolkit.copyFiles(source, destination);
		LOGGER.fine("Copied file exists : " + new File(destination, "file.txt").exists());
	}

}