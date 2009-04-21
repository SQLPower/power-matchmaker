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

package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;

import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;

/**
 * A Dialog to make arbitrary SQL queries from, to replace the somewhat
 * out-of-date Table Explorer.
 */
public class QueryDialog extends JDialog {
	
	private JComponent queryPanel;
	
	private JTree dragTree;

	private final MatchMakerSwingSession session;

	/**
	 * Creates and displays a new dialog to make SQL queries from.
	 */
	public QueryDialog(MatchMakerSwingSession session, JFrame sessionframe, String title) {
		super(sessionframe, title);
		this.session = session;
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setSize(900,450);	
		
		queryPanel = SQLQueryUIComponents.createQueryPanel(session, session.getContext().getPlDotIni(), session, this);
		queryPanel.setMinimumSize(new Dimension(100,100));		
		
		dragTree = new JTree();
		SQLObjectRoot rootNode = new SQLObjectRoot();
		try {
			if (session.getContext().getPlDotIni().getConnections().size() == 0) {
				dragTree.setVisible(false);
			} else {
				for (SPDataSource source : session.getContext().getPlDotIni().getConnections()) {
					rootNode.addChild(new SQLDatabase(source));
				}
				dragTree.setVisible(true);
			}
			dragTree.setModel(new DBTreeModel(rootNode));
		} catch (SQLObjectException e) {
			throw new RuntimeException("Could not add DataSource to rootNode", e);
		}
		
		dragTree.setCellRenderer(new DBTreeCellRenderer());
		dragTree.addMouseListener(treeListener);
		
		buildUI();		
	}

    private void buildUI() {        
        JSplitPane querySplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        querySplitPane.add(new JScrollPane(dragTree), JSplitPane.LEFT);
        querySplitPane.add(queryPanel, JSplitPane.RIGHT);
        querySplitPane.setDividerLocation(130);
       
        setLayout(new BorderLayout());
        add(querySplitPane, BorderLayout.CENTER);
    }
    
    private MouseAdapter treeListener = new MouseAdapter() {

		@Override
		public void mouseClicked(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * This displays the database connection manager upon right clicking the tree.
		 */
		private void maybeShowPopup(MouseEvent e) {
			if (!e.isPopupTrigger()) {
				return;
			}

			JPopupMenu menu = new JPopupMenu();
			menu.add(new AbstractAction("Database Connection Manager...") {

				public void actionPerformed(ActionEvent e) {
					session.getContext().showDatabaseConnectionManager(
							session.getFrame());
				}
			});

			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	};
}
