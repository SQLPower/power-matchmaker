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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

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

    /**
     * A simple container for a part of the label. We care about the text, the colour,
     * and how many pixels wide it will be when painted.
     */
    private static class LabelPart {

        final String text;
        final Color color;
        final int width;

        public LabelPart(Color color, String text, int width) {
            super();
            this.color = color;
            this.text = text;
            this.width = width;
        }
    }
    
    /**
     * The string we show for null values.
     */
    private static final String NULL_STRING = "(null)";
    
    /**
     * The string that goes between the values we display.
     */
    private static final String SEPARATOR = ", ";

    /**
     * Since this renderer is just a specialized table cell renderer, we have to provide
     * a fake parent table to keep the superclass happy. This is that table.
     */
    private final JTable DUMMY_TABLE = new JTable(1,1);;
    
    /**
     * The parts of the label to render. Gets set up in {@link #getGraphNodeRendererComponent(SourceTableRecord, boolean, boolean)},
     * and used by {@link #paintComponent(Graphics)}.
     */
    private List<LabelPart> labelParts;
    
    /**
     * Creates a new renderer for source table graph node items.
     */
    public SourceTableNodeRenderer() {
        setOpaque(true);
    }
    
    /**
     * Sets up this renderer to paint the display values of the given node.
     */
    public JComponent getGraphNodeRendererComponent(SourceTableRecord node, boolean isSelected, boolean hasFocus) {
        
        FontMetrics fm = getFontMetrics(getFont());
        labelParts = new ArrayList<LabelPart>(node.getDisplayValues().size() * 2 - 1);
        boolean first = true;
        for (Object value: node.getDisplayValues()) {
            if (!first) {
                labelParts.add(new LabelPart(getForeground(), SEPARATOR, fm.stringWidth(SEPARATOR)));
            }
            
            String label;
            Color color;
            if (value == null) {
                label = NULL_STRING;
                color = Color.GRAY;
            } else {
                label = value.toString();
                color = getForeground();
            }
            labelParts.add(new LabelPart(color, label, fm.stringWidth(label)));
            first = false;
        }

        StringBuilder sb = new StringBuilder();
        for (LabelPart p : labelParts) {
            sb.append(p.text);
        }

        // the contents of sb are simply for getting the right preferred size
    	getTableCellRendererComponent(DUMMY_TABLE, sb.toString(), isSelected, hasFocus, 0, 0);
    	
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {

        // don't call super.paintComponent() because it will try to draw the string again!
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        FontMetrics fm = getFontMetrics(getFont());
        
        int x = 0;
        if (getBorder() != null) {
            x = getBorder().getBorderInsets(this).left;
        }
        int y = fm.getAscent();
       
        for (LabelPart p : labelParts) {
            g.setColor(p.color);
            g.drawString(p.text, x, y);
            x += p.width;
        }
    }
    
}
