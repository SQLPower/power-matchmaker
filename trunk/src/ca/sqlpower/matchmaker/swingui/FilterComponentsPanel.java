package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;



public class FilterComponentsPanel extends JPanel {
    
    JTextArea filterTextArea;
    JButton editButton;
    
    public FilterComponentsPanel(){
        setLayout(new BorderLayout());        
        filterTextArea = new JTextArea();
        editButton = new JButton(new AbstractAction("Edit"){

            public void actionPerformed(ActionEvent e) {     
                FilterMakerFrame filterMaker = new FilterMakerFrame(FilterComponentsPanel.this, filterTextArea);
                filterMaker.pack();
                filterMaker.setVisible(true);
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
}
