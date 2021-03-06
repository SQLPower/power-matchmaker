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

import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.matchmaker.CachableTable;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.MergeSettings;
import ca.sqlpower.matchmaker.MungeSettings;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableIndex;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TestingAbstractMatchMakerObject;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.MungeSettings.AutoValidateSetting;
import ca.sqlpower.matchmaker.MungeSettings.PoolFilterSetting;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.munge.CleanseResultStep;
import ca.sqlpower.matchmaker.munge.DeDupeResultStep;
import ca.sqlpower.matchmaker.munge.InputDescriptor;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeResultStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepInput;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.matchmaker.munge.UpperCaseMungeStep;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLObjectException;
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
        	MatchMakerTranslateWord translateWord = new MatchMakerTranslateWord();
        	MatchMakerTranslateGroup parent = ((MatchMakerTranslateGroup) makeNewValue(MatchMakerTranslateGroup.class, null, 
				"translate word parent"));
        	parent.addChild(translateWord);
			return translateWord;
        } else if (valueType == CachableTable.class) {
        	Project project = (Project) makeNewValue(Project.class, null, "old parent");
        	return project.getSourceTablePropertiesDelegate();
        } else if (valueType == MungeSettings.class) {
        	Project project = (Project) makeNewValue(Project.class, null, "old parent");
        	return project.getMungeSettings();
        } else if (valueType == MergeSettings.class) {
        	Project project = (Project) makeNewValue(Project.class, null, "old parent");
        	return project.getMergeSettings();
        } else if (valueType == TableIndex.class) {
        	Project project = (Project) makeNewValue(Project.class, null, "old parent");
        	TableIndex index = new TableIndex(project.getSourceTablePropertiesDelegate(), "Role");
        	project.addChild(index);
        	return index;
    	} else if (valueType == TableMergeRules.class) {
        	TableMergeRules mergeRule = new TableMergeRules();
        	Project project = (Project) makeNewValue(Project.class, null, "old parent");
        	project.addChild(mergeRule);
			return mergeRule;
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
        	parent.addChild(mp);
        	return mp;
        } else if (valueType == DeDupeResultStep.class) {
        	MungeProcess mp = new MungeProcess();
        	DeDupeResultStep mrs = new DeDupeResultStep();
        	mp.addChild(mrs);
        	Project parent = ((Project) makeNewValue(Project.class, null, 
				"munge process parent"));
        	parent.addMungeProcess(mp, 0);
        	return mrs;
        } else if (valueType == CleanseResultStep.class) {
        	MungeProcess mp = new MungeProcess();
        	CleanseResultStep mrs;
			try {
				mrs = new CleanseResultStep();
			} catch (SQLObjectException e) {
				throw new RuntimeException(e);
			}
        	mp.addChild(mrs);
        	Project parent = ((Project) makeNewValue(Project.class, null, 
				"munge process parent"));
        	parent.addMungeProcess(mp, 0);
        	return mrs;
        } else if (valueType == MatchMakerTranslateGroup.class) {
        	MatchMakerTranslateGroup group = new MatchMakerTranslateGroup();
        	TranslateGroupParent parent = ((TranslateGroupParent) makeNewValue(TranslateGroupParent.class, null, 
				"translate group parent"));
        	parent.addChild(group);
			return group;
        } else if (valueType == ColumnMergeRules.MergeActionType.class) {
        	return ColumnMergeRules.MergeActionType.MAX;
        }  else if (valueType == PlFolder.class) {
        	PlFolder plFolder = new PlFolder("Generic PlFolder");
        	FolderParent parentFolder = ((FolderParent) makeNewValue(FolderParent.class, null,
        			"pl folder parent"));
        	parentFolder.addChild(plFolder);
			return plFolder;
        } else if (valueType == MungeStepOutput.class) {
        	MungeStepOutput mso = new MungeStepOutput<String>("output", String.class);
        	MungeStep ms = (MungeStep)makeNewValue(MungeStep.class, null, "parent process");
        	ms.addChild(mso, ms.getMungeStepOutputs().size());
        	return mso;
        } else if (valueType == FolderParent.class) {
        	FolderParent folderParent = new FolderParent();
        	getRootObject().addChild(folderParent, 0);
        	folderParent.setSession(new TestingMatchMakerSession(false));
        	return folderParent;
        } else if (valueType == TranslateGroupParent.class) {
        	TranslateGroupParent parent = new TranslateGroupParent();
        	getRootObject().addChild(parent, 0);
        	parent.setSession(new TestingMatchMakerSession(false));
        	return parent;
        } else if (valueType == User.class) {
        	if (oldVal == null) {
        		return new User("user", "12345");
        	} else {
        		return new User("new_" + ((User) oldVal).getUsername(), "12345");
        	}
        } else if (valueType == Group.class) {
            	if (oldVal == null) {
            		return new Group("testgroup");
            	} else {
            		return new Group("new_" + ((Group) oldVal).getName());
            	}
        } else if (valueType == Class.class) {
        	if (oldVal == String.class) {
        		return Boolean.class;
        	} else {
        		return String.class;
        	}
        } else if (valueType == Map.class) {
        	return new HashMap<String, String>();
        } else if (valueType == MatchMakerObject.class) {
        	return new TestingAbstractMatchMakerObject();
        } else if (MungeStep.class.isAssignableFrom(valueType)) {
        	MungeProcess process = ((MungeProcess) makeNewValue(MungeProcess.class, null,
				"process parent"));
        	MungeStep ms = new UpperCaseMungeStep();
        	process.addChild(ms);
        	return ms;
        } else if (valueType == MungeStepInput.class) {
        	MungeStep ms = ((MungeStep) makeNewValue(MungeStep.class, null,
				"parent step"));
        	MungeStepInput msi = new MungeStepInput(new InputDescriptor("input", Object.class), ms);
        	ms.addChild(msi, 0);
        	return msi;
        } else if (valueType == InputDescriptor.class) {
        	MungeStepInput step = ((MungeStepInput) makeNewValue(MungeStepInput.class, null,
				"step parent"));
        	return step.getDescriptor();
        }  else if (valueType == MMRootNode.class) {
        	MatchMakerSession session = new TestingMatchMakerSession(false);
        	getRootObject().addChild(session.getRootNode(), 0);
        	return session.getRootNode();
        } else if (valueType == ProjectMode.class) {
        	if (oldVal != ProjectMode.FIND_DUPES) {
        		return ProjectMode.FIND_DUPES;
        	} else {
        		return ProjectMode.CLEANSE;
        	}
        } else if (valueType == AutoValidateSetting.class) {
        	if (oldVal != AutoValidateSetting.EVERYTHING_WITH_SUGGESTION) {
        		return AutoValidateSetting.EVERYTHING_WITH_SUGGESTION;
        	} else {
        		return AutoValidateSetting.SERP_CORRECTABLE;
        	}
        } else if (valueType == PoolFilterSetting.class) {
        	if (oldVal != PoolFilterSetting.EVERYTHING) {
        		return PoolFilterSetting.EVERYTHING;
        	} else {
        		return PoolFilterSetting.VALID_OR_INVALID;
        	} 
        } else if (valueType == MatchPool.class) {
            	return new MatchPool();
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
