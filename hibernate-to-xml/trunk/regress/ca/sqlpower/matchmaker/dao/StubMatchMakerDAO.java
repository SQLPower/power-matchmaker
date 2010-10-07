package ca.sqlpower.matchmaker.dao;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * A stub DAO that doesn't save or load anything.
 *
 * @param <T>
 */
public class StubMatchMakerDAO<T> implements MatchMakerDAO<T> {

	Logger logger = Logger.getLogger(StubMatchMakerDAO.class);
	private Class<T> businessClass;

	public StubMatchMakerDAO(Class<T> businessClass) {
		this.businessClass = businessClass;
	}
	
	public void delete(Object deleteMe) {
		logger.debug("Stub call: StubMatchMakerDAO.delete()");

	}

	public List<T> findAll() {
		logger.debug("Stub call: StubMatchMakerDAO.findAll()");
		return Collections.emptyList();
	}

	public Class<T> getBusinessClass() {
		logger.debug("Stub call: StubMatchMakerDAO.getBusinessClass()");
		return businessClass;
	}

	public void save(Object saveMe) {
		logger.debug("Stub call: StubMatchMakerDAO.save()");

	}
}