package ca.sqlpower.matchmaker.swingui;

import java.util.prefs.Preferences;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.matchmaker.dao.hibernate.HibernateTestUtil;

public class SwingSessionContextTest extends TestCase {

    ArchitectSession archSession;
    SwingSessionContext context;
    Preferences prefs;
    CoreUserSettings coreSettings;
    PlDotIni ini  = new PlDotIni();
    ArchitectDataSource ds;
    
    protected void setUp() throws Exception {
        super.setUp();
        ini.addDataSource(HibernateTestUtil.getOracleDS());
        archSession = new ArchitectSessionImpl();        
        coreSettings = new CoreUserSettings();
        coreSettings.setPlDotIni(ini);        
        archSession.setUserSettings(coreSettings);       
        context = new SwingSessionContext(archSession);        
    }
    
    public void testGetLastLogin(){
        ArchitectDataSource ds = HibernateTestUtil.getOracleDS();
        context.setLastLoginDataSource(ds);
        System.out.println("42" + ds);
        ArchitectDataSource actualDS = context.getLastLoginDataSource();
        // XXX
        ds.diff(actualDS);
        assertEquals("Did not remember the last login", ds, actualDS);
        System.out.println("44" + ds);
    }
    
    //public void testGetEngineLocation(){
    //    assertNotNull(context.getEngineLocation());        
    //}

    
}
