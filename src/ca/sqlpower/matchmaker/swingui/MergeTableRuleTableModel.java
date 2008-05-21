/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;

/**
 * A Table model for the merge table rules. Shows the merge tables
 * in a JTable and allows user add/delete/reorder merge tables.
 * <p>
 * It has 4 columns:
 * <dl>
 * 		<dt>table catalog   <dd> merge table catalog in a combo box
 * 		<dt>table schema    <dd> merge table schema in a combo box
 * 		<dt>table name      <dd> merge table name in a combo box
 * 		<dt>delete dup ind  <dd> merge table delete dup ind in a check box
 * </dl>
 */
public class MergeTableRuleTableModel extends AbstractMatchMakerTableModel<MatchMakerFolder<TableMergeRules>, TableMergeRules> {

	public MergeTableRuleTableModel(Project project) {
		super(project.getTableMergeRulesFolder());
	}
	
	public int getColumnCount() {
		return 3;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return mmo.getChildren().get(rowIndex).getCatalogName();
		} else if (columnIndex == 1) {
			return mmo.getChildren().get(rowIndex).getSchemaName();
		} else if (columnIndex == 2) {
			return mmo.getChildren().get(rowIndex).getTableName();
		} else {
			return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return SQLCatalog.class;
		} else if (columnIndex == 1) {
			return SQLSchema.class;
		} else if (columnIndex == 2) {
			return SQLTable.class;
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
		}
		return null;
	}
}
