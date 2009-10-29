package com.ikokoon.persistence;

import java.io.File;
import java.util.List;

import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.Objects;

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

	/** The object database from Neodatis. */
	private ODB odb = null; // ODBFactory.open(IConstants.DATABASE_FILE);
	/** The project for the build. */
	private Project project;
	private String dataBaseFile;
	/** The closed flag. */
	private boolean closed = true;

	public DataBaseXml(String dataBaseFile, boolean create) {
		this.dataBaseFile = dataBaseFile;
		logger.info("Opening database on file : " + dataBaseFile);
		try {
			if (create) {
				File file = new File(this.dataBaseFile);
				if (!file.delete()) {
					logger.warn("Couldn't delete old database file");
				}
			}
			odb = ODBFactory.open(this.dataBaseFile);
			Objects objects = odb.getObjects(Project.class);
			if (objects.hasNext()) {
				project = (Project) objects.getFirst();
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			try {
				odb.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		// try {
		// InputStream is = new FileInputStream(dataBaseFile);
		// ObjectInputStream ois = new ObjectInputStream(is);
		// Object object = ois.readObject();
		// logger.error(object);
		// project = (Project) object;
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		if (project == null) {
			project = new Project();
		}

		project.getIndex().clear();
		persist(project);
		closed = false;
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
		return closed;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final void close() {
		if (closed) {
			logger.info("User tried to close the database again");
			return;
		}
		logger.info("Comitting and closing the database");

		try {
			// OutputStream os = new FileOutputStream(dataBaseFile);
			// ObjectOutputStream ois = new ObjectOutputStream(os);
			// project.getIndex().clear();
			// ois.writeObject(project);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			project.getIndex().clear();
			odb = ODBFactory.open(this.dataBaseFile);
			odb.store(project);
			odb.commit();
			odb.close();
		} catch (Exception e) {
			logger.error("", e);
		}
		closed = true;
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
		}
		// logger.info("Set id for : " + composite + ", unique values : " + Arrays.asList(uniqueValues) + ", id : " + id);
		// Insert the object into the index
		insert(project.getIndex(), composite, composite.getId());
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
			if (midVal < key) {
				low = mid + 1;
			} else if (midVal > key) {
				high = mid - 1;
			} else {
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
				break;
			}
		}
	}

}