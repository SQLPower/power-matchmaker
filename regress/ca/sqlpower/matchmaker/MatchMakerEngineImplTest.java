package ca.sqlpower.matchmaker;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.dao.hibernate.HibernateTestUtil;
import ca.sqlpower.matchmaker.dao.hibernate.TestingMatchMakerHibernateSession;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.TestingDefParamsObject;

public class MatchMakerEngineImplTest extends TestCase {

	Match match;
	MatchMakerEngineImpl matchMakerEngine;
	private TestingMatchMakerHibernateSession session;
	private TestingMatchMakerContext context;
	
	protected void setUp() throws Exception {
		super.setUp();
		match = new Match();
		session = new TestingMatchMakerHibernateSession(HibernateTestUtil.getOracleDS());
		session.setDatabase(new SQLDatabase());
		match.setSession(session);
		matchMakerEngine = new MatchMakerEngineImpl(session,match);
		context = new TestingMatchMakerContext();
		context.setEmailEngineLocation("fakeEmailEngine");
		context.setMatchEngineLocation("fakeMatchEngine");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}


	/**
	 * Tests to see if the version of the engine is compatable with the
	 * MatchMaker
	 * 
	 * TODO Add support to the engine to handle this
	 */
	public void testValidateMatchMakerEngineVersion(){
		assertTrue(matchMakerEngine.validateMatchMakerEngineVersion());
	}
	
	public void testValidateEmailSettingsValid() throws SQLException, PLSchemaException{
		TestingDefParamsObject def = new TestingDefParamsObject(session);
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

	
	public void testCanExecuteEmailEngineExists() throws IOException{
		File fakeEngine = new File("fakeEmailEngine");
		fakeEngine.createNewFile();
		assertTrue(fakeEngine.canRead());
		assertTrue(matchMakerEngine.canExecuteEmailEngine(context));
		fakeEngine.delete();
	}
	
	public void testCanExecuteEmailEngineNonExistant() throws IOException{
		File fakeEngine = new File("fakeEmailEngine");
		assertFalse(fakeEngine.canRead());
		assertFalse(matchMakerEngine.canExecuteEmailEngine(context));
		fakeEngine.delete();
	}
	
	public void testCanExecuteMatchEngineExists() throws IOException{
		File fakeEngine = new File("fakeMatchEngine");
		fakeEngine.createNewFile();
		assertTrue(fakeEngine.canRead());
		assertTrue(matchMakerEngine.canExecuteMatchEngine(context));
		fakeEngine.delete();
	}
	
	public void testCanExecuteMatchEngineNonExistant() throws IOException{
		File fakeEngine = new File("fakeMatchEngine");
		assertFalse(fakeEngine.canRead());
		assertFalse(matchMakerEngine.canExecuteMatchEngine(context));
		fakeEngine.delete();
	}
	
	public void testCanExecuteMatchEngine(){
		
	}
}
