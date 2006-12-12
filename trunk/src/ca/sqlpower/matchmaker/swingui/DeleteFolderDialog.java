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
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import ca.sqlpower.matchmaker.Match;
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

    private MatchMakerSwingSession session;
    
    public DeleteFolderDialog(PlFolder folder, JFrame parent, MatchMakerSwingSession session) {
        this.folder = folder;
        this.parent = parent;
        this.session = session;
        buildUI();
    }
    
    public void buildUI(){
        dialog = new JDialog(parent);
        FormLayout layout = new FormLayout("4dlu, pref, 4dlu", 
                "4dlu,pref,4dlu,pref,4dlu, pref,4dlu,pref,4dlu,pref,4dlu");
                //1    2    3    4    5     6    7   8    9    10   11
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
        
        JLabel deleteTitle = new JLabel("Delete " + folder.getName());
        pb.add(deleteTitle, cc.xy(2,2,"c,c"));
        group = new ButtonGroup();
        group.add(deleteAll);
        group.add(moveContent);
        pb.add(deleteAll, cc.xy(2,4));
        pb.add(moveContent, cc.xy(2,6));
        pb.add(moveTo, cc.xy(2,8));
        
        
        ButtonBarBuilder bb = new ButtonBarBuilder();
        bb.addGridded(okButton);
        bb.addRelatedGap();
        bb.addGlue();
        bb.addGridded(cancelButton);
        bb.addRelatedGap();
        bb.addGlue();
        
        pb.add(bb.getPanel(), cc.xy(2,10));
        
        dialog.setContentPane(pb.getPanel());
        dialog.pack();
        dialog.setVisible(true);
    }
    
    private Action okAction = new AbstractAction("ok"){

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
    
    private Action cancelAction = new AbstractAction("cancel"){
        public void actionPerformed(ActionEvent e) {
            dialog.dispose();
        }        
    };
    
}
