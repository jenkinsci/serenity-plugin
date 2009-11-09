package com.ikokoon.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.target.Target;
import com.ikokoon.serenity.model.Package;

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
		String name = Toolkit.classNameToPackageName(Target.class.getName());
		assertEquals("com.ikokoon.target", name);
	}

	@Test
	public void classesToByteCodeSignature() {
		String signature = Toolkit.classesToByteCodeSignature(String.class, Integer.class, BigDecimal.class);
		assertEquals("(Ljava/lang/Integer;Ljava/math/BigDecimal;)Ljava/lang/String;", signature);
	}

	@Test
	public void byteCodeSignatureToClassNameArray() {
		String[] classes = Toolkit.byteCodeSignatureToClassNameArray("([ZLjava/lang/Integer;ZLjava/math/BigDecimal;)[ZLjava/lang/String;");
		assertEquals(Integer.class.getName(), classes[0]);
		assertEquals(BigDecimal.class.getName(), classes[1]);
		assertEquals(String.class.getName(), classes[2]);

		classes = Toolkit.byteCodeSignatureToClassNameArray("Lorg/apache/log4j/Logger;");
		assertEquals(Logger.class.getName(), classes[0]);
	}

	@Test
	public void formatString() {
		int precision = 3;
		Package pakkage = new Package();
		String string = pakkage.format("123456789,123456789", precision);
		assertEquals(precision, string.substring(string.indexOf(',') + 1, string.length()).length());
		string = pakkage.format("123456789.123456789", precision);
		assertEquals(precision, string.substring(string.indexOf('.') + 1, string.length()).length());
	}

	@Test
	public void formatDouble() {
		int precision = 3;
		double d = 123456.8755135d;
		Package pakkage = new Package();
		d = pakkage.format(d, precision);
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

}