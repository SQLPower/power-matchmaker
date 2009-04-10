/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.matchmaker.data.CommonWordsFinder;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.swingui.ConnectionComboBoxModel;
import ca.sqlpower.swingui.JDefaultButton;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

public class CommonWordsFinderPanel {

    private final MatchMakerSwingSession session;
    private final CommonWordsFinder finder = new CommonWordsFinder();
    private final JTree columnPicker;
    private final JPanel panel;

    private final Action okAction = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            performSearch();
        }
        
    };
    
    public CommonWordsFinderPanel(final MatchMakerSwingSession session, SPDataSource defaultConnection) {
        this.session = session;
        DataSourceCollection dataSources = session.getContext().getPlDotIni();
        final JComboBox connectionChooser = new JComboBox(new ConnectionComboBoxModel(dataSources));
        connectionChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    SPDataSource ds = (SPDataSource) connectionChooser.getSelectedItem();

                    // Gets the session's shared SQLDatabase for this data source
                    SQLDatabase db = session.getDatabase(ds);
                    
                    // XXX don't know if this is going to reparent the shared database in a bad way!
                    SQLObjectRoot root = new SQLObjectRoot();
                    
                    root.addChild(db);
                    DBTreeModel model = new DBTreeModel(root);
                    columnPicker.setModel(model);
                } catch (SQLObjectException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        columnPicker = new JTree();

        DefaultFormBuilder builder = new DefaultFormBuilder(
                new FormLayout("pref:grow"));
        builder.append("Choose a column to analyze for frequently-occurring words:");
        builder.append(connectionChooser);
        builder.append(new JScrollPane(columnPicker));

        JDefaultButton okButton = new JDefaultButton("Start");
        JButton cancelButton = new JButton("Cancel");
        builder.append(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton));
        panel = builder.getPanel();
    }
    
    /**
     * Performs the search for common words in the currently-selected column
     * of the {@link #columnPicker} tree.
     */
    private void performSearch() {
        TreePath selectionPath = columnPicker.getSelectionPath();
        if (selectionPath == null || ! (selectionPath.getLastPathComponent() instanceof SQLColumn)) {
            JOptionPane.showMessageDialog(columnPicker, "You must select a column in which to locate the common words");
        }
        SQLColumn col = (SQLColumn) selectionPath.getLastPathComponent();
        
        // TODO this is not finished!
    }
}
