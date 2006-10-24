package ca.sqlpower.matchmaker.swingui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.ASUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FilterMakerDialog extends JDialog {

    private static Logger logger = Logger.getLogger(FilterMakerDialog.class);

    private JComboBox columnName;
    private JTextField conditionTextField;
    private JComboBox comparisonOperator;
    private JComboBox columnName2;
    private JButton pasteButton;    
    private JButton andButton;
    private JButton orButton;
    private JButton notButton;
    private JButton testButton;
    private JButton clearButton;
    private JButton okButton;
    private JButton cancelButton;
    private JTextArea filterText;
    private JTextArea returnText;
    private boolean trueForTextField;


    private static final String EQUALS = "=";
    private static final String LIKE = "like";
    private static final String IN = "in";
    private static final String ISNULL = "is null";
    private static final String BRACKETS = "< >";
    private static final String NOTLIKE = "not like";
    private static final String LESSTHAN = "<";
    private static final String GREATERTHAN = ">";
    private static final String LESSTHANOREQUALTHAN = "<=";
    private static final String GREATEROREQUALTHAN = ">=";

    private SQLTable matchSourceTable;

    /**
     * 
     * @param parent The parent frame
     * @param returnText the JTextArea that the filter will put text in
     * @param matchSourceTable the sqltable the filter is being used for
     * @param trueForTextField true if the last input component should be JTextArea
     *                      false if the last input component should be a dropdown  
     */

    public FilterMakerDialog(JFrame parent,
            final JTextArea returnText,
            SQLTable matchSourceTable, boolean trueForTextField) {

        super(parent, "Column Filter");
        this.returnText = returnText;
        this.matchSourceTable =  matchSourceTable;
        this.trueForTextField = trueForTextField;
        buildUI();
        addWindowListener(windowsListener);
    }
    
    /**
     * 
     * @param parent The parent dialog
     * @param returnText the JTextArea that the filter will put text in
     * @param matchSourceTable the sqltable the filter is being used for
     * @param trueForTextField true if the last input component should be JTextArea
     *                      false if the last input component should be a dropdown  
     */

    public FilterMakerDialog(JDialog parent,
            final JTextArea returnText,
            SQLTable matchSourceTable, boolean trueForTextField) {

        super(parent, "Column Filter");
        this.returnText = returnText;
        this.matchSourceTable =  matchSourceTable;
        this.trueForTextField = trueForTextField;
        buildUI();
        addWindowListener(windowsListener);
    }

    private WindowListener windowsListener = new WindowListener(){

        public void windowOpened(WindowEvent e) {
        }
        public void windowClosing(WindowEvent e) {
            returnText.setEditable(true);
        }
        public void windowClosed(WindowEvent e) {
            returnText.setEditable(true);
        }
        public void windowIconified(WindowEvent e) {
        }
        public void windowDeiconified(WindowEvent e) {
        }
        public void windowActivated(WindowEvent e) {
        }
        public void windowDeactivated(WindowEvent e) {
        }
    };



    public void buildUI(){

        FormLayout layout = new FormLayout(
                "4dlu,fill:min(70dlu;default), 4dlu, fill:150dlu:grow,4dlu, min(60dlu;default),4dlu",
        "10dlu,pref,4dlu,pref,4dlu,pref,4dlu,20dlu,4dlu,fill:60dlu:grow,10dlu,pref,10dlu");

        CellConstraints cc = new CellConstraints();

        PanelBuilder pb;
        JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(layout) : new JPanel(layout);

        pb = new PanelBuilder(layout,p);

        try {
            columnName = new JComboBox(new ColumnComboBoxModel(matchSourceTable));
        } catch (ArchitectException e) {
            ASUtils.showExceptionDialog(FilterMakerDialog.this,
                    "Unable to setup the column drop down box", e);
            return;
        }


        comparisonOperator = new JComboBox();
        pasteButton = new JButton(pasteAction);
        andButton = new JButton(andAction);
        andButton.setSize(new Dimension(1,1));
        orButton = new JButton (orAction);
        orButton.setSize(new Dimension(1,1));
        notButton = new JButton(notAction);
        notButton.setSize(new Dimension(1,1));
        testButton = new JButton(testAction);
        clearButton = new JButton (clearAction);
        okButton = new JButton(okAction);
        cancelButton = new JButton(cancelAction);
        filterText = new JTextArea();
        setFilterTextContent(returnText);

        pb.add(new JLabel("Duplicate1:"), cc.xy(2,2,"l,c"));
        pb.add(columnName, cc.xy(4,2,"f,c"));
        pb.add(new JLabel("Comparison Operator:"), cc.xy(2,4,"l,c"));
        pb.add(comparisonOperator, cc.xy(4,4));
        pb.add(new JLabel("Duplicate2:"), cc.xy(2,6,"l,c"));

        //If trueForTextArea is true, initiailize and use JTextField
        //if false, use a JComboBox and fill the dropdown with columns of the table
        if (trueForTextField){
            conditionTextField = new JTextField();
            pb.add(conditionTextField, cc.xy(4,6));
        } else{            
            try {
                columnName2 = new JComboBox(new ColumnComboBoxModel(matchSourceTable));
            } catch (ArchitectException e) {
                ASUtils.showExceptionDialog(FilterMakerDialog.this,
                        "Unable to setup the column drop down box", e);
                return;
            }
            pb.add(columnName2, cc.xy(4,6));
        }

        pb.add(pasteButton, cc.xy(6,6,"r,c"));

        ButtonBarBuilder syntaxBar = new ButtonBarBuilder();
        syntaxBar.addGridded(andButton);
        syntaxBar.addRelatedGap();
        syntaxBar.addGridded(orButton);
        syntaxBar.addRelatedGap();
        syntaxBar.addGridded(notButton);
        syntaxBar.addRelatedGap();

        pb.add(syntaxBar.getPanel(), cc.xyw(2,8,3));
        pb.add(new JTextAreaUndoWrapper(filterText), cc.xyw(2,10,5, "f,f"));

        ButtonBarBuilder bottomButtons = new ButtonBarBuilder();

        bottomButtons.addGridded(testButton);
        bottomButtons.addRelatedGap();
        bottomButtons.addGlue();
        bottomButtons.addGridded(clearButton);
        bottomButtons.addRelatedGap();
        bottomButtons.addGlue();
        bottomButtons.addGridded(okButton);
        bottomButtons.addRelatedGap();
        bottomButtons.addGlue();
        bottomButtons.addGridded(cancelButton);
        bottomButtons.addRelatedGap();
        bottomButtons.addGlue();

        pb.add(bottomButtons.getPanel(), cc.xyw(2,12,5,"f,f"));


        setupOperatorDropdown();

        getContentPane().add(pb.getPanel());

    }


    public void setFilterTextContent(JTextArea t) {
        filterText.setText(t.getText());
    }

    
    /*
     * Adds the operators into the setupOperator Dropdown
     */
    private void setupOperatorDropdown(){
        comparisonOperator.addItem(EQUALS);
        comparisonOperator.addItem(LIKE);
        comparisonOperator.addItem(IN);
        comparisonOperator.addItem(ISNULL);
        comparisonOperator.addItem(BRACKETS);
        comparisonOperator.addItem(NOTLIKE);
        comparisonOperator.addItem(LESSTHAN);
        comparisonOperator.addItem(GREATERTHAN);
        comparisonOperator.addItem(LESSTHANOREQUALTHAN);
        comparisonOperator.addItem(GREATEROREQUALTHAN);
    }

    private Action pasteAction = new AbstractAction("Paste"){

        public void actionPerformed(ActionEvent e) {
            /* We will put the text into a StringBuffer instead of appending the 
             * text to the JTextArea right away because of undo manager purposes.
             * If each component is appended at a time, undo will recognize it 
             * as many events whereas just appending it once would just be one
             * undo event
             */

            StringBuffer textBuffer = new StringBuffer();
            if (columnName.getSelectedItem() != null &&
                    comparisonOperator!= null &&
                    ((trueForTextField && conditionTextField.getText()!=null) ||
                            (trueForTextField == false && columnName2.getSelectedItem()!=null))){
                String trimmedUpperCaseText = filterText.getText().trim().toUpperCase();
                if (!filterText.getText().trim().equals("")
                        && !(trimmedUpperCaseText.endsWith(" AND")
                                || trimmedUpperCaseText.endsWith(" OR")
                                || trimmedUpperCaseText.endsWith(" NOT"))) {
                    textBuffer.append(" AND ");
                }
                textBuffer.append((String)columnName.getSelectedItem());
                textBuffer.append(" ");
                textBuffer.append((String)comparisonOperator.getSelectedItem());
                textBuffer.append(" ");

                if (trueForTextField){
                    textBuffer.append(conditionTextField.getText());
                } else{
                    textBuffer.append((String)columnName2.getSelectedItem());
                }

                filterText.append(textBuffer.toString());
            }
        }
    };

    private Action andAction = new AbstractAction("AND"){

        public void actionPerformed(ActionEvent e) {
            filterText.append(" AND ");
        }
    };

    private Action notAction = new AbstractAction("NOT"){
        public void actionPerformed(ActionEvent e) {
            filterText.append(" NOT ");
        }
    };

    private Action orAction = new AbstractAction("OR"){

        public void actionPerformed(ActionEvent e) {
            filterText.append(" OR ");
        }
    };

    private Action testAction = new AbstractAction("Test"){

        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT 1 FROM ");
            sql.append(DDLUtils.toQualifiedName(matchSourceTable.getCatalogName(),
                    matchSourceTable.getSchemaName(),
                    matchSourceTable.getName()));
            sql.append(" WHERE ");
            sql.append(filterText.getText());
            logger.debug("Test SQL:["+sql.toString()+"]");

            Connection con = null;
            Statement stmt = null;
            ResultSet rs =  null;
            try {
                con = matchSourceTable.getParentDatabase().getConnection();
                stmt = con.createStatement();
                rs = stmt.executeQuery(sql.toString());

                JOptionPane.showMessageDialog(FilterMakerDialog.this,
                        "Your filter forms a vaild SQL statement",
                        "Vaild filter",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e1) {
                ASUtils.showExceptionDialogNoReport(FilterMakerDialog.this,
                        "SQL Error", e1);
            } catch (ArchitectException e1) {
                ASUtils.showExceptionDialogNoReport(FilterMakerDialog.this,
                        "Connection Error", e1);
            } finally {
                try {
                    if ( rs != null )
                        rs.close();
                    if ( stmt != null )
                        stmt.close();
                    if (con != null)
                        con.close();
                } catch (SQLException e1) {
                    logger.debug("SQL ERROR: "+ e1.getStackTrace());
                }
            }
        }
    };

    private Action clearAction = new AbstractAction("Clear"){

        public void actionPerformed(ActionEvent e) {
            try {
                filterText.getDocument().remove(0,filterText.getDocument().getLength());
            } catch (BadLocationException e1) {
                ASUtils.showExceptionDialog(
                        FilterMakerDialog.this,
                        "Unknown Document Error",e1);

            }
        }};


        private Action okAction = new AbstractAction("OK"){

            public void actionPerformed(ActionEvent e) {
                returnText.setText(filterText.getText());
                setVisible(false);
                dispose();
            }
        };

        private Action cancelAction = new AbstractAction("Cancel"){
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        };


}
