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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.MatchMakerVersion;
import ca.sqlpower.matchmaker.swingui.action.NewTranslateGroupAction;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel to edit the translate words groups
 */
public class TranslateGroupsEditor extends NoEditEditorPane {

	private static final Logger logger = Logger.getLogger(TranslateGroupsEditor.class);

    public static final String TRANSLATE_WORDS_SPREADSHEET_KEY = "pIOfRi4wZwIh1eNPmWCRhPQ";
	
	private JScrollPane translateGroupsScrollPane;
	TranslateGroupsTableModel translateGroupsTableModel;
	private JTable translateGroupsTable;
	
	private TreePath menuPath;

	private final MatchMakerSwingSession swingSession;
	private final List<MatchMakerTranslateGroup> translateGroups;

	private FormValidationHandler handler;
	StatusComponent status = new StatusComponent();
	
	public TranslateGroupsEditor(MatchMakerSwingSession swingSession) {
		super();
		this.swingSession = swingSession;
		this.translateGroups = swingSession.getTranslations().getChildren();
		
		setupTable();
        super.setPanel(buildUI());
        handler = new FormValidationHandler(status);
        handler.resetHasValidated();
		
		MatchMakerTreeModel treeModel = (MatchMakerTreeModel) swingSession.getTree().getModel();
		menuPath = treeModel.getPathForNode(swingSession.getTranslations());
		swingSession.getTree().setSelectionPath(menuPath);
		
	}
	
	private void setupTable() {
		translateGroupsTableModel = new TranslateGroupsTableModel(swingSession);
		translateGroupsTable = new JTable(translateGroupsTableModel);
        translateGroupsTable.setName("Translate Groups");
        
        //adds an action listener that looks for a double click, that opens the selected 
        //translate word editor pane  
        translateGroupsTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				
				if (e.getClickCount() == 2) {
					int row = TranslateGroupsEditor.this.translateGroupsTable.getSelectedRow();
					swingSession.getTree().setSelectionPath(menuPath.pathByAddingChild(translateGroups.get(row)));
				}
			}		
        });

        translateGroupsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	private JPanel buildUI() {
		FormLayout layout = new FormLayout(
				"4dlu,46dlu,4dlu,fill:min(pref;"+3*(new JComboBox().getMinimumSize().width)+"px):grow,4dlu,50dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,fill:40dlu:grow,4dlu,pref,10dlu"); // rows
			//	   1     2   3    4     5   6     7   8     9    10   11
		
		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? 
				new FormDebugPanel(layout) : new JPanel(layout);
				
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();
		
		int row = 2;
		pb.add(status, cc.xy(4,row));
		
		row += 2;
		pb.add(new JLabel("Translation Groups:"), cc.xy(4,row,"l,t"));
		
		row += 2;
		translateGroupsScrollPane = new JScrollPane(translateGroupsTable);
		pb.add(translateGroupsScrollPane, cc.xy(4,row,"f,f"));
		
		ButtonBarBuilder bbb = new ButtonBarBuilder();
		//this needs to be cleaned
		bbb.addGridded(new JButton(new AbstractAction("Get Online List"){
			public void actionPerformed(ActionEvent e) {
				int opt = JOptionPane.showConfirmDialog(swingSession.getFrame(), "Download Online list?\n" +
						"The list can be viewed at: http://spreadsheets.google.com/pub?key=" + TRANSLATE_WORDS_SPREADSHEET_KEY,"Download Online List", JOptionPane.YES_NO_OPTION);
				if (opt == JOptionPane.YES_OPTION) {
					for (MatchMakerTranslateGroup mmtg : translateGroups) {
						if (mmtg.getName().equals("SQLPower Translate Words")) {
							if (JOptionPane.showConfirmDialog(swingSession.getFrame(),"You already have a translation group named, SQLPower Translate Words, would you like to rebuild it?", "Update Translate Words",JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
								return;
							} else {
								translateGroupsTableModel.removeGroup(translateGroups.indexOf(mmtg));
								break;
							}
						}
					}
					try {
					MatchMakerTranslateGroup mmtg = getOnlineTranslateGroup();
					swingSession.setCurrentEditorComponent(new TranslateWordsEditor(swingSession, mmtg));
					} catch (Exception ex) {
						SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(), "Could not generate online list", ex);
					}
				}
			}
		}));
		//new actions for delete and save should be extracted and be put into its own file.
		bbb.addGridded(new JButton(new NewTranslateGroupAction(swingSession)));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(deleteGroupAction));

		row+=2;
		pb.add(bbb.getPanel(), cc.xy(4,row,"c,c"));
		
		return pb.getPanel();
	}
	
	Action deleteGroupAction = new AbstractAction("Delete Group") {
		public void actionPerformed(ActionEvent e) {
			int selectedRow = translateGroupsTable.getSelectedRow();
			int response = JOptionPane.showConfirmDialog(swingSession.getFrame(),
				"Are you sure you want to delete the translate group?");
			if (response != JOptionPane.YES_OPTION) {
				return;
			}
		
			logger.debug("deleting translate group:"+selectedRow);
			
			if ( selectedRow >= 0 && selectedRow < translateGroupsTable.getRowCount()) {
				translateGroupsTableModel.removeGroup(selectedRow);
				if (selectedRow >= translateGroupsTable.getRowCount()) {
					selectedRow = translateGroupsTable.getRowCount() - 1;
				}
				if (selectedRow >= 0) {
					translateGroupsTable.setRowSelectionInterval(selectedRow, selectedRow);
				}
			} else {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"Please select one translate group to delete");
			}
		}
	};
	
	/**
	 * table model for the translation groups, it shows the translation groups
	 * in a JTable, allows user add/delete/reorder translation groups
	 * it has 1 column:
	 * 		translation group name  -- name in a string
	 *
	 */
	private class TranslateGroupsTableModel extends AbstractTableModel {

		private List<MatchMakerTranslateGroup> rows;
		private MatchMakerSwingSession swingSession;
		
		public TranslateGroupsTableModel(MatchMakerSwingSession swingSession) {
			this.swingSession = swingSession;
			rows = swingSession.getTranslations().getChildren();
		}
		
		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return rows.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return rows.get(rowIndex);
			} else {
				return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return MatchMakerTranslateGroup.class;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "Name";
			}
			return null;
		}

		public List<MatchMakerTranslateGroup> getTranslateGroups() {
			return rows;
		}
		
		public void removeGroup(int index) {
			if (swingSession.getTranslations().isInUseInBusinessModel(rows.get(index))) {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
					"This translation group is in use, and cannot be deleted.");
			} else {
				swingSession.delete(rows.get(index));
				fireTableDataChanged();
			}
		}
	}
	
	/**
	 * Creates a matchMaker translate group from the Google spread sheet with key
     * {@link #TRANSLATE_WORDS_SPREADSHEET_KEY}.
	 * The sheet is owned by matchmaker@sqlpower.ca and it's called TranslationWords.
	 * 
	 * @return The MatchMakerTranslateWord group from the spread sheet 
	 * @throws ArchitectException If something goes wrong
	 */
	private MatchMakerTranslateGroup getOnlineTranslateGroup() {
		SpreadsheetService sss = new SpreadsheetService("SQLPower-Power*MatchMaker-" + MatchMakerVersion.APP_VERSION);
		CellFeed cf;
		
		try {
			URL url = new URL("http://spreadsheets.google.com/feeds/cells/" + TRANSLATE_WORDS_SPREADSHEET_KEY + "/1/public/values");
			cf = sss.getFeed(url, CellFeed.class);
		} catch (Exception e) {
			throw new RuntimeException("Error could not generate translation words from google spreadsheet!",e);
		}

		
		//This bit is kind of silly but we don't get entries for empty cells
		int length = 0;
		Map<String, String> entries = new HashMap<String, String>();
		for (CellEntry ce: cf.getEntries()) {
			length = Math.max(length, ce.getCell().getRow());
			entries.put(ce.getCell().getRow() + ":" + ce.getCell().getCol(), ce.getCell().getValue());
		}
		
		
		String[][] vals= new String[2][length];
		for (int y = 1; y<=length;y++) {
			for (int x = 1; x <=2; x++) {
				String key = y + ":" + x;
				vals[x-1][y-1] = entries.get(key);
			}
		}
		

		MatchMakerTranslateGroup mmtg = new MatchMakerTranslateGroup();
		mmtg.setName("SQLPower Translate Words");
		
		for (int x = 1; x<vals[0].length; x++) {
			//this should remove holes in the list
			if (vals[0][x] != null) {
				MatchMakerTranslateWord mmtw = new MatchMakerTranslateWord();
				mmtw.setFrom(vals[0][x]);
				mmtw.setTo(vals[1][x]);
				mmtg.addChild(mmtw);
			}
		}
		
		return mmtg;
	
	}

}
