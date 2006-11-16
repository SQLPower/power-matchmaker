package ca.sqlpower.matchmaker.dao;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchSettings;
import ca.sqlpower.matchmaker.MergeSettings;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TestingAbstractMatchMakerObject;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;
import ca.sqlpower.matchmaker.util.SourceTable;
import ca.sqlpower.matchmaker.util.ViewSpec;
import ca.sqlpower.matchmaker.util.log.Level;
import ca.sqlpower.matchmaker.util.log.Log;
import ca.sqlpower.matchmaker.util.log.LogFactory;

public abstract class AbstractDAOTestCase<T extends MatchMakerObject, D extends MatchMakerDAO> extends TestCase {

	public MatchMakerSession session = new TestingMatchMakerSession();
	@Override
	protected void setUp() throws Exception {
        Connection con = null;
        try {
            con = session.getConnection();
            // You forgot to set the session's connection didn't you?
            DatabaseCleanup.clearDatabase(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }  
	}
	
    // Test and see if find all throws an exception
	public void testFindAll() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		D dao = getDataAccessObject();
		List<T> all = dao.findAll();
	}
	
	public void testSave() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		// This may fail, but it has to be done
		// make sure there are no objects of this type in the test data
		D dao = getDataAccessObject();
		List<T> all = dao.findAll();
		for (T item: all) {
			dao.delete(item);
		}
		T item1 = getNewObjectUnderTest();
		dao = getDataAccessObject();
		dao.save(item1);
	}
	public void testDeleteExisting(){
		T item1 = getNewObjectUnderTest();
		T item2 = getNewObjectUnderTest();
		D dao = getDataAccessObject();
		List<T> all = dao.findAll();
		assertNotNull("dao returning null list, it should be empty instead",dao.findAll());
		for (T item: all) {
			dao.delete(item);
		}
		assertEquals("There are still some objects of type "+item1.getClass()+" left",0,dao.findAll().size());
		dao.save(item1);
		dao.save(item2);
		all = dao.findAll();
		assertNotNull("dao returning null list, it should be empty instead",dao.findAll());
		for (T item: all) {
			dao.delete(item);
		}
		assertEquals("There are still some objects of type "+item1.getClass()+" left",0,dao.findAll().size());
	}
	
	public void testSaveAndLoadInOneSession() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		// This may fail, but it has to be done
		// make sure there are no objects of this type in the test data
		D dao = getDataAccessObject();
		List<T> all = dao.findAll();
		for (T item: all) {
			dao.delete(item);
		}
		T item1 = getNewObjectUnderTest();
		T item2 = getNewObjectUnderTest();
		dao = getDataAccessObject();
		dao.save(item1);
		dao.save(item2);
		all = dao.findAll();
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
		nonPersisting.add("oid");
		return nonPersisting;
	}
	/**
	 * Sets all setters of the object object with a new default value except
	 * for those properties listed in propertiesThatAreNotPersisted
	 * 
	 * @param object
	 * @param propertiesThatAreNotPersisted
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void setAllSetters(MatchMakerObject object,List<String> propertiesThatAreNotPersisted) throws IllegalAccessException, InvocationTargetException{
		MatchMakerObject mmo = object;

		MatchMakerEventCounter listener = new MatchMakerEventCounter();
		mmo.addMatchMakerListener(listener);

		List<PropertyDescriptor> settableProperties;
		settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(mmo.getClass()));
		for (PropertyDescriptor property : settableProperties) {
			if (propertiesThatAreNotPersisted.contains(property.getName())) continue;
			Object oldVal;

			try {
				oldVal = PropertyUtils.getSimpleProperty(mmo, property.getName());
				// check for a setter
				if (property.getWriteMethod() != null)
				{
					Object newVal; // don't init here so compiler can warn if the
					// following code doesn't always give it a value
					if (property.getPropertyType() == Integer.TYPE
							|| property.getPropertyType() == Integer.class) {
						if (oldVal == null) {
							newVal = new Integer(0);
						} else {
							newVal = ((Integer) oldVal) + 1;
						}
					} else if (property.getPropertyType() == String.class) {
						// make sure it's unique
						newVal = "new " + oldVal;
				
					} else if (property.getPropertyType() == Boolean.TYPE) {
						newVal = new Boolean(!((Boolean) oldVal).booleanValue());
					} else if (property.getPropertyType() == Long.class) {
						if (oldVal == null) {
							newVal = new Long(0L);
						} else {
							newVal = new Long(((Long) oldVal).longValue() + 1L);
						}
					} else if (property.getPropertyType() == BigDecimal.class) {
						if (oldVal == null) {
							newVal = new BigDecimal(0);
						} else {
							newVal = new BigDecimal(((BigDecimal) oldVal).longValue() + 1L);
						}
					} else if (property.getPropertyType() == SourceTable.class) {
						newVal = new SourceTable();
					} else {
						if (property.getPropertyType() == MatchSettings.class) {
							MatchSettings matchSettings = new MatchSettings();
							setAllSetters(matchSettings, new ArrayList<String>());
							newVal = matchSettings;
						} else if (property.getPropertyType() == MergeSettings.class) {
							MergeSettings mergeSettings = new MergeSettings();
							setAllSetters(mergeSettings, new ArrayList<String>());
							newVal = mergeSettings;
						} else if (property.getPropertyType() == SQLTable.class) {
							newVal = new SQLTable();
						} else if (property.getPropertyType() == ViewSpec.class) {
							newVal = new ViewSpec();
						} else if (property.getPropertyType() == Log.class) {
							newVal = LogFactory
									.getLogger(Level.DEBUG, "TestMatchMaker.log");
						} else if (property.getPropertyType() == PlFolder.class) {
							newVal = new PlFolder<Match>();
						} else if (property.getPropertyType() == Match.MatchType.class) {
							if (oldVal == Match.MatchType.BUILD_XREF) {
								newVal = Match.MatchType.FIND_DUPES;
							} else {
								newVal = Match.MatchType.BUILD_XREF;
							}
						} else if (property.getPropertyType() == MatchMakerTranslateGroup.class) {
							newVal = new MatchMakerTranslateGroup();
						} else if (property.getPropertyType() == MatchMakerObject.class) {
							newVal = new TestingAbstractMatchMakerObject();
						}else if (property.getPropertyType() == SQLColumn.class) {
							newVal = new SQLColumn();
						} else if (property.getPropertyType() == Date.class) {
							newVal = new Date();
						} else if (property.getPropertyType() == Short.class) {
							newVal = new Short("10");
						} else {
							throw new RuntimeException("This test case lacks a value for "
									+ property.getName() + " (type "
									+ property.getPropertyType().getName() + ") from "
									+ mmo.getClass());
						}
					}
					
					if (newVal instanceof MatchMakerObject){
						((MatchMakerObject)newVal).setSession(session);
					}
				
					int oldChangeCount = listener.getAllEventCounts();
				
					try {
						BeanUtils.copyProperty(mmo, property.getName(), newVal);
					} catch (InvocationTargetException e) {
						System.out.println("(non-fatal) Failed to write property '"+property.getName()+" to type "+mmo.getClass().getName());
					}
				}

			} catch (NoSuchMethodException e) {
				System.out.println("Skipping non-settable property "+property.getName()+" on "+mmo.getClass().getName());
			}
		}
	}
	
}
