package ca.sqlpower.matchmaker;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;
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
    public MatchMakerSession session = new TestingMatchMakerSession();

	protected void setUp() throws Exception {
		super.setUp();

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected abstract C getTarget();

	public void testAllSettersGenerateEvents()
	throws IllegalArgumentException, IllegalAccessException,
	InvocationTargetException, NoSuchMethodException, ArchitectException, IOException {

		MatchMakerObject mmo = getTarget();

		MatchMakerEventCounter listener = new MatchMakerEventCounter();
		mmo.addMatchMakerListener(listener);

		List<PropertyDescriptor> settableProperties;
		settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(mmo.getClass()));
        propertiesToIgnoreForEventGeneration.add("oid");
        propertiesToIgnoreForEventGeneration.add("session");
		for (PropertyDescriptor property : settableProperties) {
			if (propertiesToIgnoreForEventGeneration.contains(property.getName())) continue;
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
							newVal = ((Short) oldVal) + 1;
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
					} else if (property.getPropertyType() == MatchSettings.class) {
						newVal = new MatchSettings();
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
						newVal = new ViewSpec();
					} else if (property.getPropertyType() == File.class) {
						newVal = File.createTempFile("mmTest",".tmp");
						((File)newVal).deleteOnExit();
					} else if (property.getPropertyType() == PlFolder.class) {
						newVal = new PlFolder<Match>();
					} else if (property.getPropertyType() == Match.MatchMode.class) {
						if (oldVal == Match.MatchMode.BUILD_XREF) {
							newVal = Match.MatchMode.FIND_DUPES;
						} else {
							newVal = Match.MatchMode.BUILD_XREF;
						}
					} else if (property.getPropertyType() == MatchMakerTranslateGroup.class) {
						newVal = new MatchMakerTranslateGroup();
					} else if (property.getPropertyType() == MatchMakerObject.class) {
						newVal = new TestingAbstractMatchMakerObject();
					}else if (property.getPropertyType() == SQLColumn.class) {
						newVal = new SQLColumn();
					} else if (property.getPropertyType() == Date.class) {
						newVal = new Date();
					} else if (property.getPropertyType() == List.class) {
                        newVal = new ArrayList();
                    } else if (property.getPropertyType() == Match.class) {
                        newVal = new Match();
                        ((Match) newVal).setName("Fake_Match_"+System.currentTimeMillis());
                    } else {
						throw new RuntimeException("This test case lacks a value for "
								+ property.getName() + " (type "
								+ property.getPropertyType().getName() + ") from "
								+ mmo.getClass());
					}

					if (newVal instanceof MatchMakerObject){
						((MatchMakerObject)newVal).setSession(session);
					}

                    assertFalse("Old value and new value are equivalent for class "+property.getPropertyType()+" of property "+property.getName(),
                            oldVal == null? oldVal == newVal:oldVal.equals(newVal));
					int oldChangeCount = listener.getAllEventCounts();

					try {

						BeanUtils.copyProperty(mmo, property.getName(), newVal);

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
     * The child list should never be null for any Match Maker Object, even if
     * that object's type is childless.
     */
    public void testChildrenNotNull() {
        assertNotNull(getTarget().getChildren());
    }

    /**
     * All objects should return false for .equals(null), not true or throw an exception.
     */
    public void testNullEquality(){
        assertFalse("equals(null) has to work, and return false",getTarget().equals(null));
    }

}
