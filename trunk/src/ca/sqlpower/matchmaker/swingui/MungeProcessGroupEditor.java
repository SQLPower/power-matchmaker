/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerUtils;
import ca.sqlpower.matchmaker.MatchRuleSet;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.swingui.action.NewMatchGroupAction;
import ca.sqlpower.swingui.table.TableUtils;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A panel to edit the translate words groups
 */
public class MungeProcessGroupEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(MungeProcessGroupEditor.class);
	
	private JPanel panel;
	private JScrollPane scrollPane;
	MungeProcessTableModel mungeProcessTableModel;
	private JTable mungeProcessTable;
	
	private final MatchMakerSwingSession swingSession;
	private final Match match;

	private final FormValidationHandler handler;
	private final StatusComponent status = new StatusComponent();
	
	public MungeProcessGroupEditor(MatchMakerSwingSession swingSession, Match m) {
		this.swingSession = swingSession;
		this.match = m;

		setupTable();
        buildUI();
        handler = new FormValidationHandler(status);
        handler.resetHasValidated();

	}
	
	private void setupTable() {
		mungeProcessTableModel = new MungeProcessTableModel(swingSession, match);
		mungeProcessTable = new JTable(mungeProcessTableModel);
		TableCellRenderer renderer = new CustomTableCellRenderer();
        mungeProcessTable.setDefaultRenderer(Color.class, renderer );
        mungeProcessTable.setName("Munge Processes");
        
        //adds an action listener that looks for a double click, that opens the selected 
        //merge rule editor pane  
        mungeProcessTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				
				if (e.getClickCount() == 2) {
					int row = MungeProcessGroupEditor.this.mungeProcessTable.getSelectedRow();
//					swingSession.getTree().setSelectionPath(menuPath.pathByAddingChild(translateGroups.get(row)));
				}
			}		
        });
        mungeProcessTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableUtils.fitColumnWidths(mungeProcessTable, 15);
	}
	
	private void buildUI() {
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
		pb.add(new JLabel("Munge Processes:"), cc.xy(4,row,"l,t"));
		
		row += 2;
		scrollPane = new JScrollPane(mungeProcessTable);
		pb.add(scrollPane, cc.xy(4,row,"f,f"));
		
		ButtonBarBuilder bbb = new ButtonBarBuilder();
		//new actions for delete and save should be extracted and be put into its own file.
		bbb.addGridded(new JButton(new NewMatchGroupAction(swingSession, match)));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(deleteAction));

		row+=2;
		pb.add(bbb.getPanel(), cc.xy(4,row,"c,c"));
		
		panel = pb.getPanel();
	}

	public boolean doSave() {
		logger.debug("Do Save: Not implemented :(");
		return false;
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		logger.debug("Has unsaved changes: Not implemented :(");
		return false;
	}
	
	Action deleteAction = new AbstractAction("Delete Munge Process") {
		public void actionPerformed(ActionEvent e) {
			int selectedRow = mungeProcessTable.getSelectedRow();
			int response = JOptionPane.showConfirmDialog(swingSession.getFrame(),
				"Are you sure you want to delete the munge process?");
			if (response != JOptionPane.YES_OPTION) {
				return;
			}
		
			logger.debug("deleting translate group:"+selectedRow);
			
			if ( selectedRow >= 0 && selectedRow < mungeProcessTable.getRowCount()) {
				MatchRuleSet ruleSet = match.getMatchRuleSetFolder().getChildren().get(selectedRow);
				match.removeMatchRuleSet(ruleSet);
				swingSession.save(match);
				if (selectedRow >= mungeProcessTable.getRowCount()) {
					selectedRow = mungeProcessTable.getRowCount() - 1;
				}
				if (selectedRow >= 0) {
					mungeProcessTable.setRowSelectionInterval(selectedRow, selectedRow);
				}
			} else {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"Please select one translate group to delete");
			}
		}
	};

	
	/**
	 * table model for the munge processes, it shows the munge processes
	 * in a JTable, allows user add/delete/reorder translation groups.
	 * <p>
	 * It has 4 columns:
	 * <dl>
	 * 		<dt>name   			<dd> munge process name
	 * 		<dt>description    	<dd> munge process description
	 * 	 	<dt>color  			<dd> color of the match line
	 * 		<dt>priority      	<dd> determines the color of the match line if 
	 * 								multiple munge process produce the same match
	 * </dl>
	 */
	private class MungeProcessTableModel extends AbstractTableModel implements MatchMakerListener{

		private Match match;
		public MungeProcessTableModel(MatchMakerSwingSession swingSession, Match match) {
			this.match = match;
			MatchMakerUtils.listenToHierarchy(this, this.match);
		}

		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return match.getMatchRuleSets().size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			MatchRuleSet ruleSet = match.getMatchRuleSets().get(rowIndex);
			switch (columnIndex) {
			case 0:  return ruleSet.getName();
			case 1:  return ruleSet.getDesc();
			case 2:  return ruleSet.getColour();
			case 3:  return ruleSet.getMatchPercent();
			default: return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:  return String.class;
			case 1:  return String.class;
			case 2:  return Color.class;
			case 3:  return Integer.class;
			default: return null;
			}
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:  return "Name";
			case 1:  return "Description";
			case 2:  return "Color";
			case 3:  return "Priority";
			default: return null;
			}
		}
		
	    public void mmChildrenInserted(MatchMakerEvent evt) {
	        if(evt.getSource() instanceof Match || evt.getSource() == match.getMatchRuleSetFolder()){
	            int[] changed = evt.getChangeIndices();
	            ArrayList<Integer> changedIndices = new ArrayList<Integer>();
	            for (int selectedRowIndex:changed){
	                changedIndices.add(new Integer(selectedRowIndex));
	            }
	            Collections.sort(changedIndices);
	            for (int i=1; i < changedIndices.size(); i++){
	                if (changedIndices.get(i-1)!=changedIndices.get(i)-1){
	                    fireTableStructureChanged();
	                    return;
	                }
	            }
	            for (Object matchRuleSet:evt.getChildren()){
	                ((MatchRuleSet) matchRuleSet).addMatchMakerListener(this);
	            }
	            fireTableRowsInserted(changedIndices.get(0), changedIndices.get(changedIndices.size()-1));
	        }
	    }

	    public void mmChildrenRemoved(MatchMakerEvent evt) {
	        if(evt.getSource() instanceof Match || evt.getSource() == match.getMatchRuleSetFolder()) {
	            int[] changed = evt.getChangeIndices();
	            ArrayList<Integer> changedIndices = new ArrayList<Integer>();
	            for (int selectedRowIndex:changed){
	                changedIndices.add(new Integer(selectedRowIndex));
	            }
	            Collections.sort(changedIndices);
	            for (int i=1; i < changedIndices.size(); i++) {
	                if (changedIndices.get(i-1)!=changedIndices.get(i)-1) {
	                    fireTableStructureChanged();
	                    return;
	                }
	            }
	            for (Object matchRuleSet:evt.getChildren()) {
	                ((MatchRuleSet) matchRuleSet).removeMatchMakerListener(this);
	            }
	            fireTableRowsDeleted(changedIndices.get(0), changedIndices.get(changedIndices.size()-1));
	        }
	    }

	    public void mmPropertyChanged(MatchMakerEvent evt) { 
	        if(evt.getSource() instanceof TableMergeRules) {
	            fireTableRowsUpdated(match.getTableMergeRules().indexOf(evt.getSource()), match.getTableMergeRules().indexOf(evt.getSource()));
	        }
	    }

	    public void mmStructureChanged(MatchMakerEvent evt) {
	        fireTableStructureChanged();
	    }
	}
	
	private class CustomTableCellRenderer extends DefaultTableCellRenderer {
	    public Component getTableCellRendererComponent
	       (JTable table, Object value, boolean isSelected,
	       boolean hasFocus, int row, int column) {
	        Component cell = super.getTableCellRendererComponent
	        	(table, value, isSelected, hasFocus, row, column);
	        Color color;
	        if(value instanceof Color) {
	        	color = (Color) value;

	        }
	        else {
	        	color = Color.BLACK;
	        }
        	cell.setForeground(color);
        	cell.setBackground(color);
        	cell.setPreferredSize(new Dimension(50, cell.getHeight()));
	        return cell;
	    }
	}
}
