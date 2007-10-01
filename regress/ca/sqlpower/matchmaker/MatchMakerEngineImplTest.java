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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.dao.hibernate.TestingMatchMakerHibernateSession;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.TestingDefParamsObject;

public class MatchMakerEngineImplTest extends TestCase {

	Match match;
	MatchEngineImpl matchMakerEngine;
	private TestingMatchMakerHibernateSession session;
	private TestingMatchMakerContext context;
	private TestingDefParamsObject def;
	
	protected void setUp() throws Exception {
		super.setUp();
		match = new Match();
		session = new TestingMatchMakerHibernateSession(DBTestUtil.getOracleDS());
		session.setDatabase(new SQLDatabase());
		match.setSession(session);
		matchMakerEngine = new MatchEngineImpl(session,match);
		context = new TestingMatchMakerContext();
		session.setContext(context);
		def = new TestingDefParamsObject(session);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testValidateEmailSettingsValid() throws SQLException, PLSchemaException{
		def.setEmailReturnAddress("test@sqlpower.ca");
		def.setEmailServerName("mail");
		assertTrue(matchMakerEngine.validateEmailSetting(def));
	}
	
	public void testValidateEmailSettingsNoReturn() throws SQLException, PLSchemaException{
		TestingDefParamsObject def = new TestingDefParamsObject(session);
		def.setEmailReturnAddress(null);
		def.setEmailServerName("mail");
		assertFalse("The email is valid without a return email address",matchMakerEngine.validateEmailSetting(def));
	}
	
	public void testValidateEmailSettingsNoServer() throws SQLException, PLSchemaException{
		TestingDefParamsObject def = new TestingDefParamsObject(session);
		def.setEmailReturnAddress("test@sqlpower.ca");
		def.setEmailServerName("");
		assertFalse("The email is valid without a server name",matchMakerEngine.validateEmailSetting(def));
	}

	//   check for unwriteable file
    public void testLogCantWrite() throws IOException{
        MatchMakerSettings settings = new MatchSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        log.createNewFile();
        log.setReadOnly();
        assertFalse(log.canWrite());
        assertFalse(AbstractEngine.canWriteLogFile(settings));
        log.delete();
    }
    
    // check for append
    public void testCanWriteLogExists() throws IOException{
        MatchMakerSettings settings = new MatchSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        log.createNewFile();
        assertTrue(log.canWrite());
        assertTrue(AbstractEngine.canWriteLogFile(settings));
        log.delete();
    }
    
    // check for create
    public void testCanWriteLogNonExistantButWritable() throws IOException{
        MatchMakerSettings settings = new MatchSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        log.createNewFile();
        assertTrue(log.canWrite());
        log.delete();
        assertTrue(AbstractEngine.canWriteLogFile(settings));
    }
    
    
//  check for append
    public void testCanReadLogExists() throws IOException{
        MatchMakerSettings settings = new MatchSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        log.createNewFile();
        assertTrue(log.canWrite());
        assertTrue(AbstractEngine.canReadLogFile(settings));
        log.delete();
    }
    
    // check for create
    public void testCanReadLogNonExistant() throws IOException{
        MatchMakerSettings settings = new MatchSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        // this is an unreadable file (I hope)
        log.mkdir();
        assertTrue(AbstractEngine.canReadLogFile(settings));
        log.delete();
    }
    

}
