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

import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLTable;



public class FilterComponents{

	private static Logger logger = Logger.getLogger(FilterComponents.class);

    private JTextArea filterTextArea;
    private JButton editButton;
    private SQLTable table;
    private Container parent;

    public FilterComponents(Container parent){
        this(null, parent);
    }

    public FilterComponents(SQLTable t, Container parent){
    	table = t;
        this.parent= parent;
        buildUI();
    }

	private void buildUI() {
        filterTextArea = new JTextArea();
        editButton = new JButton(new AbstractAction("Edit"){

            public void actionPerformed(ActionEvent e) {
                FilterMakerDialog filterMaker = null;
            	// We set the getfilterTextArea to be uneditable so we don't run
            	// into conflicts with the filterMaker, the filterMaker will
            	// reenable its editability automatically
            	if (parent instanceof JFrame){
            		//Last parameter is false because we want a JComboBox for the last input component
            		filterMaker = new FilterMakerDialog((JFrame)parent,getFilterTextArea(),getTable(),false);
            		getFilterTextArea().setEditable(false);

            	} else if (parent instanceof JDialog) {
            		//Last parameter is false because we want a JComboBox for the last input component
            		filterMaker = new FilterMakerDialog((JDialog)parent,getFilterTextArea(),getTable(), false);
            		getFilterTextArea().setEditable(false);
            	}

            	if (filterMaker == null) {
            		logger.info("Warning: parent neither JFrame nor JDialog");
            		throw new IllegalStateException("This panel is not in a window");
            	}
            	filterMaker.pack();
            	filterMaker.setVisible(true);
            }
        });

        filterTextArea.setWrapStyleWord(true);
        filterTextArea.setLineWrap(true);
	}

    /*
     * Returns the text input in the filter textfield.
     */
    public JTextArea getFilterTextArea(){
        return filterTextArea;
    }

	public SQLTable getTable() {
		return table;
	}

	public void setTable(SQLTable table) {
		if (this.table != table) {
			this.table = table;
		}
	}

	public void setFilterTextArea(JTextArea filterTextArea) {
		if (this.filterTextArea != filterTextArea) {
			this.filterTextArea = filterTextArea;
		}
	}

    public JButton getEditButton(){
        return editButton;
    }

}