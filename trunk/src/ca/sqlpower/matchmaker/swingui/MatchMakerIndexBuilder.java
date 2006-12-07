package ca.sqlpower.matchmaker.swingui;

import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.matchmaker.Match;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MatchMakerIndexBuilder implements EditorPane {

	private static final Logger logger = Logger.getLogger(MatchMakerIndexBuilder.class);
	private Match match;
	private JDialog dialog;
	private JPanel panel;
	private List<CustomTableColumn> selectedColumns;
	private JTextField indexName;
	private MatchMakerSwingSession swingSession;
	private JTable columntable;
	private final SQLTable sqlTable;
	private final IndexColumnTableModel indexColumnTableModel;
	private boolean modified = false;
	
	
	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	public MatchMakerIndexBuilder(Match match, MatchMakerSwingSession swingSession) throws ArchitectException {
		this.match = match;
		this.swingSession = swingSession;

		sqlTable = match.getSourceTable();
		SQLIndex oldIndex = match.getSourceTableIndex();
		
		String name;
		if (oldIndex != null && 
				sqlTable.getIndexByName(oldIndex.getName()) == null) {
			name = oldIndex.getName();
		} else {
			for( int i=0; ;i++) {
				name = match.getSourceTableName()+"_UPK"+(i==0?"":String.valueOf(i));
				if (sqlTable.getIndexByName(name) == null) break;
			}
		}

		indexName = new JTextField(name,80);
		indexColumnTableModel = new IndexColumnTableModel(sqlTable,oldIndex);
		columntable = new JTable(indexColumnTableModel);
		columntable.addColumnSelectionInterval(1, 1);

		dialog = new JDialog(swingSession.getFrame());
		buildUI();
		dialog.pack();
		dialog.setLocationRelativeTo(swingSession.getFrame());
		dialog.setVisible(true);
	}
	
	private void buildUI() {
		
		FormLayout layout = new FormLayout(
				"4dlu,fill:min(200dlu;pref):grow,4dlu",
		// column1    2    3  
				"10dlu,pref,4dlu,pref,10dlu,fill:min(200dlu;pref):grow,4dlu,pref,4dlu");
		//       1     2    3    4    5     6    7                     8    9 
		PanelBuilder pb;
		JPanel panel = logger.isDebugEnabled() ? new FormDebugPanel(layout)
				: new JPanel(layout);
		pb = new PanelBuilder(layout, panel);

		CellConstraints cc = new CellConstraints();

		JButton save = new JButton(new AbstractAction("OK") {
			public void actionPerformed(ActionEvent e) {
				doSave();
				dialog.setVisible(false);
				dialog.dispose();
			}
		});

		JButton exit = new JButton(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				if (hasUnsavedChanges()) {
					int responds = JOptionPane.showConfirmDialog(
							dialog,
							"Do you want to save before close the index builder?");
					if ( responds != JOptionPane.NO_OPTION)
						return;
				}
				dialog.setVisible(false);
				dialog.dispose();
			}
		});

		pb.add(new JLabel("Table Name:" + match.getSourceTableName()),
					cc.xy(2, 2, "l,c"));
		pb.add(indexName, cc.xy(2, 4, "l,f"));
		final ScrollPane scrollPane = new ScrollPane();
		scrollPane.add(columntable);
		pb.add(scrollPane, cc.xy(2, 6, "f,f"));
		
		ButtonBarBuilder bbb = new ButtonBarBuilder();
		bbb.addGridded(save);
		bbb.addRelatedGap();
		bbb.addGridded(exit);

		pb.add(bbb.getPanel(), cc.xy(2,8,"r,c"));
		dialog.getContentPane().add(panel);
		
	}
	
	/**
	 * this class repersens the table row model of the pick your owner 
	 * column for index table. which has only 3 columns. 
	 *
	 */
	private class CustomTableColumn implements Comparable<CustomTableColumn> {
		private boolean key;
		private Integer position;
		private SQLColumn sqlColumn;
		
		public CustomTableColumn(boolean key, Integer position, SQLColumn column) {
			this.key = key;
			this.position = position;
			this.sqlColumn = column;
		}

		public void setSqlColumn(SQLColumn column) {
			this.sqlColumn = column;
		}

		public void setKey(boolean key) {
			this.key = key;
			if ( !key ) {
				position = null;
			}
		}

		public void setPosition(Integer position) {
			this.position = position;
		}

		public SQLColumn getSQLColumn() {
			return sqlColumn;
		}

		public boolean isKey() {
			return key;
		}

		public Integer getPosition() {
			return position;
		}

		public int compareTo(CustomTableColumn o) {
			if (getPosition() == null)
				return -1;
			else if ( o.getPosition() == null )
				return 1;
			else 
				return getPosition().compareTo(o.getPosition());
		}
	}
	
	private class IndexColumnTableModel extends AbstractTableModel {

		private List<CustomTableColumn> candidateColumns
							= new ArrayList<CustomTableColumn>(); 
		public IndexColumnTableModel(SQLTable sqlTable, SQLIndex oldIndex) throws ArchitectException {
			
			int pos = 0;
			for ( SQLColumn column : sqlTable.getColumns()) {
				SQLIndex.Column indexColumn = (oldIndex==null?null: 
					(Column) oldIndex.getChildByName(column.getName()));
				if (indexColumn!=null) pos++;
				candidateColumns.add(
						new CustomTableColumn((indexColumn!=null),
								(indexColumn==null?null:pos),column));
			}
		}
		
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return candidateColumns.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if ( columnIndex == 0 ) {
				return candidateColumns.get(rowIndex).isKey();
			} else if ( columnIndex == 1 ) {
				return candidateColumns.get(rowIndex).getPosition();
			}  else if ( columnIndex == 2 ) {
				return candidateColumns.get(rowIndex).getSQLColumn();
			} else {
				throw new IllegalArgumentException("unknown columnIndex: " + columnIndex);
			}
		}
		
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if ( columnIndex == 0 ) {
				return Boolean.class;
			} else if ( columnIndex == 1 ) {
				return Integer.class;
			}  else if ( columnIndex == 2 ) {
				return SQLColumn.class;
			} else {
				throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
			}
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			setModified(true);
			if ( columnIndex == 0 ) {
				candidateColumns.get(rowIndex).setKey((Boolean) aValue);
				if ( (Boolean) aValue ) {
					int max = -1;
					for ( CustomTableColumn column : candidateColumns ) {
						if ( column.getPosition() != null && max < column.getPosition().intValue()) {
							max = column.getPosition().intValue();
						}
					}
					candidateColumns.get(rowIndex).setPosition(new Integer(max+1));
				} else {
					candidateColumns.get(rowIndex).setPosition(null);
				}
			} else if ( columnIndex == 1 ) {
				candidateColumns.get(rowIndex).setPosition((Integer) aValue);
			}  else if ( columnIndex == 2 ) {
			} else {
				throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
			}
			fireTableDataChanged();
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if ( columnIndex == 0 ) {
				return true;
			} else if ( columnIndex == 1 ) {
				return true;
			}  else if ( columnIndex == 2 ) {
				return false;
			} else {
				throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
			}
		}

		public List<CustomTableColumn> getCandidateColumns() {
			return candidateColumns;
		}
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return isModified();
	}
	
	public boolean doSave() {
		
		selectedColumns = new ArrayList<CustomTableColumn>();
		for ( CustomTableColumn column : indexColumnTableModel.getCandidateColumns() ) {
			if ( column.isKey() ) selectedColumns.add(column);
		}
		Collections.sort(selectedColumns);

		if ( selectedColumns.size() == 0 || indexName.getText().length() == 0 )
			return false;
		SQLIndex index = new SQLIndex(indexName.getText(),true,null,IndexType.OTHER,null);
		for ( CustomTableColumn column : selectedColumns ) {
			try {
				index.addChild(index.new Column(column.getSQLColumn(),false,false));
			} catch (ArchitectException e) {
				ASUtils.showExceptionDialog(swingSession.getFrame(),
						"Unexcepted error when adding Column to the Index",
						e, null);
			}
		}
		match.setSourceTableIndex(index);
		return true;
	}

}
