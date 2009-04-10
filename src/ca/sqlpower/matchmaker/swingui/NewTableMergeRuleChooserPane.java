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

import java.awt.event.ComponentListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.Resizable;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validated;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.validation.swingui.ValidationHandler;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class NewTableMergeRuleChooserPane implements DataEntryPanel, Resizable, Validated {
	private static final Logger logger = Logger.getLogger(ProjectEditor.class);
	
	private final Project project;
	private final MatchMakerSwingSession swingSession;
	
	private final SQLObjectChooser chooser;
	private final JComboBox parentMergeRule;
	
	private final JPanel panel;
	
	private final StatusComponent status = new StatusComponent();
	private final FormValidationHandler handler = new FormValidationHandler(status);
	
	public NewTableMergeRuleChooserPane(MatchMakerSwingSession swingSession, Project project) {
		this.project = project;
		this.swingSession = swingSession;
		this.chooser = new SQLObjectChooser(
				swingSession, swingSession.getFrame(),
				project.getSourceTable().getParentDatabase().getDataSource());
		
		this.parentMergeRule = new JComboBox();
		for (TableMergeRules tmr : project.getTableMergeRules()) {
        	parentMergeRule.addItem(tmr.getSourceTable());
        }
		
		this.panel = buildUI();
		addValidators();
		handler.resetHasValidated();
	}
	
	private JPanel buildUI() {
		FormLayout layout = new FormLayout(
				"10dlu,pref,4dlu,fill:max(pref;" + 5*new JComboBox().getMinimumSize().getWidth() + "px):grow,10dlu", 
	        	"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,10dlu");
	        	//1    2    3    4    5     6    7   8    9 
		CellConstraints cc = new CellConstraints();

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		PanelBuilder pb = new PanelBuilder(layout, p);
		
		int row = 2;
		pb.add(status, cc.xyw(2, row, 3, "f,f"));
		
		row += 2;
		pb.add(new JLabel("Catalog:"), cc.xy(2, row));
		pb.add(chooser.getCatalogComboBox(), cc.xy(4, row));
        
        row += 2;
        pb.add(new JLabel("Schema:"), cc.xy(2, row));
        pb.add(chooser.getSchemaComboBox(), cc.xy(4, row));
        
        row += 2;
        pb.add(new JLabel("Table:"), cc.xy(2, row));
        pb.add(chooser.getTableComboBox(), cc.xy(4, row));
        
        row += 2;
        pb.add(new JLabel("Index:"), cc.xy(2, row));
        pb.add(chooser.getUniqueKeyComboBox(), cc.xy(4, row));
        
        row +=2;
        pb.add(new JLabel("Parent Table:"), cc.xy(2, row));
        pb.add(parentMergeRule, cc.xy(4, row));
        
		return pb.getPanel();
	}
	
	private void addValidators() {
		MergeColumnRuleComboBoxValidator v1 = new MergeColumnRuleComboBoxValidator(chooser);
		handler.addValidateObject(chooser.getCatalogComboBox(), v1);
		handler.addValidateObject(chooser.getSchemaComboBox(), v1);
		handler.addValidateObject(chooser.getTableComboBox(), v1);
		handler.addValidateObject(chooser.getUniqueKeyComboBox(), v1);
		handler.addValidateObject(parentMergeRule, v1);
	}
	
	public void addResizeListener(ComponentListener cl) {
		chooser.getCatalogComboBox().addComponentListener(cl);
	}
	
	public boolean applyChanges() {
		TableMergeRules mergeRule = new TableMergeRules();
		mergeRule.setTable((SQLTable) chooser.getTableComboBox().getSelectedItem());
		mergeRule.setTableIndex((SQLIndex) chooser.getUniqueKeyComboBox().getSelectedItem());
        mergeRule.deriveColumnMergeRules();
        // XXX: This linear searching through TableMergeRules is occurring all over MatchMaker. 
        // I think we really need to make the list of merge rules a Map of table name to merge rule.
        for (TableMergeRules tmr: project.getTableMergeRules()) {
        	if (tmr.getTableName().equals(((SQLTable)parentMergeRule.getSelectedItem()).getName())) {
        		mergeRule.setParentMergeRule(tmr);
        	}
        }
		
		swingSession.setCurrentEditorComponent(
				new MergeColumnRuleEditor(swingSession,project,mergeRule));
		return true;
	}

	public void discardChanges() {
		// no changes to discard
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		// no changes to be saved
		return true;
	}

	public ValidationHandler getHandler() {
		return handler;
	}
	
	/**
	 * Validates the SQLChooser ComboBoxes
	 */
	private class MergeColumnRuleComboBoxValidator implements Validator {

		private SQLObjectChooser chooser;
		public MergeColumnRuleComboBoxValidator(SQLObjectChooser chooser) {
			this.chooser = chooser;
		}
		
		public ValidateResult validate(Object contents) {
			if (chooser.getTableComboBox().getSelectedItem() == null) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Merge table is required");
				
			// Checks if an existing merge rule is already set to operate on 
			// the chosen schema and table.
			} else if (chooser.getTableComboBox().isEnabled()){
				List<TableMergeRules> mergeRules = project.getTableMergeRules();
				String catalogName = null;
				if (chooser.getCatalogComboBox().getSelectedItem() != null) {
					catalogName = ((SQLCatalog) chooser.getCatalogComboBox().getSelectedItem()).getName();
				}
				String schemaName = null;
				if (chooser.getSchemaComboBox().getSelectedItem() != null) {
					schemaName = ((SQLSchema)chooser.getSchemaComboBox().getSelectedItem()).getName();
				}
				String tableName = ((SQLTable)chooser.getTableComboBox().getSelectedItem()).getName();
				SQLTable parentTable = ((SQLTable) parentMergeRule.getSelectedItem());

				for (TableMergeRules rule: mergeRules) {
					if (((rule.getCatalogName() == null && catalogName == null) || (rule.getCatalogName() != null && rule.getCatalogName().equals(catalogName))) &&
							((rule.getSchemaName() == null && schemaName == null) || (rule.getSchemaName() != null && rule.getSchemaName().equals(schemaName))) &&
							rule.getTableName().equals(tableName)) {
						if (rule.isSourceMergeRule()) {
							return ValidateResult.createValidateResult(Status.FAIL, 
									"Only one merge rule can operate on the Source Table");
						}
						if (rule.getParentMergeRule().getSourceTable().equals(parentTable)) {
							return ValidateResult.createValidateResult(Status.FAIL,
									"Only one merge rule can operate on an individual table" +
							" with a particular parent table. Please choose a different parent table.");
						}
					}
				}
			}
			
			return ValidateResult.createValidateResult(Status.OK, "");
		}
	}
}
