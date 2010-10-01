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

import javax.swing.event.TableModelEvent;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * A Table model for the merge table rules. Shows the merge tables
 * in a JTable and allows user add/delete/reorder merge tables.
 * <p>
 * It has 4 columns:
 * <dl>
 * 		<dt>table catalog   <dd> merge table catalog
 * 		<dt>table schema    <dd> merge table schema
 * 		<dt>table name      <dd> merge table name
 * 		<dt>merge action  <dd> table merge rule merge action in a combo box
 * </dl>
 */
public class MergeTableRuleTableModel extends AbstractMatchMakerTableModel<Project> {
	
	Project project;

	public MergeTableRuleTableModel(Project project) {
		super(project);
		this.project = project;
	}
	
	public int getColumnCount() {
		return 4;
	}
	
	public int getRowCount() {
		return project.getTableMergeRules().size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		TableMergeRules tableMergeRule = mmo.getChildren(TableMergeRules.class).get(rowIndex);
		if (columnIndex == 0) {
			return tableMergeRule.getCatalogName();
		} else if (columnIndex == 1) {
			return tableMergeRule.getSchemaName();
		} else if (columnIndex == 2) {
			return tableMergeRule.getTableName();
		} else if (columnIndex == 3) {
			if (tableMergeRule.isSourceMergeRule()) {
				return null;
			} else {
				return tableMergeRule.getChildMergeAction();
			}
		} else {
			return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		TableMergeRules rule = mmo.getChildren(TableMergeRules.class).get(rowIndex);
		
		if (columnIndex == 3 && !rule.isSourceMergeRule()) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return SQLCatalog.class;
		} else if (columnIndex == 1) {
			return SQLSchema.class;
		} else if (columnIndex == 2) {
			return SQLTable.class;
		} else if (columnIndex == 3) {
			return ChildMergeActionType.class;
		}
		return super.getColumnClass(columnIndex);
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0) {
			return SQLObjectChooser.CATALOG_STRING;
		} else if (columnIndex == 1) {
			return SQLObjectChooser.SCHEMA_STRING;
		} else if (columnIndex == 2) {
			return "Name";
		} else if (columnIndex == 3) {
			return "Merge Action";
		}
		return null;
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		TableMergeRules rule = mmo.getChildren(TableMergeRules.class).get(rowIndex);
		if (rule.isSourceMergeRule()) return;
		
		if (columnIndex == 3) {
			rule.setChildMergeAction((ChildMergeActionType) aValue);
		} else {
			throw new RuntimeException("setValueAt: Unexcepted column index:"+columnIndex);
		}
		fireTableChanged(new TableModelEvent(this,rowIndex));
	}
}
