/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.dao.OpenSaveHandler;
import ca.sqlpower.matchmaker.util.MatchMakerNewValueMaker;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.testutil.NewValueMaker;
import ca.sqlpower.testutil.SPObjectRoot;
import ca.sqlpower.testutil.TestUtils;

public class SaveAndOpenWorkspaceActionTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(SaveAndOpenWorkspaceActionTest.class);
	
	MMRootNode root;
	
	NewValueMaker valueMaker;
	
	SPObjectRoot fakeRoot = new SPObjectRoot();

	public SaveAndOpenWorkspaceActionTest(String name) {
		super(name);
	}
	
	public void setUp() throws Exception {
		super.setUp();
		
		root = new TestingMatchMakerSession().getRootNode();
		valueMaker = new MatchMakerNewValueMaker(fakeRoot, new PlDotIni());
	}
	
	public void testEverythingSavesAndLoadsProperly() {
		buildChildren(root);
		File f = null;
		try {
			f = File.createTempFile("dqguruxmltest", null);

		} catch (Exception e) {
			fail("Couldn't create temp file");
		}
		
		OpenSaveHandler.doSaveAs(f, root.getSession());
		MatchMakerSession newS = new TestingMatchMakerSession();
		OpenSaveHandler.doOpen(f, newS);
		
		assertTrue(checkEquality(root, newS.getRootNode()));
	}
	
	/**
	 * Takes two SPObjects and recursively determines if all persistable properties
	 * are equal. This is used in testing before-states and after-states for
	 * persistence tests.
	 */
	private boolean checkEquality(SPObject spo1, SPObject spo2) {

		try {
			Set<String> s = TestUtils.findPersistableBeanProperties(spo1, false, false);
			List<PropertyDescriptor> settableProperties = Arrays.asList(
					PropertyUtils.getPropertyDescriptors(spo1.getClass()));
			
	        for (PropertyDescriptor property : settableProperties) {
	            @SuppressWarnings("unused")
				Object oldVal;
	            if (!s.contains(property.getName())) continue;
	            if (property.getName().equals("parent")) continue; //Changing the parent causes headaches.
	            if (property.getName().equals("session")) continue;
	            if (property.getName().equals("type")) continue;
	            try {
	                oldVal = PropertyUtils.getSimpleProperty(spo1, property.getName());
	                // check for a getter
	                if (property.getReadMethod() == null) continue;
	                
	            } catch (NoSuchMethodException e) {
	                logger.debug("Skipping non-settable property " + property.getName() + " on " + 
	                		spo1.getClass().getName());
	                continue;
	            }
	            Object spo1Property = PropertyUtils.getSimpleProperty(spo1, property.getName());
	            Object spo2Property = PropertyUtils.getSimpleProperty(spo2, property.getName());
	            
	            assertEquals("Failed to equate " + property.getName() + " on object of type " + spo1.getClass(),
	            		spo1Property, spo2Property);
	        }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// look at children
		Iterator<? extends SPObject> i = (spo1.getChildren().iterator());
		Iterator<? extends SPObject> j = (spo2.getChildren().iterator());
		while (i.hasNext() && j.hasNext()) {
			SPObject ii = i.next();
			SPObject jj = j.next();
			logger.debug("Comparing: " + ii.getClass().getSimpleName() + "," + jj.getClass().getSimpleName());
			checkEquality(ii, jj);
		}
		return (!(i.hasNext() || j.hasNext()));
	}
	
	
	private void buildChildren(SPObject parent) {
		
		for (Class c : parent.getAllowedChildTypes()) {
			try {			
				if (parent.getChildren(c).size() > 0) {
					logger.debug("It already had a " + c.getSimpleName() + "!");
					continue;
				}
				SPObject child;
				child = ((SPObject) valueMaker.makeNewValue(c, null, "child"));
				parent.addChild(child, parent.getChildren(c).size());
			} catch (Exception e) {
				logger.warn("Could not add a " + c.getSimpleName() + " to a " +
						parent.getClass().getSimpleName() + " because of a "
						+ e.getClass().getName());
			}
			try {
				Set<String> s = TestUtils.findPersistableBeanProperties(parent, false, false);
				
				List<PropertyDescriptor> settableProperties = Arrays.asList(
						PropertyUtils.getPropertyDescriptors(parent.getClass()));
				TableMergeRules testParent = null; // special case- the parent of all others
				
				//set all properties of the object
		        for (PropertyDescriptor property : settableProperties) {
		            Object oldVal;

		            if (!s.contains(property.getName())) continue;
		            if (property.getName().equals("parent")) continue; //Changing the parent causes headaches.
		            if (property.getName().equals("session")) continue;
		            if (property.getName().equals("type")) continue;
		            try {
		                oldVal = PropertyUtils.getSimpleProperty(parent, property.getName());

		                // check for a setter
		                if (property.getWriteMethod() == null) continue;
		                
		            } catch (NoSuchMethodException e) {
		                logger.debug("Skipping non-settable property " + property.getName() + " on " + 
		                		parent.getClass().getName());
		                continue;
		            }
		            Object newVal = valueMaker.makeNewValue(property.getPropertyType(), oldVal, property.getName());
		            if (property.getName().equals("parentMergeRule")) {
		            	if (testParent == null) {
		            		newVal = null;
		            		testParent = (TableMergeRules)parent;
		            	} else {
		            		newVal = testParent;
		            	}
		            }
		            try {
		                logger.debug("Setting property '" + property.getName() + "' to '" + newVal + 
		                		"' (" + (newVal == null ?"null": newVal.getClass().getName()) + ")");
		                BeanUtils.copyProperty(parent, property.getName(), newVal);
		                
		            } catch (InvocationTargetException e) {
		                logger.debug("(non-fatal) Failed to write property '" + property.getName() + 
		                		" to type " + parent.getClass().getName());
		            }
		        }
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		for (SPObject spo : parent.getChildren()) {
			buildChildren(spo);
		}
	}

}
