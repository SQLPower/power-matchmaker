package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.table.TableUtils;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MergeSettings;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.util.MatchMakerQFAFactory;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MergeTableRuleEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(MergeTableRuleEditor.class);
	private JPanel panel;
	StatusComponent status = new StatusComponent();
	private final MatchMakerSwingSession swingSession;
	private final Match match;
	private FormValidationHandler handler;
	private JTextArea desc = new JTextArea(3,80);
	private JTable mergeRulesTable;
	MergeTableRuleTableModel mergeTableRuleTableModel;
	private JScrollPane mergeRulesScrollPane;
	private boolean adjustingTableColumns = false;
	
	public MergeTableRuleEditor(MatchMakerSwingSession swingSession,Match match) throws ArchitectException {
		this.swingSession = swingSession;
		this.match = match;
		if (match == null) throw new NullPointerException("You can't edit a null match");
        handler = new FormValidationHandler(status);

        setupRulesTable(swingSession,match);
        
		
        buildUI();
        setDefaultSelections();
        handler.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				refreshActionStatus();
			}
        });
        handler.resetHasValidated(); // avoid false hits when newly created
        
        
	}
	
	private void setupRulesTable(MatchMakerSwingSession swingSession, Match match) throws ArchitectException {
		SQLObjectChooser chooser = new SQLObjectChooser(swingSession);
		mergeTableRuleTableModel = new MergeTableRuleTableModel(match,chooser);
		mergeRulesTable = new JTable(mergeTableRuleTableModel);
        mergeRulesTable.setName("Merge Tables");
        
        mergeRulesTable.setDefaultRenderer(Boolean.class,new CheckBoxRenderer());
        mergeRulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        mergeRulesTable.getColumnModel().getColumn(0).setCellEditor(
        		new DefaultCellEditor(chooser.getCatalogComboBox()));
        mergeRulesTable.getColumnModel().getColumn(1).setCellEditor(
        		new DefaultCellEditor(chooser.getSchemaComboBox()));
        mergeRulesTable.getColumnModel().getColumn(2).setCellEditor(
        		new DefaultCellEditor(chooser.getTableComboBox()));

		TableUtils.fitColumnWidths(mergeRulesTable, 15);
	}

	private void buildUI() {
		
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:min(pref;"+3*(new JComboBox().getMinimumSize().width)+"px):grow, 4dlu,pref,4dlu", // columns
				"10dlu,pref,4dlu,min(pref;40dlu),4dlu,pref,12dlu,pref:grow,4dlu,pref,4dlu"); // rows
			//	 1     2    3    4               5    6    7     8         9    10   11      


		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? 
				new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		int row = 2;
		pb.add(status, cc.xy(4,row));
		row += 2;
		
        desc.setWrapStyleWord(true);
        desc.setLineWrap(true);
		pb.add(new JLabel("Description:"), cc.xy(2,row,"r,t"));
		pb.add(new JScrollPane(desc), cc.xy(4,row,"f,f"));
		row += 2;
		pb.add(new JLabel("Merge Rules:"), cc.xy(4,row,"c,c"));
		row += 2;
		mergeRulesScrollPane = new JScrollPane(mergeRulesTable);
		pb.add(mergeRulesScrollPane, cc.xy(4,row,"f,f"));

		ButtonStackBuilder bsb = new ButtonStackBuilder();
		bsb.addGridded(new JButton(moveUp));
		bsb.addRelatedGap();
		bsb.addGridded(new JButton(moveDown));
		pb.add(bsb.getPanel(), cc.xy(6,row,"c,c"));
		ButtonBarBuilder bbb = new ButtonBarBuilder();
		bbb.addGridded(new JButton("New"));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(deleteRule));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(saveAction));
		row+=2;
		pb.add(bbb.getPanel(), cc.xy(4,row,"c,c"));
		panel = pb.getPanel();
	}

	private void setDefaultSelections() {
		MergeSettings settings = match.getMergeSettings();
		desc.setText(settings.getDescription());
	}
	
	private void refreshActionStatus() {
	}
	
	
	private Action moveUp = new AbstractAction("^") {
		public void actionPerformed(ActionEvent e) {
			final int selectedRow = mergeRulesTable.getSelectedRow();
			final List<TableMergeRules> mergeRules = mergeTableRuleTableModel.getMergeRules();
			logger.debug("moving merge rule "+selectedRow+" up");
			if ( selectedRow > 0 &&	selectedRow < mergeRules.size()) {
				TableMergeRules rule1 = mergeRules.get(selectedRow);
				TableMergeRules rule2 = mergeRules.get(selectedRow-1);
				mergeRules.remove(rule2);
				mergeRules.add(mergeRules.indexOf(rule1)+1, rule2);
				mergeRulesTable.setRowSelectionInterval(selectedRow-1, selectedRow-1);
			}
		}
	};
	
	private Action moveDown = new AbstractAction("v") {
		public void actionPerformed(ActionEvent e) {
			final int selectedRow = mergeRulesTable.getSelectedRow();
			final List<TableMergeRules> mergeRules = mergeTableRuleTableModel.getMergeRules();
			logger.debug("moving merge rule "+selectedRow+" down");
			if ( selectedRow >= 0 && selectedRow < mergeRules.size()-1) {
				TableMergeRules rule1 = mergeRules.get(selectedRow);
				TableMergeRules rule2 = mergeRules.get(selectedRow+1);
				mergeRules.remove(rule1);
				mergeRules.add(mergeRules.indexOf(rule2)+1, rule1);
				mergeRulesTable.setRowSelectionInterval(selectedRow+1, selectedRow+1);
			}
		}
	};
	
	private Action deleteRule = new AbstractAction("Delete") {
		public void actionPerformed(ActionEvent e) {
			int selectedRow = mergeRulesTable.getSelectedRow();
			final List<TableMergeRules> mergeRules = mergeTableRuleTableModel.getMergeRules();
			logger.debug("deleting merge rule:"+selectedRow);
			if ( selectedRow >= 0 && selectedRow < mergeRules.size()) {
				mergeRules.remove(selectedRow);
				if (selectedRow >= mergeRules.size()) {
					selectedRow = mergeRules.size()-1;
				}
				mergeTableRuleTableModel.fireTableDataChanged();
				if (selectedRow >= 0) {
					mergeRulesTable.setRowSelectionInterval(selectedRow, selectedRow);
				}
			} else {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"Please select one merge rule to delete");
			}
		}
	};
	
	
	/**
     * Saves the list in table model to merge rules
     */
	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(final ActionEvent e) {
            try {
                boolean ok = doSave();
            } catch (Exception ex) {
                ASUtils.showExceptionDialog(swingSession.getFrame(),
                		"Merge Interface Not Saved", ex, new MatchMakerQFAFactory());
            }
		}
	};
	
	public boolean doSave() {
		return false;
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return false;
	}

	/**
	 * table model for the merge table rules, it shows the merge tables
	 * in a JTable, allows user add/delete/reorder merge tables
	 * it has 4 columns:
	 * 		table catalog    -- merge table catalog in a combo box
	 * 		table schema    -- merge table schema in a combo box
	 * 		table name    -- merge table name in a combo box
	 * 		delete dup ind     -- merge table delete dup ind in a check box
	 *
	 */
	private class MergeTableRuleTableModel extends AbstractTableModel {

		private List<TableMergeRules> rules;
		private SQLObjectChooser chooser;
		
		public MergeTableRuleTableModel(Match match, SQLObjectChooser chooser) {
			this.chooser = chooser;
			rules = new ArrayList<TableMergeRules>();
			rules.addAll(match.getTableMergeRules());
for ( TableMergeRules r : rules) {
	System.out.println("name:"+r.toString()+"  "+r.isDeleteDup());
}
		}
		
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return rules.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			TableMergeRules r = rules.get(rowIndex);
			if (columnIndex == 0) {
				return r.getSourceTable()==null?null:r.getSourceTable().getCatalog();
			} else if (columnIndex == 1) {
				return r.getSourceTable()==null?null:r.getSourceTable().getSchema();
			} else if (columnIndex == 2) {
				return r.getSourceTable();
			} else if ( columnIndex == 3) {
				r.isDeleteDup();
System.out.println(r.getName()+"   get value is "+r.isDeleteDup());				
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				//return chooser.getCatalogComboBox().isEnabled();
				return false;
			} else if (columnIndex == 1) {
				//return chooser.getSchemaComboBox().isEnabled();
				return false;
			} else if (columnIndex == 2) {
				//return chooser.getTableComboBox().isEnabled();
				return false;
			}
			return true;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return SQLCatalog.class;
			} else if (columnIndex == 1) {
				return SQLSchema.class;
			} else if (columnIndex == 2) {
				return SQLTable.class;
			} else if ( columnIndex == 3) {
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return chooser.getCatalogTerm().getText();
			} else if (columnIndex == 1) {
				return chooser.getSchemaTerm().getText();
			} else if (columnIndex == 2) {
				return "Name";
			} else if ( columnIndex == 3) {
				return "Delete Dup?";
			}
			return null;
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			TableMergeRules r = rules.get(rowIndex);
			if (columnIndex == 0) {
				SQLCatalog cat = (SQLCatalog) aValue;
				r.setCatalogName(cat==null?null:cat.getName());
				r.getSourceTable();
			} else if (columnIndex == 1) {
				SQLSchema sch = (SQLSchema) aValue;
				r.setSchemaName(sch==null?null:sch.getName());
				r.getSourceTable();
			} else if (columnIndex == 2) {
				SQLTable table = (SQLTable)aValue;
				r.setTable(table);
			} else if ( columnIndex == 3) {
System.out.println(r.getName()+ "      set value to "+ ((Boolean) aValue));				
				r.setDeleteDup((Boolean) aValue);
			}
		}

		public List<TableMergeRules> getMergeRules() {
			return rules;
		}
	}
}
