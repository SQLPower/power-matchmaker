package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import ca.sqlpower.matchmaker.PlFolder;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
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

    public DeleteFolderDialog(PlFolder folder, JFrame parent) {
        this.folder = folder;
        this.parent = parent;
        buildUI();
        dialog.setVisible(true);
    }
    
    public void buildUI(){
        dialog = new JDialog(parent);
        FormLayout layout = new FormLayout("4dlu, pref, 4dlu, pref,4dlu", 
                "4dlu,pref,4dlu,pref,6dlu, pref,4dl,pref,4dlu,pref,4dlu");
                //1    2    3    4    5     6    7   8    9    10   11
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(layout);        
        deleteAll = new JRadioButton();        
        moveContent = new JRadioButton();
        moveTo = new JComboBox();        
        okButton = new JButton(okAction);
        cancelButton = new JButton(cancelAction);
        
        JLabel deleteTitle = new JLabel("Delete " + folder.getName());
        pb.add(deleteTitle, cc.xyw(2,2,4,"c,c"));
        group.add(deleteAll);
        group.add(moveContent);
        pb.add(deleteAll, cc.xy(2,4));
        pb.add(new JLabel("delete all content"), cc.xy(4,4));
        pb.add(moveContent, cc.xy(2,6));
        pb.add(new JLabel ("move to:"), cc.xy(4,6));
        pb.add(moveTo, cc.xy(4,8));
        
        
        ButtonBarBuilder bb = new ButtonBarBuilder();
        bb.addGridded(okButton);
        bb.addRelatedGap();
        bb.addGlue();
        bb.addGridded(cancelButton);
        bb.addRelatedGap();
        bb.addGlue();
        
        pb.add(bb.getPanel(), cc.xy(4,10));
        
        dialog.getContentPane().add(pb.getPanel());
    }
    
    private Action okAction = new AbstractAction("ok"){

        public void actionPerformed(ActionEvent e) {

        }
        
    };
    
    private Action cancelAction = new AbstractAction("cancel"){
        public void actionPerformed(ActionEvent e) {
            dialog.dispose();
        }        
    };
    
}
