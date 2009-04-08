/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.engine;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import ca.sqlpower.matchmaker.MungeSettings;
import ca.sqlpower.matchmaker.MungeSettings.AutoValidateSetting;
import ca.sqlpower.matchmaker.MungeSettings.PoolFilterSetting;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A {@link DataEntryPanel} implementation with settings regarding
 * Address Validation behaviour of the Address Correction Engine. 
 * The main settings at current include:
 * <li>PoolFilterSetting: A setting which determines which Addresses get loaded
 * into the Address Pool</li>
 * <li>AutoValidationSetting: A setting which determines which Addresses will
 * get automatically validated</li>
 */
public class AddressValidationSettingsPanel implements DataEntryPanel {

	/**
	 * The panel that gets generated.
	 */
	private JPanel panel;
	
	/**
	 * The {@link MungeSettings} this panel will load the current setting
	 * from and will save modifications to
	 */
	private MungeSettings settings;
	
	// Radio buttons for setting the PoolFilterSetting
	private JCheckBox valid;
	private JCheckBox differentFormat;
	private JCheckBox invalid;
	
	// Radio buttons for setting the AutoValidationSetting
	private JRadioButton avcNothingRB;
	private JRadioButton avcSerpCorrectableRB;
	private JRadioButton avcEverythingWithOneSuggestionRB;
	private JRadioButton avcEverythingWithSuggestionRB;
	
	/**
	 * @param mungeSettings The MungeSettings containing the particular
	 * {@link PoolFilterSetting} and {@link AutoValidateSetting} that this
	 * panel will be used to set.
	 */
	public AddressValidationSettingsPanel(MungeSettings mungeSettings) {
		this.settings = mungeSettings;
	}
	
	private void buildUI() {
		FormLayout validationSettingsLayout = new FormLayout("pref");
		DefaultFormBuilder dfb = new DefaultFormBuilder(validationSettingsLayout);
		dfb.append("Address Filter Setting");
		
		dfb.nextLine();
		valid = new JCheckBox("Include all valid addresses");
		if (settings.getPoolFilterSetting() == PoolFilterSetting.EVERYTHING ||
				settings.getPoolFilterSetting() == PoolFilterSetting.VALID_ONLY ||
				settings.getPoolFilterSetting() == PoolFilterSetting.VALID_OR_DIFFERENT_FORMAT ||
				settings.getPoolFilterSetting() == PoolFilterSetting.VALID_OR_INVALID) {
			valid.setSelected(true);
		}
		dfb.append(valid);
		
		dfb.nextLine();
		differentFormat = new JCheckBox("Include all valid addresses with different formatting");
		if (settings.getPoolFilterSetting() == PoolFilterSetting.EVERYTHING || 
				settings.getPoolFilterSetting() == PoolFilterSetting.INVALID_OR_DIFFERENT_FORMAT ||
				settings.getPoolFilterSetting() == PoolFilterSetting.VALID_OR_DIFFERENT_FORMAT ||
				settings.getPoolFilterSetting() == PoolFilterSetting.DIFFERENT_FORMAT_ONLY) {
			differentFormat.setSelected(true);
		}
		dfb.append(differentFormat);
		
		dfb.nextLine();
		invalid = new JCheckBox("Include all invalid addresses");
		if (settings.getPoolFilterSetting() == PoolFilterSetting.EVERYTHING ||
				settings.getPoolFilterSetting() == PoolFilterSetting.INVALID_OR_DIFFERENT_FORMAT ||
				settings.getPoolFilterSetting() == PoolFilterSetting.VALID_OR_INVALID ||
				settings.getPoolFilterSetting() == PoolFilterSetting.INVALID_ONLY) {
			invalid.setSelected(true);
		}
		dfb.append(invalid);
		
		
		dfb.nextLine();
		dfb.appendUnrelatedComponentsGapRow();
		dfb.nextLine();
		dfb.append("Auto-correction Setting");
		ButtonGroup autoValidateButtonGroup = new ButtonGroup();
		
		dfb.nextLine();
		avcNothingRB = new JRadioButton(AutoValidateSetting.NOTHING.getLongDescription());
		if (settings.getAutoValidateSetting() == AutoValidateSetting.NOTHING) {
			avcNothingRB.setSelected(true);
		}
		dfb.append(avcNothingRB);
		autoValidateButtonGroup.add(avcNothingRB);
		
		dfb.nextLine();
		avcSerpCorrectableRB = new JRadioButton(AutoValidateSetting.SERP_CORRECTABLE.getLongDescription());
		if (settings.getAutoValidateSetting() == AutoValidateSetting.SERP_CORRECTABLE) {
			avcSerpCorrectableRB.setSelected(true);
		}
		dfb.append(avcSerpCorrectableRB);
		autoValidateButtonGroup.add(avcSerpCorrectableRB);
		
		dfb.nextLine();
		avcEverythingWithOneSuggestionRB = new JRadioButton(AutoValidateSetting.EVERYTHING_WITH_ONE_SUGGESTION.getLongDescription());
		if (settings.getAutoValidateSetting() == AutoValidateSetting.EVERYTHING_WITH_ONE_SUGGESTION) {
			avcEverythingWithOneSuggestionRB.setSelected(true);
		}
		dfb.append(avcEverythingWithOneSuggestionRB);
		autoValidateButtonGroup.add(avcEverythingWithOneSuggestionRB);
		
		dfb.nextLine();
		avcEverythingWithSuggestionRB = new JRadioButton(AutoValidateSetting.EVERYTHING_WITH_SUGGESTION.getLongDescription());
		if (settings.getAutoValidateSetting() == AutoValidateSetting.EVERYTHING_WITH_SUGGESTION) {
			avcEverythingWithSuggestionRB.setSelected(true);
		}
		dfb.append(avcEverythingWithSuggestionRB);
		autoValidateButtonGroup.add(avcEverythingWithSuggestionRB);
		
		panel = dfb.getPanel();
	}
	
	public boolean applyChanges() {
		if (valid.isSelected() && differentFormat.isSelected() && invalid.isSelected()) {
			settings.setPoolFilterSetting(PoolFilterSetting.EVERYTHING);
		} else if (valid.isSelected() && differentFormat.isSelected() && !invalid.isSelected()) {
			settings.setPoolFilterSetting(PoolFilterSetting.VALID_OR_DIFFERENT_FORMAT);
		} else if (valid.isSelected() && !differentFormat.isSelected() && invalid.isSelected()) {
			settings.setPoolFilterSetting(PoolFilterSetting.VALID_OR_INVALID);
		} else if (valid.isSelected() && !differentFormat.isSelected() && !invalid.isSelected()) {
			settings.setPoolFilterSetting(PoolFilterSetting.VALID_ONLY); 
		} else if (!valid.isSelected() && differentFormat.isSelected() && invalid.isSelected()) {
			settings.setPoolFilterSetting(PoolFilterSetting.INVALID_OR_DIFFERENT_FORMAT);
		} else if (!valid.isSelected() && differentFormat.isSelected() && !invalid.isSelected()) {
			settings.setPoolFilterSetting(PoolFilterSetting.DIFFERENT_FORMAT_ONLY);
		} else if (!valid.isSelected() && !differentFormat.isSelected() && invalid.isSelected()) {
			settings.setPoolFilterSetting(PoolFilterSetting.INVALID_ONLY);
		} else if (!valid.isSelected() && !differentFormat.isSelected() && !invalid.isSelected()) {
			settings.setPoolFilterSetting(PoolFilterSetting.NOTHING);
		}
			
		
		if (avcNothingRB.isSelected()) {
			settings.setAutoValidateSetting(AutoValidateSetting.NOTHING);
		} else if (avcSerpCorrectableRB.isSelected()) {
			settings.setAutoValidateSetting(AutoValidateSetting.SERP_CORRECTABLE);
		} else if (avcEverythingWithOneSuggestionRB.isSelected()) {
			settings.setAutoValidateSetting(AutoValidateSetting.EVERYTHING_WITH_ONE_SUGGESTION);
		} else if (avcEverythingWithSuggestionRB.isSelected()) {
			settings.setAutoValidateSetting(AutoValidateSetting.EVERYTHING_WITH_SUGGESTION);
		}
		
		return true;
	}

	public void discardChanges() {
		// Do nothing
	}

	public JComponent getPanel() {
		if (panel == null) buildUI();
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return false;
	}
}
