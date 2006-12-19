package ca.sqlpower.matchmaker.swingui;

import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.SourceTableRecord;

public class SourceTableRecordViewer {
    
    private final JPanel panel;
    
    public SourceTableRecordViewer(SourceTableRecord view, SourceTableRecord master) throws ArchitectException, SQLException {
        this.panel = createPanel(view, master);
    }
 
    // FIXME: get rid of throws clause when source table records are pre-populated by a join
    private final JPanel createPanel(SourceTableRecord view, SourceTableRecord master) throws ArchitectException, SQLException {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        Iterator<Object> viewIt = view.fetchValues().iterator();
        Iterator<Object> masterIt = master.fetchValues().iterator();
        while (viewIt.hasNext()) {
            Object viewVal = viewIt.next();
            Object masterVal = masterIt.next();
            
            boolean same;
            
            if (viewVal == null) {
                same = masterVal == null;
            } else {
                same = viewVal.equals(masterVal);
            }
            
            JLabel colValueLabel = new JLabel(String.valueOf(viewVal));
            
            if (!same) {
                colValueLabel.setFont(colValueLabel.getFont().deriveFont(Font.BOLD));
            }
            
            panel.add(colValueLabel);
        }
        
        return panel;
    }
    
    public JPanel getPanel() {
        return panel;
    }
}
