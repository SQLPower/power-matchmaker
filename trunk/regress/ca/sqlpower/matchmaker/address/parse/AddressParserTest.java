/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.address.parse;

import java.io.File;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.debug.DebugEventListener;
import org.antlr.runtime.debug.DebugTokenStream;

import com.sleepycat.je.DatabaseException;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;

public class AddressParserTest extends TestCase {
	
	/**
	 * Path pointing to the directory containing the address database.
	 */
	private static AddressDatabase addressDB;
	
	static {
		try {
			String bdbPath = System.getProperty("ca.sqlpower.matchmaker.test.addressDB");
		    if (bdbPath == null) {
		        throw new RuntimeException(
		                "Please define the system property ca.sqlpower.matchmaker.test.addressDB" +
		                " to point to the directory where your BDB instance is which contains addresses");
		    }
			addressDB = new AddressDatabase(new File(bdbPath));
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

    private static DebugEventListener debugListener = new DebugEventListener() {

        public void LT(int i, Token t) {
            System.out.println("LT("+i+","+t+")");
            
        }

        public void LT(int i, Object t) {
            System.out.println("LT("+i+","+t+")");
        }

        public void addChild(Object root, Object child) {
            System.out.println("addChild("+root+","+child+")");
            
        }

        public void becomeRoot(Object newRoot, Object oldRoot) {
            System.out.println("becomeRoot("+newRoot+","+oldRoot+")");
            
        }

        public void beginBacktrack(int level) {
            System.out.println("beginBacktrack("+level+")");
        }

        public void beginResync() {
            System.out.println("beginResync()");
            
        }

        public void commence() {
            System.out.println("commence()");
            
        }

        public void consumeHiddenToken(Token t) {
            System.out.println("consumeHiddenToken("+t+")");
            
        }

        public void consumeNode(Object t) {
            System.out.println("consumeNode("+t+")");
            
        }

        public void consumeToken(Token t) {
            System.out.println("consumeToken("+t+")");
            
        }

        public void createNode(Object t) {
            System.out.println("createNode("+t+")");
            
        }

        public void createNode(Object node, Token token) {
            System.out.println("createNode("+node+","+token+")");
            
        }

        public void endBacktrack(int level, boolean successful) {
            System.out.println("endBacktrack("+level+","+successful+")");
            
        }

        public void endResync() {
            System.out.println("endResync()");
            
        }

        public void enterAlt(int alt) {
            System.out.println("enterAlt("+alt+")");
            
        }

        public void enterDecision(int decisionNumber) {
            System.out.println("enterDecision("+decisionNumber+")");
            
        }

        public void enterRule(String grammarFileName, String ruleName) {
            System.out.println("enterRule("+grammarFileName+","+ruleName+")");
            
        }

        public void enterSubRule(int decisionNumber) {
            System.out.println("enterSubRule("+decisionNumber+")");
            
        }

        public void errorNode(Object t) {
            System.out.println("errorNode("+t+")");
            
        }

        public void exitDecision(int decisionNumber) {
            System.out.println("exitDecision("+decisionNumber+")");
            
        }

        public void exitRule(String grammarFileName, String ruleName) {
            System.out.println("exitRule("+grammarFileName+","+ruleName+")");
            
        }

        public void exitSubRule(int decisionNumber) {
            System.out.println("exitSubRule("+decisionNumber+")");
            
        }

        public void location(int line, int pos) {
            System.out.println("location("+line+","+pos+")");
            
        }

        public void mark(int marker) {
            System.out.println("mark("+marker+")");
            
        }

        public void nilNode(Object t) {
            System.out.println("nilNode("+t+")");
            
        }

        public void recognitionException(RecognitionException e) {
            System.out.println("recognitionException("+e+")");
            e.printStackTrace(System.out);
            
        }

        public void rewind() {
            System.out.println("rewind()");
            
        }

        public void rewind(int marker) {
            System.out.println("rewind("+marker+")");
            
        }

        public void semanticPredicate(boolean result, String predicate) {
            System.out.println("semanticPredicate("+result+","+predicate+")");
            
        }

        public void setTokenBoundaries(Object t, int tokenStartIndex,
                int tokenStopIndex) {
            System.out.println("setTokenBoundaries("+t+","+tokenStartIndex+","+tokenStopIndex+")");
            
        }

        public void terminate() {
            System.out.println("terminate()");
            
        }
        
    };
    
    private static Address parseStreetOnly(String addr) throws RecognitionException {
        ANTLRStringStream input = new ANTLRStringStream(addr.toUpperCase());
        AddressLexer lexer = new AddressLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        TokenStream dbgtokens = new DebugTokenStream(tokens, debugListener);
        AddressParser p = new AddressParser(dbgtokens);
        p.setStartsUrbanNotRural(true);
        p.setAddressDatabase(addressDB);
        p.streetAddress();
        return p.getAddress();
    }
    
    public void testNumberStreetType() throws Exception {
        Address a = parseStreetOnly("4950 Yonge St");
        System.out.println(a);
        assertEquals("YONGE", a.getStreet());
        assertEquals("ST", a.getStreetType());
        assertEquals(Integer.valueOf(4950), a.getStreetNumber());
    }

    public void testSuiteNumberStreetType() throws Exception {
        Address a = parseStreetOnly("2110-4950 Yonge St");
        System.out.println(a);
        assertEquals("YONGE", a.getStreet());
        assertEquals("ST", a.getStreetType());
        assertEquals("2110", a.getSuite());
        assertTrue(a.isSuitePrefix());
        assertEquals(Integer.valueOf(4950), a.getStreetNumber());
    }

    public void testNumberStreetTypeSuite() throws Exception {
        Address a = parseStreetOnly("4950 Yonge St Suite 2110");
        System.out.println(a);
        assertEquals("YONGE", a.getStreet());
        assertEquals("ST", a.getStreetType());
        assertEquals("2110", a.getSuite());
        assertEquals("SUITE", a.getSuiteType());
        assertFalse(a.isSuitePrefix());
        assertEquals(Integer.valueOf(4950), a.getStreetNumber());
    }

    public void testNumberStreetDirection() throws Exception {
        Address a = parseStreetOnly("50 The Esplanade W");
        System.out.println(a);
        assertEquals("THE ESPLANADE", a.getStreet());
        assertNull(a.getStreetType());
        assertEquals(Integer.valueOf(50), a.getStreetNumber());
        assertEquals("W", a.getStreetDirection());
    }

    /**
     * This is some data we came across that caused a NumberFormatException in the
     * parser.
     */
    public void testMalformedStreetNumber() throws Exception {
        Address a = parseStreetOnly("12345-123st");
        System.out.println(a);
        
        // NOTE: this fails now due to a workaround in parseStreetOnly()
        assertEquals("123ST", a.getStreet());
        assertNull(a.getStreetType());
        assertEquals("12345", a.getSuite());
        assertNull(a.getSuiteType());
        assertTrue(a.isSuitePrefix());
    }
}
