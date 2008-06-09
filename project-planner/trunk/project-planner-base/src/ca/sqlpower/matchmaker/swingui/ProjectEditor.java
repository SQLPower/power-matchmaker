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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.action.DuplicateProjectAction;
import ca.sqlpower.matchmaker.swingui.action.NewMungeProcessAction;
import ca.sqlpower.matchmaker.undo.AbstractUndoableEditorPane;
import ca.sqlpower.matchmaker.validation.ProjectDescriptionValidator;
import ca.sqlpower.matchmaker.validation.ProjectNameValidator;
import ca.sqlpower.swingui.SPSUtils;
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
public class ProjectEditor extends AbstractUndoableEditorPane<Project, MungeProcess> {

	private static final Logger logger = Logger.getLogger(ProjectEditor.class);

	/**
	 * The panel that holds this editor's GUI.
	 */
	protected static final String EMAIL_PROPERTY_KEY = "email";

	private final JPanel panel;
	private JPanel workflowListPane;

	private JPanel sharingListPane;
	private StatusComponent status = new StatusComponent();
	private JLabel projectOwner = new JLabel();
	private JTextField projectName = new JTextField();
	private JTextArea desc = new JTextArea();

	private JCheckBox isSharingWithEveryone;
	private JLabel galleryIcon;
	private JLabel sharingIcon;

	private JList viewOnlyList;
	private JList viewAndModifyList;
	private JList workflowList;
	private DefaultListModel viewOnlyListModel = new DefaultListModel();
	private DefaultListModel viewAndModifyListModel = new DefaultListModel();
	private DefaultListModel workflowListModel = new DefaultListModel();

	private JButton addToViewOnly;
	private JButton removeFromViewOnly;
	private JButton addToViewAndModify;
	private JButton removeFromViewAndModify;
	private JButton addWorkflow;
	private JButton removeWorkflow;
	private JButton saveProject;


	private boolean changed;


	private final MatchMakerSwingSession swingSession;

	/**
	 * The project that this editor is editing.  If you want to edit a different match,
	 * create a new ProjectEditor.
	 */
	private final Project project;
	private FormValidationHandler handler;

	/**
	 * Construct a ProjectEditor; for a project that is not new, we create a backup for it,
	 * and give it the name of the old one, when we save it, we will remove
	 * the backup from the folder, and insert the new one.
	 * @param swingSession  -- a MatchMakerSession
	 * @param project the project Object to be edited
	 * @param folder the project's parent folder
	 */
	public ProjectEditor(final MatchMakerSwingSession swingSession,
			Project project)
	throws ArchitectException {
		super(swingSession, project);
		if (project == null)
			throw new IllegalArgumentException("You can't edit a null project");
		this.swingSession = swingSession;
		this.project = project;
		handler = new FormValidationHandler(status, true);
		handler.setValidatedAction(saveAction);
		saveProject = new JButton(saveAction);
		swingSession.loadPermissions(project);
		loadPermissionList();
		panel = buildUI();
		loadWorkflowList();
		setDefaultSelections();
		addValidators();

		handler.resetHasValidated(); // avoid false hits when newly created
	}

	private void addValidators() {
		Validator v = new ProjectNameValidator(swingSession, project);
		handler.addValidateObject(projectName, v);

		Validator v6 = new ProjectDescriptionValidator(swingSession, project);
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
		projectOwner.setName("Project Owner");
		projectName.setName("Project Name");
		isSharingWithEveryone = new JCheckBox("Share with Everyone. (Your Project will appear in the public gallery)");
		galleryIcon = new JLabel();

		// the project editor panel layout
		FormLayout layout = new FormLayout("4dlu,pref,4dlu,fill:min(pref;"
				+ new JComboBox().getMinimumSize().width
				+ "px):grow, 50dlu,pref,4dlu", // Columns
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,50dlu," + //Up to the text area for project description
				"8dlu,4dlu,8dlu," + // First separator
				"pref,8dlu,pref,8dlu,pref," + // Up to second separator
				"20dlu,4dlu,8dlu," + // Second separator
				"pref,8dlu,pref"); // Rows

		PanelBuilder pb;

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout)
		: new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();
		int row = 2;
		pb.add(status, cc.xy(4, row));
		row += 2;
		pb.add(new JLabel("Project Owner:"), cc.xy(2, row, "r,c"));
		pb.add(projectOwner, cc.xy(4, row));
		row += 2;
		pb.add(new JLabel("Project Name:"), cc.xy(2, row, "r,c"));
		pb.add(projectName, cc.xy(4, row));
		projectName.setEnabled(project.isOwner());
		row += 2;
		desc.setWrapStyleWord(true);
		desc.setLineWrap(true);
		
		// gets the system default font
		Font defaultFont = (Font)UIManager.get("Label.font");
		desc.setFont(defaultFont);
		
		pb.add(new JLabel("Description:"), cc.xy(2, row, "r,t"));
		pb.add(new JScrollPane(desc), cc.xy(4, row, "f,f"));
		desc.setEnabled(project.isOwner());
		row += 2;

		pb.add(new JSeparator(), cc.xyw(2, row, 3));
		row += 2;

		if (project.isOwner()) {
			galleryIcon.setIcon(new ImageIcon(getClass().getResource("/icons/gallery.png")));
			pb.add(galleryIcon, cc.xy(2, row, "r,c"));

			isSharingWithEveryone.setSelected(project.isPublic());
			pb.add(isSharingWithEveryone, cc.xy(4, row, "l,t"));
			row += 2;

			sharingIcon = new JLabel(new ImageIcon(getClass().getResource("/icons/share.png")));
			pb.add(sharingIcon, cc.xy(2, row, "r, c"));
			pb.add(new JLabel("Share with the following people:"), cc.xy(4, row, "l,t"));
			row += 2;

			//sharingListPane contains the 2 jlists which would list the id's of those who have the permission to access the project.
			sharingListPane = logger.isDebugEnabled() ? new FormDebugPanel(new FormLayout("left:pref:grow,10px,right:pref:grow",
			"pref")) : new JPanel(new FormLayout("left:pref:grow,10px,right:pref:grow",
			"pref"));
			FormLayout permissionPaneLayout = new FormLayout(
					"20dlu,4dlu,20dlu,4dlu,fill:pref:grow", "pref,4dlu,50dlu,4dlu,pref");

			JPanel viewOnlyPane = logger.isDebugEnabled() ? new FormDebugPanel(permissionPaneLayout)
			: new JPanel(permissionPaneLayout);

			JPanel viewAndModifyPane = logger.isDebugEnabled() ? new FormDebugPanel(permissionPaneLayout)
			: new JPanel(permissionPaneLayout);

			//viewOnlyPane contains the list of those who are permitted to view the project. At the same time, it also
			//contains the add and remove button to edit the list.
			viewOnlyPane.add(new JLabel("View Only:"), cc.xyw(1, 1, 5));
			viewOnlyList = new JList(viewOnlyListModel);
			viewOnlyPane.add(new JScrollPane(viewOnlyList), cc.xyw(1, 3, 5, "f,f"));
			viewOnlyList.setEnabled(!project.isPublic());
			addToViewOnly = new JButton(new AddRemoveIcon(AddRemoveIcon.Type.ADD));
			viewOnlyPane.add(addToViewOnly, cc.xy(1, 5));
			addToViewOnly.setEnabled(!project.isPublic());
			removeFromViewOnly = new JButton(new AddRemoveIcon(AddRemoveIcon.Type.REMOVE));
			viewOnlyPane.add(removeFromViewOnly, cc.xy(3, 5));
			removeFromViewOnly.setEnabled(!project.isPublic());
			viewOnlyPane.setPreferredSize(new Dimension(140, 135));

			//viewAndModifyPane contains the list of those who are permitted to view and to modify the project. At the same time,
			//it also contains the add and remove button to edit this list.
			viewAndModifyPane.add(new JLabel("View and Modify:"), cc.xyw(1, 1, 5));
			viewAndModifyList = new JList(viewAndModifyListModel);
			viewAndModifyPane.add(new JScrollPane(viewAndModifyList), cc.xyw(1, 3, 5, "f,f"));
			addToViewAndModify = new JButton(new AddRemoveIcon(AddRemoveIcon.Type.ADD));
			viewAndModifyPane.add(addToViewAndModify, cc.xy(1, 5));
			removeFromViewAndModify = new JButton(new AddRemoveIcon(AddRemoveIcon.Type.REMOVE));
			viewAndModifyPane.add(removeFromViewAndModify, cc.xy(3, 5));
			viewAndModifyPane.setPreferredSize(new Dimension(140, 135));

			sharingListPane.add(viewOnlyPane, cc.xy(1, 1, "f, t"));
			sharingListPane.add(viewAndModifyPane, cc.xy(3, 1, "f, t"));

			pb.add(sharingListPane, cc.xy(4, row, "f, t"));
			row += 2;
			
		} else {
			disableComponents();
		}

		pb.add(new JSeparator(), cc.xyw(2, row, 3));
		row += 2;


		pb.add(new JLabel("Workflows:"), cc.xy(2, row, "r,t"));

		workflowListPane = new JPanel(new GridLayout(1, 2, 10, 0));
		JPanel workflowPane = logger.isDebugEnabled() ? new FormDebugPanel(new FormLayout("20dlu,4dlu,20dlu,4dlu,fill:pref:grow", "50dlu,4dlu,pref"))
			: new JPanel(new FormLayout("20dlu,4dlu,20dlu,4dlu,fill:pref:grow", "50dlu,4dlu,pref"));
		workflowList = new JList(workflowListModel);
		workflowPane.add(new JScrollPane(workflowList), cc.xyw(1, 1, 5, "f,f"));
		addWorkflow = new JButton(new AddRemoveIcon(AddRemoveIcon.Type.ADD));
		workflowPane.add(addWorkflow, cc.xy(1, 3));
		removeWorkflow = new JButton(new AddRemoveIcon(AddRemoveIcon.Type.REMOVE));
		workflowPane.add(removeWorkflow, cc.xy(3, 3));
		workflowPane.setPreferredSize(new Dimension(140, 112));
		if (!project.isOwner()) {
			addWorkflow.setVisible(false);
			removeWorkflow.setVisible(false);
		}
		workflowListPane.add(workflowPane);
		workflowListPane.add(new JPanel());
		pb.add(workflowListPane, cc.xy(4, row, "f, t"));
		row += 2;
		// We don't want the save button to take up the whole column width
		// so we wrap it in a JPanel with a FlowLayout. If there is a better
		// way, please fix this.
		JPanel savePanel = new JPanel(new FlowLayout());
		savePanel.add(saveProject);
		pb.add(savePanel, cc.xy(4, row, "l, t"));

		//Set up the action events associated with the add and remove buttons.
		configureActions();
		return pb.getPanel();
	}

	/**
	 * This sets up the action events associated to the components in the panel.
	 */
	private void configureActions() {
		workflowList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					int row = workflowList.getSelectedIndex();
					JTree tree = swingSession.getTree();					
					tree.setSelectionPath(tree.getSelectionPath().pathByAddingChild(project.getChildren().get(row)));
				}
			}
		});

		addWorkflow.addActionListener(new NewMungeProcessAction(swingSession, project));

		removeWorkflow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!workflowList.isSelectionEmpty()) {
					int response = JOptionPane.showConfirmDialog(swingSession.getFrame(),
							"Are you sure you want to delete the " + workflowList.getSelectedValues().length + 
					" selected workflow(s)?");
					if (response != JOptionPane.YES_OPTION) {
						return;
					}
					changed = true;
					for (Object obj : workflowList.getSelectedValues()) {
						workflowListModel.removeElement(obj);
						project.removeChild((MungeProcess) obj);
					}
				} else {
					JOptionPane.showMessageDialog(swingSession.getFrame(),
					"Please select one workflow to delete");
				}
			}
		});
		
		if (!project.isOwner()) return;

		addToViewOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String memberId = JOptionPane.showInputDialog(swingSession.getFrame(),
						"Please enter the user's email:", "Add View Only Permission", JOptionPane.QUESTION_MESSAGE);
				if (memberId != null && !memberId.trim().equals("")) {
					if (!viewOnlyListModel.contains(memberId)) {
						viewOnlyListModel.addElement(memberId);
						changed = true;
					}
					viewOnlyList.setSelectedValue(memberId, true);
				}
			}
		});

		addToViewAndModify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String memberId = JOptionPane.showInputDialog(swingSession.getFrame(),
						"Please enter the user's email:", "Add View and Modify Permission", JOptionPane.QUESTION_MESSAGE);
				if (memberId != null && !memberId.trim().equals("")) {
					if (!viewAndModifyListModel.contains(memberId)) {
						viewAndModifyListModel.addElement(memberId);
						changed = true;
					}
					viewAndModifyList.setSelectedValue(memberId, true);
				}
			}
		});


		removeFromViewOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!viewOnlyList.isSelectionEmpty()) {
					changed = true;
					for (Object obj : viewOnlyList.getSelectedValues()) {
						viewOnlyListModel.removeElement(obj);
					}
				}
			}
		});

		removeFromViewAndModify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!viewAndModifyList.isSelectionEmpty()) {
					changed = true;
					for (Object obj : viewAndModifyList.getSelectedValues()) {
						viewAndModifyListModel.removeElement(obj);
					}
				}
			}
		});

		isSharingWithEveryone.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changed = true;
				if(isSharingWithEveryone.isSelected()) {
					viewOnlyList.setEnabled(false);
					addToViewOnly.setEnabled(false);
					removeFromViewOnly.setEnabled(false);
				}
				else {
					viewOnlyList.setEnabled(true);
					addToViewOnly.setEnabled(true);
					removeFromViewOnly.setEnabled(true);
				}
			}
		});
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
	@Override
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
		super.applyChanges();

		project.setPublic(isSharingWithEveryone.isSelected());

		List<String> viewOnlyUsers = new ArrayList<String>();
		List<String> viewModifyUsers = new ArrayList<String>();

		for (Object obj : viewOnlyListModel.toArray()) {
			viewOnlyUsers.add((String) obj);
		}
		for (Object obj : viewAndModifyListModel.toArray()) {
			viewModifyUsers.add((String) obj);
		}

		project.setViewOnlyUsers(viewOnlyUsers);
		project.setViewModifyUsers(viewModifyUsers);

		boolean validSave = swingSession.savePermissions(project);
		if(!validSave) {
			JOptionPane.showMessageDialog(getParentFrame(), "Invalid user(s) when saving permissions.", "Error on save permission lists", JOptionPane.WARNING_MESSAGE);
		}
		loadPermissionList();
		changed = false;
		return true;
	}

	/**
	 * Load permission lists and group status
	 */
	private void loadPermissionList() {
		viewAndModifyListModel.clear();
		for (String userId : project.getViewModifyUsers()){
			viewAndModifyListModel.addElement(userId);
		}
		viewOnlyListModel.clear();
		for (String userId : project.getViewOnlyUsers()){
			viewOnlyListModel.addElement(userId);
		}
		projectOwner.setText(project.getOwner());
		if (!project.isOwner()) {
			if (project.canModify()) {
				saveProject.setAction(new DuplicateProjectAction(swingSession, project));
			} 
		}
	}	

	private void disableComponents() {
		if (project.canModify()) {
			saveProject.setAction(new DuplicateProjectAction(swingSession, project));
		} 
		saveProject.setVisible(project.canModify());
	}


	private void loadWorkflowList() { 
		this.workflowListModel.clear();

		for (MungeProcess workflow: project.getChildren()) {
			workflowListModel.addElement(workflow); 
		}
	}


	public boolean hasUnsavedChanges() {
		return handler.hasPerformedValidation() || changed;
	}

	@Override
	public void undoEventFired(MatchMakerEvent<Project, MungeProcess> evt) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: ProjectEditor.undoEventFired()");
		
	}
}