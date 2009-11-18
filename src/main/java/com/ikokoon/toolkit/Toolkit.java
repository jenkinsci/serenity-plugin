package com.ikokoon.toolkit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.model.Unique;

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
		return Long.valueOf(Math.abs(hash));
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
		// First try the Class package name if it is in the path, which it should be of course
		if (className.indexOf('$') > -1) {
			if (className.endsWith(".class")) {
				className = className.substring(0, className.lastIndexOf('.'));
			}
			if (className.indexOf('.') > -1) {
				return className.substring(0, className.lastIndexOf('.'));
			}
		}
		try {
			Class<?> klass = Class.forName(className);
			String packageName = klass.getPackage().getName();
			return packageName;
		} catch (ClassNotFoundException e) {
			logger.error("Class not found : " + className, e);
		}
		// Default package or class not found
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
		StringTokenizer tokenizer = new StringTokenizer(signature, ";<>*()[]+-");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			String className = Toolkit.slashToDot(token);
			className = stripByteCodeCharacters(className);
			if (className.trim().equals("")) {
				continue;
			}
			classNames.add(className);
		}
		return classNames.toArray(new String[classNames.size()]);
	}

	private static final String stripByteCodeCharacters(String string) {
		StringBuilder builder = new StringBuilder();
		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			switch (c) {
			case 'B':
			case 'C':
			case 'D':
			case 'F':
			case 'I':
			case 'J':
			case 'L':
			case 'S':
			case 'T':
			case 'V':
			case 'Z':
				if (i <= 4) {
					break;
				}
			default:
				builder.append(c);
				break;
			}
		}
		string = builder.toString();
		if (string.startsWith("I") || string.startsWith("L") || string.startsWith("V") || string.startsWith("D") || string.startsWith("Z")) {
			return stripByteCodeCharacters(string);
		}
		return string;
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
		if (object != null && klass != null && buffer != null) {
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
			} catch (Exception t) {
				logger.error("Exception generating string for object " + object, t);
			}
		}
		if (buffer != null) {
			return buffer.toString();
		}
		return null;
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
	@SuppressWarnings("unchecked")
	public static <E> E getValue(Class<E> klass, Object object, String name) {
		if (object == null) {
			return null;
		}
		Field field = getField(object.getClass(), name);
		if (field != null) {
			try {
				field.setAccessible(true);
				return (E) field.get(object);
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
			logger.warn("Couldn't delete file : " + file);
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
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(bytes, 0, bytes.length);
		} catch (FileNotFoundException e) {
			logger.error("File " + file + " not found", e);
		} catch (IOException e) {
			logger.error("IO exception writing file contents", e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					logger.error("Exception closing the output stream", e);
				}
			}
		}
	}

	/**
	 * Formats a double to the required precision.
	 * 
	 * @param d
	 *            the double to format
	 * @param precision
	 *            the precision for the result
	 * @return the double formatted to the required precision
	 */
	public static double format(double d, int precision) {
		String doubleString = Double.toString(d);
		doubleString = format(doubleString, precision);
		try {
			d = Double.parseDouble(doubleString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return d;
	}

	/**
	 * Formats a string to the desired precision.
	 * 
	 * @param string
	 *            the string to format to a precision
	 * @param precision
	 *            the precision of the result
	 * @return the string formatted to the required precision
	 */
	public static String format(String string, int precision) {
		if (string == null) {
			return string;
		}
		char[] chars = string.trim().toCharArray();
		StringBuilder builder = new StringBuilder();
		int decimal = 1;
		int state = 0;
		int decimals = 0;
		for (char c : chars) {
			switch (c) {
			case '.':
			case ',':
				state = decimal;
				builder.append(c);
				break;
			default:
				if (state == decimal) {
					if (decimals++ >= precision) {
						break;
					}
				}
				builder.append(c);
				break;
			}
		}
		return builder.toString();
	}

	/**
	 * Serializes an object to a byte array then to a base 64 string of the byte array.
	 * 
	 * @param object
	 *            the object to serialise to base 64
	 * @return the string representation of the object in serialised base 64
	 */
	public static String serializeToBase64(Object object) {
		String base64 = null;
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(object);
			byte[] bytes = byteArrayOutputStream.toByteArray();
			base64 = Base64.encode(bytes);
			base64 = base64.replaceAll("\n", "");
			base64 = base64.replaceAll("\r", "");
			base64 = base64.replaceAll("\t", "");
		} catch (Exception e) {
			logger.error("Exception serializing the object : " + object, e);
		}
		return base64;
	}

	/**
	 * De-serializes an object from a base 64 string to an object.
	 * 
	 * @param base64
	 *            the base 64 string representation of the object
	 * @return the object de-serialised from the string or null if an exception is thrown
	 */
	public static Object deserializeFromBase64(String base64) {
		byte[] bytes = Base64.decode(base64);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			return objectInputStream.readObject();
		} catch (Exception e) {
			logger.error("Exception deserializing the object from the base 64 string : " + base64, e);
		}
		return null;
	}

	/**
	 * Returns an array of values that are defined as being a unique combination for the entity by using the Unique annotation for the class.
	 * 
	 * @param <T>
	 *            the type of object to be inspected for unique fields
	 * @param t
	 *            the object t inspect for unique field combinations
	 * @return the array of unique field values for the entity
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] getUniqueValues(T t) {
		Unique unique = t.getClass().getAnnotation(Unique.class);
		if (unique == null) {
			return (T[]) new Object[] { t };
		}
		String[] fields = unique.fields();
		List<T> values = new ArrayList<T>();
		for (String field : fields) {
			Object value = Toolkit.getValue(Object.class, t, field);
			T[] uniqueValues = (T[]) getUniqueValues(value);
			for (T uniqueValue : uniqueValues) {
				values.add(uniqueValue);
			}
		}
		return (T[]) values.toArray(new Object[values.size()]);
	}

	/**
	 * This function will copy files or directories from one location to another. note that the source and the destination must be mutually exclusive.
	 * This function can not be used to copy a directory to a sub directory of itself. The function will also have problems if the destination files
	 * already exist.
	 * 
	 * @param src
	 *            A File object that represents the source for the copy
	 * @param dest
	 *            A File object that represents the destination for the copy.
	 */
	public static void copyFile(File src, File dest) {
		// Check to ensure that the source is valid...
		if (!src.exists()) {
			logger.warn("Source file/directory does not exist : " + src);
			return;
		} else if (!src.canRead()) { // check to ensure we have rights to the source...
			logger.warn("Source file/directory not readable : " + src);
			return;
		}
		// is this a directory copy?
		if (src.isDirectory()) {
			if (!dest.exists()) { // does the destination already exist?
				// if not we need to make it exist if possible (note this is mkdirs not mkdir)
				if (!dest.mkdirs()) {
					logger.warn("Could not create the new destination directory : " + dest);
				}
			}
			// get a listing of files...
			String list[] = src.list();
			// copy all the files in the list.
			for (int i = 0; i < list.length; i++) {
				File dest1 = new File(dest, list[i]);
				File src1 = new File(src, list[i]);
				copyFile(src1, dest1);
			}
		} else {
			// This was not a directory, so lets just copy the file
			FileInputStream fin = null;
			FileOutputStream fout = null;
			byte[] buffer = new byte[4096]; // Buffer 4K at a time (you can change this).
			int bytesRead;
			try {
				// open the files for input and output
				fin = new FileInputStream(src);
				fout = new FileOutputStream(dest);
				// while bytesRead indicates a successful read, lets write...
				while ((bytesRead = fin.read(buffer)) >= 0) {
					fout.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) { // Error copying file...
				logger.error("Exception copying the source " + src + ", to destination : " + dest, e);
			} finally { // Ensure that the files are closed (if they were open).
				if (fin != null) {
					try {
						fin.close();
					} catch (Exception e) {
						logger.error("Exception closing the source input stream : " + fin, e);
					}
				}
				if (fout != null) {
					try {
						fout.close();
					} catch (Exception e) {
						logger.error("Exception closing the destination output stream : " + fout, e);
					}
				}
			}
		}
	}

	/**
	 * If Java 1.4 is unavailable, the following technique may be used.
	 * 
	 * @param aInput
	 *            is the original String which may contain substring aOldPattern
	 * @param aOldPattern
	 *            is the non-empty substring which is to be replaced
	 * @param aNewPattern
	 *            is the replacement for aOldPattern
	 */
	public static String replaceOld(final String aInput, final String aOldPattern, final String aNewPattern) {
		if (aOldPattern.equals("")) {
			throw new IllegalArgumentException("Old pattern must have content.");
		}
		final StringBuffer result = new StringBuffer();
		// startIdx and idxOld delimit various chunks of aInput; these
		// chunks always end where aOldPattern begins
		int startIdx = 0;
		int idxOld = 0;
		while ((idxOld = aInput.indexOf(aOldPattern, startIdx)) >= 0) {
			// grab a part of aInput which does not include aOldPattern
			result.append(aInput.substring(startIdx, idxOld));
			// add aNewPattern to take place of aOldPattern
			result.append(aNewPattern);

			// reset the startIdx to just after the current match, to see
			// if there are any further matches
			startIdx = idxOld + aOldPattern.length();
		}
		// the final chunk will go to the end of aInput
		result.append(aInput.substring(startIdx));
		return result.toString();
	}

}
