/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.swingui;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;

public class MatchMakerSwingSessionTest extends TestCase {
    
    Logger logger = Logger.getLogger(MatchMakerSwingSessionTest.class);
    
    private MatchMakerSwingSession session;
    PlFolder folder1;
    PlFolder folder2;
    JDBCDataSource ds;
    
    @Override
    protected void setUp() throws Exception {
        folder1 = new PlFolder("Test Folder");
        folder2 = new PlFolder("Test Folder2");
        ds = DBTestUtil.getSqlServerDS();
        
        SwingSessionContext stubContext = new StubSwingSessionContext() {
            @Override
            public List<JDBCDataSource> getDataSources() {
                List<JDBCDataSource> dsList = new ArrayList<JDBCDataSource>();
                dsList.add(ds);
                return dsList;
            }
        };
        
        MatchMakerSession stubSessionImp = new StubMatchMakerSession(){
        	FolderParent folders;
        	SQLDatabase db = null;
        	
            @Override
            public FolderParent getCurrentFolderParent() {
            	folders = new FolderParent();
                folders.addChild(folder1);
                folders.addChild(folder2);
                return folders;
            }
            
            @Override
            public SQLDatabase getDatabase() {
            	if (db == null) {
            		db = new SQLDatabase(ds);
            	} 
            	return db;
            }
           
            @Override
            public PlFolder findFolder(String foldername) {
                for (PlFolder folder : getCurrentFolderParent().getChildren(PlFolder.class)){
                    if (folder.getName().equals(foldername)) return folder;
                }
                return null;
            }
            
            @Override
            public TranslateGroupParent getTranslations() {
                TranslateGroupParent tgp = new TranslateGroupParent();
                return tgp;
            }
            
            @Override
            public Connection getConnection() {
            	try {
            	return getDatabase().getConnection();
            	} catch (SQLObjectException e) {
            		throw new RuntimeException("Error getting Connection!", e);
            	}
            }
            
            @Override
            public boolean close() {
                // hibernate session impl is supposed to close its database
            	if (db.isConnected()) db.disconnect();
            	return true;
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
    
    public void testConnectionCloses() {
    	//opens the connection
    	session.getConnection();
    	assertTrue("Test is not very usefull if the connection starts closed!",session.getDatabase().isConnected());
    	session.close();
    	assertFalse("Database connection is not closed! ", session.getDatabase().isConnected());
    }
}
