package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.matchmaker.Match;

/**
 * The MatchResultVisualizer produces graphical representations of the matches
 * in a Match Result Table.
 */
public class MatchResultVisualizer {
    
    private static final Logger logger = Logger.getLogger(MatchResultVisualizer.class);

    /**
     * A list of colours to assign to different match groups (for colouring the edges in the graph).
     * Currently, this is the Brewer Colour Scheme "set19".
     */
    private static final String[] COLOURS = {
        "#e41a1c",
        "#377eb8",
        "#4daf4a",
        "#80b1d3",
        "#984ea3",
        "#ff7f00",
        "#ffff33",
        "#a65628",
        "#f781bf",
        "#999999"
    };
    
    /**
     * This action just calls the {@link #refreshGraph()} method.
     */
    private final Action refreshGraphAction = new AbstractAction("Refresh Graph") {
        public void actionPerformed(ActionEvent e) {
            try {
                refreshGraph();
            } catch (Exception ex) {
                ASUtils.showExceptionDialogNoReport(settingsPanel, "Couldn't refresh graph!", ex);
            }
        }
    };
    
    /**
     * The group names that have been seen by {@link colourName()} in the order it saw them.
     */
    private final List<String> groupNames = new ArrayList<String>();
    
    /**
     * This panel holds all the interface components for graph settings.
     */
    private final JPanel settingsPanel;
    
    /**
     * This is the match whose result table we're visualizing.
     */
    private final Match match;
    
    /**
     * The session the match lives in.  Used as a source of database connections,
     * and other session-y stuff.
     */
    private final MatchMakerSwingSession session;

    /**
     * The dialog that contains the SettingsPanel.  The constructor
     * creates it and sets its parent to the SwingSession's frame. 
     */
    private final JDialog dialog;

    /**
     * Text field that holds the pathname for output location for the generated dot file.
     */
    private JTextField dotFileField;

    /**
     * Pops up a file chooser for the dotFileField.
     */
    private Action pickDotFileAction = new AbstractAction("...") {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            int choice = fc.showDialog(settingsPanel, "Choose");
            if (choice == JOptionPane.OK_OPTION) {
                dotFileField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        }
    };
   
    public MatchResultVisualizer(Match match, MatchMakerSwingSession session) {
        this.match = match;
        this.session = session;
        settingsPanel = new JPanel();
        settingsPanel.add(dotFileField = new JTextField());
        settingsPanel.add(new JButton(pickDotFileAction ));
        settingsPanel.add(new JButton(refreshGraphAction));
        
        File dotFile = new File(
                System.getProperty("user.home"), "matchmaker_graph_"+match.getName()+".dot");
        dotFileField.setText(dotFile.getAbsolutePath());
        
        dialog = new JDialog(session.getFrame());
        dialog.setTitle("Match Result Visualizer");
        dialog.add(settingsPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(session.getFrame());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public JPanel getSettingsPanel() {
        return settingsPanel;
    }
    
    /**
     * Refreshes the visualization graph based on the current contents of the match
     * result table.
     * 
     * @throws SQLException
     * @throws IOException
     * @throws ArchitectException
     */
    public void refreshGraph() throws SQLException, IOException, ArchitectException {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        PrintWriter out = null;
        try {
            File dotFile = new File(dotFileField.getText());
            out = new PrintWriter(new BufferedWriter(new FileWriter(dotFile)));
            out.println("digraph " + match.getName());
            out.println("{");
            
            con = session.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM "+DDLUtils.toQualifiedName(match.getResultTable()));
            while (rs.next()) {
                out.print("  \"");
                out.print(lhsNodeName(rs));
                out.print("\" -> \"");
                out.print(rhsNodeName(rs));
                out.print("\" [color=\"");
                out.print(colourName(rs.getString("GROUP_ID")));
                out.println("\"];");
            }

            out.println("}");
            
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ex) { logger.error("Couldn't close result set", ex); }
            if (stmt != null) try { stmt.close(); } catch (SQLException ex) { logger.error("Couldn't close statement", ex); }
            if (con != null) try { con.close(); } catch (SQLException ex) { logger.error("Couldn't close connection", ex); }
            if (out != null) out.close();
        }
    }

    /**
     * Translates the given match group name to an X11 colour name that GraphViz can use.
     * 
     * @param string The match group name
     * @return An X11 colour name such that every time this method is called with the same
     * match group name, the same colour name is returned, but no two match group names will
     * translate to the same colour for the lifetime of this instance of MatchResultVisualizer.
     * @throws ArrayIndexOutOfBoundsException if you use more match groups than we have set up
     * colours for.  (see COLOURS and add more items to it if you're running into this problem).
     */
    private Object colourName(String groupName) {
        int index = groupNames.indexOf(groupName);
        if (index < 0) {
            groupNames.add(groupName);
            index = groupNames.size()-1;
        }
        return COLOURS[index];
    }

    /**
     * Calls nodeName() with the correct parameters to get the unique identifier
     * values of the right-hand-side record.
     */
    private String rhsNodeName(ResultSet rs) throws ArchitectException, SQLException {
        int indexSize = match.getSourceTableIndex().getChildCount();
        return nodeName(rs, indexSize + 1, indexSize);
    }

    /**
     * Calls nodeName() with the correct parameters to get the unique identifier
     * values of the left-hand-side record.
     */
    private String lhsNodeName(ResultSet rs) throws SQLException, ArchitectException {
        return nodeName(rs, 1, match.getSourceTableIndex().getChildCount());
    }
    
    /**
     * Comes up with a name for a graph node by concatenating the unique key values
     * from the current row of the given result set.
     * <p>
     * This is a subroutine of rhsNodeName and lhsNodeName, which are smart enough
     * to fill in the tricky index parameters for you.
     * 
     * @param rs The result set to get current values from
     * @param startIndex The column number to start getting values from
     * @param count The number of consecutive columns to get values from
     * @return A string containing the concatenation of the given values.
     * @throws SQLException if there is trouble retrieving any column values as strings
     */
    private String nodeName(ResultSet rs, int startIndex, int count) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < startIndex + count; i++) {
            if (i != startIndex) sb.append("::");
            sb.append(rs.getString(i));
        }
        return sb.toString();
    }

    public void showDialog() {
        dialog.setVisible(true);
        dialog.requestFocus();
    }


}
