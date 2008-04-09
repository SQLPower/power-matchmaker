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

import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphNodeRenderer;

/**
 * A TableCellRenderer used for visually representing the SourceTableRecord in
 * the match validation graphs. By default, the value of the SourceTableRecord's
 * primary key is used to display the node. However, you can use the 
 * setDisplayColumns method to provide a different set of SQLColumns to display
 * the value of for each SourceTableRecord node.
 */
public class SourceTableNodeRenderer extends DefaultTableCellRenderer implements GraphNodeRenderer<SourceTableRecord> {

    private JTable DUMMY_TABLE;
    
    /**
     * The list of columns whose values will be used to choose the display label
     * for a node.
     */
    private List<SQLColumn> displayColumns;
    
    public SourceTableNodeRenderer(List<SQLColumn> displayColumns) {
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.displayColumns = displayColumns;
    }
    
    public JComponent getGraphNodeRendererComponent(SourceTableRecord node, boolean isSelected, boolean hasFocus) {
        DUMMY_TABLE = new JTable(1,1);
    	getTableCellRendererComponent(DUMMY_TABLE, makeLabel(node), isSelected, hasFocus, 0, 0);
        return this;
    }

    private String makeLabel(SourceTableRecord node) {
    	String ret = null;
    	StringBuilder sb = new StringBuilder();
    	boolean first = true;
    	for(Object value: node.getDisplayValues()) {
    		if (!first) {
    			sb.append(", ");
    		}
    		if (value == null) {
    			sb.append("null");
    			continue;
    		}
    		sb.append(value.toString());
    		first = false;
    	}
    	ret = sb.toString();
    	if (ret == null || ret.equals("")) return "Error";
    	return ret;
    }
    
	public List<SQLColumn> getDisplayColumns() {
		return displayColumns;
	}

	public void setDisplayColumns(List<SQLColumn> displayColumns) {
		this.displayColumns = displayColumns;
	}

}
