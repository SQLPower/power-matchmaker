package ca.sqlpower.matchmaker.dao.hibernate;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;

public class HibernateSessionContextTest extends TestCase {

    /**
     * The session context we're testing.
     */
	private MatchMakerSessionContext ctx;
    
    /**
     * The sole data source the setUp() method puts in the session context.  Provided
     * here for convenience.  You could get the same data source with ctx.getDataSources().get(0).
     */
	private ArchitectDataSource ds;
    
    /**
     * The path that we tell the session context the PL.INI file lives in.
     */
    private static final String PLINIPATH = "/fake/pl.ini";
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DataSourceCollection ini = new PlDotIni();
        ds = DBTestUtil.getOracleDS();
        ini.addDataSource(ds);
        ctx = new MatchMakerHibernateSessionContext(ini, PLINIPATH);
    }
    
    public void testGetDataSources() {
        assertNotNull(ctx.getDataSources());
    }
    
	public void testCreateSession() throws Exception {
		MatchMakerSession session = ctx.createSession(ds, ds.getUser(), ds.getPass());
		assertNotNull(session);
	}
    
    /**
     * The context might have to alter the username and password in the data source
     * we give it, but we don't want this to permanently alter the settings in pl.ini.
     * This test insures the data source we give it remains untouched.
     */
    public void testDataSourceNotModifiedByLogin() {
        Map<String, String> originalProps = new HashMap<String, String>(ds.getPropertiesMap());
        try {
            ctx.createSession(ds, "cows", "moo");
        } catch (Exception ex) {
            // the username/password is wrong, so the login will fail. That's not what we're testing
        }
        assertEquals("DataSource should not have been modified", originalProps, ds.getPropertiesMap());
    }
    
    public void testGetHibernateSessionFactory() throws Exception{
        MatchMakerSession mmSession = ctx.createSession(ds, ds.getUser(), ds.getPass());
        //we're testing this since the purpose of the getHibernateSessionFactory is to
        //initialize the PlFolder
        assertNotNull(mmSession.getFolders());
    }
    
    public void testGetEngineLocationWindows() {
        final String realOsName = System.getProperty("os.name");
        final String sep = System.getProperty("file.separator");
        try {
            System.setProperty("os.name", "Windows");
            assertEquals("You have to change this test if you modify PLINIPATH", "/fake/pl.ini", PLINIPATH);
            assertEquals(sep+"fake"+sep+"Match_ODBC.exe", ctx.getMatchEngineLocation());
        } finally {
            System.setProperty("os.name", realOsName);
        }
    }

    public void testGetEngineLocationUnix() {
        final String realOsName = System.getProperty("os.name");
        final String sep = System.getProperty("file.separator");
        try {
            System.setProperty("os.name", "unix");
            assertEquals("You have to change this test if you modify PLINIPATH", "/fake/pl.ini", PLINIPATH);
            assertEquals(sep+"fake"+sep+"Match_ODBC", ctx.getMatchEngineLocation());
        } finally {
            System.setProperty("os.name", realOsName);
        }
    }
}
