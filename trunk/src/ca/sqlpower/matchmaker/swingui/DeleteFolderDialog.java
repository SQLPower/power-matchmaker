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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.PlFolder;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DeleteFolderDialog {
    
    private PlFolder folder;
    private JDialog dialog;
    private JFrame parent;
    private JRadioButton deleteAll;
    private JRadioButton moveContent;
    private ButtonGroup group;
    private JComboBox moveTo;
    private JButton okButton;
    private JButton cancelButton;

    private MatchMakerSwingSession session;
    
    public DeleteFolderDialog(PlFolder folder, JFrame parent, MatchMakerSwingSession session) {
        this.folder = folder;
        this.parent = parent;
        this.session = session;
        buildUI();
    }
    
    public void buildUI() {
        FormLayout layout = new FormLayout("4dlu, pref, 4dlu", 
                "4dlu,pref,4dlu,pref,4dlu, pref,4dlu,pref,4dlu");
                //1    2    3    4    5     6    7   8    9 
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(layout);        
        deleteAll = new JRadioButton("Delete all content");        
        moveContent = new JRadioButton("Move to:");
        // default to move the contents
        moveContent.setSelected(true);
        moveTo = new JComboBox();
        List<PlFolder> folders = new ArrayList<PlFolder>();
        folders.addAll(session.getCurrentFolderParent().getChildren());
        folders.remove(folder);
        moveTo.setModel(new DefaultComboBoxModel(folders.toArray()));
        moveTo.setRenderer(new MatchMakerObjectComboBoxCellRenderer());
        okButton = new JButton(okAction);
        cancelButton = new JButton(cancelAction);
        
        group = new ButtonGroup();
        group.add(deleteAll);
        group.add(moveContent);
        pb.add(deleteAll, cc.xy(2,2));
        pb.add(moveContent, cc.xy(2,4));
        pb.add(moveTo, cc.xy(2,6));
        
        pb.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton), cc.xy(2,8));
        
        dialog = new JDialog(parent,"Delete " + folder.getName());
        pb.setBorder(new EmptyBorder(10,10,10,10));
        dialog.setContentPane(pb.getPanel());
        dialog.pack();
        dialog.setVisible(true);
    }
    
    private Action okAction = new AbstractAction("Ok"){

        public void actionPerformed(ActionEvent e) {
        	if ( moveContent.getModel().isSelected()) {
        		PlFolder newFolder = (PlFolder) moveTo.getSelectedItem();
        		while (folder.getChildCount() >0) {
        			Match m = (Match) folder.getChildren().get(0);
        			session.move(m, newFolder);
        		}
        		session.getCurrentFolderParent().deleteAndRemoveChild(folder);
        	} else if ( deleteAll.getModel().isSelected()){
        		session.getCurrentFolderParent().deleteAndRemoveChild(folder);
        	}
        	dialog.dispose();
        }
        
    };
    
    private Action cancelAction = new AbstractAction("Cancel"){
        public void actionPerformed(ActionEvent e) {
            dialog.dispose();
        }        
    };
    
}
