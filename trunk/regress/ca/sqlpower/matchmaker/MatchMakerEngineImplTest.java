package ca.sqlpower.matchmaker;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.dao.hibernate.HibernateTestUtil;
import ca.sqlpower.matchmaker.dao.hibernate.TestingMatchMakerHibernateSession;
import ca.sqlpower.matchmaker.event.EngineEvent;
import ca.sqlpower.matchmaker.event.EngineListener;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.TestingDefParamsObject;

public class MatchMakerEngineImplTest extends TestCase {

	private class CountingEngineListener implements EngineListener {
		int ends;
		int starts;
		boolean removeMe;
		
		public boolean isRemoveMe() {
			return removeMe;
		}

		public void setRemoveMe(boolean removeMe) {
			this.removeMe = removeMe;
		}

		public void engineEnd(EngineEvent e) {
			ends++;
			if (removeMe){
				e.getSource().removeEngineListener(this);
			}
		}

		public void engineStart(EngineEvent e) {
			starts++;
			if (removeMe){
				e.getSource().removeEngineListener(this);
			}
		}

		public int getAllEvents(){
			return ends+starts;
		}
		public int getEnds() {
			return ends;
		}

		public void setEnds(int ends) {
			this.ends = ends;
		}

		public int getStarts() {
			return starts;
		}

		public void setStarts(int starts) {
			this.starts = starts;
		}
		
	}
	
	Match match;
	MatchMakerEngineImpl matchMakerEngine;
	private TestingMatchMakerHibernateSession session;
	private TestingMatchMakerContext context;
	private TestingDefParamsObject def;
	private CountingEngineListener l1;
	private CountingEngineListener l2;
	
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
		session.setContext(context);
		def = new TestingDefParamsObject(session);
		l1 = new CountingEngineListener();
		l2 = new CountingEngineListener();
		matchMakerEngine.addEngineListener(l1);
		matchMakerEngine.addEngineListener(l2);
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
	
	//   check for unwriteable file
    public void testLogCantWrite() throws IOException{
        MatchMakerSettings settings = new MatchSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        log.createNewFile();
        log.setReadOnly();
        assertFalse(log.canWrite());
        assertFalse(matchMakerEngine.canWriteLogFile(settings));
        log.delete();
    }
    
    // check for append
    public void testCanWriteLogExists() throws IOException{
        MatchMakerSettings settings = new MatchSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        log.createNewFile();
        assertTrue(log.canWrite());
        assertTrue(matchMakerEngine.canWriteLogFile(settings));
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
        assertTrue(matchMakerEngine.canWriteLogFile(settings));
    }
    
    
//  check for append
    public void testCanReadLogExists() throws IOException{
        MatchMakerSettings settings = new MatchSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        log.createNewFile();
        assertTrue(log.canWrite());
        assertTrue(matchMakerEngine.canReadLogFile(settings));
        log.delete();
    }
    
    // check for create
    public void testCanReadLogNonExistant() throws IOException{
        MatchMakerSettings settings = new MatchSettings(); 
        File log = new File("mmenginetest.log");
        settings.setLog(log);
        // this is an unreadable file (I hope)
        log.mkdir();
        assertTrue(matchMakerEngine.canReadLogFile(settings));
        log.delete();
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
	
	public void testEndEventsFire(){
		matchMakerEngine.fireEngineEnd();
		assertEquals("Wrong number of events received",1,l1.getAllEvents());
		assertEquals("Wrong number of events received",1,l2.getAllEvents());
		assertEquals("Wrong type of events received",1,l1.ends);
		assertEquals("Wrong type of events received",1,l2.ends);
	}
	
	public void testEndEventsFireWhenOneListenerIsRemoved(){
		matchMakerEngine.fireEngineEnd();
		l2.setRemoveMe(true);
		assertEquals("Wrong number of events received",1,l1.getAllEvents());
		assertEquals("Wrong number of events received",1,l2.getAllEvents());
		assertEquals("Wrong type of events received",1,l1.ends);
		assertEquals("Wrong type of events received",1,l2.ends);
	}
	
	public void testStartEventsFire(){
		matchMakerEngine.fireEngineStart();
		assertEquals("Wrong number of events received",1,l1.getAllEvents());
		assertEquals("Wrong number of events received",1,l2.getAllEvents());
		assertEquals("Wrong type of events received",1,l1.getStarts());
		assertEquals("Wrong type of events received",1,l2.getStarts());
	}
	
	public void testStartEventsFireWhenOneListenerIsRemoved(){
		matchMakerEngine.fireEngineStart();
		l2.setRemoveMe(true);
		assertEquals("Wrong number of events received",1,l1.getAllEvents());
		assertEquals("Wrong number of events received",1,l2.getAllEvents());
		assertEquals("Wrong type of events received",1,l1.getStarts());
		assertEquals("Wrong type of events received",1,l2.getStarts());
	}
	
	public void testHasOdbcDsnNull(){
		ArchitectDataSource ds = new ArchitectDataSource();
		ds.setOdbcDsn(null);
		assertFalse("An empty dsn should be considered invalid",MatchMakerEngineImpl.hasODBCDSN(ds));
	}
	public void testHasOdbcDsnEmpty(){
		ArchitectDataSource ds = new ArchitectDataSource();
		ds.setOdbcDsn("");
		assertFalse("An empty dsn should be considered invalid",MatchMakerEngineImpl.hasODBCDSN(ds));
	}
	// Because we can't check to see if this is a valid odbc
	// dsn we have to go on length of string
	public void testHasOdbcDsnvalid(){
		ArchitectDataSource ds = new ArchitectDataSource();
		ds.setOdbcDsn("Valid");
		
		assertTrue("Dsn should be considered valid",MatchMakerEngineImpl.hasODBCDSN(ds));
	}
}
