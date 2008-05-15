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


package ca.sqlpower.matchmaker.swingui;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TranslateGroupParent;

public class MatchMakerSwingSessionTest extends TestCase {
    
    Logger logger = Logger.getLogger(MatchMakerSwingSessionTest.class);
    
    private MatchMakerSwingSession session;
    PlFolder folder1;
    PlFolder folder2;
    
    @Override
    protected void setUp() throws Exception {
        folder1 = new PlFolder("Test Folder");
        folder2 = new PlFolder("Test Folder2");
        
        SwingSessionContext stubContext = new StubSwingSessionContext();
        
        MatchMakerSession stubSessionImp = new StubMatchMakerSession(){
        	FolderParent folders;
        	SQLDatabase db = null;
        	
            @Override
            public FolderParent getCurrentFolderParent() {
            	folders = new FolderParent(this);
                folders.getChildren().add(folder1);
                folders.getChildren().add(folder2);
                return folders;
            }
            
            @Override
            public PlFolder findFolder(String foldername) {
                for (PlFolder folder : getCurrentFolderParent().getChildren()){
                    if (folder.getName().equals(foldername)) return folder;
                }
                if (SHARED_FOLDER_NAME.equals(foldername) || GALLERY_FOLDER_NAME.equals(foldername)) {
                	return new PlFolder<Project>(foldername);
                }
                return null;
            }
            
            @Override
            public TranslateGroupParent getTranslations() {
                TranslateGroupParent tgp = new TranslateGroupParent(this);
                return tgp;
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
