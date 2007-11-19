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

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.util.EditableJTable;
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
	private final Project project;

	private final JPanel panel;
	private final JTextField indexName;
	private final ColumnChooserTableModel columnChooserTableModel;
	private String oldName;
	
    /** Displays validation results */
    private StatusComponent statusComponent;

    /** Handles the validation rules for this form. */
    private FormValidationHandler validationHandler;

	public MatchMakerIndexBuilder(Project project, MatchMakerSwingSession swingSession) throws ArchitectException {
		this.project = project;
		this.swingSession = swingSession;

		final SQLTable sqlTable = project.getSourceTable();
		final SQLIndex oldIndex = project.getSourceTableIndex();

		if (oldIndex != null &&
				sqlTable.getIndexByName(oldIndex.getName()) == null) {
			oldName = oldIndex.getName();
		} else {
			for( int i=0; ;i++) {
				oldName = project.getSourceTableName()+"_UPK"+(i==0?"":String.valueOf(i));
				if (sqlTable.getIndexByName(oldName) == null) break;
			}
		}

		columnChooserTableModel = new ColumnChooserTableModel(sqlTable, oldIndex, true);
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
		pb.add(new JLabel("Table: " + DDLUtils.toQualifiedName(project.getSourceTable())),
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
		
		List<SQLColumn> selectedColumns = columnChooserTableModel.getSelectedSQLColumns();

		if (selectedColumns.size() == 0) return false;

        if (validationHandler.getFailResults().size() != 0) return false;

		SQLIndex index = new SQLIndex(indexName.getText(),true,null,IndexType.OTHER,null);
		try {
			for (SQLColumn column : selectedColumns) {
				index.addChild(index.new Column(column,false,false));
			}
			logger.debug("Index columns after save: "+index.getChildren());
		} catch (ArchitectException e) {
			SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
				            "Unexpected error when adding Column to the Index",
				            e);
		}

		project.setSourceTableIndex(index);
		return true;
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
