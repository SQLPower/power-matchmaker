package ca.sqlpower.matchmaker.swingui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphNodeRenderer;

public class SourceTableNodeRenderer extends JLabel implements GraphNodeRenderer<SourceTableRecord> {

    public SourceTableNodeRenderer() {
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    
    public JComponent getGraphNodeRendererComponent(SourceTableRecord node) {
        setText(node.getKeyValues().toString());
        return this;
    }

}
