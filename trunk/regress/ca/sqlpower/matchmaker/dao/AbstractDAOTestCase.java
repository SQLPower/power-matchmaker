/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.dao;

import java.awt.Color;
import java.beans.PropertyDescriptor;
import java.io.File;
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
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MungeSettings;
import ca.sqlpower.matchmaker.MergeSettings;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TestingAbstractMatchMakerObject;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;
import ca.sqlpower.matchmaker.util.ViewSpec;

public abstract class AbstractDAOTestCase<T extends MatchMakerObject, D extends MatchMakerDAO> extends TestCase {

    ///////// Methods that subclasses can/should override //////////

    /**
     * Should return a new instance of an object that is being used by the
     * DAO.  Each new object must be not equal to any of the previously
     * created objects.  Every gettable property must be set to a non-default value
     *
     * @return a new test object
     * @throws Exception
     */
    public abstract T createNewObjectUnderTest() throws Exception;

    /**
     * This should return the data access object that is being tested
     * @throws Exception
     */
    public abstract D getDataAccessObject() throws Exception;

    /**
     * Get the current match maker sesssion from the concrete dao objects
     */
    public abstract MatchMakerSession getSession() throws Exception;

    /**
     * Notify the session that the new session has started
     * @throws Exception
     */
    public abstract void resetSession() throws Exception;
    /**
     * gets a list of strings that this object dosn't persist
     */
    public List<String> getNonPersitingProperties(){
        ArrayList<String> nonPersisting = new ArrayList<String>();
        nonPersisting.add("oid");
        nonPersisting.add("session");
        nonPersisting.add("undoing");
        return nonPersisting;
    }


    //////// Assert methods provided to subclasses /////////

    /**
     * Ensures that the given MatchMakerObject and all its descendants have a
     * reference to the given session.  Every findXXXX() method in every DAO
     * should explicitly test this assertion on the returned object(s).
     */
    public void assertHierarchyHasSession(MatchMakerSession expected, MatchMakerObject<MatchMakerObject, MatchMakerObject> root) {
        assertNotNull(
                "the MatchMakerSession went missing for a "+root.getClass().getName(),
                root.getSession());

        // this also implies a null check, but the error message is not ideal (hence the previous assert)
        assertSame(
                "session out of sync for "+root.getClass().getName(),
                expected, root.getSession());

        for (MatchMakerObject<MatchMakerObject, MatchMakerObject> child : root.getChildren()) {
            assertHierarchyHasSession(expected, child);
        }
    }

    ///////// Base TestCase implementation ///////////

	@Override
	protected void setUp() throws Exception {
        Connection con = null;
        try {
            con = getSession().getConnection();
            // You forgot to set the session's connection didn't you?
            DatabaseCleanup.clearDatabase(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        resetSession();
	}



    //////// Tests that all subtests will inherit ////////

    /** Test and see if find all throws an exception. */
	public void testFindAll() throws Exception {
		D dao = getDataAccessObject();
		List<T> all = dao.findAll();

        // the database is empty at this point (the test is really to find mapping errors, but this assertion won't hurt)
        assertEquals(0, all.size());
	}

	public void testSave() throws Exception {
		D dao = getDataAccessObject();
		T item1 = createNewObjectUnderTest();
		dao = getDataAccessObject();
		dao.save(item1);
	}

	public void testDeleteExisting() throws Exception {
		T item1 = createNewObjectUnderTest();
		T item2 = createNewObjectUnderTest();
		D dao = getDataAccessObject();
		dao.save(item1);
		dao.save(item2);
		List<T> all = dao.findAll();
		assertNotNull("dao returning null list, it should be empty instead",dao.findAll());
		for (T item: all) {
			System.out.println("The item to delete is " + item.getName());
			dao.delete(item);
		}
		assertEquals("There are still some objects of type "+item1.getClass()+" left",0,dao.findAll().size());
	}

	public void testSaveAndLoadInTwoSessions() throws Exception {
		D dao = getDataAccessObject();
		List<T> all;
		T item1 = createNewObjectUnderTest();
		dao.save(item1);
		resetSession();
		dao = getDataAccessObject();
		all = dao.findAll();
        assertTrue("We want at least one item", 1 <= all.size());
		T savedItem1 = all.get(0);
		for (T item: all){
			item.setSession(getSession());
		}

        assertHierarchyHasSession(getSession(), savedItem1);

		List<PropertyDescriptor> properties;
		properties = Arrays.asList(PropertyUtils.getPropertyDescriptors(item1.getClass()));

        // list all the readable properties
		List<PropertyDescriptor> gettableProperties = new ArrayList<PropertyDescriptor>();
		for (PropertyDescriptor d: properties){
		    if( d.getReadMethod() != null ) {
		        gettableProperties.add(d);
		    }
		}

        // compare the values of each readable property
		List<String> nonPersistingProperties = getNonPersitingProperties();
		for (PropertyDescriptor d: gettableProperties){
		    if (!nonPersistingProperties.contains(d.getName())) {
		        Object old = BeanUtils.getSimpleProperty(item1, d.getName());
		        Object newItem = BeanUtils.getSimpleProperty(savedItem1, d.getName());
		        assertEquals(
                        "The property "+d.getName() +" was not persisted for object "+this.getClass(),
                        String.valueOf(old),
                        String.valueOf(newItem));
		    }
		}

	}


    //////// Utility methods for subclasses to (ab)use ////////

	/**
	 * Sets all setters of the object object with a new default value except
	 * for those properties listed in propertiesThatAreNotPersisted
	 *
	 * @param object
	 * @param propertiesThatAreNotPersisted
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void setAllSetters(MatchMakerObject object,List<String> propertiesThatAreNotPersisted) throws Exception {
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
                        if (oldVal == null) {
                            newVal = "string";
                        } else {
                            newVal = "new " + oldVal;
                        }
					} else if (property.getPropertyType() == Boolean.TYPE ||
							property.getPropertyType() == Boolean.class) {
                        if(oldVal == null){
                            newVal = new Boolean(false);
                        } else {
                            newVal = new Boolean(!((Boolean) oldVal).booleanValue());
                        }
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
					} else if (property.getPropertyType() == MungeSettings.class) {
						MungeSettings mungeSettings = new MungeSettings();
						setAllSetters(mungeSettings, propertiesThatAreNotPersisted);
						newVal = mungeSettings;
					} else if (property.getPropertyType() == MergeSettings.class) {
						MergeSettings mergeSettings = new MergeSettings();
						setAllSetters(mergeSettings, propertiesThatAreNotPersisted);
						newVal = mergeSettings;
					} else if (property.getPropertyType() == SQLTable.class) {
						newVal = new SQLTable();
						((SQLTable)newVal).setName("Fake Table");
					} else if (property.getPropertyType() == ViewSpec.class) {
						newVal = new ViewSpec("select clause", "from clause", "where clause");
					} else if (property.getPropertyType() == File.class) {
						newVal = File.createTempFile("mmTest",".tmp");
						((File)newVal).deleteOnExit();
					} else if (property.getPropertyType() == PlFolder.class) {
						newVal = new PlFolder<Project>();
					} else if (property.getPropertyType() == ProjectMode.class) {
						if (oldVal == ProjectMode.BUILD_XREF) {
							newVal = ProjectMode.FIND_DUPES;
						} else {
							newVal = Project.ProjectMode.BUILD_XREF;
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
                    } else if (property.getPropertyType() == SQLIndex.class) {
                        newVal = new SQLIndex("new index",false,"",IndexType.HASHED,"");
                    } else if (property.getPropertyType() == Color.class) {
                        if (oldVal == null) {
                            newVal = new Color(0xFAC157);
                        } else {
                            Color oldColor = (Color) oldVal;
                            newVal = new Color( (oldColor.getRGB()+0xF00) % 0x1000000);
                        }
                    } else if (property.getPropertyType() == ChildMergeActionType.class) {
                    	if (oldVal != null && oldVal.equals(ChildMergeActionType.DELETE_ALL_DUP_CHILD)) {
                    		newVal = ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT;
                    	} else {
                    		newVal = ChildMergeActionType.DELETE_ALL_DUP_CHILD;
                    	}
					} else {
						throw new RuntimeException("This test case lacks a value for "
								+ property.getName() + " (type "
								+ property.getPropertyType().getName() + ") from "
								+ mmo.getClass());
					}


					if (newVal instanceof MatchMakerObject){
						((MatchMakerObject)newVal).setSession(getSession());
					}

                    assertNotNull("Ooops we should have set "+property.getName() + " to a value in "+mmo.getClass().getName(),newVal);
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
