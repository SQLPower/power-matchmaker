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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.swingui.table.TableUtils;
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
	private JTextArea desc = new JTextArea(3,80);
	private JTable mergeRulesTable;
	MergeTableRuleTableModel mergeTableRuleTableModel;
	private JScrollPane mergeRulesScrollPane;

	private final MatchMakerSwingSession swingSession;
	private Match match;

	StatusComponent status = new StatusComponent();
	private FormValidationHandler handler;
	
	public MergeTableRuleEditor(MatchMakerSwingSession swingSession,Match match) {
		this.swingSession = swingSession;
		this.match = match;
		if (match == null) {
			throw new NullPointerException("You can't edit a null match");
		}
        handler = new FormValidationHandler(status);
        setupRulesTable(swingSession,match);
        buildUI();
        setDefaultSelections();
        handler.resetHasValidated(); // avoid false hits when newly created
	}
	
	private void setupRulesTable(MatchMakerSwingSession swingSession, Match match) {
		mergeTableRuleTableModel = new MergeTableRuleTableModel(match,swingSession);
		mergeRulesTable = new TableMergeRulesTable(mergeTableRuleTableModel);
        mergeRulesTable.setName("Merge Tables");

        mergeRulesTable.setDefaultRenderer(Boolean.class,new CheckBoxRenderer());
        mergeRulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TableUtils.fitColumnWidths(mergeRulesTable, 15);
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
			mergeTableRuleTableModel.newRules();
		}
	};
	
	/**
     * Saves the list in table model to merge rules
     */
	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(final ActionEvent e) {
            try {
                doSave();
            } catch (Exception ex) {
                MMSUtils.showExceptionDialog(swingSession.getFrame(),
                		"Merge Interface Not Saved", ex);
            }
		}
	};
	
	public boolean doSave() {
		final List<TableMergeRules> editingRules = 
			mergeTableRuleTableModel.getMergeRules();

		logger.debug("#1 children size="+match.getTableMergeRules().size());
		
		for ( int i=0; ;i++) {
			TableMergeRules r1,r2;
			if (i < editingRules.size()) {
				r1 = editingRules.get(i);
			} else {
				r1 = null;
			}
			if (i < match.getTableMergeRules().size()) {
				r2 = match.getTableMergeRules().get(i);
			} else {
				r2 = null;
			}
			if (r1 != null && r2 != null) {
				r2.setSeqNo(new Long(10*i));
				r2.setDeleteDup(r1.isDeleteDup());
				r2.setTable((SQLTable) mergeTableRuleTableModel.getSQLObjectChooser(i).getTableComboBox().getSelectedItem());
				logger.debug("r2 table="+r2.getSourceTable());
				try {
					r2.setTableIndex(r1.getTableIndex());
				} catch (ArchitectException e) {
					throw new ArchitectRuntimeException(e);
				}
				while (r2.getChildCount() > 0) {
					r2.removeChild(r2.getChildren().get(0));
				}
				for (ColumnMergeRules cr : r1.getChildren()) {
					r2.addChild(cr);
				}
				swingSession.save(match);
			} else if ( r1 != null && r2 == null ) {
				r1.setSeqNo(new Long(10*i));
				match.addTableMergeRule(r1);
				swingSession.save(match);
			} else if ( r1 == null && r2 != null ) {
				swingSession.delete(r2);
			} else {
				break;
			}
		}
		
		logger.debug("#3 children size="+match.getTableMergeRules().size());
		return true;
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		final List<TableMergeRules> editingRules = 
			mergeTableRuleTableModel.getMergeRules();
		if (editingRules.size() != match.getTableMergeRules().size()) return true;
		for (int i=0; i<editingRules.size(); i++ ) {
			TableMergeRules r1 = editingRules.get(i);
			TableMergeRules r2 = match.getTableMergeRules().get(i);
			if ( r1.getSourceTable() != r2.getSourceTable() ||
					r1.isDeleteDup() != r2.isDeleteDup()) {
				return true;
			}
		}
		return false;
	}

	private class TableMergeRuleRow {
		private final TableMergeRules rules;
		private final SQLObjectChooser chooser;
		public TableMergeRuleRow(TableMergeRules rules, 
				MatchMakerSwingSession swingSession) {
			this.rules = rules.duplicate(new MatchMakerFolder<TableMergeRules>(), swingSession);
			this.chooser = new SQLObjectChooser(swingSession);
			chooser.getCatalogComboBox().setSelectedItem(
					(rules.getSourceTable()==null?null:rules.getSourceTable().getCatalog()));
			chooser.getSchemaComboBox().setSelectedItem(
					(rules.getSourceTable()==null?null:rules.getSourceTable().getSchema()));
			chooser.getTableComboBox().setSelectedItem(
					rules.getSourceTable());
			TableMergeRulesValidator v1 = new TableMergeRulesValidator(chooser);
			handler.addValidateObject(chooser.getCatalogComboBox(), v1);
			handler.addValidateObject(chooser.getSchemaComboBox(), v1);
			handler.addValidateObject(chooser.getTableComboBox(), v1);
		}
		public SQLObjectChooser getChooser() {
			return chooser;
		}
		public TableMergeRules getRules() {
			return rules;
		}
		
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

		private List<TableMergeRuleRow> rows;
		private SQLObjectChooser chooser;
		private MatchMakerSwingSession swingSession;
		
		public MergeTableRuleTableModel(Match match, 
				MatchMakerSwingSession swingSession) {
			this.swingSession = swingSession;
			this.chooser = new SQLObjectChooser(swingSession);
			rows = new ArrayList<TableMergeRuleRow>();
			for (TableMergeRules r : match.getTableMergeRules()) {
				rows.add(new TableMergeRuleRow(r,swingSession));
			}
		}
		
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return rows.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return rows.get(rowIndex).getChooser().getCatalogComboBox().getSelectedItem();
			} else if (columnIndex == 1) {
				return rows.get(rowIndex).getChooser().getSchemaComboBox().getSelectedItem();
			} else if (columnIndex == 2) {
				return rows.get(rowIndex).getChooser().getTableComboBox().getSelectedItem();
			} else if ( columnIndex == 3) {
				return rows.get(rowIndex).getRules().isDeleteDup();
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
			TableMergeRules r = rows.get(rowIndex).getRules();
			if (columnIndex == 0) {
				setValueAt(null, rowIndex, 1);
			} else if (columnIndex == 1) {
				setValueAt(null, rowIndex, 2);
			} else if (columnIndex == 2) {
				r.setTable((SQLTable) aValue );
			} else if ( columnIndex == 3) {
				r.setDeleteDup(((Boolean) aValue).booleanValue());
			}
			fireTableDataChanged();
		}

		public List<TableMergeRules> getMergeRules() {
			List<TableMergeRules> list = new ArrayList<TableMergeRules>();
			for ( TableMergeRuleRow r : rows) {
				list.add(r.getRules());
			}
			return list;
		}
		
		public SQLObjectChooser getSQLObjectChooser(int row) {
			return rows.get(row).getChooser();
		}
		

		public void newRules() {
			rows.add(new TableMergeRuleRow(new TableMergeRules(),swingSession));
			fireTableDataChanged();
		}
		
		public void removeRules(int index) {
			rows.remove(index);
			fireTableDataChanged();
		}
		
		public void moveRuleUp(int index) {
			TableMergeRuleRow selectedRow = rows.get(index);
			TableMergeRuleRow targetRow = rows.get(index-1);
			rows.remove(targetRow);
			rows.add(rows.indexOf(selectedRow)+1, targetRow);
			fireTableDataChanged();
		}
		public void moveRuleDown(int index) {
			TableMergeRuleRow selectedRow = rows.get(index);
			TableMergeRuleRow targetRow = rows.get(index+1);
			rows.remove(targetRow);
			rows.add(rows.indexOf(selectedRow), targetRow);
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
