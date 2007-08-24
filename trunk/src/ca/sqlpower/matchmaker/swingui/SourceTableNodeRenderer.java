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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphNodeRenderer;

public class SourceTableNodeRenderer extends DefaultTableCellRenderer implements GraphNodeRenderer<SourceTableRecord> {

    private static final JTable DUMMY_TABLE = new JTable(1, 1);
    
    public SourceTableNodeRenderer() {
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    
    public JComponent getGraphNodeRendererComponent(SourceTableRecord node, boolean isSelected, boolean hasFocus) {
        getTableCellRendererComponent(DUMMY_TABLE, node.getKeyValues().toString(), isSelected, hasFocus, 0, 0);
        return this;
    }

}
