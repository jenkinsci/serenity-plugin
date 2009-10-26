package com.ikokoon.toolkit;

import static org.junit.Assert.assertNotNull;

import java.beans.XMLDecoder;
import java.io.InputStream;

import org.junit.Test;

import com.ikokoon.ATest;

public class ObjectFactoryTest extends ATest {

	@Test
	public void getObject() throws Exception {
		InputStream inputStream = ObjectFactoryTest.class.getResource("/serenity/serenity.db").openStream();
		Object[] parameters = new Object[] { inputStream };
		Class<XMLDecoder> klass = (Class<XMLDecoder>) this.getClass().getClassLoader().loadClass(XMLDecoder.class.getName());
		XMLDecoder decoder = ObjectFactory.getObject(klass, parameters);
		Object cache = decoder.readObject();
		assertNotNull(cache);
	}

}
