package ca.sqlpower.matchmaker.dao;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;

public abstract class AbstractDAOTestCase<T extends MatchMakerObject, D extends MatchMakerDAO> extends TestCase {

	public MatchMakerSession session = new TestingMatchMakerSession();
	
	public void testFindAll() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
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
	
	public void testSaveAndLoadInOneSession() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		T item1 = getNewObjectUnderTest();
		T item2 = getNewObjectUnderTest();
		D dao = getDataAccessObject();
		dao.save(item1);
		dao.save(item2);
		List<T> all = dao.findAll();
		assertTrue("All should  be larger than 1",all.size()>1);
		assertTrue("The "+item1.getClass() + " item1 was not in the list",all.contains(item1));
		assertTrue("The "+item2.getClass() + " item2 was not in the list",all.contains(item2));
		
		T savedItem1 = all.get(all.indexOf(item1));
		List<PropertyDescriptor> properties;
		properties = Arrays.asList(PropertyUtils.getPropertyDescriptors(item1.getClass()));
		
		List<PropertyDescriptor> gettableProperties = new ArrayList<PropertyDescriptor>();
		for (PropertyDescriptor d: properties){
			if( d.getReadMethod() != null ) {
				gettableProperties.add(d);
			}
		}
		
		List<String> nonPersistingProperties = getNonPersitingProperties();
		for (PropertyDescriptor d: gettableProperties){
			if (!nonPersistingProperties.contains(d.getName())) {
				 Object old = BeanUtils.getSimpleProperty(item1, d.getName());
				 Object newItem = BeanUtils.getSimpleProperty(savedItem1, d.getName());
				 assertEquals("The property "+d.getName() +" was not persisted for object "+this.getClass(),old,newItem);
			}
		}
	}
	
	/**
	 * Should return a new instance of an object that is being used by the 
	 * DAO.  Each new object must be not equal to any of the previously 
	 * created objects.  Every gettable property must be set to a non-default value
	 * 
	 * @return a new test object 
	 */
	public abstract T getNewObjectUnderTest();
	
	/**
	 * This should return the data access object that is being tested
	 */
	public abstract D getDataAccessObject();
	
	/**
	 * gets a list of strings that this object dosn't persist
	 */
	public List<String> getNonPersitingProperties(){
		ArrayList<String> nonPersisting = new ArrayList<String>();
		return nonPersisting;
	}
	
}
