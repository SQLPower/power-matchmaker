package ca.sqlpower.matchmaker.swingui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.sql.SQLException;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.SourceTableRecord;

import com.jgoodies.forms.builder.ButtonBarBuilder;

public class SourceTableRecordViewer {

    private final JPanel buttonPanel;
    private final JPanel panel;
    private static final Logger logger = Logger.getLogger(SourceTableRecordViewer.class);

    public SourceTableRecordViewer(SourceTableRecord view, SourceTableRecord master, JButton masterButton, 
            JButton noMatchButton) throws ArchitectException, SQLException {
        this.panel = new JPanel(new GridLayout(0, 1));

        logger.debug("Creating source table record viewer for " + master);

        panel.setBackground(Color.WHITE);

        ButtonBarBuilder bb = new ButtonBarBuilder();
        bb.addGridded(masterButton);
        bb.addGridded(noMatchButton);

        buttonPanel = bb.getPanel();
        panel.addComponentListener(new ComponentListener() {

            void syncSize(int width) {
                buttonPanel.setPreferredSize(new Dimension(panel.getWidth(), buttonPanel.getPreferredSize().height));
            }
            public void componentHidden(ComponentEvent e) {
                syncSize(panel.getWidth());
            }

            public void componentMoved(ComponentEvent e) {
                syncSize(panel.getWidth());
            }

            public void componentResized(ComponentEvent e) {
                syncSize(panel.getWidth());
            }

            public void componentShown(ComponentEvent e) {
                syncSize(panel.getWidth());
            }
        });

        JLabel label = new JLabel();  // just a label to read defaults from
        Color differentForeground = label.getForeground();
        Color sameForeground = differentForeground;
        Font font = label.getFont();
        Font sameFont = font.deriveFont(Font.PLAIN);
        Color sameBackground = Color.WHITE;
        Font differentFont = font.deriveFont(Font.BOLD);
        Color differentBackground = new Color(240, 200, 200);

        Iterator<Object> viewIt = view.fetchValues().iterator();
        Iterator<Object> masterIt = master.fetchValues().iterator();
        boolean darkRow = false;
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
            colValueLabel.setOpaque(true);

            if (same) {
                colValueLabel.setFont(sameFont);
                colValueLabel.setForeground(darkRow ? darkerColor(sameForeground) : sameForeground);
                colValueLabel.setBackground(darkRow ? darkerColor(sameBackground) : sameBackground);
            } else {
                colValueLabel.setFont(differentFont);
                colValueLabel.setForeground(darkRow ? darkerColor(differentForeground) : differentForeground);
                colValueLabel.setBackground(darkRow ? darkerColor(differentBackground) : differentBackground);
            }

            panel.add(colValueLabel);
            darkRow = !darkRow;
        }

    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    public JPanel getButtonPanel() {
        return buttonPanel;
    }
        
    public static JPanel headerPanel(Match match) throws ArchitectException {
        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
        
        Color baseBg = darkerColor(Color.WHITE);
        Color darkBg = darkerColor(baseBg);

        boolean darkRow = false;
        for (SQLColumn col : match.getSourceTable().getColumns()){
            JLabel label = new JLabel(col.getName()); 
            label.setOpaque(true);
            label.setBackground(darkRow ? darkBg : baseBg);
            panel.add(label);
            darkRow = !darkRow;
        }
        return panel;
    }
    
    /**
     * A much less severe version of Color.darker().  Adapted from the
     * Sun JDK version of Color.darker().
     * 
     * @param c The colour to create a darker version of.  This object is
     * not affected by this call.
     * @return A slightly darker shade of the given colour.
     */
    private static Color darkerColor(Color c) {
        final float FACTOR = 0.97f;
        return new Color(
                Math.max((int) (c.getRed() * FACTOR), 0),
                Math.max((int) (c.getGreen() * FACTOR), 0),
                Math.max((int) (c.getBlue() * FACTOR), 0));
    }
}
