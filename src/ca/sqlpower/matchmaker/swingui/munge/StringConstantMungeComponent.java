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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.StringConstantMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class StringConstantMungeComponent extends AbstractMungeComponent {

    public StringConstantMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
        super(step, handler, session);
    }

    @Override
    protected JPanel buildUI() {
        StringConstantMungeStep step = ((StringConstantMungeStep)getStep());
        final JTextField valueField = new JTextField(step.getOutValue());
        valueField.getDocument().addDocumentListener(new DocumentListener() {

            void change() {
            	((StringConstantMungeStep)getStep()).setOutValue(valueField.getText());
            }
            
            public void changedUpdate(DocumentEvent e) { change(); }
            public void insertUpdate(DocumentEvent e) { change(); }
            public void removeUpdate(DocumentEvent e) { change(); }
        });
        
        final JCheckBox retNull = new JCheckBox("Return null");
        retNull.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				boolean b = retNull.isSelected();
				valueField.setEnabled(!b);
				((StringConstantMungeStep)getStep()).setReturnNull(b);
			}
        });
        
        retNull.setSelected(Boolean.valueOf(((StringConstantMungeStep)getStep()).isReturnNull()));
        valueField.setEnabled(!retNull.isSelected());
        
        FormLayout layout = new FormLayout("pref,4dlu,pref:grow");
        DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        fb.append("Value:", valueField);
        fb.append("",retNull);
        
        return fb.getPanel();
    }

}
