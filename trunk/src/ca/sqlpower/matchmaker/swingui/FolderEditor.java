package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel.MMTreeNode;
import ca.sqlpower.validation.AlwaysOKValidator;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FolderEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(MatchEditor.class);
	private JPanel panel;
	private final MatchMakerSwingSession swingSession;
	private PlFolder<Match> folder;

	StatusComponent status = new StatusComponent();
	private FormValidationHandler handler;

	private JTextField folderName = new JTextField(40);
	private JTextArea folderDesc = new JTextArea(4,40);

	public FolderEditor(MatchMakerSwingSession swingSession, PlFolder<Match> folder) {
		this.swingSession = swingSession;
		this.folder = folder;
		handler = new FormValidationHandler(status);
		buildUI();
		setDefaultSelection();
		handler.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				refreshActionStatus();
			}
        });
		handler.setValidated(false);
	}

	private void buildUI() {
		folderName.setName("Folder Name");
		folderDesc.setName("Folder Description");

    	JButton saveButton = new JButton(saveAction);

    	FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,10dlu"); // rows
    	//		 1     2    3    4    5    6    7    8    9    10      11    12   13   14  15   16       17    18     19  20    21   22    23   24    25

		PanelBuilder pb;

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		pb.add(status, cc.xy(4,2,"r,c"));
		pb.add(new JLabel("Folder Name:"), cc.xy(2,4,"r,c"));
		pb.add(new JLabel("Description:"), cc.xy(2,6,"r,c"));

		pb.add(folderName, cc.xy(4,4,"l,c"));
		pb.add(new JScrollPane(folderDesc), cc.xy(4,6,"l,c"));

		pb.add(saveButton, cc.xyw(2,8,3,"c,c"));
		panel = pb.getPanel();

    }

	private void setDefaultSelection() {
		folderName.setText(folder.getName());
		folderDesc.setText(folder.getFolderDesc());

		Validator v1 = new FolderNameValidator(swingSession);
        handler.addValidateObject(folderName,v1);

        Validator v2 = new AlwaysOKValidator();
        handler.addValidateObject(folderDesc,v2);
	}

	private void refreshActionStatus() {
		ValidateResult worst = handler.getWorstValidationStatus();
    	saveAction.setEnabled(true);

    	if ( worst.getStatus() == Status.FAIL ) {
    		saveAction.setEnabled(false);
    	}
	}

	/**
     * Saves the folder
     */
	private Action saveAction = new AbstractAction("Save") {

		public void actionPerformed(final ActionEvent e) {

			List<String> fail = handler.getFailResults();
	    	List<String> warn = handler.getWarnResults();

	    	if ( fail.size() > 0 ) {
	    		StringBuffer failMessage = new StringBuffer();
	    		for ( String f : fail ) {
	    			failMessage.append(f).append("\n");
	    		}
	    		JOptionPane.showMessageDialog(swingSession.getFrame(),
	    				"You have to fix these errors before saving:\n"+failMessage.toString(),
	    				"Folder error",
	    				JOptionPane.ERROR_MESSAGE);
	    		return;
	    	} else if ( warn.size() > 0 ) {
	    		StringBuffer warnMessage = new StringBuffer();
	    		for ( String w : warn ) {
	    			warnMessage.append(w).append("\n");
	    		}
	    		JOptionPane.showMessageDialog(swingSession.getFrame(),
	    				"Warning: match will be saved with these warnings:\n"+warnMessage.toString(),
	    				"Folder warning",
	    				JOptionPane.INFORMATION_MESSAGE);
	    	}

	        if ( !folderName.getText().equals(folder.getName()) ) {
	        	if ( swingSession.findFolder(folderName.getText()) != null ) {
	        		JOptionPane.showMessageDialog(getPanel(),
	        				"Folder name \""+folderName.getText()+
	        				"\" exist or invalid. The folder can not be saved",
	        				"Folder name invalid",
	        				JOptionPane.ERROR_MESSAGE);
	        		return;
	        	}
	        	folder.setName(folderName.getText());
	        }
	        folder.setFolderDesc(folderDesc.getText());
	        logger.debug("Saving folder:" + folder.getName());

	        if ( !swingSession.getFolders().contains(folder) ) {
	        	MMTreeNode parent = (MMTreeNode) ((MatchMakerTreeModel)swingSession.getTree().getModel()).getRoot();
	        	MatchMakerTreeModel treeModel = (MatchMakerTreeModel)swingSession.getTree().getModel();
	        	if (treeModel.getIndexOfChild(parent, folder) == -1){
	        		swingSession.getFolders().add(folder);
	        		treeModel.addFolderToCurrent(folder);
	        	}
	        }

	        PlFolderDAO dao = (PlFolderDAO)swingSession.getDAO(PlFolder.class);
	        dao.save(folder);
	        handler.setValidated(false);
		}
	};

	public boolean doSave() {
		saveAction.actionPerformed(null);
		return true;
	}

	private class FolderNameValidator implements Validator {

		private MatchMakerSwingSession session;

		public FolderNameValidator(MatchMakerSwingSession session) {
    		this.session = session;
		}

		public ValidateResult validate(Object contents) {

			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Folder name is required");
			} else if ( !value.equals(folder.getName()) &&
						session.findFolder(value) != null ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Folder name is invalid or already exists.");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }

	public JPanel getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return handler.isValidated();
	}


}
