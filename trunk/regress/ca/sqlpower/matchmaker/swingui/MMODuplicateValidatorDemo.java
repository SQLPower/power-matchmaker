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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.StubMatchMakerObject;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A simple GUI for testing out MMODuplicate Validator.  There is one single 
 * StubMatchMakerObject that is a parent.  By typing some text in and hitting create
 * it adds a StubMatchMakerObject to that parent with only the name property and is
 * shown in the JTextArea.  Since it is just a test, only adding is allowed, remove
 * has not been implemented yet.  The status shows warning message if the field is 
 * empty or null and fails if the field is a duplicate name with an existing 
 * StubMatchMakerObject.
 * 
 * see @link {@link MMODuplicateValidator}
 *
 */
public class MMODuplicateValidatorDemo {

    private JDialog d;
    private JComboBox items;
    private JTextField field;
    private List<MatchMakerObject> children;
    private JButton create;
    private StubMatchMakerObject obj;
    private StatusComponent statusLabel;
    private FormValidationHandler form;
    private JTextArea area;
    private JButton exit;

    private MMODuplicateValidatorDemo() {
        obj = new StubMatchMakerObject("Parent");
        obj.setAllowChildren(true);
        buildUI();
        form.addPropertyChangeListener(new PropertyChangeListener() {        
            public void propertyChange(PropertyChangeEvent evt) {
                create.setEnabled(true);
                if (form.getWorstValidationStatus().getStatus() != Status.OK){
                    create.setEnabled(false);
                }
            }        
        });
        d.pack();
        d.setVisible(true);        
    }
        
    
    private void buildUI(){
        statusLabel = new StatusComponent();
        form = new FormValidationHandler(statusLabel);
        field = new JTextField();
        d = new JDialog();
        items = new JComboBox();
        area = new JTextArea();
        area.setEditable(false);
        create = new JButton (new AbstractAction("Create"){

            public void actionPerformed(ActionEvent e) {
                StubMatchMakerObject child = new StubMatchMakerObject(field.getText());
                child.setAllowChildren(false);
                obj.addChild(child);
                field.setText("");
                refreshTextArea();
            }
            
        });
        
        exit = new JButton(new AbstractAction("Exit"){

            public void actionPerformed(ActionEvent e) {
                d.setVisible(false);
                d.dispose();
            }
            
        });
        
        FormLayout layout = new FormLayout(
                "4dlu,pref,4dlu,fill:50dlu:grow,4dlu,pref,4dlu"
                ,"4dlu,pref,4dlu,pref,4dlu,fill:80dlu:grow, 4dlu, pref,4dlu");     
        PanelBuilder pb = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        pb.add(statusLabel, cc.xyw(2,2,6));
        pb.add(new JLabel("Match Name"), cc.xy(2,4));
        pb.add(field, cc.xy(4,4));
        pb.add(create, cc.xy(6,4));
        pb.add(new JScrollPane(area), cc.xyw(2,6,5));
        pb.add(exit, cc.xy(6,8));
        
        Validator v1 = new MMODuplicateValidator(obj, null, "Cannot have Duplicate Name");
        form.addValidateObject(field, v1);
        d.getContentPane().add(pb.getPanel());
        
    }
    
    private void refreshTextArea(){
        StringBuffer text = new StringBuffer();
        boolean first = true;
        for (StubMatchMakerObject o : (List<StubMatchMakerObject>)obj.getChildren()){
            if(!first){
                text.append("\n");
            } 
            text.append(o.getName());
            first = false;
        }
        area.setText(text.toString());
    }
    
    public static void main(String[] args) {      
        new MMODuplicateValidatorDemo();
    }
        
}
