package com.ikokoon.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.objectweb.asm.Type;

import com.ikokoon.serenity.ATest;
import com.ikokoon.target.Target;

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
		Type type = Type.getObjectType(className);
		logger.info("Type : " + type.getClassName() + ", " + type.getDescriptor() + ", " + type.getInternalName() + ", " + type.getSize() + ", "
				+ type.getSort());

		Type[] types = Type.getArgumentTypes("(Ljava/lang/Integer;Ljava/math/BigDecimal;)Ljava/lang/String;");
		for (Type argumentType : types) {
			logger.info("Type : " + argumentType.getClassName() + ", " + argumentType.getDescriptor() + ", " + argumentType.getInternalName() + ", "
					+ argumentType.getSize() + ", " + argumentType.getSort());
		}

		Type returnType = Type.getReturnType("(Ljava/lang/Integer;Ljava/math/BigDecimal;)Ljava/lang/String;");
		logger.info("Type : " + returnType.getClassName() + ", " + returnType.getDescriptor() + ", " + returnType.getInternalName() + ", "
				+ returnType.getSize() + ", " + returnType.getSort());

		String name = Toolkit.classNameToPackageName(Toolkit.dotToSlash(Target.class.getName()));
		assertEquals("com.ikokoon.target", name);
	}

	@Test
	public void classesToByteCodeSignature() {
		String signature = Toolkit.classesToByteCodeSignature(String.class, Integer.class, BigDecimal.class);
		assertEquals("(Ljava/lang/Integer;Ljava/math/BigDecimal;)Ljava/lang/String;", signature);
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

	@SuppressWarnings("unchecked")
	@Test
	public void serializeAndDeserializeToAndFrom64() {
		Target<Object, Object> target = new Target<Object, Object>(Target.class);
		String string = Toolkit.serializeToBase64(target);
		assertNotNull(string);
		target = (Target<Object, Object>) Toolkit.deserializeFromBase64(string);
		assertNotNull(target);
	}

	@Test
	public void deleteFile() throws Exception {
		File folder = createFolderAndOneFile("target/folder", "file.txt");
		Toolkit.deleteFile(folder);
		logger.info("Deleted folder and file : " + folder.exists());
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
		File source = createFolderAndOneFile("target/folder", "file.txt");
		File destination = new File(".", "target/folderCopy");
		if (destination.exists()) {
			Toolkit.deleteFile(destination);
		}
		logger.info("Deleted destination : " + destination.exists());
		Toolkit.copyFile(source, destination);
		logger.info("Copied file exists : " + new File(destination, "file.txt").exists());
	}

}