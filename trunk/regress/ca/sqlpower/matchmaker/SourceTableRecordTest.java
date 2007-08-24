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


package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.matchmaker.swingui.StubMatchMakerSession;
import junit.framework.TestCase;

public class SourceTableRecordTest extends TestCase {

    MatchMakerSession session;
    Match match;
    MatchPool pool;
    
    @Override
    protected void setUp() throws Exception {
        session = new StubMatchMakerSession();
        match = new Match();
        match.setSession(session);
        pool = new MatchPool(match);
    }
    
    public void testNoNullKeyValuesAllowed() {
        try {
            new SourceTableRecord(session, match, (List<Object>) null);
            fail("It allowed a null keyvalues list");
        } catch (NullPointerException ex) {
            // expected
        }
    }
    
    public void testEqualsWhenTrue() {
        List<Object> oneKeyValues = new ArrayList<Object>();
        oneKeyValues.add(new String("cows"));
        oneKeyValues.add(new String("moo"));
        oneKeyValues.add(new String("moo"));
        oneKeyValues.add(null);
        SourceTableRecord one = new SourceTableRecord(session, match, pool, oneKeyValues);
        
        List<Object> twoKeyValues = new ArrayList<Object>();
        twoKeyValues.add(new String("cows"));
        twoKeyValues.add(new String("moo"));
        twoKeyValues.add(new String("moo"));
        twoKeyValues.add(null);
        SourceTableRecord two = new SourceTableRecord(session, match, pool, twoKeyValues);

        assertTrue(one.equals(two));
        assertTrue(two.equals(one));
        
        assertTrue("Equals is inconsistent with hashCode", one.hashCode() == two.hashCode());
    }
    
    public void testEqualsWhenFalse() {
        List<Object> oneKeyValues = new ArrayList<Object>();
        oneKeyValues.add(new String("moo"));
        oneKeyValues.add(new String("cows"));
        oneKeyValues.add(new String("moo"));
        oneKeyValues.add(null);
        SourceTableRecord one = new SourceTableRecord(session, match, pool, oneKeyValues);
        
        List<Object> twoKeyValues = new ArrayList<Object>();
        twoKeyValues.add(new String("cows"));
        twoKeyValues.add(new String("moo"));
        twoKeyValues.add(new String("moo"));
        twoKeyValues.add(null);
        SourceTableRecord two = new SourceTableRecord(session, match, pool,twoKeyValues);

        assertFalse(one.equals(two));
        assertFalse(two.equals(one));
    }

    public void testEqualsWhenFalseAndKeysHaveDifferentLength() {
        List<Object> oneKeyValues = new ArrayList<Object>();
        oneKeyValues.add(new String("cows"));
        oneKeyValues.add(new String("moo"));
        SourceTableRecord one = new SourceTableRecord(session, match, pool,oneKeyValues);
        
        List<Object> twoKeyValues = new ArrayList<Object>();
        twoKeyValues.add(new String("cows"));
        twoKeyValues.add(new String("moo"));
        twoKeyValues.add(new String("moo"));
        twoKeyValues.add(null);
        SourceTableRecord two = new SourceTableRecord(session, match, pool,twoKeyValues);

        assertFalse(one.equals(two));
        assertFalse(two.equals(one));
    }
    
}
