package ca.sqlpower.matchmaker.swingui;

import java.awt.CardLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.sqlpower.architect.SQLTable;

public class ViewBuilderWizard extends JFrame {
    
    private static final int FIRSTPANELNUMBER = 1;
    private static final int LASTPANELNUMBER = 5;
    
    private int currentPanelNumber;
    private List<SQLTable> tableList;
    private JButton nextButton;
    private JButton backButton;
    private JPanel contentPanel;
    
    public ViewBuilderWizard(){
        //set the first panel count to be 1, the starting point
        currentPanelNumber = 1;
        LayoutManager cl = new CardLayout();
        setLayout(cl);
        nextButton = new JButton(nextButtonAction);        
        backButton = new JButton(backButtonAction);
    }

    private Action nextButtonAction = new AbstractAction(){

        public void actionPerformed(ActionEvent e) {
            switch(currentPanelNumber){
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5: 
                break;
            default:
                break;            
            }
            refreshButtonStatus();
        }
        
    };
    
    private Action backButtonAction = new AbstractAction(){

        public void actionPerformed(ActionEvent e) {
            switch(currentPanelNumber){
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5: 
                break;
            default:
                break;            
            }
            refreshButtonStatus();
        }
        
    };
    
    private void refreshButtonStatus(){
        if(currentPanelNumber == FIRSTPANELNUMBER){
            backButton.setEnabled(false);
        } else if (currentPanelNumber == LASTPANELNUMBER){
            nextButton.setText("Finish");
        } else if (currentPanelNumber > LASTPANELNUMBER || 
                    currentPanelNumber < FIRSTPANELNUMBER){
            throw new IllegalArgumentException("Invalid Panel Number! " +
                    "There is no step " + currentPanelNumber + " in this wizard");
        }
    }

    
    
}
