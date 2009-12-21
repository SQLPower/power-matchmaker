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

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.util.EditableJTable;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLIndex.AscendDescend;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.table.TableUtils;
import ca.sqlpower.validation.RegExValidator;
import ca.sqlpower.validation.Validated;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Opens a dialog that allows the user to choose index
 */
public class MatchMakerIndexBuilder implements DataEntryPanel, Validated{

	private static final Logger logger = Logger.getLogger(MatchMakerIndexBuilder.class);
	private final MatchMakerSwingSession swingSession;
	private final MutableComboBoxModel indexModel;
	private final SQLTable table;

	private final JPanel panel;
	private final JTextField indexName;
	private final ColumnChooserTableModel columnChooserTableModel;
	private String oldName;
	
    /** Displays validation results */
    private StatusComponent statusComponent;

    /** Handles the validation rules for this form. */
    private FormValidationHandler validationHandler;

	public MatchMakerIndexBuilder(final SQLTable table, final MutableComboBoxModel indexModel, final MatchMakerSwingSession swingSession) throws SQLObjectException {
		this.table = table;
		this.indexModel = indexModel;
		this.swingSession = swingSession;

		final SQLIndex oldIndex = (SQLIndex)indexModel.getSelectedItem();

		if (oldIndex != null &&
				table.getIndexByName(oldIndex.getName()) == null) {
			oldName = oldIndex.getName();
		} else {
			for( int i=0; ;i++) {
				oldName = table.getName()+"_UPK"+(i==0?"":String.valueOf(i));
				if (table.getIndexByName(oldName) == null) break;
			}
		}

		columnChooserTableModel = new ColumnChooserTableModel(table, oldIndex, true);
		final EditableJTable columntable = new EditableJTable(columnChooserTableModel);
		columntable.addColumnSelectionInterval(1, 1);
		TableUtils.fitColumnWidths(columntable, 15);

		FormLayout layout = new FormLayout(
				"4dlu,fill:pref:grow,4dlu",
		//column 1    2              3
				"10dlu,pref:grow,4dlu,pref:grow,4dlu,pref:grow,10dlu,fill:min(200dlu;pref):grow,4dlu");
		//row    1     2         3    4         5    6         7     8                          9    10   11
		
		panel = logger.isDebugEnabled() ? new FormDebugPanel(layout)
				: new JPanel(layout);
		PanelBuilder pb = new PanelBuilder(layout, panel);

		CellConstraints cc = new CellConstraints();
		
		statusComponent = new StatusComponent();
        pb.add(statusComponent, cc.xy(2, 2));
		pb.add(new JLabel("Table: " + DDLUtils.toQualifiedName(table)),
					cc.xy(2, 4));
		indexName = new JTextField(oldName,15);
		pb.add(indexName, cc.xy(2, 6));
		JScrollPane scrollPane = new JScrollPane(columntable);
        pb.add(scrollPane, cc.xy(2, 8, "f,f"));

        validationHandler = new FormValidationHandler(statusComponent);
		validationHandler.addValidateObject(indexName,
                new RegExValidator(
                        "[a-z_][a-z0-9_]*",
                        "Index name must be a valid SQL identifier",
                        false));
	}

	public boolean hasUnsavedChanges() {
		return columnChooserTableModel.isModified() || !indexName.getText().equals(oldName);
	}

	public boolean applyChanges() {
		
		if (validationHandler.getFailResults().size() != 0) return false;

		List<SQLColumn> selectedColumns = columnChooserTableModel.getSelectedSQLColumns();
		if (selectedColumns.size() == 0) return false;

        String newName = indexName.getText();
        SQLIndex index = null;
        boolean contains = false;
        try {
	        for (int i = 0; i < indexModel.getSize(); i++) {
				index = (SQLIndex)indexModel.getElementAt(i);
				if (index.getName().equalsIgnoreCase(newName)) {
					index.setUnique(true);
					index.setQualifier(null);
					index.setFilterCondition(null);
					while (index.getChildCount() > 0) {
						index.removeChild(index.getChildren().get(0));
					}
					contains = true;
				}
			}
	        if (!contains) {
	        	index = new SQLIndex(newName, true, null, null, null);
	    		indexModel.addElement(index);
	        }
	        
	        for (SQLColumn column : selectedColumns) {
	    		index.addChild(index.new Column(column, AscendDescend.UNSPECIFIED));
			}
	    	indexModel.setSelectedItem(index);
			logger.debug("Index columns after save: "+index.getChildren());
			return true;
		} catch (SQLObjectException e) {
			SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
				            "Unexpected error when adding Column to the Index",
				            e);
			return false;
		} catch (ObjectDependentException e) {
			SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
		            "Unexpected error when adding Column to the Index",
		            e);
			return false;
		}
	}

	public void discardChanges() {
		//does nothing for now...
	}

	public JComponent getPanel() {
		return panel;
	}

	public FormValidationHandler getHandler() {
		return validationHandler;
	}

}
