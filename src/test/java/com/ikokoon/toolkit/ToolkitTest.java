package com.ikokoon.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ikokoon.ATest;
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
		com.ikokoon.instrumentation.model.Package pakkage = new com.ikokoon.instrumentation.model.Package();
		String string = pakkage.format("123456789,123456789", precision);
		assertEquals(precision, string.substring(string.indexOf(',') + 1, string.length()).length());
		string = pakkage.format("123456789.123456789", precision);
		assertEquals(precision, string.substring(string.indexOf('.') + 1, string.length()).length());
	}

	@Test
	public void formatDouble() {
		int precision = 3;
		double d = 123456.8755135d;
		com.ikokoon.instrumentation.model.Package pakkage = new com.ikokoon.instrumentation.model.Package();
		d = pakkage.format(d, precision);
		assertTrue(123456.875 == d);
		assertFalse(123456.8755 == d);
	}

}