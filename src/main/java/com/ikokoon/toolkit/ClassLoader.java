package com.ikokoon.toolkit;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * This class is a test to see if the XMLDecoder can be loaded by a custom class loader. The purpose of which is because Hudson delegates to the
 * server classloader to get the basic Java classes but the XMLDecoder classloader does not have access to the classes in the individual plugins, so
 * it is basically useless of course. In any case it is not possible because the classes that start with java.lang are not allowed to be loaded by
 * anything other than the system class loader.
 * 
 * @author Michael Couck
 * @since 18.10.09
 * @version 01.00
 */
public class ClassLoader extends URLClassLoader {

	public ClassLoader(URL[] urls, java.lang.ClassLoader parent) {
		super(urls, parent);
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return super.loadClass(name, resolve);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		InputStream inputStream = getResourceAsStream(Toolkit.dotToSlash(name) + ".class");
		byte[] bytes = Toolkit.getContents(inputStream).toByteArray();
		Class<?> klass = this.defineClass(name, bytes, 0, bytes.length);
		return klass;
		// return super.loadClass(name);
	}

}
