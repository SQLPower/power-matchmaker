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

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class UserPreferencesEditor implements DataEntryPanel {

    /**
     * User preferences are not repository-specific, so they are stored
     * as properties of the session context.  This is the session context
     * we are editing.
     */
    private final SwingSessionContext context;
    
    /**
     * The panel that holds this editor's UI.
     */
    private final JPanel panel;

    /**
     * The radio button that chooses to log in
     */
    private JRadioButton autoLoginRadioButton = new JRadioButton("Automatically connect to this repository");
    private JComboBox autoLoginDataSourceBox = new JComboBox();
    private JRadioButton loginDialogRadioButton = new JRadioButton("Show the Login Dialog");
    private JTextField addressDataPath = new JTextField();
    
    public UserPreferencesEditor(SwingSessionContext context) {
        this.context = context;
        panel = buildUI();
    }
    
    /**
     * Subroutine of the constructor that puts together the panel, creating
     * all the GUI components and initializing them to the current values
     * in the session context.
     */
    private JPanel buildUI() {
        FormLayout layout = new FormLayout("pref,4dlu,pref:grow");
        DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        fb.setDefaultDialogBorder();
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(autoLoginRadioButton);
        bg.add(loginDialogRadioButton);
        
        autoLoginRadioButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                autoLoginDataSourceBox.setEnabled(autoLoginRadioButton.isSelected());
            }
        });

        autoLoginRadioButton.setSelected(context.isAutoLoginEnabled());
        loginDialogRadioButton.setSelected(!context.isAutoLoginEnabled());
        
        // only add data sources that are marked as being repositories
        for (SPDataSource ds : context.getPlDotIni().getConnections()) {
            if (ds.getPlSchema() != null && ds.getPlSchema().length() > 0) {
                autoLoginDataSourceBox.addItem(ds);
            }
        }

        autoLoginDataSourceBox.setSelectedItem(context.getAutoLoginDataSource());
        autoLoginDataSourceBox.setEnabled(context.isAutoLoginEnabled());
        
        addressDataPath.setText(context.getAddressCorrectionDataPath());
        
        fb.append("On Startup:", autoLoginRadioButton);
        fb.append("",            autoLoginDataSourceBox);
        fb.append("",            loginDialogRadioButton);
        fb.appendSeparator();
        fb.append("Address Correction Data Path:", addressDataPath);
        
        return fb.getPanel();
    }
    
    public boolean applyChanges() {
        context.setAutoLoginEnabled(autoLoginRadioButton.isSelected());
        context.setAutoLoginDataSource((SPDataSource) autoLoginDataSourceBox.getSelectedItem());
        context.setAddressCorrectionDataPath(addressDataPath.getText());
        return true;
    }

    public void discardChanges() {
        // nothing to do
    }

    public JComponent getPanel() {
        return panel;
    }

	public boolean hasUnsavedChanges() {
		return true;
	}
}
