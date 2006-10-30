package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLTable;



public class FilterComponentsPanel extends JPanel {

	private static Logger logger = Logger.getLogger(FilterComponentsPanel.class);

    JTextArea filterTextArea;
    JButton editButton;
    SQLTable table;


    public FilterComponentsPanel(){
        buildUI();
    }

    public FilterComponentsPanel(SQLTable t){
    	table = t;
        buildUI();
    }

	private void buildUI() {
		setLayout(new BorderLayout());
        filterTextArea = new JTextArea();
        editButton = new JButton(new AbstractAction("Edit"){

            public void actionPerformed(ActionEvent e) {
            	Container parent = SwingUtilities.getWindowAncestor(FilterComponentsPanel.this);
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
        add(new JScrollPane(filterTextArea), BorderLayout.CENTER);
        add(editButton, BorderLayout.EAST);
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
			firePropertyChange("this.table", this.table, table);
			this.table = table;
		}
	}

	public void setFilterTextArea(JTextArea filterTextArea) {
		if (this.filterTextArea != filterTextArea) {
			firePropertyChange("this.filterTextArea", this.filterTextArea, filterTextArea);
			this.filterTextArea = filterTextArea;
		}
	}
}