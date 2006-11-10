package ca.sqlpower.matchmaker.dao.hibernate;

import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;

public abstract class AbstractDAOTestCase<T extends MatchMakerObject, D extends MatchMakerDAO> extends TestCase {

	public MatchMakerSession session = new TestingMatchMakerSession();
	
	public void testFindAll(){
		D dao = getDataAccessObject();
		List<T> all = dao.findAll();
		if (all.size() == 0) {
			testSaveAndLoadInOneSession();
		}
	}
	
	public void testDeleteExisting(){
		T item1 = getNewObjectUnderTest();
		T item2 = getNewObjectUnderTest();
		D dao = getDataAccessObject();
		dao.save(item1);
		dao.save(item2);
		List<T> all = dao.findAll();
		for (T item: all) {
			dao.delete(item);
		}
		assertNotNull("dao returning null list, it should be empty instead",dao.findAll());
		assertEquals("There are still some objects of type "+item1.getClass()+" left",0,dao.findAll().size());
	}
	
	public void testSaveAndLoadInOneSession(){
		T item1 = getNewObjectUnderTest();
		T item2 = getNewObjectUnderTest();
		D dao = getDataAccessObject();
		dao.save(item1);
		dao.save(item2);
		List<T> all = dao.findAll();
		assertTrue("All should  be larger than 1",all.size()>1);
		assertTrue("The "+item1.getClass() + " item1 was not in the list",all.contains(item1));
		assertTrue("The "+item2.getClass() + " item2 was not in the list",all.contains(item2));
	}
	
	
	/**
	 * Should return a new instance of an object that is being used by the 
	 * DAO.  Each new object must be not equal to any of the previously 
	 * created objects.
	 * 
	 * @return a new test object 
	 */
	public abstract T getNewObjectUnderTest();
	
	/**
	 * This should return the data access object that is being tested
	 */
	public abstract D getDataAccessObject();
		
	
}
