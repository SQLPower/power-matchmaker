package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.hibernate.HibernateTestUtil;

public class MatchMakerSwingSessionTest extends TestCase {
    
    Logger logger = Logger.getLogger(MatchMakerSwingSessionTest.class);
    
    private MatchMakerSwingSession session;
    PlFolder folder1;
    PlFolder folder2;
    ArchitectDataSource ds;
    
    @Override
    protected void setUp() throws Exception {
        folder1 = new PlFolder("Test Folder");
        folder2 = new PlFolder("Test Folder2");
        ds = HibernateTestUtil.getSqlServerDS();
        
        SwingSessionContext stubContext = new StubSwingSessionContext() {
            @Override
            public List<ArchitectDataSource> getDataSources() {
                List<ArchitectDataSource> dsList = new ArrayList<ArchitectDataSource>();
                dsList.add(ds);
                return dsList;
            }
        };
        
        MatchMakerSession stubSessionImp = new StubMatchMakerSession(){
            @Override
            public List<PlFolder> getFolders() {
                List<PlFolder> folders = new ArrayList <PlFolder>();
                folders.add(folder1);
                folders.add(folder2);
                return folders;
            }
            
            @Override
            public SQLDatabase getDatabase() {
                return new SQLDatabase(ds);
            }
           
            @Override
            public PlFolder findFolder(String foldername) {
                for (PlFolder folder : getFolders()){
                    if (folder.getName().equals(foldername)) return folder;
                }
                return null;
            }
            
        };
        
        session = new MatchMakerSwingSession(stubContext, stubSessionImp);
    }
    
    
    public void testGetFolders(){
        assertEquals("Got the wrong number of folders", 2, session.getFolders().size());
        assertTrue("Missing Folder", session.getFolders().contains(folder1));
        assertTrue("Missing Folder", session.getFolders().contains(folder2));        
    }
    
    public void testGetFolderByName(){
        assertNull("There is at least one folder that should not exist", session.findFolder("Randomness"));
        assertEquals("Got the wrong folder", folder1, session.findFolder("Test Folder"));
        assertEquals("Got the wrong folder", folder2, session.findFolder("Test Folder2"));
    }          
}
