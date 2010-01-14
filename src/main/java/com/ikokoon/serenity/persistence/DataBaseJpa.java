package com.ikokoon.serenity.persistence;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.toolkit.Toolkit;

public class DataBaseJpa extends DataBase {

	private Logger logger = Logger.getLogger(this.getClass());
	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = IConstants.SERENITY_PERSISTENCE_UNIT)
	private EntityManager entityManager;
	private boolean closed = true;

	DataBaseJpa() {
		getEntityManager();
		closed = false;
	}

	public synchronized <E extends Composite<?, ?>> E find(Class<E> klass, Long id) {
		boolean commit = begin();
		try {
			E e = entityManager.find(klass, id);
			logger.info("find(klass, id) : " + e);
			return e;
		} catch (Exception e) {
			logger.error("Exception finding : " + klass + ", " + id, e);
		} finally {
			commit(commit);
		}
		return null;
	}

	public synchronized <E extends Composite<?, ?>> E find(Class<E> klass, List<Object> parameters) {
		Long id = Toolkit.hash(parameters.toArray());
		return find(klass, id);
	}

	@SuppressWarnings("unchecked")
	public synchronized <E extends Composite<?, ?>> List<E> find(Class<E> klass) {
		return entityManager.createQuery("from " + klass.getSimpleName()).getResultList();
	}

	public synchronized boolean isClosed() {
		return closed;
	}

	public synchronized <E extends Composite<?, ?>> E persist(E composite) {
		// setId(composite);
		boolean commit = begin();
		entityManager.persist(composite);
		// entityManager.refresh(composite);
		commit(commit);
		return composite;
	}

	public synchronized <E extends Composite<?, ?>> E remove(Class<E> klass, Long id) {
		E composite = find(klass, id);
		if (composite != null) {
			boolean commit = begin();
			entityManager.remove(composite);
			commit(commit);
		}
		return composite;
	}

	public synchronized void close() {
		logger.info("Closing the JPA database : " + entityManager.isOpen());
		// if (entityManager.isOpen()) {
		// boolean commit = begin();
		// try {
		// entityManager.flush();
		// } finally {
		// commit(commit);
		// }
		// entityManager.close();
		// closed = true;
		// }
	}

	/**
	 * This method starts a transaction for the entity manager.
	 */
	private synchronized boolean begin() {
		EntityTransaction transaction = entityManager.getTransaction();
		if (!transaction.isActive()) {
			entityManager.getTransaction().begin();
			return true;
		}
		return false;
	}

	/**
	 * This method commits the transaction for he entity manager.
	 */
	private synchronized void commit(boolean commit) {
		if (commit) {
			EntityTransaction transaction = entityManager.getTransaction();
			if (transaction.isActive()) {
				if (!transaction.getRollbackOnly()) {
					transaction.commit();
				} else {
					transaction.rollback();
				}
			}
		}
	}

	/**
	 * Initialises the entity manager.
	 */
	private synchronized EntityManager getEntityManager() {
		if (entityManager == null) {
			try {
				EntityManagerFactory factory = Persistence.createEntityManagerFactory(IConstants.SERENITY_PERSISTENCE_UNIT);
				entityManager = factory.createEntityManager();
			} catch (Exception e) {
				logger.error("Error setting up the entity manager in stand alone.", e);
			}
		}
		return entityManager;
	}

	public <E extends Composite<?, ?>> List<E> find(Class<E> klass, Map<String, Object> parameters) {
		// TODO implement me
		throw new RuntimeException("Not implempented.");
	}

}
