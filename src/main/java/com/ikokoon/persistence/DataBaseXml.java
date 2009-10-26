package com.ikokoon.persistence;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;

import com.ikokoon.instrumentation.model.IComposite;
import com.ikokoon.instrumentation.model.Project;
import com.ikokoon.toolkit.Toolkit;

/**
 * This database implementation is in memory and serializes the data model to XML when it closes.
 * 
 * @author Michael Couck
 * @since 11.10.09
 * @version 01.00
 */
public class DataBaseXml extends ADataBase {

	private Project project;

	/**
	 * Constructor tries to open the XML data model and load the existing data into memory from the previous run. This assists in the speed of
	 * execution as the insertion into the data model takes far longer than selection due to the re-indexing of the indexes etc.
	 */
	public DataBaseXml(File file) {
		this(file, null);
	}

	public DataBaseXml(File file, ClassLoader classLoader) {
		logger.info("Initilizing the database data model in memory");
		InputStream inputStream = null;
		try {
			file = getFile(file);
			inputStream = new FileInputStream(file);
			XMLDecoder decoder = new XMLDecoder(inputStream);
			project = (Project) decoder.readObject();
		} catch (NoSuchElementException e) {
			logger.info("No data generated for the project yet? First run.");
		} catch (Exception e) {
			logger.error("Exception reading the data from the serialized file", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		if (project == null) {
			project = new Project();
			persist(project);
		}
		logger.info("Finished initilizing the database data model in memory");
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final IComposite persist(IComposite composite) {
		setIds(composite);
		logger.debug("Persisted object : " + composite);
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final IComposite find(Long id) {
		List<IComposite> index = project.getIndex();
		return search(index, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final IComposite find(List<Object> parameters) {
		Long id = Toolkit.hash(parameters.toArray());
		List<IComposite> index = project.getIndex();
		return search(index, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final IComposite remove(Long id) {
		IComposite composite = find(id);
		composite.getParent().getChildren().remove(composite);
		composite.setParent(null);
		project.getIndex().remove(composite);
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final boolean isClosed() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final void close() {
		logger.info("Comitting and closing the database");
		FileOutputStream fileOutputStream = null;
		try {
			File file = getFile(null);
			fileOutputStream = new FileOutputStream(file);
			XMLEncoder encoder = new XMLEncoder(fileOutputStream);
			encoder.writeObject(project);
			encoder.close();
		} catch (Exception e) {
			logger.error("Couldn't find the database file? Permissioning on the OS perhaps?", e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (Exception e) {
					logger.error("Exception closing the output stream to the database file", e);
				}
			}
		}
	}

	/**
	 * This method sets the ids in a graph of objects. The objects need to be stored, perhaps using the top level object in the heirachy, then the
	 * database is consulted for it's uid for the object. The uid is set in the field that has the Identifier annotation on the setter method for the
	 * field.
	 * 
	 * @param <T>
	 *            the type of object
	 * @param object
	 *            the object to set the id for
	 * @param list
	 *            a list of already set id fields
	 */
	protected synchronized final <T> void setIds(IComposite composite) {
		if (composite == null) {
			return;
		}
		if (composite.getId() == null) {
			Object[] uniqueValues = getUniqueValues(composite);
			Long id = Toolkit.hash(uniqueValues);
			composite.setId(id);
			// logger.info("Set id for : " + composite + ", unique values : " + Arrays.asList(uniqueValues) + ", id : " + id);
			// Insert the object into the index
			insert(project.getIndex(), composite, id);
		}
		List<IComposite> children = composite.getChildren();
		for (IComposite child : children) {
			setIds(child);
		}
	}

	protected IComposite search(List<IComposite> index, long key) {
		int low = 0;
		int high = index.size() - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			IComposite composite = index.get(mid);
			long midVal = composite.getId();
			int cmp;
			if (midVal < key) {
				cmp = -1; // Neither val is NaN, thisVal is smaller
			} else if (midVal > key) {
				cmp = 1; // Neither val is NaN, thisVal is larger
			} else {
				long midBits = Double.doubleToLongBits(midVal);
				long keyBits = Double.doubleToLongBits(key);
				cmp = (midBits == keyBits ? 0 : // Values are equal
						(midBits < keyBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
								1)); // (0.0, -0.0) or (NaN, !NaN)
			}
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else {
				return composite;
			}
		}
		return null;
	}

	protected void insert(List<IComposite> index, IComposite toInsert, long key) {
		if (index.size() == 0) {
			index.add(toInsert);
			return;
		}
		int low = 0;
		int high = index.size();
		while (low <= high) {
			int mid = (low + high) >>> 1;
			if (mid >= index.size()) {
				index.add(mid, toInsert);
				break;
			}
			IComposite composite = index.get(mid);
			long midVal = composite.getId();
			// logger.info("Low : " + low + ", high : " + high + ", mid : " + mid + ", mid val : " + midVal);
			if (midVal < key) {
				int next = mid + 1;
				if (index.size() > next) {
					IComposite nextComposite = index.get(next);
					long nextVal = nextComposite.getId();
					if (nextVal > key) {
						index.add(next, toInsert);
						break;
					}
				}
				low = mid + 1;
			} else if (midVal > key) {
				int previous = mid - 1;
				if (previous >= 0) {
					IComposite previousComposite = index.get(previous);
					long previousVal = previousComposite.getId();
					if (previousVal < key) {
						index.add(mid, toInsert);
						break;
					}
				} else {
					index.add(0, toInsert);
					break;
				}
				high = mid - 1;
			} else {
				// Found key? Duplicate?
				// throw new RuntimeException("Duplicate key found : " + toInsert + ", " + key);
				break;
			}
		}
	}

}