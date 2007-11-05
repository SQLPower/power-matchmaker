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



package ca.sqlpower.matchmaker.dao.hibernate;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.dao.AbstractTableMergeRulesDAOTestCase;
import ca.sqlpower.matchmaker.dao.TableMergeRuleDAO;


public class TableMergeRulesDAOOracleTest extends AbstractTableMergeRulesDAOTestCase {
    
    public TableMergeRulesDAOOracleTest() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @Override
	public void resetSession() throws Exception {
		((TestingMatchMakerHibernateSession) getSession()).resetSession();
	}
    
	@Override
	public TableMergeRuleDAO getDataAccessObject() throws Exception {
		return new TableMergeRulesDAOHibernate(getSession());
	}

    @Override
    public MatchMakerHibernateSession getSession() throws Exception {
        return HibernateTestUtil.getOracleHibernateSession();
    }
    
    public void testSaveAndLoadInTwoSessionsWithChildren() throws Exception {
		TableMergeRuleDAO dao = getDataAccessObject();
		List<TableMergeRules> all;
		TableMergeRules item1 = createNewObjectUnderTest();
		item1.setVisible(true);
		ColumnMergeRules cmr1 = createColumnMergeRules(item1);
		ColumnMergeRules cmr2 = createColumnMergeRules(item1);
		
		dao.save(item1);
		
		resetSession();
		dao = getDataAccessObject();
		all = dao.findAll();
        assertTrue("We want at least one item", 1 <= all.size());
        TableMergeRules savedItem1 = all.get(0);
		for (TableMergeRules item: all){
			item.setSession(getSession());
		}

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
}
