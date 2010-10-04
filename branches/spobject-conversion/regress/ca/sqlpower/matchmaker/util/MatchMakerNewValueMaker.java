/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.util;

import java.util.HashMap;
import java.util.Map;

import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TestingAbstractMatchMakerObject;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.munge.DeDupeResultStep;
import ca.sqlpower.matchmaker.munge.InputDescriptor;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeResultStep;
import ca.sqlpower.matchmaker.munge.MungeStepInput;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.matchmaker.munge.UpperCaseMungeStep;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.GenericNewValueMaker;

public class MatchMakerNewValueMaker extends GenericNewValueMaker {      

    public MatchMakerNewValueMaker(SPObject root) {
        this(root, new PlDotIni());
    }
    
    public MatchMakerNewValueMaker(final SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
        super(root, dsCollection);
    }    
    
    @Override
    public Object makeNewValue(Class<?> valueType, Object oldVal, String propName) {
        if(valueType == MatchMakerTranslateWord.class) {
        	return new MatchMakerTranslateWord();
    	} else if (valueType == TableMergeRules.class) {
        	return new TableMergeRules();
        } else if (valueType == ColumnMergeRules.class) {
        	return new ColumnMergeRules();
        } else if (valueType == SQLInputStep.class) {
        	return new SQLInputStep();
        } else if (valueType == Project.class) {
        	Project project = new Project();
        	PlFolder parent = ((PlFolder) makeNewValue(PlFolder.class, null, 
        			"project parent"));
        	parent.addChild(project);
			return project;
        } else if (valueType == MungeProcess.class) {
        	MungeProcess mp = new MungeProcess();
        	MungeResultStep mrs = new DeDupeResultStep();
        	mp.addChild(mrs);
        	Project parent = ((Project) makeNewValue(Project.class, null, 
				"munge process parent"));
        	parent.addMungeProcess(mp, 0);
        	return mp;
        } else if (valueType == MatchMakerTranslateGroup.class) {
        	return new MatchMakerTranslateGroup();
        } else if (valueType == ColumnMergeRules.MergeActionType.class) {
        	return ColumnMergeRules.MergeActionType.MAX;
        }  else if (valueType == PlFolder.class) {
        	PlFolder plFolder = new PlFolder("Generic PlFolder");
        	FolderParent parentFolder = ((FolderParent) makeNewValue(FolderParent.class, null,
        			"pl folder parent"));
        	parentFolder.addChild(plFolder);
			return plFolder;
        } else if (valueType == MungeStepOutput.class) {
        	return new MungeStepOutput<String>("output", String.class);
        } else if (valueType == FolderParent.class) {
        	FolderParent folderParent = new FolderParent();
        	getRootObject().addChild(folderParent, 0);
        	folderParent.setSession(new TestingMatchMakerSession());
        	return folderParent;
        } else if (valueType == TranslateGroupParent.class) {
        	TranslateGroupParent parent = new TranslateGroupParent();
        	getRootObject().addChild(parent, 0);
        	parent.setSession(new TestingMatchMakerSession());
        	return parent;
        } else if (valueType == Class.class) {
        	if (oldVal == String.class) {
        		return Boolean.class;
        	} else {
        		return String.class;
        	}
        } else if (valueType == Map.class) {
        	return new HashMap<String, String>();
        } else if (valueType == UpperCaseMungeStep.class) {
        	return new UpperCaseMungeStep();
        } else if (valueType == MatchMakerObject.class) {
        	return new TestingAbstractMatchMakerObject();
        } else if (valueType == MungeStepInput.class) {
        	return new MungeStepInput(null, new InputDescriptor("input", Object.class),new UpperCaseMungeStep());
        }  else if (valueType == MMRootNode.class) {
        	MatchMakerSession session = new TestingMatchMakerSession();
        	getRootObject().addChild(session.getRootNode(), 0);
        	return session.getRootNode();
        } else if (valueType == ProjectMode.class) {
        	if (oldVal != ProjectMode.FIND_DUPES) {
        		return ProjectMode.FIND_DUPES;
        	} else {
        		return ProjectMode.CLEANSE;
        	}
        } else if (valueType == AddressDatabase.class) {
        	/*
        	 * FIXME This needs to be fixed somehow not to return null
        	 * but it does not really do anything to harm the functionality
        	 */
        	
        	return null;
        } else if (valueType == TableMergeRules.ChildMergeActionType.class) {
        	if (TableMergeRules.ChildMergeActionType.DELETE_ALL_DUP_CHILD.equals(oldVal)) {
        		return TableMergeRules.ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT;
        	} else {
        		return TableMergeRules.ChildMergeActionType.DELETE_ALL_DUP_CHILD;
        	}
        } else {
            return super.makeNewValue(valueType, oldVal, propName);
        }
    }
}
