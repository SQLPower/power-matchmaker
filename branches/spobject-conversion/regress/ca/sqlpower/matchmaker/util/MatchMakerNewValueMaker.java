/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.util;

import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
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
        	return new Project();
        } else if (valueType == MungeProcess.class) {
        	return new MungeProcess();
        } else if (valueType == MatchMakerTranslateGroup.class) {
        	return new MatchMakerTranslateGroup();
        } else {
            return super.makeNewValue(valueType, oldVal, propName);
        }
    }
}
