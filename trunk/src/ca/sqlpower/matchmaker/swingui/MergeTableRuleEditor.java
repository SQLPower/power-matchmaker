package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.table.TableUtils;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.matchmaker.util.MatchMakerQFAFactory;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
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
	private Match match;
	private FormValidationHandler handler;
	private JTextArea desc = new JTextArea(3,80);
	private JTable mergeRulesTable;
	MergeTableRuleTableModel mergeTableRuleTableModel;
	private JScrollPane mergeRulesScrollPane;
	private boolean hasChanges = false;
	
	
	public MergeTableRuleEditor(MatchMakerSwingSession swingSession,Match match) throws ArchitectException {
		this.swingSession = swingSession;
		this.match = match;
		if (match == null) throw new NullPointerException("You can't edit a null match");
        handler = new FormValidationHandler(status);
        setupRulesTable(swingSession,match);
        buildUI();
        setDefaultSelections();
        handler.resetHasValidated(); // avoid false hits when newly created
	}
	
	private void setupRulesTable(MatchMakerSwingSession swingSession, Match match) throws ArchitectException {
		mergeTableRuleTableModel = new MergeTableRuleTableModel(match,swingSession);
		mergeRulesTable = new TableMergeRulesTable(mergeTableRuleTableModel);
        mergeRulesTable.setName("Merge Tables");
        mergeRulesTable.setDefaultRenderer(Boolean.class,new CheckBoxRenderer());
        mergeRulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TableUtils.fitColumnWidths(mergeRulesTable, 15);
		mergeTableRuleTableModel.addTableModelListener(new TableModelListener(){
			public void tableChanged(TableModelEvent e) {
				hasChanges = true;
			}});
	}

	private void buildUI() {
		
		FormLayout layout = new FormLayout(
				"4dlu,14dlu,4dlu,fill:min(pref;"+3*(new JComboBox().getMinimumSize().width)+"px):grow, 4dlu,pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,min(pref;40dlu),12dlu,pref,4dlu,fill:40dlu:grow,4dlu,pref,4dlu"); // rows
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
		pb.add(new JLabel("Description:"), cc.xy(4,row,"l,t"));
		row += 2;
		pb.add(new JScrollPane(desc), cc.xy(4,row,"f,f"));
		row += 2;
		pb.add(new JLabel("Merge Rules:"), cc.xy(4,row,"l,t"));
		row += 2;
		mergeRulesScrollPane = new JScrollPane(mergeRulesTable);
		pb.add(mergeRulesScrollPane, cc.xy(4,row,"f,f"));

		ButtonStackBuilder bsb = new ButtonStackBuilder();
		bsb.addGridded(new JButton(moveUp));
		bsb.addRelatedGap();
		bsb.addGridded(new JButton(moveDown));
		pb.add(bsb.getPanel(), cc.xy(6,row,"c,c"));
		ButtonBarBuilder bbb = new ButtonBarBuilder();
		bbb.addGridded(new JButton(newRule));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(deleteRule));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(saveAction));
		row+=2;
		pb.add(bbb.getPanel(), cc.xy(4,row,"c,c"));
		panel = pb.getPanel();
	}

	private void setDefaultSelections() {
		desc.setText(match.getTableMergeRulesFolder().getFolderDesc());
		desc.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(DocumentEvent e) {
				hasChanges = true;
			}

			public void insertUpdate(DocumentEvent e) {
				hasChanges = true;
			}

			public void removeUpdate(DocumentEvent e) {
				hasChanges = true;
			}});
	}
	
	private Action moveUp = new AbstractAction("^") {
		public void actionPerformed(ActionEvent e) {
			final int selectedRow = mergeRulesTable.getSelectedRow();
			logger.debug("moving merge rule "+selectedRow+" up");
			if ( selectedRow > 0 &&	selectedRow < mergeRulesTable.getRowCount()) {
				mergeTableRuleTableModel.moveRuleUp(selectedRow);
				mergeRulesTable.setRowSelectionInterval(selectedRow-1, selectedRow-1);
			}
		}
	};
	
	private Action moveDown = new AbstractAction("v") {
		public void actionPerformed(ActionEvent e) {
			final int selectedRow = mergeRulesTable.getSelectedRow();
			logger.debug("moving merge rule "+selectedRow+" down");
			if ( selectedRow >= 0 && selectedRow < mergeRulesTable.getRowCount()-1) {
				mergeTableRuleTableModel.moveRuleDown(selectedRow);
				mergeRulesTable.setRowSelectionInterval(selectedRow+1, selectedRow+1);
			}
		}
	};
	
	private Action deleteRule = new AbstractAction("Delete") {
		public void actionPerformed(ActionEvent e) {
			int selectedRow = mergeRulesTable.getSelectedRow();
			logger.debug("deleting merge rule:"+selectedRow);
			if ( selectedRow >= 0 && selectedRow < mergeRulesTable.getRowCount()) {
				mergeTableRuleTableModel.removeRules(selectedRow);
				if (selectedRow >= mergeRulesTable.getRowCount()) {
					selectedRow = mergeRulesTable.getRowCount() - 1;
				}
				if (selectedRow >= 0) {
					mergeRulesTable.setRowSelectionInterval(selectedRow, selectedRow);
				}
			} else {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"Please select one merge rule to delete");
			}
		}
	};
	
	private Action newRule = new AbstractAction("New") {
		public void actionPerformed(ActionEvent e) {
			logger.debug("creating new merge rule:");
			try {
				mergeTableRuleTableModel.newRules();
			} catch (ArchitectException e1) {
				ASUtils.showExceptionDialog(swingSession.getFrame(),
						"Unexcepted Error",
						e1, null);
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
	public List<TableMergeRules> removeList = new ArrayList<TableMergeRules>();
	
	public void doUnSave() {
		PlFolder<Match> folder = (PlFolder<Match>) match.getParent();
		int index = folder.getChildren().indexOf(match);
		// XXX: need the interface support rollback!!! 
		match = swingSession.getMatchByName(match.getName());
	}
	
	public boolean doSave() {
		int i = 1;
		for (TableMergeRules r : match.getTableMergeRules()) {
			r.setSeqNo(new Long(i++));
		}
		
		match.getTableMergeRulesFolder().setFolderDesc(desc.getText());
		
		for (TableMergeRules r : removeList) {
			swingSession.delete(r);
		}
		
		swingSession.save(match);
		hasChanges = false;
		return true;
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return hasChanges;
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

		private SQLObjectChooser chooser;
		private MatchMakerSwingSession swingSession;
		private Match match;
		private List<SQLObjectChooser> choosers = new ArrayList<SQLObjectChooser>();
		
		public MergeTableRuleTableModel(Match match, 
				MatchMakerSwingSession swingSession) throws ArchitectException {
			this.swingSession = swingSession;
			this.match = match;
			for (TableMergeRules r : match.getTableMergeRules() ) {
				SQLObjectChooser chooser = new SQLObjectChooser(swingSession);
				chooser.getCatalogComboBox().setSelectedItem(
						(r.getSourceTable()==null?null:r.getSourceTable().getCatalog()));
				chooser.getSchemaComboBox().setSelectedItem(
						(r.getSourceTable()==null?null:r.getSourceTable().getSchema()));
				chooser.getTableComboBox().setSelectedItem(
						r.getSourceTable());
				TableMergeRulesValidator v1 = new TableMergeRulesValidator(chooser);
				handler.addValidateObject(chooser.getCatalogComboBox(), v1);
				handler.addValidateObject(chooser.getSchemaComboBox(), v1);
				handler.addValidateObject(chooser.getTableComboBox(), v1);
				choosers.add(chooser);
			}
			this.chooser = new SQLObjectChooser(swingSession);
		}
		
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return match.getTableMergeRules().size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			SQLObjectChooser chooser = getSQLObjectChooser(rowIndex); 
			if (columnIndex == 0) {
				return chooser.getCatalogComboBox().getSelectedItem();
			} else if (columnIndex == 1) {
				return chooser.getSchemaComboBox().getSelectedItem();
			} else if (columnIndex == 2) {
				return chooser.getTableComboBox().getSelectedItem();
			} else if ( columnIndex == 3) {
				return match.getTableMergeRules().get(rowIndex).isDeleteDup();
			} else {
				return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return chooser.getCatalogComboBox().isEnabled();
			} else if (columnIndex == 1) {
				return chooser.getSchemaComboBox().isEnabled();
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
			TableMergeRules r = match.getTableMergeRules().get(rowIndex);
			if (columnIndex == 0) {
				SQLCatalog oldCatalog = r.getSourceTable() == null ?
						null : r.getSourceTable().getCatalog();
				if (aValue != oldCatalog) {
					setValueAt(null, rowIndex, 1);
				}
			} else if (columnIndex == 1) {
				SQLSchema oldSchema = r.getSourceTable() == null ?
						null : r.getSourceTable().getSchema();
				if (aValue != oldSchema) {
					setValueAt(null, rowIndex, 2);
				}
			} else if (columnIndex == 2) {
				r.setTable((SQLTable) aValue );
				fireTableDataChanged();
			} else if ( columnIndex == 3) {
				r.setDeleteDup(((Boolean) aValue).booleanValue());
				fireTableDataChanged();
			}
		}

		public SQLObjectChooser getSQLObjectChooser(int row) {
			return choosers.get(row);
		}
		

		public void newRules() throws ArchitectException {
			TableMergeRules tableMergeRules = new TableMergeRules();
			tableMergeRules.setName("new merge strategy");
			match.getTableMergeRulesFolder().addChild(tableMergeRules);
			choosers.add(new SQLObjectChooser(swingSession));
			fireTableDataChanged();
		}
		
		public void removeRules(int index) {
			removeList.add(match.getTableMergeRules().get(index));
			match.getTableMergeRulesFolder().removeChild(
					match.getTableMergeRules().get(index));
			choosers.remove(index);
			fireTableDataChanged();
		}
		
		public void moveRuleUp(int index) {
			TableMergeRules rules = match.getTableMergeRules().get(index-1);
			SQLObjectChooser chooser = choosers.get(index-1);
			
			removeRules(index-1);
			match.getTableMergeRulesFolder().addChild(index, rules);
			choosers.add(index, chooser);
			fireTableDataChanged();
		}

		public void moveRuleDown(int index) {
			TableMergeRules rules = match.getTableMergeRules().get(index);
			SQLObjectChooser chooser = choosers.get(index);
			removeRules(index);
			match.getTableMergeRulesFolder().addChild(index+1, rules);
			choosers.add(index+1, chooser);
			fireTableDataChanged();
		}
	}
	
	private class TableMergeRulesTable extends EditableJTable {

		private MergeTableRuleTableModel mergeTableRuleTableModel;
		public TableMergeRulesTable(MergeTableRuleTableModel mergeTableRuleTableModel) {
			super(mergeTableRuleTableModel);
			this.mergeTableRuleTableModel = mergeTableRuleTableModel;
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 3) {
				return super.getCellEditor(row, column);
			} else {
				SQLObjectChooser chooser =  mergeTableRuleTableModel.getSQLObjectChooser(row);
				if (column == 0) {
					return new DefaultCellEditor(chooser.getCatalogComboBox());
				} else if (column == 1) {
					return new DefaultCellEditor(chooser.getSchemaComboBox());
				} else if (column == 2) {
					return new DefaultCellEditor(chooser.getTableComboBox());
				} else {
					throw new RuntimeException("Unexpected column index:"+column);
				}
			}
		}
	}
	
	private class TableMergeRulesValidator implements Validator {

		private SQLObjectChooser chooser;
		public TableMergeRulesValidator(SQLObjectChooser chooser) {
			this.chooser = chooser;
		}
		
		public ValidateResult validate(Object contents) {
			if (chooser.getTableComboBox().getSelectedItem() == null) {
				return ValidateResult.createValidateResult(Status.WARN,
						"Merge table is required");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }
}
