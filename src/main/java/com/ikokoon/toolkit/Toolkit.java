package com.ikokoon.toolkit;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Unique;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains methods for changing a string to the byte code representation and visa versa. Also some other nifty functions like
 * stripping a string of white space etc.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.07.09
 */
public final class Toolkit {

    /**
     * The LOGGER.
     */
    private static Logger logger = Logger.getLogger(Toolkit.class.getName());

    /**
     * Simple, fast hash function to generate quite unique hashes from strings(i.e. toCharArray()).
     *
     * @param string the string to generate the hash from
     * @return the integer representation of the hash of the string characters, typically quite unique for strings less than 10 characters
     */
    public static Long hash(final String string) {
        // Must be prime of course
        long seed = 131; // 31 131 1313 13131 131313 etc..
        long hash = 0;
        char[] chars = string.toCharArray();
        for (final char aChar : chars) {
            hash = (hash * seed) + aChar;
        }
        return Math.abs(hash);
    }

    /**
     * Builds a hash from an array of objects.
     *
     * @param objects the objects to build the hash from
     * @return the hash of the objects
     */
    public static Long hash(final Object... objects) {
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) {
            builder.append(object);
        }
        return Toolkit.hash(builder.toString());
    }

    /**
     * This method replaces the / in the byte code name with . which is XML friendly.
     *
     * @param name the byte code name of a class
     * @return the Java name of the class
     */
    public static String slashToDot(final String name) {
        if (name == null) {
            return null;
        }
        return name.replace('/', '.').replace('\\', '.');
    }

    /**
     * This method replaces the . in the byte code name with / which is what we expect from byte code.
     *
     * @param name the name of the class or package
     * @return the byte code name of the class or package
     */
    public static String dotToSlash(final String name) {
        if (name == null) {
            return null;
        }
        return name.replace('.', '/');
    }

    /**
     * Takes the name of a class and returns the package name for the class.
     *
     * @param className the name of the class fully qualified
     * @return the package name of the class
     */
    public static String classNameToPackageName(final String className) {
        String strippedClassName = Toolkit.slashToDot(className);
        int index = strippedClassName.lastIndexOf('.');
        if (index > -1) {
            return strippedClassName.substring(0, index);
        }
        // Default and exception package
        return "";
    }

    /**
     * Removes any whitespace from the string.
     *
     * @param string the string to remove whitespace from
     * @return the string without any whitespace that includes carriage returns etc.
     */
    public static String stripWhitespace(final String string) {
        if (string == null) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        char[] chars = string.toCharArray();
        int state = 0;
        for (char c : chars) {
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
     * Gets a field in the class or in the heirachy of the class.
     *
     * @param klass the original class
     * @param name  the name of the field
     * @return the field in the object or super classes of the object
     */
    private static Field getField(final Class<?> klass, final String name) {
        Class<?> targetClass = klass;
        do {
            try {
                Field field = targetClass.getDeclaredField(name);
                if (field != null) {
                    return field;
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null);
        return null;
    }

    /**
     * Returns the value of the field specified from the object specified.
     *
     * @param klass  the class to get the value for
     * @param object the object to get the field value from
     * @param name   the name of the field in the object
     * @param <E>    the type to return
     * @return the value of the field if there is such a field or null if there isn't ir if anything goes wrong
     */
    @SuppressWarnings({"unchecked", "WeakerAccess", "UnusedParameters"})
    public static <E> E getValue(final Class<E> klass, final Object object, final String name) {
        if (object == null) {
            return null;
        }
        Field field = getField(object.getClass(), name);
        if (field != null) {
            try {
                field.setAccessible(true);
                return (E) field.get(object);
            } catch (final Exception e) {
                logger.log(Level.SEVERE, "Exception accessing the field's value", e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <E> E executeMethod(final Object object, final String methodName, final Object[] parameters) {
        Class<?>[] parameterTypes = new Class[parameters.length];
        int index = 0;
        for (Object parameter : parameters) {
            parameterTypes[index++] = parameter.getClass();
        }
        try {
            Method method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return (E) method.invoke(object, parameters);
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, "Security exception", e);
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "No such method exception", e);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Illegal argument exception", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Illegal access exception", e);
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE, "Target invocation exception", e);
        }
        return null;
    }

    /**
     * Deletes all the files recursively and then the directories recursively.
     *
     * @param file          the directory or file to delete
     * @param maxRetryCount the  number of times to retry to delete the file
     */
    public static void deleteFile(final File file, final int maxRetryCount) {
        deleteFile(file, maxRetryCount, 0);
    }

    private static void deleteFile(final File file, final int maxRetryCount, final int retryCount) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File children[] = file.listFiles();
            assert children != null;
            for (final File child : children) {
                deleteFile(child, maxRetryCount, 0);
            }
        }
        logger.info("File : " + file);
        if (file.delete()) {
            logger.info("Deleted file : " + file);
        } else {
            if (retryCount >= maxRetryCount) {
                if (file.exists()) {
                    logger.info("Couldn't delete file : " + file);
                }
            } else {
                logger.info("Retrying count : " + retryCount + ", file : " + file);
                deleteFile(file, maxRetryCount, retryCount + 1);
            }
        }
    }

    /**
     * Deletes all the files in a directory with one of the specified extensions.
     *
     * @param file       the file to delete or the directory to delete files in
     * @param extensions the extensions of files to delete
     */
    public static void deleteFiles(final File file, final String... extensions) {
        if (file == null || !file.exists() || !file.canWrite()) {
            return;
        }
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            if (files != null) {
                for (final File newFile : files) {
                    deleteFiles(newFile, extensions);
                }
            }
        }
        if (file.isFile()) {
            String fileName = file.getName();
            for (String extension : extensions) {
                if (fileName.endsWith(extension)) {
                    if (file.delete()) {
                        logger.fine("Deleted file : " + file);
                    } else {
                        logger.warning("Couldn't delete file : " + file);
                    }
                }
            }
        }
    }

    /**
     * This is the interface to select files with below.
     *
     * @author Michael Couck
     * @version 01.00
     * @since 10.01.10
     */
    public interface IFileFilter {
        boolean matches(final File file);
    }

    /**
     * Finds files on the file system below the directory specified recursively using the selection criteria supplied in the IFileFilter
     * parameter.
     *
     * @param file   the file to start looking from
     * @param filter the filter to select files with
     * @param list   the list of files to add the selected files to
     */
    public static void findFiles(final File file, final IFileFilter filter, final List<File> list) {
        if (file == null || !file.exists()) {
            return;
        }
        if (filter.matches(file)) {
            list.add(file);
        }
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (int j = 0; files != null && j < files.length; j++) {
                findFiles(files[j], filter, list);
            }
        }
    }

    /**
     * Reads the contents of the file and returns the contents in a byte array form.
     *
     * @param file the file to read the contents from
     * @return the file contents in a byte array output stream
     */
    public static ByteArrayOutputStream getContents(final File file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (final FileNotFoundException e) {
            logger.log(Level.SEVERE, "No file by that name.", e);
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "General error accessing the file " + file, e);
        }
        return getContents(inputStream);
    }

    /**
     * Reads the contents of the file and returns the contents in a byte array form.
     *
     * @param inputStream the file to read the contents from
     * @return the file contents in a byte array output stream
     */
    public static ByteArrayOutputStream getContents(final InputStream inputStream) {
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
            logger.fine("Read bytes : " + bos.toString(IConstants.ENCODING));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception accessing the file contents.", e);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception closing input stream " + inputStream, e);
            }
        }
        return bos;
    }

    public static String getStringContents(final InputStream inputStream) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = getContents(inputStream);
            return byteArrayOutputStream.toString(IConstants.ENCODING);
        } catch (final UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, IConstants.ENCODING + " not supported on this platform : ", e);
        }
        return null;
    }

    /**
     * Writes the contents of a byte array to a file.
     *
     * @param file  the file to write to
     * @param bytes the byte data to write
     */
    public static void setContents(final File file, final byte[] bytes) {
        FileOutputStream fileOutputStream = null;
        try {
            //noinspection ResultOfMethodCallIgnored
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes, 0, bytes.length);
        } catch (final FileNotFoundException e) {
            logger.log(Level.SEVERE, "File " + file + " not found", e);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "IO exception writing file contents", e);
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "General exception setting the file contents", e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Exception closing the output stream", e);
                }
            }
        }
    }

    /**
     * Formats a double to the required precision.
     *
     * @param d         the double to format
     * @param precision the precision for the result
     * @return the double formatted to the required precision
     */
    public static double format(final double d, final int precision) {
        String doubleString = Double.toString(d);
        doubleString = format(doubleString, precision);
        try {
            return Double.parseDouble(doubleString);
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Exception formatting : " + d + ", " + precision, e);
        }
        return d;
    }

    /**
     * Formats a string to the desired precision.
     *
     * @param string    the string to format to a precision
     * @param precision the precision of the result
     * @return the string formatted to the required precision
     */
    public static String format(final String string, final int precision) {
        if (string == null) {
            return null;
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
     * Returns an array of values that are defined as being a unique combination for the entity by using the Unique annotation for the
     * class.
     *
     * @param <T> the type of object to be inspected for unique fields
     * @param t   the object t inspect for unique field combinations
     * @return the array of unique field values for the entity
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] getUniqueValues(final T t) {
        Unique unique = t.getClass().getAnnotation(Unique.class);
        if (unique == null) {
            return (T[]) new Object[]{t};
        }
        String[] fields = unique.fields();
        List<T> values = new ArrayList<>();
        for (String field : fields) {
            Object value = Toolkit.getValue(Object.class, t, field);
            T[] uniqueValues = (T[]) getUniqueValues(value);
            Collections.addAll(values, uniqueValues);
        }
        //noinspection SuspiciousToArrayCall
        return (T[]) values.toArray(new Object[values.size()]);
    }

    public static void copyFile(final File in, final File out) {
        if (!out.getParentFile().exists()) {
            if (!out.getParentFile().mkdirs()) {
                logger.info("Didn't create parent directories : " + out.getParentFile().getAbsolutePath());
            }
        }
        if (!out.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                out.createNewFile();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception creating new file : " + out.getAbsolutePath(), e);
            }
        }
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(in).getChannel();
            outChannel = new FileOutputStream(out).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception copying file : " + in + ", to : " + out, e);
        } finally {
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "", e);
                }
            }
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "", e);
                }
            }
        }
    }

    /**
     * This function will copy files or directories from one location to another. note that the source and the destination must be mutually
     * exclusive. This function can not be used to copy a directory to a sub directory of itself. The function will also have problems if
     * the destination files already exist.
     *
     * @param src  A File object that represents the source for the copy
     * @param dest A File object that represents the destination for the copy.
     */
    static void copyFiles(final File src, final File dest) {
        // Check to ensure that the source is valid...
        if (!src.exists()) {
            logger.warning("Source file/directory does not exist : " + src);
            return;
        } else if (!src.canRead()) { // check to ensure we have rights to the source...
            logger.warning("Source file/directory not readable : " + src);
            return;
        }
        // is this a directory copy?
        if (src.isDirectory()) {
            if (!dest.exists()) { // does the destination already exist?
                // if not we need to make it exist if possible (note this is mkdirs not mkdir)
                if (!dest.mkdirs()) {
                    logger.warning("Could not create the new destination directory : " + dest);
                }
            }
            // get a listing of files...
            String children[] = src.list();
            // copy all the files in the list.
            if (children != null) {
                for (final String aChildren : children) {
                    File childSrc = new File(src, aChildren);
                    File childDest = new File(dest, aChildren);
                    copyFiles(childSrc, childDest);
                }
            }
        } else {
            // This was not a directory, so lets just copy the file
            copyFile(src, dest);
        }
    }

    /**
     * If Java 1.4 is unavailable, the following technique may be used.
     *
     * @param aInput      is the original String which may contain substring aOldPattern
     * @param aOldPattern is the non-empty substring which is to be replaced
     * @param aNewPattern is the replacement for aOldPattern
     * @return the new string with the replaced characters
     */
    public static String replaceAll(final String aInput, final String aOldPattern, final String aNewPattern) {
        if (aOldPattern.equals("")) {
            throw new IllegalArgumentException("Old pattern must have content.");
        }
        final StringBuilder result = new StringBuilder();
        // startIdx and idxOld delimit various chunks of aInput; these
        // chunks always end where aOldPattern begins
        int startIdx = 0;
        int idxOld;
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

    public static boolean createFile(final File file) {
        boolean allCreated = file == null;
        if (file != null && file.getParentFile() != null && !file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                logger.warning("Directory : " + file.getParent() + ", not created.");
                allCreated = false;
            }
        }
        if (file != null && !file.exists()) {
            try {
                if (!file.createNewFile()) {
                    logger.warning("Didn't create file : " + file.getAbsolutePath());
                    allCreated = false;
                }
            } catch (final Exception e) {
                logger.log(Level.SEVERE, "Exception creating file : " + file, e);
            }
        }
        if (file != null && file.exists()) {
            allCreated = true;
        }
        return allCreated;
    }

    /**
     * Gets a single directory. First looking to find it, if it can not be found then it is created.
     *
     * @param file the directory that is requested
     * @return the found or newly created {@link File} or <code>null</code> if something went wrong.
     */
    public static synchronized File getOrCreateDirectory(final File file) {
        try {
            if (file.exists() && file.isDirectory()) {
                return file;
            }
            boolean created = file.mkdirs();
            if (created && file.exists()) {
                return file;
            }
            return null;
        } finally {
            Toolkit.class.notifyAll();
        }
    }

    /**
     * Verifies that all the characters in a string are digits, ie. the string is a number.
     *
     * @param string the string to verify for digit data
     * @return whether every character in a string is a digit
     */
    public static boolean isDigits(final String string) {
        if (string == null || string.trim().equals("")) {
            return false;
        }
        char[] chars = string.toCharArray();
        for (char c : chars) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static String cleanFilePath(final String path) {
        String filePath = replaceAll(path, "/./", "/");
        filePath = replaceAll(filePath, "\\.\\", "/");
        filePath = replaceAll(filePath, "\\", "/");
        // filePath = filePath.removeEnd(filePath, ".");
        if (filePath.endsWith(".")) {
            filePath = filePath.substring(0, filePath.length() - 1);
        }
        return filePath;
    }

}
