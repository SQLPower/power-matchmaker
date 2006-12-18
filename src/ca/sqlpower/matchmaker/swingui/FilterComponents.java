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

import com.jgoodies.forms.layout.CellConstraints;



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
        CellConstraints cc = new CellConstraints();		
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