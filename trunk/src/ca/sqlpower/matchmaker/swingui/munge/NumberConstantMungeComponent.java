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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.event.ActionEvent;
import java.math.BigDecimal;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.NumberConstantMungeStep;
import ca.sqlpower.matchmaker.munge.StringConstantMungeStep;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class NumberConstantMungeComponent extends AbstractMungeComponent {
	private JTextField valueField;
	private JCheckBox retNull;
	
    public NumberConstantMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
        super(step, handler, session);
        getHandler().addValidateObject(valueField, new NumberValidator());
        getHandler().resetHasValidated();
    }

    @Override
    protected JPanel buildUI() {
    	valueField = new JTextField(getStepParameter(NumberConstantMungeStep.VALUE_PARAMETER_NAME, ""));
    	
        valueField.getDocument().addDocumentListener(new DocumentListener() {     
            void change() {
                getStep().setParameter(NumberConstantMungeStep.VALUE_PARAMETER_NAME, valueField.getText());
            }
            
            public void changedUpdate(DocumentEvent e) { change(); }
            public void insertUpdate(DocumentEvent e) { change(); }
            public void removeUpdate(DocumentEvent e) { change(); }
        });
        
        retNull = new JCheckBox("Return null");
        retNull.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				boolean b = retNull.isSelected();
				valueField.setEnabled(!b);
				getStep().setParameter(StringConstantMungeStep.RETURN_NULL, String.valueOf(b));
			}
        });
        
        retNull.setSelected(new Boolean(getStep().getParameter(StringConstantMungeStep.RETURN_NULL)).booleanValue());
        valueField.setEnabled(!retNull.isSelected());
        
        FormLayout layout = new FormLayout("pref,4dlu,pref:grow");
        DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        fb.append("Value:", valueField);
        fb.append("",retNull);
        
        return fb.getPanel();
    }
    
    private class NumberValidator implements Validator {
		public ValidateResult validate(Object contents) {
			if (retNull.isSelected()) {
				return ValidateResult.createValidateResult(Status.OK, "");
			}
			try {
				new BigDecimal(valueField.getText());
				return ValidateResult.createValidateResult(Status.OK, "");
			} catch (NumberFormatException e) {
				return ValidateResult.createValidateResult(Status.FAIL, valueField.getText()  + " is an invalid number");
			}
		}
    }
}
