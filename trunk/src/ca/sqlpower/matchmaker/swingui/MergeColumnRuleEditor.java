package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MergeColumnRuleEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(MergeColumnRuleEditor.class);
	private JPanel panel;
	StatusComponent status = new StatusComponent();
	private final MatchMakerSwingSession swingSession;
	private final Match match;
	private TableMergeRules mergeRule;
	private FormValidationHandler handler;
	private final SQLObjectChooser chooser;
	private final JCheckBox deleteDup = new JCheckBox();
	
	
	private ColumnMergeRuleTableModel ruleTableModel;
	private ColumnMergeRulesTable ruleTable;
	
	public MergeColumnRuleEditor(MatchMakerSwingSession swingSession,
			Match match, TableMergeRules mergeRule) throws ArchitectException {
		this.swingSession = swingSession;
		this.match = match;
		this.mergeRule = mergeRule;
		if (match == null) throw new NullPointerException("You can't edit a null match");
		if (mergeRule == null) throw new NullPointerException("You can't edit a null merge rule");
		chooser = new SQLObjectChooser(swingSession);
        handler = new FormValidationHandler(status);

        ruleTableModel = new ColumnMergeRuleTableModel(mergeRule);
        ruleTable = new ColumnMergeRulesTable(ruleTableModel);
        
        
        buildUI();
        setDefaultSelections();
        handler.resetHasValidated(); // avoid false hits when newly created
        
        
	}
	
	private void buildUI() {
		
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:min(pref;"+(new JComboBox().getMinimumSize().width)+"px):grow, 4dlu,pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,fill:40dlu:grow,4dlu,pref,4dlu"); // rows
			//	 1     2    3    4               5    6    7     8         9    10   11      
			//    status    cat       schema    table     index     del dup   table      button bar

		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled() ? 
				new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		int row = 2;
		pb.add(status, cc.xy(4,row));
		row += 2;
		pb.add(chooser.getCatalogTerm(), cc.xy(2,row,"r,t"));
		pb.add(chooser.getCatalogComboBox(), cc.xy(4,row,"f,t"));
		row += 2;
		pb.add(chooser.getSchemaTerm(), cc.xy(2,row,"r,t"));
		pb.add(chooser.getSchemaComboBox(), cc.xy(4,row,"f,t"));
		
		row += 2;
		pb.add(new JLabel("Table Name:"), cc.xy(2,row,"r,t"));
		pb.add(chooser.getTableComboBox(), cc.xy(4,row,"f,t"));

		row += 2;
		pb.add(new JLabel("Index Name:"), cc.xy(2,row,"r,t"));
		pb.add(chooser.getUniqueKeyComboBox(), cc.xy(4,row,"f,t"));
		
		row += 2;
		pb.add(new JLabel("Delete Dup:"), cc.xy(2,row,"r,t"));
		pb.add(deleteDup, cc.xy(4,row,"l,t"));
		
		
		row += 2;
		pb.add(new JScrollPane(ruleTable), cc.xy(4,row,"f,f"));

		ButtonBarBuilder bbb = new ButtonBarBuilder();
		bbb.addGridded(new JButton(deriveAction));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(clearAction));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(newRuleAction));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(deleteRuleAction));
		bbb.addRelatedGap();
		bbb.addGridded(new JButton(saveAction));
		
		row+=2;
		pb.add(bbb.getPanel(), cc.xy(4,row,"c,c"));
		panel = pb.getPanel();
	}
	
	private Action newRuleAction = new AbstractAction("New") {
		public void actionPerformed(ActionEvent e) {
			ColumnMergeRules newRules = new ColumnMergeRules();
			newRules.setActionType(MergeActionType.IGNORE);
			mergeRule.addChild(newRules);
		}};
		
	private Action deleteRuleAction = new AbstractAction("Delete") {
		public void actionPerformed(ActionEvent e) {
			int selectedRow = ruleTable.getSelectedRow();
			if (selectedRow == -1) {
				JOptionPane.showMessageDialog(swingSession.getFrame(),
						"You have to select a column first!");
				return;
			} else {
				mergeRule.removeChild(mergeRule.getChildren().get(selectedRow));
			}
		}};

	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(ActionEvent e) {
			swingSession.save(match);
		}
	};
		

	private Action deriveAction = new AbstractAction("Derive Collision Criteria") {
		public void actionPerformed(ActionEvent e) {
			try {
				List<SQLColumn> columns = new ArrayList<SQLColumn>(
						mergeRule.getSourceTable().getColumns()); 
				for (SQLColumn column : columns) {
					if (!doesColumnExistsInChildren(column) ) {
						ColumnMergeRules newRules = new ColumnMergeRules();
						newRules.setColumn(column);
						newRules.setActionType(MergeActionType.IGNORE);
						mergeRule.addChild(newRules);
					}
				}
			} catch (ArchitectException e1) {
				ASUtils.showExceptionDialog("Unexcepted Error", e1);
			}
		}
		
		private boolean doesColumnExistsInChildren( SQLColumn column) {
			for ( ColumnMergeRules columnMergeRules : mergeRule.getChildren()) {
				if ( columnMergeRules.getColumnName().equals(column.getName()) )
					return true;
			}
			return false;
		}
	};
	
	private Action clearAction = new AbstractAction("Clear Collision Criteria") {
		public void actionPerformed(ActionEvent e) {
			while (true) {
				if (mergeRule.getChildCount() > 0) {
					ColumnMergeRules columnMergeRule = (ColumnMergeRules) mergeRule.getChildren().get(0);
					mergeRule.removeChild(columnMergeRule);
				} else {
					break;
				}
			}
		}
	};
	
	private void setDefaultSelections() throws ArchitectException {
		chooser.getCatalogComboBox().setSelectedItem(mergeRule.getSourceTable().getCatalog());
		chooser.getSchemaComboBox().setSelectedItem(mergeRule.getSourceTable().getSchema());
		chooser.getTableComboBox().setSelectedItem(mergeRule.getSourceTable());
		chooser.getUniqueKeyComboBox().setSelectedItem(mergeRule.getTableIndex());
		deleteDup.setSelected(mergeRule.isDeleteDup());
	}

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
	 * table model of the column merge rules that belongs to the table merge rule.
	 * columns are column name, action type
	 * row count = children count of table merge rule.
	 */
	private class ColumnMergeRuleTableModel extends AbstractTableModel {

		private TableMergeRules mergeRule;
		public ColumnMergeRuleTableModel(TableMergeRules mergeRule) {
			this.mergeRule = mergeRule;
			if (mergeRule != null) {
				this.mergeRule.addMatchMakerListener(new MatchMakerListener<TableMergeRules, ColumnMergeRules>(){

					public void mmChildrenInserted(MatchMakerEvent<TableMergeRules, ColumnMergeRules> evt) {
						fireTableDataChanged();
					}

					public void mmChildrenRemoved(MatchMakerEvent<TableMergeRules, ColumnMergeRules> evt) {
						fireTableDataChanged();
					}

					public void mmPropertyChanged(MatchMakerEvent<TableMergeRules, ColumnMergeRules> evt) {
						fireTableDataChanged();
					}

					public void mmStructureChanged(MatchMakerEvent<TableMergeRules, ColumnMergeRules> evt) {
						fireTableDataChanged();
					}});
			}
		}
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return mergeRule.getChildCount();
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return "Column";
			} else if (column == 1) {
				return "Action";
			} else {
				throw new RuntimeException("getColumnName: Unexcepted column index:"+column);
			}
		}
	
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return ((ColumnMergeRules)(mergeRule.getChildren().get(rowIndex))).getColumn();
			} else if (columnIndex == 1) {
				return ((ColumnMergeRules)(mergeRule.getChildren().get(rowIndex))).getActionType();
			} else {
				throw new RuntimeException("getValueAt: Unexcepted column index:"+columnIndex);
			}		
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			ColumnMergeRules rule = mergeRule.getChildren().get(rowIndex);
			if (columnIndex == 0) {
				rule.setColumn((SQLColumn) aValue);
			} else if (columnIndex == 1) {
				rule.setActionType((MergeActionType) aValue);
			} else {
				throw new RuntimeException("setValueAt: Unexcepted column index:"+columnIndex);
			}	
		}
	}
	
	private class ColumnMergeRulesTable extends EditableJTable {

		private ColumnMergeRuleTableModel columnMergeRuleTableModel;
		public ColumnMergeRulesTable(ColumnMergeRuleTableModel columnMergeRuleTableModel) {
			super(columnMergeRuleTableModel);
			this.columnMergeRuleTableModel = columnMergeRuleTableModel;
		}

		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 0) {
				return new DefaultCellEditor(chooser.getColumnComboBox());
			} else if (column == 1) {
				return new DefaultCellEditor(
						new JComboBox(
								new DefaultComboBoxModel(
										ColumnMergeRules.MergeActionType.values())));
			} else {
				return super.getCellEditor(row, column);
			}
		}
	}

}
