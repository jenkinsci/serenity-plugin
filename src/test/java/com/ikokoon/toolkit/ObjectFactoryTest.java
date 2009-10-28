package com.ikokoon.toolkit;

import static org.junit.Assert.assertNotNull;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.ikokoon.ATest;

public class ObjectFactoryTest extends ATest {

	@Test
	public void getObject() throws Exception {
		InputStream inputStream = new ByteArrayInputStream(new byte[0]);
		Object[] parameters = new Object[] { inputStream, new String("Dummy") };
		Class<XMLDecoder> klass = (Class<XMLDecoder>) this.getClass().getClassLoader().loadClass(XMLDecoder.class.getName());
		XMLDecoder decoder = ObjectFactory.getObject(klass, parameters);
		assertNotNull(decoder);

		String string = ObjectFactory.getObject(String.class, parameters);
		assertNotNull(string);
	}

}
