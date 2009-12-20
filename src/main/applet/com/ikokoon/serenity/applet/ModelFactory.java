package com.ikokoon.serenity.applet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;

import com.ikokoon.serenity.model.IModel;
import com.ikokoon.toolkit.Base64;

public class ModelFactory {

	protected static IModel getModel(URL documentBase, String uri) {
		try {
			URL url = new URL(documentBase, uri);
			InputStream inputStream = url.openStream();
			String base64 = getContents(inputStream).toString();
			return (IModel) deserializeFromBase64(base64);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Reads the contents of the file and returns the contents in a byte array form.
	 * 
	 * @param inputStream
	 *            the file to read the contents from
	 * @return the file contents in a byte array output stream
	 * @throws Exception
	 */
	private static ByteArrayOutputStream getContents(InputStream inputStream) {
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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bos;
	}

	protected static String projectBase64 = "rO0ABXNyACBjb20uaWtva29vbi5zZXJlbml0eS5tb2RlbC5Nb2RlbPHdBGNwC4A5AgADTAAGbGVnZW5kdAAQTGphdmEvdXRpbC9MaXN0O0wAB21ldHJpY3NxAH4AAUwABG5hbWV0ABJMamF2YS9sYW5nL1N0cmluZzt4cHNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAAFdwQAAAAKdAAIQ292ZXJhZ2V0AApDb21wbGV4aXR5dAAMQWJzdHJhY3RuZXNzdAAJU3RhYmlsaXR5dAAIRGlzdGFuY2V4c3EAfgAEAAAABXcEAAAACnNxAH4ABAAAAAJ3BAAAAApzcgAQamF2YS5sYW5nLkRvdWJsZYCzwkopa/sEAgABRAAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHBAOAAAAAAAAHNxAH4ADUBWQAAAAAAAeHNxAH4ABAAAAAJ3BAAAAApzcQB+AA1AMQAAAAAAAHNxAH4ADUAUAAAAAAAAeHNxAH4ABAAAAAJ3BAAAAApzcQB+AA1AOQAAAAAAAHNxAH4ADUBQQAAAAAAAeHNxAH4ABAAAAAJ3BAAAAApzcQB+AA1AQgAAAAAAAHNxAH4ADUBXQAAAAAAAeHNxAH4ABAAAAAJ3BAAAAApzcQB+AA1AUEAAAAAAAHNxAH4ADUBAgAAAAAAAeHh0ACJjb20uaWtva29vbi5zZXJlbml0eS5tb2RlbC5Qcm9qZWN0";
	protected static String packageBase64 = "rO0ABXNyACBjb20uaWtva29vbi5zZXJlbml0eS5tb2RlbC5Nb2RlbPHdBGNwC4A5AgADTAAGbGVnZW5kdAAQTGphdmEvdXRpbC9MaXN0O0wAB21ldHJpY3NxAH4AAUwABG5hbWV0ABJMamF2YS9sYW5nL1N0cmluZzt4cHNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAAFdwQAAAAKdAAIQ292ZXJhZ2V0AApDb21wbGV4aXR5dAAMQWJzdHJhY3RuZXNzdAAJU3RhYmlsaXR5dAAIRGlzdGFuY2V4c3EAfgAEAAAABXcEAAAACnNxAH4ABAAAAAJ3BAAAAApzcgAQamF2YS5sYW5nLkRvdWJsZYCzwkopa/sEAgABRAAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHBAT4AAAAAAAHNxAH4ADUA3AAAAAAAAeHNxAH4ABAAAAAJ3BAAAAApzcQB+AA1AMQAAAAAAAHNxAH4ADUBNgAAAAAAAeHNxAH4ABAAAAAJ3BAAAAApzcQB+AA1AOQAAAAAAAHNxAH4ADUBCAAAAAAAAeHNxAH4ABAAAAAJ3BAAAAApzcQB+AA1AUIAAAAAAAHNxAH4ADUAUAAAAAAAAeHNxAH4ABAAAAAJ3BAAAAApzcQB+AA1AUQAAAAAAAHNxAH4ADUA1AAAAAAAAeHh0ABJjb20uaWtva29vbi50YXJnZXQ=";
	protected static String classBase64 = "rO0ABXNyACBjb20uaWtva29vbi5zZXJlbml0eS5tb2RlbC5Nb2RlbPHdBGNwC4A5AgADTAAGbGVnZW5kdAAQTGphdmEvdXRpbC9MaXN0O0wAB21ldHJpY3NxAH4AAUwABG5hbWV0ABJMamF2YS9sYW5nL1N0cmluZzt4cHNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAADdwQAAAAKdAAIQ292ZXJhZ2V0AApDb21wbGV4aXR5dAAJU3RhYmlsaXR5eHNxAH4ABAAAAAN3BAAAAApzcQB+AAQAAAACdwQAAAAKc3IAEGphdmEubGFuZy5Eb3VibGWAs8JKKWv7BAIAAUQABXZhbHVleHIAEGphdmEubGFuZy5OdW1iZXKGrJUdC5TgiwIAAHhwQFRAAAAAAABzcQB+AAtAFAAAAAAAAHhzcQB+AAQAAAACdwQAAAAKc3EAfgALQDMAAAAAAABzcQB+AAtANQAAAAAAAHhzcQB+AAQAAAACdwQAAAAKc3EAfgALQDeAAAAAAABzcQB+AAtAViAAAAAAAHh4dAAZY29tLmlrb2tvb24udGFyZ2V0LlRhcmdldA==";

	/**
	 * This generates a dummy model for testing and the initial data displayed rather than nothing.
	 * 
	 * @return a generated dummy model
	 */
	protected static IModel getModel() {
		IModel model = (IModel) deserializeFromBase64(projectBase64);
		return model;
	}
}
