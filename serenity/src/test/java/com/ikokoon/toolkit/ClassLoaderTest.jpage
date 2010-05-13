package com.ikokoon.toolkit;

import static org.junit.Assert.assertNotNull;

import java.beans.XMLDecoder;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.junit.Test;

import com.ikokoon.serenity.ATest;

public class ClassLoaderTest extends ATest {

	@Test
	public void loadClass() throws Exception {
		URL[] urls = getUrls();
		ClassLoader loader = new ClassLoader(urls, getClass().getClassLoader());
		Class<?> klass = loader.loadClass(XMLDecoder.class.getName());
		assertNotNull(klass);
	}

	private URL[] getUrls() {
		String classpath = System.getProperty("java.class.path");
		StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator + File.separator);
		ArrayList<URL> urls = new ArrayList<URL>();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (!token.endsWith(".jar")) {
				continue;
			}
			try {
				URL url = new URL(token);
				urls.add(url);
			} catch (MalformedURLException e) {
				logger.error("Exception adding the url : " + token, e);
			}
		}
		return urls.toArray(new URL[urls.size()]);
	}

}
