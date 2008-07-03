/*
 * Copyright (c) 2008, SQL Power Group Inc.
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


package ca.sqlpower.matchmaker;
import java.awt.Color;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;
import ca.sqlpower.matchmaker.munge.DeDupeResultStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.util.ViewSpec;

/**
 * A base test that all test cases of MatchMakerObject implementations should extend.
 *
 * @param <C> The class under test
 * @version $Id$
 */
public abstract class MatchMakerTestCase<C extends MatchMakerObject> extends TestCase {

	/**
	 * The object under test.
	 */
	C target;

    Set<String>propertiesToIgnoreForEventGeneration = new HashSet<String>();
    Set<String>propertiesThatDifferOnSetAndGet = new HashSet<String>();
    Set<String>propertiesThatHaveSideEffects = new HashSet<String>();
    
    public MatchMakerSession session = new TestingMatchMakerSession();

	protected Set<String> propertiesToIgnoreForDuplication = new HashSet<String>();
	
	/**
	 * List of properties that share instances between duplicate and original.
	 */
	protected Set<String> propertiesShareInstanceForDuplication = new HashSet<String>();

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected abstract C getTarget() throws ArchitectException;

	public void testDuplicate() throws Exception {
		MatchMakerObject mmo = getTarget();

		List<PropertyDescriptor> settableProperties;
		settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(mmo.getClass()));
        propertiesToIgnoreForDuplication.add("oid");
        propertiesToIgnoreForDuplication.add("parent");
        propertiesToIgnoreForDuplication.add("parentProject");
        propertiesToIgnoreForDuplication.add("session");
        propertiesToIgnoreForDuplication.add("class");
        propertiesToIgnoreForDuplication.add("createDate");
        propertiesToIgnoreForDuplication.add("createAppUser");
        propertiesToIgnoreForDuplication.add("createOsUser");
        propertiesToIgnoreForDuplication.add("lastUpdateDate");
        propertiesToIgnoreForDuplication.add("lastUpdateAppUser");
        propertiesToIgnoreForDuplication.add("lastUpdateOSUser");
        propertiesToIgnoreForDuplication.add("mergingEngine");
        propertiesToIgnoreForDuplication.add("matchingEngine");
        propertiesToIgnoreForDuplication.add("cleansingEngine");
        propertiesToIgnoreForDuplication.add("undoing");
        
        //this throws an exception if the DS does not exist
        propertiesToIgnoreForDuplication.add("spDataSource");
        
        // First pass: set all settable properties, because testing the duplication of
        //             an object with all its properties at their defaults is not a
        //             very convincing test of duplication!
		for (PropertyDescriptor property : settableProperties) {
			if (propertiesToIgnoreForDuplication.contains(property.getName())) continue;
			Object oldVal;
			try {
				oldVal = PropertyUtils.getSimpleProperty(mmo, property.getName());
				// check for a setter
				if (property.getWriteMethod() != null)
				{
					Object newVal = getNewDifferentValue(mmo, property, oldVal);
					BeanUtils.copyProperty(mmo, property.getName(), newVal);
				}
			} catch (NoSuchMethodException e) {
				System.out.println("Skipping non-settable property "+property.getName()+" on "+mmo.getClass().getName());
			}
		}
		// Second pass get a copy make sure all of 
		// the original mutable objects returned from getters are different
		// between the two objects, but have the same values. 
		MatchMakerObject duplicate = mmo.duplicate(mmo.getParent(),session);
		for (PropertyDescriptor property : settableProperties) {
			if (propertiesToIgnoreForDuplication.contains(property.getName())) continue;
			Object oldVal;
			try {
				oldVal = PropertyUtils.getSimpleProperty(mmo, property.getName());
				Object copyVal = PropertyUtils.getSimpleProperty(duplicate, property.getName());
				if(oldVal == null) {
					throw new NullPointerException("We forgot to set "+property.getName());
				} else {
					if (mmo instanceof MungeProcess && property.getDisplayName().equals("children")){
						assertEquals("The number of MungeStep's are different.", ((List) oldVal).size(), ((List) copyVal).size());
						for (int i = 0; i < ((List) oldVal).size(); i++) {
							MungeStep oldStep = (MungeStep) ((List) oldVal).get(i);
							MungeStep copyStep = (MungeStep) ((List) copyVal).get(i);
							assertNotSame("The two MungeStep's share the same instance.", oldVal, copyVal);

							assertEquals("The two names are different.", oldStep.getName(), copyStep.getName());
							assertEquals("The two undoing properties are different.", oldStep.isUndoing(), copyStep.isUndoing());
							assertEquals("The two visible properties are different.", oldStep.isVisible(), copyStep.isVisible());
							assertEquals("The two lists of parameters are different.", oldStep.getParameterNames().size(), copyStep.getParameterNames().size());
							for (String param : oldStep.getParameterNames()) {
								assertEquals("The two values for parameter " + param + " are different",
										oldStep.getParameter(param), copyStep.getParameter(param));
							}
						}
					} else if (oldVal instanceof MungeStep) {
						MungeStep oldStep = (MungeStep) oldVal;
						MungeStep copyStep = (MungeStep) copyVal;
						assertNotSame("The two MungeStep's share the same instance.", oldVal, copyVal);

						assertEquals("The two names are different.", oldStep.getName(), copyStep.getName());
						assertEquals("The two undoing properties are different.", oldStep.isUndoing(), copyStep.isUndoing());
						assertEquals("The two visible properties are different.", oldStep.isVisible(), copyStep.isVisible());
						assertEquals("The two lists of parameters are different.", oldStep.getParameterNames().size(), copyStep.getParameterNames().size());
						for (String param : oldStep.getParameterNames()) {
							assertEquals("The two values for parameter " + param + " are different",
									oldStep.getParameter(param), copyStep.getParameter(param));
						}
					} else {
						assertEquals("The two values for property "+property.getDisplayName() + " in " + mmo.getClass().getName() + " should be equal",oldVal,copyVal);

						if (propertiesShareInstanceForDuplication.contains(property.getName())) return;

						// Ok, the duplicate object's property value compared equal.
						// Now we want to make sure if we modify that property on the original,
						// it won't affect the copy.
						Object newCopyVal = modifyObject(mmo, property, copyVal);

						assertFalse(
								"The two values are the same mutable object for property "+property.getDisplayName() + " was "+oldVal+ " and " + copyVal,
								oldVal.equals(newCopyVal));
					}
				}
			} catch (NoSuchMethodException e) {
				System.out.println("Skipping non-settable property "+property.getName()+" on "+mmo.getClass().getName());
			}
		}
	}
	
	/**
	 * Returns a new value that is not equal to oldVal. If oldVal is immutable, the
	 * returned object will be a new instance compatible with oldVal.  If oldVal is 
	 * mutable, it will be modified in some way so it is no longer equal to its original
	 * value. {@link #getNewDifferentValue(MatchMakerObject, PropertyDescriptor, Object)}
	 * is a similar method that does not take mutability into account and always returns 
	 * a new value.
	 * 
	 * @param mmo The object to which the property belongs.  You might need this
	 *  if you have a special case for certain types of objects.
	 * @param property The property that should be modified.  It belongs to mmo.
	 * @param oldVal The existing value of the property to modify.  The returned value
	 * will not equal this one at the time this method was first called, although it may
	 * be the same instance as this one, but modified in some way.
	 */
	private Object modifyObject(MatchMakerObject mmo, PropertyDescriptor property, Object oldVal) throws IOException {
		if (property.getPropertyType() == Integer.TYPE
				|| property.getPropertyType() == Integer.class) {
				return ((Integer) oldVal) + 1;
		} else if (property.getPropertyType() == Short.TYPE
				|| property.getPropertyType() == Short.class) {
				return ((Short) oldVal) + 1;
		} else if (property.getPropertyType() == String.class) {
			if (oldVal == null) {
				return "new";
			} else {
				return "new " + oldVal;
			}
		} else if (property.getPropertyType() == Boolean.class || property.getPropertyType() == Boolean.TYPE ) {
	        return new Boolean(!((Boolean) oldVal).booleanValue());
		} else if (property.getPropertyType() == Long.class) {
			return new Long(((Long) oldVal).longValue() + 1L);
		} else if (property.getPropertyType() == BigDecimal.class) {
			return new BigDecimal(((BigDecimal) oldVal).longValue() + 1L);
		} else if (property.getPropertyType() == MungeSettings.class) {
		    Integer processCount = ((MatchMakerSettings) oldVal).getProcessCount();
		    ((MatchMakerSettings) oldVal).setProcessCount(new Integer(processCount +1));
		    return oldVal;
		} else if (property.getPropertyType() == MergeSettings.class) {
		    Integer processCount = ((MatchMakerSettings) oldVal).getProcessCount();
		    processCount = new Integer(processCount +1);
		    ((MatchMakerSettings) oldVal).setProcessCount(processCount);
		    return oldVal;
		} else if (property.getPropertyType() == SQLTable.class) {
			((SQLTable) oldVal).setRemarks("Testing Remarks");
			return oldVal;
		} else if (property.getPropertyType() == ViewSpec.class) {
			((ViewSpec) oldVal).setName("Testing New Name");
			return oldVal;
		} else if (property.getPropertyType() == File.class) {
			oldVal = File.createTempFile("mmTest2",".tmp");
			((File)oldVal).deleteOnExit();
			return oldVal;
		} else if (property.getPropertyType() == ProjectMode.class) {
			if (oldVal == ProjectMode.BUILD_XREF) {
				return ProjectMode.FIND_DUPES;
			} else {
				return ProjectMode.BUILD_XREF;
			}
		} else if (property.getPropertyType() == MergeActionType.class) {
			if (oldVal == MergeActionType.AUGMENT) {
				return MergeActionType.SUM;
			} else {
				return MergeActionType.AUGMENT;
			}
		} else if (property.getPropertyType() == MatchMakerObject.class) {
			((MatchMakerObject) oldVal).setName("Testing New Name");
			return oldVal;
		} else if (property.getPropertyType() == MatchMakerFolder.class) {
			((MatchMakerObject) oldVal).setName("Testing New Name");
			return oldVal;
		} else if (property.getPropertyType() == MatchMakerTranslateGroup.class) {
			((MatchMakerObject) oldVal).setName("Testing New Name2");
			return oldVal;
		} else if (property.getPropertyType() == SQLColumn.class) {
			((SQLColumn) oldVal).setRemarks("Testing Remarks");
			return oldVal;
		} else if (property.getPropertyType() == Date.class) {
			((Date)oldVal).setTime(((Date)oldVal).getTime()+10000);
			return oldVal;
		} else if (property.getPropertyType() == List.class) {
			if (property.getName().equals("children")) {
				if (mmo instanceof TableMergeRules) {
					((List) oldVal).add(new ColumnMergeRules());
				} else {
					((List) oldVal).add(new StubMatchMakerObject());
				}
			} else {
				((List)oldVal).add("Test");
			}
		    return oldVal;
		} else if (property.getPropertyType() == SQLIndex.class ) {
			((SQLIndex) oldVal).setName("modified index");
			return oldVal;
        } else if (property.getPropertyType() == Color.class) {
            if (oldVal == null) {
                return new Color(0xFAC157);
            } else {
                Color oldColor = (Color) oldVal;
                return new Color( (oldColor.getRGB()+0xF00) % 0x1000000);
            }
        } else if (property.getPropertyType() == ChildMergeActionType.class) {
        	if (oldVal != null && oldVal.equals(ChildMergeActionType.DELETE_ALL_DUP_CHILD)){
        		return ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT;
        	} else {
        		return ChildMergeActionType.DELETE_ALL_DUP_CHILD;
        	}
        } else if (property.getPropertyType() == TableMergeRules.class) {
        	if (oldVal == null){
        		return mmo;
        	} else {
        		return null;
        	}
		} else {
			throw new RuntimeException("This test case lacks the ability to modify values for "
					+ property.getName() + " (type "
					+ property.getPropertyType().getName() + ")");
		}
	}

	public void testAllSettersGenerateEvents()
	throws IllegalArgumentException, IllegalAccessException,
	InvocationTargetException, NoSuchMethodException, IOException,
	ArchitectException {

		MatchMakerObject mmo = getTarget();

		MatchMakerEventCounter listener = new MatchMakerEventCounter();
		mmo.addMatchMakerListener(listener);

		List<PropertyDescriptor> settableProperties;
		settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(mmo.getClass()));
        propertiesToIgnoreForEventGeneration.add("oid");
        propertiesToIgnoreForEventGeneration.add("session");
        propertiesToIgnoreForEventGeneration.add("parentProject");
        propertiesToIgnoreForEventGeneration.add("undoing");
        
		for (PropertyDescriptor property : settableProperties) {
			if (propertiesToIgnoreForEventGeneration.contains(property.getName())) continue;
			Object oldVal;

			try {
				oldVal = PropertyUtils.getSimpleProperty(mmo, property.getName());
				// check for a setter
				if (property.getWriteMethod() != null)
				{
					Object newVal = getNewDifferentValue(mmo, property, oldVal);

                    assertFalse("Old value and new value are equivalent for class "+property.getPropertyType()+" of property "+property.getName() +" old ["+oldVal+"] new ["+newVal+"]",
                            oldVal == null? oldVal == newVal:oldVal.equals(newVal));
					int oldChangeCount = listener.getAllEventCounts();

					try {

						PropertyUtils.setProperty(mmo, property.getName(), newVal);

						// some setters fire multiple events (they change more than one property)
						assertTrue("Event for set "+property.getName()+" on "+mmo.getClass().getName()+" didn't fire!",
								listener.getAllEventCounts() > oldChangeCount);
						if (listener.getAllEventCounts() == oldChangeCount + 1) {
							assertEquals("Property name mismatch for "+property.getName()+ " in "+mmo.getClass(),
									property.getName(),
									listener.getLastEvt().getPropertyName());
							assertEquals("New value for "+property.getName()+" was wrong",
									newVal,
									listener.getLastEvt().getNewValue());
						}
					} catch (InvocationTargetException e) {
						System.out.println("(non-fatal) Failed to write property '"+property.getName()+" to type "+mmo.getClass().getName());
					}
				}

			} catch (NoSuchMethodException e) {
				System.out.println("Skipping non-settable property "+property.getName()+" on "+mmo.getClass().getName());
			}
		}
	}

	/**
	 * Returns a new value that is not equal to oldVal. The returned object
	 * will always be a NEW instance compatible with oldVal. This differs from
	 * {@link #modifyObject(MatchMakerObject, PropertyDescriptor, Object)} in that
	 * this does not take mutability into account.
	 * 
	 * @param mmo The object to which the property belongs.  You might need this
	 *  if you have a special case for certain types of objects.
	 * @param property The property that should be modified.  It belongs to mmo.
	 * @param oldVal The existing value of the property.
	 */
	private Object getNewDifferentValue(MatchMakerObject mmo, PropertyDescriptor property, Object oldVal) throws IOException {
		Object newVal; // don't init here so compiler can warn if the
		// following code doesn't always give it a value
		if (property.getPropertyType() == Integer.TYPE
				|| property.getPropertyType() == Integer.class) {
			if (oldVal == null)
				newVal = new Integer(0);
			else {
				newVal = ((Integer) oldVal) + 1;
			}
		} else if (property.getPropertyType() == Short.TYPE
				|| property.getPropertyType() == Short.class) {
			if (oldVal == null)
				newVal = new Short("0");
			else {
				Integer temp = (Short) oldVal + 1;
				newVal = Short.valueOf(temp.toString());
			}
		} else if (property.getPropertyType() == String.class) {
			// make sure it's unique
			newVal = "new " + oldVal;

		} else if (property.getPropertyType() == Boolean.class || property.getPropertyType() == Boolean.TYPE ) {
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
			newVal = new MungeSettings();
		    Integer processCount = ((MatchMakerSettings) newVal).getProcessCount();
		    if (processCount == null) {
		        processCount = new Integer(0);
		    } else {
		        processCount = new Integer(processCount +1);
		    }
		    ((MatchMakerSettings) newVal).setProcessCount(processCount);
		} else if (property.getPropertyType() == MergeSettings.class) {
			newVal = new MergeSettings();
		    Integer processCount = ((MatchMakerSettings) newVal).getProcessCount();
		    if (processCount == null) {
		        processCount = new Integer(0);
		    } else {
		        processCount = new Integer(processCount +1);
		    }
		    ((MatchMakerSettings) newVal).setProcessCount(processCount);
		} else if (property.getPropertyType() == SQLTable.class) {
			newVal = new SQLTable();
		} else if (property.getPropertyType() == ViewSpec.class) {
			newVal = new ViewSpec("*", "test_table", "true");
		} else if (property.getPropertyType() == File.class) {
			newVal = File.createTempFile("mmTest",".tmp");
			((File)newVal).deleteOnExit();
		} else if (property.getPropertyType() == PlFolder.class) {
			newVal = new PlFolder<Project>();
		} else if (property.getPropertyType() == ProjectMode.class) {
			if (oldVal == ProjectMode.BUILD_XREF) {
				newVal = ProjectMode.FIND_DUPES;
			} else {
				newVal = ProjectMode.BUILD_XREF;
			}
		} else if (property.getPropertyType() == MergeActionType.class) {
			if (oldVal == MergeActionType.AUGMENT) {
				newVal = MergeActionType.SUM;
			} else {
				newVal = MergeActionType.AUGMENT;
			}
		}else if (property.getPropertyType() == MatchMakerTranslateGroup.class) {
			newVal = new MatchMakerTranslateGroup();
		} else if (property.getPropertyType() == MatchMakerObject.class) {
			newVal = new TestingAbstractMatchMakerObject();
		}else if (property.getPropertyType() == SQLColumn.class) {
			newVal = new SQLColumn();
		} else if (property.getPropertyType() == Date.class) {
			newVal = new Date();
		} else if (property.getPropertyType() == List.class) {
		    newVal = new ArrayList();
		} else if (property.getPropertyType() == Project.class) {
		    newVal = new Project();
		    ((Project) newVal).setName("Fake_Project_"+System.currentTimeMillis());
		} else if (property.getPropertyType() == SQLIndex.class) {
			return new SQLIndex("new index", false, "", "HASHED", "");
        } else if (property.getPropertyType() == Color.class) {
            if (oldVal == null) {
                newVal = new Color(0xFAC157);
            } else {
                Color oldColor = (Color) oldVal;
                newVal = new Color( (oldColor.getRGB()+0xF00) % 0x1000000);
            }
        } else if (property.getPropertyType() == ChildMergeActionType.class) {
        	if (oldVal != null && oldVal.equals(ChildMergeActionType.DELETE_ALL_DUP_CHILD)){
        		newVal = ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT;
        	} else {
        		newVal = ChildMergeActionType.DELETE_ALL_DUP_CHILD;
        	}
        } else if (property.getPropertyType() == DeDupeResultStep.class) {
        	newVal = new DeDupeResultStep();
        } else if (property.getPropertyType() == TableMergeRules.class) {
        	if (oldVal == null){
        		newVal = mmo;
        	} else {
        		newVal = null;
        	}
		} else {
			throw new RuntimeException("This test case lacks a value for "
					+ property.getName() + " (type "
					+ property.getPropertyType().getName() + ") from "
					+ mmo.getClass());
		}

		if (newVal instanceof MatchMakerObject){
			((MatchMakerObject)newVal).setSession(session);
		}
		return newVal;
	}
	
	/**
	 * Tests for inconsistent getter/setter pairs. It checks to see if a call to
	 * the getter of a property will return the same value that has been previously
	 * used in a setter of the property.
	 * 
	 * @throws Exception
	 */
	public void testInconsistentGetterSetterPairs() throws Exception {
		MatchMakerObject mmo = getTarget();
		propertiesThatDifferOnSetAndGet.add("session");
		List<PropertyDescriptor> settableProperties;
		settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(mmo.getClass()));
		for (PropertyDescriptor property : settableProperties) {
			if (propertiesThatDifferOnSetAndGet.contains(property.getName())) continue;
			Object oldVal;
			try {
				oldVal = PropertyUtils.getSimpleProperty(mmo, property.getName());
				// check for a setter
				if (property.getWriteMethod() != null)
				{
					Object newVal = getNewDifferentValue(mmo, property, oldVal);
					PropertyUtils.setProperty(mmo, property.getName(), newVal);
				
					Object valueGot = PropertyUtils.getProperty(mmo, property.getName());
					assertEquals("Property "+property.getName()+" differs on object "+ mmo +" between getter and setter",newVal,valueGot);
				}
			} catch (NoSuchMethodException e) {
				
				System.out.println("Skipping non-settable property "+property.getName()+" on "+mmo.getClass().getName());
			}
		}
	}
	
	/**
	 * Tests for getters and setters that have side effects on other properties of the object.
	 * 
	 * Note: This test isn't all that good because given the right order we
	 * can override the side effects
	 */
	public void testGettersAndSettersSideEffects() throws Exception {
		MatchMakerObject mmo = getTarget();
		propertiesThatDifferOnSetAndGet.add("session");
		propertiesThatDifferOnSetAndGet.add("undoing");
		
		propertiesThatHaveSideEffects.addAll(propertiesThatDifferOnSetAndGet);
		List<PropertyDescriptor> settableProperties;
		Map<String,Object> setValues = new HashMap<String,Object>();
		settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(mmo.getClass()));
		for (PropertyDescriptor property : settableProperties) {
			if (!propertiesThatHaveSideEffects.contains(property.getName())) {
				Object oldVal;
				try {
					oldVal = PropertyUtils.getSimpleProperty(mmo, property.getName());
					// check for a setter
					if (property.getWriteMethod() != null)
					{
						Object newVal = getNewDifferentValue(mmo, property, oldVal);
						PropertyUtils.setProperty(mmo, property.getName(), newVal);
						setValues.put(property.getName(), newVal);
					}
				} catch (NoSuchMethodException e) {
					System.out.println("Skipping non-settable property "+property.getName()+" on "+mmo.getClass().getName());
				}
			}
		}
		for (String propertyName: setValues.keySet()) {
			Object valueGot = PropertyUtils.getProperty(mmo, propertyName);
			assertEquals("Property "+propertyName+" differs on object "+ mmo +" between getter and setter",setValues.get(propertyName),valueGot);
		}
	}

    /**
     * The child list should never be null for any Match Maker Object, even if
     * that object's type is childless.
     */
    public void testChildrenNotNull() throws ArchitectException {
        assertNotNull(getTarget().getChildren());
    }

    /**
     * All objects should return false for .equals(null), not true or throw an exception.
     */
    public void testNullEquality() throws ArchitectException {
        assertFalse("equals(null) has to work, and return false",getTarget().equals(null));
    }

}
