/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.dao.hibernate.RepositoryUtil;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.SQLObjectChooser;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSUtils.FileExtensionFilter;
import ca.sqlpower.util.SQLPowerUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Action for initializing a MatchMaker repository schema.  Prompts the user
 * for a target location, then generates and executes the appropriate DDL
 * statements.
 */
public class CreateRepositoryAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(CreateRepositoryAction.class);
    
    private final MatchMakerSwingSession session;
    
    public CreateRepositoryAction(MatchMakerSwingSession session) {
        super("Create Repository...");
        this.session = session;
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            
            SQLObject target = SQLObjectChooser.showSchemaChooserDialog(
                    session, session.getFrame(), "New Repository", "Create...");
            
            // check if user cancelled
            if (target == null) {
                logger.debug("User cancelled the create action--aborting.");
                return;
            }
            
            SQLDatabase targetDB = SQLPowerUtils.getAncestor(target, SQLDatabase.class);
            SQLCatalog targetCatalog = SQLPowerUtils.getAncestor(target, SQLCatalog.class);
            SQLSchema targetSchema = SQLPowerUtils.getAncestor(target, SQLSchema.class);
            
            // set the repository owner in the data source
            StringBuilder targetOwner = new StringBuilder();
            if (targetCatalog != null) {
                targetOwner.append(targetCatalog.getName());
            }
            if (targetCatalog != null && targetSchema != null) {
                targetOwner.append(".");
            }
            if (targetSchema != null) {
                targetOwner.append(targetSchema.getName());
            }
            targetDB.getDataSource().setPlSchema(targetOwner.toString());
            
            // create the schema itself
            List<String> sqlScript = RepositoryUtil.makeRepositoryCreationScript(target);
            showSQLScriptDialog(targetDB, sqlScript);
            
        } catch (Exception ex) {
            MMSUtils.showExceptionDialog(session.getFrame(), "Repository Creation Failed", ex);
        }
    }
    
    /**
     * Presents a dialog containing the given SQL script, along with buttons
     * which allow the user to execute it in the database, save it, or copy it
     * to the system clipboard. The script is presented as a stack of editable
     * JTextArea components so the user can tweak individual statements in the
     * script before running it.
     * <p>
     * This method is general enough that it should probably be moved to somewhere
     * in the SQL Power Library.
     * 
     * @param targetDB The database in which to execute the script (the target catalog
     * and schema do not need to be specified because the script is assumed to use
     * fully-qualified object names).
     * @param sqlScript The script to display and execute.
     */
    private void showSQLScriptDialog(final SQLDatabase targetDB, List<String> sqlScript) {
        final JFrame frame = session.getFrame();
        final JDialog editor = new JDialog(session.getFrame(), "SQL Script", true);
        JPanel cp = new JPanel();
        CellConstraints cc = new CellConstraints();
        JButton executeButton;
        
        Box statementsBox = Box.createVerticalBox();
        final List<JTextArea> sqlTextFields = new ArrayList<JTextArea>(sqlScript.size());
        for (String statement : sqlScript) {
            JTextArea sqlTextArea = new JTextArea(statement);
            statementsBox.add(sqlTextArea);
            sqlTextFields.add(sqlTextArea);
        }
        
        Action saveAction = new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) {
                AbstractDocument doc = new DefaultStyledDocument();
                for (JTextArea sqlText : sqlTextFields) {
                    try {
                        doc.insertString(doc.getLength(),
                                        sqlText.getText(),
                                        null);
                        doc.insertString(doc.getLength(),";\n",null);
                    } catch (BadLocationException e1) {
                        SPSUtils.showExceptionDialogNoReport(frame, "Unexcepted Document Error",e1);
                    }
                }
                SPSUtils.saveDocument(frame, doc,
                        (FileExtensionFilter)SPSUtils.SQL_FILE_FILTER);
            }
        };
        Action copyAction = new AbstractAction("Copy to Clipboard") {
            public void actionPerformed(ActionEvent e) {
                StringBuilder buf = new StringBuilder();
                for (JTextArea sqlText : sqlTextFields) {
                    buf.append(sqlText.getText());
                    buf.append(";\n");
                }
                StringSelection selection = new StringSelection(buf.toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            }
        };
        Action executeAction = new AbstractAction("Execute") {
            public void actionPerformed(ActionEvent e) {
                // Builds the gui part of the error pane that will be used
                // if a sql statement fails
                JPanel errorPanel = new JPanel(new FormLayout("4dlu,300dlu,4dlu",
                    "4dlu,pref,4dlu,pref,4dlu,200dlu,4dlu,pref,4dlu"));
                CellConstraints cc = new CellConstraints();
                
                JLabel topLabel = new JLabel();
                JTextArea errorMsgLabel = new JTextArea();
                errorMsgLabel.setLineWrap(true);
                errorMsgLabel.setWrapStyleWord(true);
                errorMsgLabel.setPreferredSize(new Dimension(300, 40));
                
                JLabel bottomLabel = new JLabel();
                JTextArea errorStmtLabel = new JTextArea();
                JScrollPane errorStmtPane = new JScrollPane(errorStmtLabel);
                errorStmtPane.setPreferredSize(new Dimension(300, 300));
                errorStmtPane.scrollRectToVisible(new Rectangle(0,0));
                
                int row = 2;
                errorPanel.add(topLabel, cc.xy(2, row));
                row += 2;
                errorPanel.add(errorMsgLabel, cc.xy(2, row));
                row += 2;
                errorPanel.add(errorStmtPane, cc.xy(2,row));
                row += 2;
                errorPanel.add(bottomLabel, cc.xy(2,row, "f,f"));
                
                // Does the actual work of executing the sql statments
                Connection con = null;
                Statement stmt = null;
                String sql = null;
                try {
                    con = targetDB.getConnection();
                    stmt = con.createStatement();
                    int successCount = 0;
                    boolean ignoreAll = false;
                    
                    for (JTextArea sqlText : sqlTextFields ) {
                        sql = sqlText.getText().trim();
                        try {
                            stmt.executeUpdate(sql);
                            successCount += 1;
                        } catch (SQLException e1) {
                            // TODO: Improve this so it deals with conflicts.
                            if (!ignoreAll) {
                                topLabel.setText("There was an error in the SQL statement: ");
                                errorMsgLabel.setText(e1.getMessage());
                                errorStmtLabel.setText(sql);
                                bottomLabel.setText("Do you want to continue executing the create script?");
                                int choice = JOptionPane.showOptionDialog(editor,
                                        errorPanel, "SQL Error", JOptionPane.YES_NO_OPTION,
                                        JOptionPane.ERROR_MESSAGE, null,
                                        new String[] {"Abort", "Ignore", "Ignore All"}, "Ignore" );
                                if (choice == 2) {
                                    ignoreAll = true;
                                } else if (choice == 0) {
                                    break;
                                }
                            }
                        }
                    }

                    JOptionPane.showMessageDialog(editor,
                            "Successfully executed " + successCount + " of " +
                            sqlTextFields.size() + " SQL Statements." +
                            (successCount == 0 ? "\n\nBetter luck next time." : ""));

                    //close the dialog if all the statements executed successfully
                    //if not, the dialog remains on the screen
                    if (successCount == sqlTextFields.size()){
                        editor.dispose();
                    }
                } catch (Exception ex) {
                    SPSUtils.showExceptionDialogNoReport(
                            editor,
                            "Script Execution Failure",
                            ex.getMessage(),
                            ex);
                } finally {
                    try {
                        if (stmt != null) {
                            stmt.close();
                        }
                    } catch (SQLException ex) {
                        logger.warn("Couldn't close statement", ex);
                    }
                    try {
                        if (con != null) {
                            con.close();
                        }
                    } catch (SQLException ex) {
                        logger.warn("Couldn't close connection", ex);
                    }
                }
            }
        };
        Action cancelAction = new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                editor.dispose();
            }
        };

        // the gui layout part
        cp.setLayout(new FormLayout("10dlu,350dlu,10dlu", "10dlu,350dlu,4dlu,pref,10dlu"));

        int row = 2;
        cp.add(new JScrollPane(statementsBox), cc.xy(2,row));
        row += 2;

        ButtonBarBuilder bbb = ButtonBarBuilder.createLeftToRightBuilder();
        bbb.addGridded(new JButton(saveAction));
        bbb.addGridded(new JButton(copyAction));
        executeButton = new JButton(executeAction);
        bbb.addGridded(executeButton);
        bbb.addGridded(new JButton(cancelAction));
        cp.add(bbb.getPanel(), cc.xy(2,row, "c,c"));

        editor.setContentPane(cp);
        SPSUtils.makeJDialogCancellable(editor, cancelAction, false);
        editor.getRootPane().setDefaultButton(executeButton);
        editor.pack();
        editor.setLocationRelativeTo(frame);
        editor.setVisible(true);
    }

}
