package ca.sqlpower.matchmaker.swingui.action;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.swingui.MatchMakerObjectComboBoxCellRenderer;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.validation.MatchNameValidator;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

public class DuplicateMatchAction extends AbstractAction {
	
	private static final Logger logger = Logger.getLogger(DuplicateMatchAction.class);
	StatusComponent status = new StatusComponent();

	private MatchMakerSwingSession swingSession;
	private Match match;
	private Action okAction;
	private Action cancelAction;
	private JComboBox folderComboBox;
	private FormValidationHandler handler;
	
	public DuplicateMatchAction(MatchMakerSwingSession swingSession, Match match) {
		super("Duplicate");
		this.swingSession = swingSession;
		this.match = match;
		handler = new FormValidationHandler(status);
	}
	
	private class DuplicatePanel implements DataEntryPanel {

		
		private final JPanel panel;
		private JTextField targetNameField;

		public DuplicatePanel(String newName, JComboBox folderComboBox) {
			panel = new JPanel(new GridLayout(5,1));
			panel.add(status);
			targetNameField = new JTextField(newName,60);
			panel.add(targetNameField);
			panel.add(new JLabel(""));
			panel.add(folderComboBox);
		}
		
		public boolean applyChanges() {
			return true;
		}

		public void discardChanges() {
		}

		public JComponent getPanel() {
			return panel;
		}
		public String getDupName() {
			return targetNameField.getText();
		}

		public JTextField getMatchNameField() {
			return targetNameField;
		}
	}
	public void actionPerformed(ActionEvent e) {

		String newName = null;
		for ( int count=0; ; count++) {
			newName = match.getName() +
								"_DUP" +
								(count==0?"":String.valueOf(count));
			if ( swingSession.isThisMatchNameAcceptable(newName) )
				break;
		}
		final JDialog dialog;

		final List<PlFolder> folders = swingSession.getCurrentFolderParent().getChildren();
		folderComboBox = new JComboBox(new DefaultComboBoxModel(folders.toArray()));
		folderComboBox.setRenderer(new MatchMakerObjectComboBoxCellRenderer());
		folderComboBox.setSelectedItem(match.getParent());
		
		final DuplicatePanel archPanel = new DuplicatePanel(newName,folderComboBox);
        
		okAction = new AbstractAction("OK") {
			public void actionPerformed(ActionEvent e) {
				String newName = archPanel.getDupName();
				PlFolder<Match> folder = (PlFolder<Match>) folderComboBox
				.getSelectedItem();
				Match newmatch = match.duplicate(folder,swingSession);
				newmatch.setName(newName);
				folder.addChild(newmatch);
				swingSession.save(newmatch);
			
			}};
			
		cancelAction = new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
			}};
			
		dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(archPanel,
				swingSession.getFrame(),
				"Duplicate Match",
				"OK",
				okAction,
				cancelAction);
		
		dialog.pack();
		dialog.setLocationRelativeTo(swingSession.getFrame());
		dialog.setVisible(true);
		
		Validator v = new MatchNameValidator(swingSession,new Match());
        handler.addValidateObject(archPanel.getMatchNameField(),v);
	}

}
