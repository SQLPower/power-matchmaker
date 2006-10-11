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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLTable;



public class FilterComponentsPanel extends JPanel {
    
    JTextArea filterTextArea;
    JButton editButton;
    SQLTable table;
    
    public FilterComponentsPanel(SQLTable t){
    	table = t;
        setLayout(new BorderLayout());
        filterTextArea = new JTextArea();
        editButton = new JButton(new AbstractAction("Edit"){

            public void actionPerformed(ActionEvent e) {     
            	Container parent = FilterComponentsPanel.this;
            	FilterMakerFrame filterMaker = null;
            	while(parent != null  ){
            		parent = parent.getParent();
            		try {
            			if (parent instanceof JFrame){
            				filterMaker = new FilterMakerFrame((JFrame)parent,getFilterTextArea(),getTable());
            				break;
            			} else if (parent instanceof JDialog) {
            				filterMaker = new FilterMakerFrame((JDialog)parent,getFilterTextArea(),getTable());
            				break;
            			}
            		} catch (ArchitectException ex) {
						throw new ArchitectRuntimeException(ex);
					}
            	}
            	if (filterMaker != null) {
            		filterMaker.pack();
            		filterMaker.setVisible(true);
            	} else {
            		throw new IllegalStateException("This panel is not in a window");
            	}
            	
            }            
        });
 
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
