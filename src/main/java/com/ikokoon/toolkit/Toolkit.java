package com.ikokoon.toolkit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.ikokoon.instrumentation.model.Line;
import com.ikokoon.instrumentation.model.Method;
import com.ikokoon.instrumentation.model.Project;
import com.ikokoon.persistence.IDataBase;

/**
 * This class contains methods for changing a string to the byte code representation and visa versa. Also some other nifty functions like stripping a
 * string of white space etc.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
public class Toolkit {

	/** The logger. */
	private static Logger logger = Logger.getLogger(Toolkit.class);

	/**
	 * Simple, fast hash function to generate quite unique hashes from strings(i.e. toCharArray()).
	 * 
	 * @param string
	 *            the string to generate the hash from
	 * @return the integer representation of the hash of the string characters, typically quite unique for strings less than 10 characters
	 */
	public static final Long hash(String string) {
		// Must be prime of course
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;
		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			hash = (hash * seed) + chars[i];
		}
		return new Long(Math.abs(hash));
	}

	/**
	 * Builds a hash from an array of objects.
	 * 
	 * @param objects
	 *            the objects to build the hash from
	 * @return the hash of the objects
	 */
	public static final Long hash(Object... objects) {
		StringBuilder builder = new StringBuilder();
		for (Object object : objects) {
			builder.append(object);
		}
		Long hash = Toolkit.hash(builder.toString());
		return hash;
	}

	/**
	 * This method replaces the / in the byte code name with . which is XML friendly.
	 * 
	 * @param name
	 *            the byte code name of a class
	 * @return the Java name of the class
	 */
	public static String slashToDot(String name) {
		if (name == null) {
			return name;
		}
		name = name.replace('/', '.');
		return name;
	}

	/**
	 * This method replaces the . in the byte code name with / which is what we expect from byte code.
	 * 
	 * @param name
	 *            the name of the class or package
	 * @return the byte code name of the class or package
	 */
	public static String dotToSlash(String name) {
		if (name == null) {
			return name;
		}
		name = name.replace('.', '/');
		return name;
	}

	/**
	 * Takes the name of a class and returns the package name for the class.
	 * 
	 * @param name
	 *            the name of the class fully qualified
	 * @return the package name of the class
	 */
	public static String classNameToPackageName(String className) {
		className = slashToDot(className);
		if (className.endsWith(".class")) {
			className = className.substring(0, className.lastIndexOf('.'));
		}
		if (className.indexOf('.') > -1) {
			return className.substring(0, className.lastIndexOf('.'));
		}
		// Default package
		return "";
	}

	/**
	 * Builds the string required in byte code to call a method given parameters and a return type.
	 * 
	 * @param returnType
	 *            the type that the method being called returns
	 * @param parameters
	 *            the parameter types of the method
	 * @return the byte code string representation of a method
	 */
	public static String classesToByteCodeSignature(Class<?> returnType, Class<?>... parameters) {
		StringBuilder builder = new StringBuilder("(");
		for (Class<?> parameter : parameters) {
			builder.append("L");
			builder.append(Toolkit.dotToSlash(parameter.getName()));
			builder.append(";");
		}
		builder.append(")");
		if (returnType != null) {
			builder.append("L");
			builder.append(Toolkit.dotToSlash(returnType.getName()));
			builder.append(";");
		} else {
			builder.append("V");
		}
		return builder.toString();
	}

	/**
	 * Takes the signature of a method, a fields or a class and returns the class names in it.
	 * 
	 * @param signature
	 *            the signature of the field or method
	 * @return the classes in the description/signature
	 */
	public static String[] byteCodeSignatureToClassNameArray(String signature) {
		List<String> classNames = new ArrayList<String>();
		if (signature == null) {
			return classNames.toArray(new String[classNames.size()]);
		}
		StringTokenizer tokenizer = new StringTokenizer(signature, ";<>*");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			logger.debug(token);
			StringBuilder builder = new StringBuilder();
			char[] chars = token.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				switch (c) {
				case '(':
					break;
				case ')':
					break;
				case '[':
					break;
				case ']':
					break;
				case '+':
					break;
				case '-':
					break;
				case 'B':
					if (i <= 4) {
						break;
					}
				case 'C':
					if (i <= 4) {
						break;
					}
				case 'D':
					if (i <= 4) {
						break;
					}
				case 'F':
					if (i <= 4) {
						break;
					}
				case 'I':
					if (i <= 4) {
						break;
					}
				case 'J':
					if (i <= 4) {
						break;
					}
				case 'L':
					if (i <= 4) {
						break;
					}
				case 'S':
					if (i <= 4) {
						break;
					}
				case 'Z':
					if (i <= 4) {
						break;
					}
				default:
					builder.append(c);
					break;
				}
			}
			token = builder.toString();
			logger.debug(token);
			String className = Toolkit.slashToDot(token);
			classNames.add(className);
		}
		return classNames.toArray(new String[classNames.size()]);
	}

	/**
	 * Removes any whitespace from the string.
	 * 
	 * @param string
	 *            the string to remove whitespace from
	 * @return the string without any whitespace that includes carrige returns etc.
	 */
	public static String stripWhitespace(String string) {
		if (string == null) {
			return string;
		}
		StringBuffer buffer = new StringBuffer();
		char[] chars = string.toCharArray();
		int state = 0;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (Character.isWhitespace(c)) {
				if (state == 1) {
					continue;
				}
				state = 1;
				buffer.append(' ');
			} else {
				state = 0;
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	/**
	 * Removes all the characters from the string that are not defined in the unicode set for all languages. This efectively removes all the binary
	 * data from a string that may come from an executable file or a binary file that is no in human readable format.
	 * 
	 * @param string
	 *            the string to remove the non readable characters from
	 * @return the string sans the non readable characters
	 */
	public static final String stripNonCharacters(String string) {
		if (string == null) {
			return string;
		}
		StringBuffer buffer = new StringBuffer();
		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (Character.isDefined(c)) {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	/**
	 * Takes an object and adds all the fields in the object and all super class objects to a string buffer that can be preinted.
	 * 
	 * @param object
	 *            the object to get all the fields from
	 * @param klass
	 *            the class of the object to start from
	 * @param buffer
	 *            the buffer to put all the field values in
	 * @return a string buffer with all the fields in an object including the super classes
	 */
	public static String toString(Object object, Class<?> klass, StringBuffer buffer) {
		if (object != null && klass != null && buffer != null)
			try {
				buffer.append(String.valueOf(klass.getName())).append("\n");
				Class<?>[] interfaceClasses = klass.getInterfaces();
				for (Class<?> interfaceClass : interfaceClasses) {
					buffer.append("        :");
					buffer.append(interfaceClass.getName());
					buffer.append("\n");
				}
				Field fields[] = klass.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					fields[i].setAccessible(true);
					try {
						Object field = fields[i].get(object);
						buffer.append("        :");
						buffer.append(fields[i].getName());
						buffer.append("=");
						buffer.append(field);
						buffer.append("\n");
					} catch (Throwable t) {
						logger.error("Exception generating string for object " + object, t);
					}
				}
				klass = klass.getSuperclass();
				return toString(object, klass, buffer);
			} catch (Throwable t) {
				logger.error("Exception generating string for object " + object, t);
			}
		return buffer.toString();
	}

	/**
	 * Gets a field in the class or in the heirachy of the class.
	 * 
	 * @param klass
	 *            the original class
	 * @param name
	 *            the name of the field
	 * @return the field in the object or super classes of the object
	 */
	public static Field getField(Class<?> klass, String name) {
		if (klass == null || name == null) {
			return null;
		}
		Field field = null;
		try {
			field = klass.getDeclaredField(name);
		} catch (Throwable t) {
			t.getCause();
		}
		if (field == null) {
			Class<?> superClass = klass.getSuperclass();
			if (superClass != null) {
				field = getField(superClass, name);
			}
		}
		return field;
	}

	/**
	 * Returns the value of the field specified from the object specified.
	 * 
	 * @param object
	 *            the object to get the field value from
	 * @param name
	 *            the name of the field in the object
	 * @return the value of the field if there is such a field or null if there isn't ir if anything goes wrong
	 */
	public static Object getValue(Object object, String name) {
		if (object == null) {
			return null;
		}
		Field field = getField(object.getClass(), name);
		if (field != null) {
			try {
				field.setAccessible(true);
				return field.get(object);
			} catch (Exception e) {
				logger.error("Exception accessing the field's value", e);
			}
		}
		return null;
	}

	/**
	 * Sets the field in the object to the value specified in the parameter list.
	 * 
	 * @param object
	 *            the target object to set the field for
	 * @param name
	 *            the name of the field
	 * @param value
	 *            the value to set for the field
	 * @return whether the field was set or not
	 */
	public static boolean setField(Object object, String name, Object value) {
		if (object == null) {
			return false;
		}
		Field field = getField(object.getClass(), name);
		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(object, value);
				return true;
			} catch (Throwable t) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Deletes all the files recursively and then the directories recursively.
	 * 
	 * @param file
	 *            the directory or file to delete
	 */
	public static void deleteFile(File file) {
		if (file == null || !file.exists() || !file.canWrite()) {
			return;
		}
		if (file.isDirectory()) {
			File files[] = file.listFiles();
			for (int j = 0; j < files.length; j++) {
				file = files[j];
				deleteFile(file);
			}
		}
		if (file.delete()) {
			logger.debug("Deleted file : " + file);
		} else {
			logger.debug("Couldn't delete file : " + file);
		}
	}

	/**
	 * Reads the contents of the file and returns the contents in a byte array form.
	 * 
	 * @param file
	 *            the file to read the contents from
	 * @return the file contents in a byte array output stream
	 * @throws Exception
	 */
	public static ByteArrayOutputStream getContents(File file) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			logger.error("No file by that name.", e);
		} catch (Exception e) {
			logger.error("General error accessing the file " + file, e);
		}
		return getContents(inputStream);
	}

	/**
	 * Reads the contents of the file and returns the contents in a byte array form.
	 * 
	 * @param inputStream
	 *            the file to read the contents from
	 * @return the file contents in a byte array output stream
	 * @throws Exception
	 */
	public static ByteArrayOutputStream getContents(InputStream inputStream) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (inputStream == null) {
			return bos;
		}
		try {
			byte[] bytes = new byte[1024];
			int read;
			while ((read = inputStream.read(bytes)) > -1) {
				bos.write(bytes, 0, read);
			}
			logger.debug("Read bytes : " + bos.toString());
		} catch (Exception e) {
			logger.error("Exception accessing the file contents.", e);
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				logger.error("Exception closing input stream " + inputStream, e);
			}
		}
		return bos;
	}

	/**
	 * Writes the contents of a byte array to a file.
	 * 
	 * @param file
	 *            the file to write to
	 * @param bytes
	 *            the byte data to write
	 */
	public static void setContents(File file, byte[] bytes) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(bytes, 0, bytes.length);
		} catch (FileNotFoundException e) {
			logger.error("File " + file + " not found", e);
		} catch (IOException e) {
			logger.error("IO exception writing file contents", e);
		}

	}

	public static void dump(IDataBase dataBase) {
		Project project = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		List<com.ikokoon.instrumentation.model.Package> packages = project.getChildren();
		for (com.ikokoon.instrumentation.model.Package pakkage : packages) {
			logger.error("Package : " + pakkage + ", " + pakkage.getChildren().size());
			for (com.ikokoon.instrumentation.model.Class klass : pakkage.getChildren()) {
				logger.error("Class : " + klass + ", " + klass.getChildren());
				for (Method method : klass.getChildren()) {
					logger.error("Method : " + method + ", " + method.getChildren().size());
					for (Line line : method.getChildren()) {
						logger.error("Line : " + line);
					}
				}
			}
		}
	}

}
