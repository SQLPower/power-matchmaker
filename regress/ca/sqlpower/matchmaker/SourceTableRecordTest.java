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
            new SourceTableRecord(session, match, pool, null);
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
