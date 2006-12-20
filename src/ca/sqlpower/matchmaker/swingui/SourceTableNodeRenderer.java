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
