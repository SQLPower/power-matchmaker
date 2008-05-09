/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.validation.ProjectNameValidator;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.validation.AlwaysOKValidator;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The MatchEditor is the GUI for editing all aspects of a {@link Project} instance.
 */
public class ProjectEditor implements MatchMakerEditorPane<Project> {

	private static final Logger logger = Logger.getLogger(ProjectEditor.class);

	/**
	 * The panel that holds this editor's GUI.
	 */
	protected static final String EMAIL_PROPERTY_KEY = "email";
	private static final String FAILED_ENTRY = "failedEntry";

	private static final String VIEW_ONLY_USERS_KEY = "viewOnlyUsers";
	private static final String VIEW_AND_MODIFY_USERS_KEY = "viewAndModifyUsers";
	private static final String PUBLIC_GROUP_KEY = "publicGroup";
	private static final String OWNERSHIP_KEY = "owner";
	private final JPanel panel;

	private JPanel sharingListPane;
	private StatusComponent status = new StatusComponent();
	private JTextField projectName = new JTextField();
	private JTextArea desc = new JTextArea();

	private JCheckBox isSharingWithEveryone;
	private JList viewOnlyList;
	private JList viewAndModifyList;
	private JTextField toViewOnly;
	private JTextField toViewAndModify;
	private JButton addToViewOnly;
	private JButton removeFromViewOnly;
	private JButton addToViewAndModify;
	private JButton removeFromViewAndModify;
	private JButton saveProject;
	private JLabel sharingLabel;
	private JLabel sharingWithEveryoneLabel;
	private JSONArray viewOnlyUsers = new JSONArray();;
	private JSONArray viewAndModifyUsers = new JSONArray();;
	private ArrayList<String> vArray = new ArrayList<String>();
	private ArrayList<String> vamArray = new ArrayList<String>();
	private Boolean isOwner;

	private final MatchMakerSwingSession swingSession;

	/**
	 * The project that this editor is editing.  If you want to edit a different match,
	 * create a new ProjectEditor.
	 */
	private final Project project;
	private FormValidationHandler handler;

	private PlFolder<Project> folder;

	/**
	 * Construct a ProjectEditor; for a project that is not new, we create a backup for it,
	 * and give it the name of the old one, when we save it, we will remove
	 * the backup from the folder, and insert the new one.
	 * @param swingSession  -- a MatchMakerSession
	 * @param project the project Object to be edited
	 * @param folder the project's parent folder
	 */
	public ProjectEditor(final MatchMakerSwingSession swingSession,
			Project project, PlFolder<Project> folder)
			throws ArchitectException {
		if (project == null)
			throw new IllegalArgumentException("You can't edit a null project");
		folder = swingSession.getDefaultPlFolder();

		this.swingSession = swingSession;
		this.project = project;
		this.folder = folder;
		handler = new FormValidationHandler(status, true);
		handler.setValidatedAction(saveAction);
		panel = buildUI();
		setDefaultSelections();
		addValidators();

		handler.resetHasValidated(); // avoid false hits when newly created
	}

	private void addValidators() {
		Validator v = new ProjectNameValidator(swingSession, project);
		handler.addValidateObject(projectName, v);

		Validator v6 = new AlwaysOKValidator();
		handler.addValidateObject(desc, v6);
	}

	/**
	 * Saves the current project (which is referenced in the plMatch member variable of this editor instance).
	 * If there is no current plMatch, a new one will be created and its properties will be set just like
	 * they would if one had existed.  In either case, this action will then use Hibernate to save the
	 * project object back to the database (but it should use the MatchHome interface instead).
	 */
	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(final ActionEvent e) {
			try {
				boolean ok = applyChanges();
				if (!ok) {
					JOptionPane.showMessageDialog(swingSession.getFrame(),
							"Project Not Saved", "Not Saved",
							JOptionPane.WARNING_MESSAGE);
				}
			} catch (Exception ex) {
				MMSUtils.showExceptionDialog(swingSession.getFrame(),
						"Project Not Saved", ex);
			}
		}
	};

	private Window getParentWindow() {
		return SPSUtils.getWindowInHierarchy(panel);
	}

	/**
	 * Returns the parent (owning) frame of this project editor.  If the owner
	 * isn't a frame (it might be a dialog or AWT Window) then null is returned.
	 * You should always use {@link #getParentWindow()} in preference to
	 * this method unless you really really need a JFrame.
	 *
	 * @return the parent JFrame of this project editor's panel, or null if
	 * the owner is not a JFrame.
	 */
	private JFrame getParentFrame() {
		Window owner = getParentWindow();
		if (owner instanceof JFrame)
			return (JFrame) owner;
		else
			return null;
	}

	private JPanel buildUI() {
		
		projectName.setName("Project Name");
		saveProject = new JButton(saveAction);
		isSharingWithEveryone = new JCheckBox("Share with Everyone. (Your Project will appear in the gallery)");
		sharingLabel = new JLabel("Sharing:");
		sharingWithEveryoneLabel = new JLabel("Share with the following people:");

		FormLayout layout = new FormLayout("4dlu,pref,4dlu,fill:min(pref;"
				+ new JComboBox().getMinimumSize().width
				+ "px):grow, 4dlu,pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,80dlu," + //Up to the text area for project description
						"4dlu,10dlu,16dlu,pref,2dlu,pref,4dlu,pref"); // rows

		PanelBuilder pb;

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout)
				: new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();
		int row = 2;
		pb.add(status, cc.xy(4, row));
		row += 2;
		pb.add(new JLabel("Project Name:"), cc.xy(2, row, "r,c"));
		pb.add(projectName, cc.xy(4, row));
		row += 2;
		desc.setWrapStyleWord(true);
		desc.setLineWrap(true);
		pb.add(new JLabel("Description:"), cc.xy(2, row, "r,t"));
		pb.add(new JScrollPane(desc), cc.xy(4, row, "f,f"));

		row += 2;
		
		pb.add(sharingLabel, cc.xy(2, row, "r,t"));
		pb.add(isSharingWithEveryone, cc.xy(4, row, "l,t"));
		row += 2;
		
		pb.add(sharingWithEveryoneLabel, cc.xy(4, row,
				"l,t"));
		row += 2;

		//sharingListPane contains the 2 jlists which would list the id's of those who have the permission to access the project.
		sharingListPane = new JPanel(new FormLayout("pref,8dlu,pref",
				"pref"));
		JPanel viewOnlyPane = new JPanel(new FormLayout(
				"20dlu,4dlu,20dlu,4dlu,100dlu", "20dlu,100dlu,pref,pref"));
		JPanel viewAndModifyPane = new JPanel(new FormLayout(
				"20dlu,4dlu,20dlu,4dlu,100dlu", "20dlu,100dlu,pref,pref"));

		//viewOnlyPane contains the list of those who are permitted to view the project. At the same time, it also
		//contains the add and remove button to edit the list.
		viewOnlyPane.add(new JLabel("View Only:"), cc.xywh(1, 1, 5, 1));
		viewOnlyList = new JList();
		JScrollPane viewOnlyScrollPane = new JScrollPane(viewOnlyList);
		viewOnlyScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		viewOnlyScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		viewOnlyPane.add(viewOnlyScrollPane, cc.xywh(1, 2, 5, 1));
		toViewOnly = new JTextField();
		viewOnlyPane.add(toViewOnly, cc.xywh(1, 3, 5, 1));
		addToViewOnly = new JButton();
		addToViewOnly.setIcon(new AddRemoveIcon(AddRemoveIcon.Type.ADD));
		viewOnlyPane.add(addToViewOnly, cc.xy(1, 4));
		removeFromViewOnly = new JButton();
		removeFromViewOnly.setIcon(new AddRemoveIcon(
				AddRemoveIcon.Type.REMOVE));
		viewOnlyPane.add(removeFromViewOnly, cc.xy(3, 4));

		//viewAndModifyPane contains the list of those who are permitted to view and to modify the project. At the same time,
		//it also contains the add and remove button to edit this list.
		viewAndModifyPane.add(new JLabel("View and Modify:"), cc.xywh(1, 1, 5,
				1));
		viewAndModifyList = new JList();
		JScrollPane viewAndModifyScrollPane = new JScrollPane(viewAndModifyList);
		viewAndModifyScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		viewAndModifyScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		viewAndModifyPane.add(viewAndModifyScrollPane, cc.xywh(1, 2, 5, 1));
		toViewAndModify = new JTextField();
		viewAndModifyPane.add(toViewAndModify, cc.xywh(1, 3, 5, 1));
		addToViewAndModify = new JButton();
		addToViewAndModify.setIcon(new AddRemoveIcon(
				AddRemoveIcon.Type.ADD));
		viewAndModifyPane.add(addToViewAndModify, cc.xy(1, 4));
		removeFromViewAndModify = new JButton();
		removeFromViewAndModify.setIcon(new AddRemoveIcon(
				AddRemoveIcon.Type.REMOVE));
		viewAndModifyPane.add(removeFromViewAndModify, cc.xy(3, 4));

		sharingListPane.add(viewOnlyPane, cc.xy(1, 1));
		sharingListPane.add(viewAndModifyPane, cc.xy(3, 1));

		pb.add(sharingListPane, cc.xy(4, row, "l, t"));
		row += 2;
		
		// We don't want the save button to take up the whole column width
		// so we wrap it in a JPanel with a FlowLayout. If there is a better
		// way, please fix this.
		JPanel savePanel = new JPanel(new FlowLayout());
		savePanel.add(saveProject);
		pb.add(savePanel, cc.xy(4, row));

		//Set up the action events associated with the add and remove buttons.
		configureActions();
		loadPermissionList();
		return pb.getPanel();
	}

	/**
	 * This sets up the action events associated to the components in the panel.
	 */
	private void configureActions() {
		addToViewOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addToViewOnlyList();
				refreshLists();
			}
		});

		addToViewAndModify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addToViewAndModifyList();
				refreshLists();
			}
		});

		removeFromViewOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeFromViewOnlyList();
				refreshLists();
			}
		});

		removeFromViewAndModify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeFromViewAndModifyList();
				refreshLists();
			}
		});
		
		isSharingWithEveryone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isSharingWithEveryone.isSelected()) {
					viewOnlyList.setEnabled(false);
					addToViewOnly.setEnabled(false);
					removeFromViewOnly.setEnabled(false);
					toViewOnly.setEnabled(false);
				}
				else {
					viewOnlyList.setEnabled(true);
					addToViewOnly.setEnabled(true);
					removeFromViewOnly.setEnabled(true);
					toViewOnly.setEnabled(true);
				}
				refreshLists();
			}
		});
	}

	private void addToViewOnlyList() {
		if (toViewOnly.getText() != null && !toViewOnly.getText().trim().equals("")) {
			if (!vArray.contains(toViewOnly.getText())) {
				vArray.add(toViewOnly.getText());
			}
		}
	}

	private void addToViewAndModifyList() {
		if (toViewAndModify.getText() != null && !toViewAndModify.getText().trim().equals("")) {
			if (!vamArray.contains(toViewAndModify.getText())) {
				vamArray.add(toViewAndModify.getText());
			}
		}
	}

	private void removeFromViewOnlyList() {
		if (!viewOnlyList.isSelectionEmpty()) {
			int[] selectedIndices = viewOnlyList.getSelectedIndices();
			for (int i = 0; i < selectedIndices.length; i++) {
				vArray.remove(selectedIndices[i] - i);
			}
		}
	}

	private void removeFromViewAndModifyList() {
		if (!viewAndModifyList.isSelectionEmpty()) {
			int[] selectedIndices = viewAndModifyList.getSelectedIndices();
			for (int i = 0; i < selectedIndices.length; i++) {
				vamArray.remove(selectedIndices[i] - i);
			}
		}
	}
	
	//this method packs the two lists of users into a jsonObject for transmission.
	private String getPermissions() throws JSONException{
		viewOnlyUsers = new JSONArray();
		for(int i = 0; i < vArray.size(); i++){
			viewOnlyUsers.put(vArray.get(i));
		}
		viewAndModifyUsers = new JSONArray();
		for(int i = 0; i < vamArray.size(); i++){
			viewAndModifyUsers.put(vamArray.get(i));
		}
		JSONObject permissions = new JSONObject();
		permissions.put(VIEW_ONLY_USERS_KEY, viewOnlyUsers);
		permissions.put(VIEW_AND_MODIFY_USERS_KEY, viewAndModifyUsers);
		permissions.put(PUBLIC_GROUP_KEY, isSharingWithEveryone.isSelected());
		
		return permissions.toString();
	}

	private void refreshLists() {
		viewOnlyList.setListData(vArray.toArray());
		viewAndModifyList.setListData(vamArray.toArray());

	}

	private void setDefaultSelections() throws ArchitectException {

		projectName.setText(project.getName());
		desc.setText(project.getDescription());

	}

	public JPanel getPanel() {
		return panel;
	}

	/**
	 * Copies all the values from the GUI components into the PlMatch
	 * object this component is editing, then persists it to the database.
	 * @return true if save OK
	 */
	public boolean applyChanges() {
		List<String> fail = handler.getFailResults();

		if (fail.size() > 0) {
			StringBuffer failMessage = new StringBuffer();
			for (String f : fail) {
				failMessage.append(f).append("\n");
			}
			JOptionPane.showMessageDialog(swingSession.getFrame(),
					"You have to fix these errors before saving:\n"
							+ failMessage.toString(), "Project error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		//sets the project name, id and desc
		project.setDescription(desc.getText());
		String id = projectName.getText();

		if (!id.equals(project.getName())) {
			if (!swingSession.isThisProjectNameAcceptable(id)) {
				JOptionPane.showMessageDialog(getPanel(),
						"<html>Project name \"" + projectName.getText()
								+ "\" does not exist or is invalid.\n"
								+ "The project has not been saved",
						"Project name invalid", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			project.setName(id);
		}

		logger.debug(project.getResultTable());
		logger.debug("Saving Project:" + project.getName());
		handler.resetHasValidated();

		if (project.getParent() != swingSession.getDefaultPlFolder()) {
			swingSession.getDefaultPlFolder().addChild(project);
		}
		logger.debug("Parent is " + project.getParent().getName());
		logger.debug(project.getResultTable());
		logger.debug("saving");
		swingSession.save(project);
		try {
			boolean validSave = swingSession.savePermissions(project.getOid(), getPermissions());
			if(!validSave) {
				JOptionPane.showMessageDialog(getParentFrame(), "Invalid user(s) when saving permissions.", "Error on save permission lists", JOptionPane.WARNING_MESSAGE);
				refreshLists();
			}
			loadPermissionList();
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		}
		
		return true;
	}
	
	/**
	 * Load permission lists and group status at start
	 */
	public void loadPermissionList() {
		if(project.getOid() == null){
			return;
		}
		this.vArray = new ArrayList<String>();
		this.vamArray = new ArrayList<String>();
		JSONObject loadList = swingSession.loadPermissions(project.getOid());
		JSONArray vJArray = new JSONArray();
		JSONArray vamJArray = new JSONArray();
		Boolean isPublic = false;
		try{
			vJArray = (JSONArray)loadList.get(VIEW_ONLY_USERS_KEY);
			vamJArray = (JSONArray)loadList.get(VIEW_AND_MODIFY_USERS_KEY);
			isOwner = (Boolean)loadList.get(OWNERSHIP_KEY);
			for(int i = 0; i < vJArray.length(); i++){
				vArray.add(vJArray.getString(i));
			}
			for(int i = 0; i < vamJArray.length(); i++){
				vamArray.add(vamJArray.getString(i));
			}
			isPublic = (Boolean)loadList.get(PUBLIC_GROUP_KEY);
		} catch (JSONException ex){
			throw new RuntimeException(ex);
		}
		isSharingWithEveryone.setSelected(isPublic);
		
		if(!isOwner) {
			sharingLabel.setVisible(false);
			sharingWithEveryoneLabel.setVisible(false);
			isSharingWithEveryone.setVisible(false);
			sharingListPane.setVisible(false);
			saveProject.setVisible(false);
		}
		else if(isPublic) {
			viewOnlyList.setEnabled(false);
			addToViewOnly.setEnabled(false);
			removeFromViewOnly.setEnabled(false);
			toViewOnly.setEnabled(false);
		}
		refreshLists();
	}

	public boolean hasUnsavedChanges() {
		return handler.hasPerformedValidation();
	}

	public void discardChanges() {
		logger.error("Cannot discard changes");
	}

	public Project getCurrentEditingMMO() {
		return project;
	}
}