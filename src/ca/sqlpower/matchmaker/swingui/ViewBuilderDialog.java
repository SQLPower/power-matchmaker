package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ViewBuilderDialog extends JDialog {
    
    private static Logger logger = Logger.getLogger(ViewBuilderDialog.class);

    private final MatchMakerSwingSession swingSession;
    private SQLTable viewTable;
    
    private JTextField viewNameField;
    private JComboBox fromClauseDropdown;
    private JTextArea viewTextArea;
    private JButton viewPasteButton;
    private JButton viewRemoveButton;
    
    private JComboBox selectTableDropdown;
    private JComboBox selectColumnDropdown;
    private JButton selectPasteButton;
    private JTextArea selectTextArea;
    
    private JComboBox whereFirstTableDropdown;
    private JComboBox whereFirstColumnDropdown;
    private JComboBox whereSecondTableDropdown;
    private JComboBox whereSecondColumnDropdown;
    private JButton wherePasteButton;   
    private JTextArea whereTextArea;

    private JButton testButton;
    private JButton viewButton;
    private JButton cancelButton;
    private JButton okButton;

    //TODO: the UI has been built but the functionality has not been done yet
    public ViewBuilderDialog(MatchMakerSwingSession swingSession, JFrame parent, SQLTable viewTable) throws ArchitectException{
        super(parent);
        this.swingSession = swingSession;
        this.viewTable = viewTable;
        setTitle("View Builder");
        buildUI();
        viewNameField.setText(viewTable.getName());
        for (SQLTable t : (List<SQLTable>) (swingSession.getPlRepositoryDatabase().getTables())) {
            fromClauseDropdown.addItem(t);
        }
    }
    
    public void buildUI() {
        
        FormLayout layout = new FormLayout(
                "4dlu,pref,4dlu,pref,8dlu,fill:min(170dlu;default):grow," +
                // 1    2     3     4   5        6                              
                "4dlu,fill:min(170dlu;default):grow,2dlu,pref,4dlu",
                // 7            8                    9    10   11
                
                "4dlu, pref, 4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,fill:70dlu:grow," +                
                //1     2     3    4   5     6    7    8    9    10   11     12
                "4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu, " +
                //13   14    15    16  17  18   19   20   21   22   23
                "fill:70dlu:grow,4dlu,pref,4dlu,pref,4dlu");
                //24               25  26    27 28     29
        
        layout.setRowGroups(new int[][]{{10,26},{12,24}});
                
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb;
        JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(layout) : new JPanel(layout);

        pb = new PanelBuilder(layout,p);
                
        viewNameField = new JTextField();
        fromClauseDropdown = new JComboBox();
        viewTextArea = new JTextArea();
        viewPasteButton = new JButton(viewPasteAction);
        viewRemoveButton = new JButton(viewRemoveAction);
        
        
        selectTableDropdown = new JComboBox();
        selectColumnDropdown = new JComboBox();
        selectTextArea = new JTextArea();
        selectPasteButton = new JButton(selectPasteAction);      
        
        whereFirstTableDropdown = new JComboBox();
        whereFirstColumnDropdown = new JComboBox();
        whereSecondTableDropdown = new JComboBox();
        whereSecondColumnDropdown = new JComboBox();
        whereTextArea = new JTextArea();
        wherePasteButton = new JButton(wherePasteAction);
        testButton = new JButton(testAction);
        viewButton = new JButton (viewAction);
        cancelButton = new JButton (cancelAction);
        okButton = new JButton (okAction);

        JLabel viewLabel = new JLabel("View Table:");        
        pb.add(viewLabel, cc.xy(2,2));
        pb.add(viewNameField, cc.xyw(2,4,2));
        JLabel fromClauseLabel = new JLabel("From Clause:");
        pb.add(fromClauseLabel, cc.xy(2,6));
        pb.add(fromClauseDropdown, cc.xyw(2,8,2));
        
        ButtonBarBuilder bb = new ButtonBarBuilder();
        bb.addGridded(viewPasteButton);
        bb.addRelatedGap();
        bb.addGridded(viewRemoveButton);
        bb.addRelatedGap();
        pb.add(bb.getPanel(), cc.xyw(2,10,2));
        JTextAreaUndoWrapper viewUndoTextArea = new JTextAreaUndoWrapper(viewTextArea);
              
        pb.add(viewUndoTextArea, cc.xy(2,12,"f,f"));      
        
        
        JLabel selectLabel = new JLabel("Select Clause:");        
        pb.add(selectLabel, cc.xy(6,4));
        JLabel selectTableLabel = new JLabel("Table Name:");
        pb.add(selectTableLabel, cc.xy(6,6,"l,c"));
        JLabel selectColumnLabel = new JLabel("Column Name:");
        pb.add(selectColumnLabel, cc.xy(8,6,"l,c"));
        pb.add(selectTableDropdown, cc.xy(6,8));
        pb.add(selectColumnDropdown, cc.xy(8,8));
        
        ButtonStackBuilder selectBSB = new ButtonStackBuilder();
        selectBSB.addGridded(selectPasteButton);
        selectBSB.addRelatedGap();
        pb.add(selectBSB.getPanel(), cc.xywh(10,8,1,2));
        JTextAreaUndoWrapper selectUndoTextArea = new JTextAreaUndoWrapper(selectTextArea);
        pb.add(selectUndoTextArea, cc.xywh(6,10,5,3,"f,f"));      
        
        JLabel whereClauseLabel = new JLabel("Where Clause");
        pb.add(whereClauseLabel, cc.xy(6,14,"l,c"));        
        pb.add(new JLabel("Table Name1:"), cc.xy(6,16));
        pb.add(new JLabel("Column Name1:"), cc.xy(8,16));
        pb.add(whereFirstTableDropdown, cc.xy(6,18));
        pb.add(whereFirstColumnDropdown, cc.xy(8,18));
        pb.add(new JLabel("Table Name2:"), cc.xy(6,20));
        pb.add(new JLabel("Column Name2:"), cc.xy(8,20));
        pb.add(whereSecondTableDropdown, cc.xy(6,22));
        pb.add(whereSecondColumnDropdown, cc.xy(8,22));
        pb.add(wherePasteButton, cc.xy(10,22));
        JTextAreaUndoWrapper whereUndoTextArea = new JTextAreaUndoWrapper(whereTextArea);
        pb.add(whereUndoTextArea, cc.xywh(6,24,5,2,"f,f"));
           
        ButtonBarBuilder bb1 = new ButtonBarBuilder();
        bb1.addGridded(testButton);
        bb1.addUnrelatedGap();
        bb1.addGridded(viewButton);
        bb1.addUnrelatedGap();
        bb1.addGlue();
        bb1.addGridded(okButton);
        bb1.addRelatedGap();
        bb1.addGridded(cancelButton);
        bb1.addRelatedGap();
        pb.add(bb1.getPanel(), cc.xyw(2,28,10,"f,f"));
        getContentPane().add(pb.getPanel());        
    }


    ////////////////Button Actions////////////
    
    Action viewPasteAction = new AbstractAction("Paste"){

        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            
        }
        
    };
    
    Action selectPasteAction = new AbstractAction("Paste"){

        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            
        }
        
    };
    Action wherePasteAction = new AbstractAction("Paste"){

        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            
        }
        
    };
    Action viewRemoveAction = new AbstractAction("Remove"){
        public void actionPerformed(ActionEvent e) {    
            viewTextArea.setText("");
        }       
    };
    
    Action viewAction = new AbstractAction("View"){

        public void actionPerformed(ActionEvent e) {          
        }
        
    };
    Action testAction = new AbstractAction("Test"){

        public void actionPerformed(ActionEvent e) {          
        }
        
    };
    Action cancelAction = new AbstractAction("Cancel"){

        public void actionPerformed(ActionEvent e) {
        }
        
    };
    Action okAction = new AbstractAction("OK"){

        public void actionPerformed(ActionEvent e) {                  
        }
        
    };

    
    
}
