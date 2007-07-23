package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.PlFolder;

public class MatchMakerSwingSessionTest extends TestCase {
    
    Logger logger = Logger.getLogger(MatchMakerSwingSessionTest.class);
    
    private MatchMakerSwingSession session;
    PlFolder folder1;
    PlFolder folder2;
    SPDataSource ds;
    
    @Override
    protected void setUp() throws Exception {
        folder1 = new PlFolder("Test Folder");
        folder2 = new PlFolder("Test Folder2");
        ds = DBTestUtil.getSqlServerDS();
        
        SwingSessionContext stubContext = new StubSwingSessionContext() {
            @Override
            public List<SPDataSource> getDataSources() {
                List<SPDataSource> dsList = new ArrayList<SPDataSource>();
                dsList.add(ds);
                return dsList;
            }
        };
        
        MatchMakerSession stubSessionImp = new StubMatchMakerSession(){
        	FolderParent folders;
        	
            @Override
            public FolderParent getCurrentFolderParent() {
            	folders = new FolderParent(this);
                folders.getChildren().add(folder1);
                folders.getChildren().add(folder2);
                return folders;
            }
            
            @Override
            public SQLDatabase getDatabase() {
                return new SQLDatabase(ds);
            }
           
            @Override
            public PlFolder findFolder(String foldername) {
                for (PlFolder folder : getCurrentFolderParent().getChildren()){
                    if (folder.getName().equals(foldername)) return folder;
                }
                return null;
            }
            
        };
        
        session = new MatchMakerSwingSession(stubContext, stubSessionImp);
    }
    
    
    public void testGetFolders(){
        assertEquals("Got the wrong number of folders", 2, session.getCurrentFolderParent().getChildren().size());
        assertTrue("Missing Folder", session.getCurrentFolderParent().getChildren().contains(folder1));
        assertTrue("Missing Folder", session.getCurrentFolderParent().getChildren().contains(folder2));        
    }
    
    public void testGetFolderByName(){
        assertNull("There is at least one folder that should not exist", session.findFolder("Randomness"));
        assertEquals("Got the wrong folder", folder1, session.findFolder("Test Folder"));
        assertEquals("Got the wrong folder", folder2, session.findFolder("Test Folder2"));
    }          
}
