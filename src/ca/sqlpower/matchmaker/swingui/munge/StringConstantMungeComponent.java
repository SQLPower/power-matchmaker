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

package ca.sqlpower.matchmaker.swingui.munge;

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
        
        final JTextField valueField = new JTextField(getStepParameter(StringConstantMungeStep.VALUE_PARAMETER_NAME, ""));
        valueField.getDocument().addDocumentListener(new DocumentListener() {

            void change() {
                getStep().setParameter(StringConstantMungeStep.VALUE_PARAMETER_NAME, valueField.getText());
            }
            
            public void changedUpdate(DocumentEvent e) { change(); }
            public void insertUpdate(DocumentEvent e) { change(); }
            public void removeUpdate(DocumentEvent e) { change(); }
        });
        
        FormLayout layout = new FormLayout("pref,4dlu,pref:grow");
        DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        fb.append("Value:", valueField);
        
        return fb.getPanel();
    }

}
