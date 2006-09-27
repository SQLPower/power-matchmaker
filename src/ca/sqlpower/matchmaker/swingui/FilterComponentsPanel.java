package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;



public class FilterComponentsPanel extends JPanel {

    JLabel filterLabel;
    JTextArea filterTextField;
    JButton editButton;
    
    public FilterComponentsPanel(){
        setLayout(new BorderLayout());
        filterLabel = new JLabel("Filter");
        filterTextField = new JTextArea();
        editButton = new JButton(new AbstractAction("Edit"){

            public void actionPerformed(ActionEvent e) {     
                FilterMakerFrame filterMaker = new FilterMakerFrame(FilterComponentsPanel.this);
                filterMaker.pack();
                filterMaker.setVisible(true);
            }            
        });
 
        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.add(filterLabel, BorderLayout.CENTER);
        westPanel.add(new JLabel ("  "), BorderLayout.EAST);
        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(editButton, BorderLayout.CENTER);
        eastPanel.add(new JLabel ("  "), BorderLayout.WEST);
        add(westPanel, BorderLayout.WEST);
        add(filterTextField, BorderLayout.CENTER);
        add(eastPanel, BorderLayout.EAST);
    }
    
    /*
     * Returns the text input in the filter textfield.
     */
    public JTextArea getFilterTextField(){
        return filterTextField;
    }
}
