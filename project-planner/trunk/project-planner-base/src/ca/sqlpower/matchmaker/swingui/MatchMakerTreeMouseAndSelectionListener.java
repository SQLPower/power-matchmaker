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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel.MatchActionNode;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel.MatchActionType;
import ca.sqlpower.matchmaker.swingui.action.DeleteMungeProcessAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteMungeStepAction;
import ca.sqlpower.matchmaker.swingui.action.DeletePlFolderAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteProjectAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteTranslateGroupAction;
import ca.sqlpower.matchmaker.swingui.action.DuplicateProjectAction;
import ca.sqlpower.matchmaker.swingui.action.NewMungeProcessAction;
import ca.sqlpower.matchmaker.swingui.action.NewProjectAction;
import ca.sqlpower.matchmaker.swingui.action.NewTranslateGroupAction;
import ca.sqlpower.matchmaker.swingui.action.Refresh;

/**
 * This appears to be a mouse event listener for the MatchMaker tree component
 * of the GUI. It creates pop-up menus when a popup event is triggered, (ex.
 * right-clicking), and also changes the MatchMaker's main editor component
 * according to the selected item in the tree
 */
public class MatchMakerTreeMouseAndSelectionListener extends MouseAdapter
		implements TreeSelectionListener {

	private static final Logger logger = Logger
			.getLogger(MatchMakerTreeMouseAndSelectionListener.class);

	private final MatchMakerSwingSession swingSession;

	private final JFrame owningFrame;

	public MatchMakerTreeMouseAndSelectionListener(
			MatchMakerSwingSession swingSession) {
		this.swingSession = swingSession;
		this.owningFrame = swingSession.getFrame();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		makePopup(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		makePopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		makePopup(e);
	}

	/**
	 * If the event is a pop-up trigger this method selects the tree node that
	 * is clicked on and displays the pop-up menu.
	 */
	private void makePopup(MouseEvent e) {

		if (e.isPopupTrigger()) {
			JTree t = (JTree) e.getSource();
			TreePath p = t.getPathForLocation(e.getX(), e.getY());
			if (!t.isPathSelected(p)) {
				t.setSelectionPath(p);
			}

			JPopupMenu m = new JPopupMenu();
			int row = t.getRowForLocation(e.getX(), e.getY());
			TreePath tp = t.getPathForRow(row);
			m.add(new JMenuItem(new Refresh(swingSession)));
			if (tp != null) {
				Object o = tp.getLastPathComponent();
				if (o instanceof FolderParent) {
					MatchMakerTreeModel model = (MatchMakerTreeModel) t
							.getModel();
					int index = model.getIndexOfChild(model.getRoot(), o);
					/** * create folder under current only */
					if (index == 0) {
						createNewFolderMenuItem(m);
					}
				} else if (o instanceof PlFolder) {
					addFolderMenuItems(m, (PlFolder) o);
				} else if (o instanceof Project) {
					addProjectMenuItems(m, (Project) o);
				} else if (o instanceof MatchMakerFolder<?>) {
					MatchMakerFolder<?> folder = (MatchMakerFolder<?>) o;
					if (folder.getName().equals(Project.MUNGE_PROCESSES_FOLDER_NAME)) {
						addMungeProcessesFolderMenuItems(m, folder);
					}
				} else if (o instanceof MungeProcess) {
					addMungeProcessMenuItems(m, (MungeProcess) o);
				} else if (o instanceof MungeStep) {
					addMungeStepMenuItems(m, (MungeStep) o);
				} else if (o instanceof TranslateGroupParent) {
					addTranslateMenuItems(m, (TranslateGroupParent) o);
				} else if (o instanceof MatchMakerTranslateGroup) {
					addTranslateGroupMenuItems(m, (MatchMakerTranslateGroup) o);
				}
			}
			m.show(t, e.getX(), e.getY());
		}
	}

	/**
	 * Attaches a menu item for the actions of a munge process, if the user
	 * has rights.
	 * 
	 * @param m
	 *            The popup menu that the menu item would be attached onto.
	 * @param folder
	 *            The current folder being right-clicked on.
	 */
	private void addMungeProcessesFolderMenuItems(JPopupMenu m,
			MatchMakerFolder<?> folder) {
		if (((Project) folder.getParent()).isOwner()) {
			m.add(new JMenuItem(new NewMungeProcessAction(swingSession,
					(Project) folder.getParent())));
		}
	}

	/**
	 * Attaches a menu item for the actions of a munge step, if the user
	 * has rights.
	 * 
	 * @param m
	 *            The popup menu that the menu item would be attached onto.
	 * @param rule
	 *            The current folder being right-clicked on.
	 */
	private void addMungeStepMenuItems(JPopupMenu m, MungeStep step) {
		if (((Project)step.getParent().getParent()).isOwner()) {
			m.add(new JMenuItem(new DeleteMungeStepAction(swingSession, step)));
		}
	}

	/**
	 * Attaches a menu item for the actions of a munge process, if the user
	 * has rights.
	 * 
	 * @param m
	 *            The popup menu that the menu item would be attached onto.
	 * @param mungeProcess
	 *            The current folder being right-clicked on.
	 */
	private void addMungeProcessMenuItems(JPopupMenu m, MungeProcess mungeProcess) {
		if (mungeProcess.getParentProject().isOwner()) { 
			m.add(new JMenuItem(new DeleteMungeProcessAction(swingSession, mungeProcess)));
		}
	}

	private void createNewFolderMenuItem(JPopupMenu m) {
		m.add(new JMenuItem(new AbstractAction("New Folder") {
			public void actionPerformed(ActionEvent e) {
				PlFolder<Project> folder = new PlFolder<Project>();
				FolderEditor editor = new FolderEditor(swingSession, folder);
				swingSession.setCurrentEditorComponent(editor);
			}
		}));
	}

	private void addProjectMenuItems(JPopupMenu m, final Project project) {
		m.addSeparator();
		if (project.isOwner()) {
			m.add(new JMenuItem(new NewMungeProcessAction(swingSession, project)));
			m.add(new JMenuItem(new DeleteProjectAction(swingSession, project)));
		}
		if (project.canModify()) {
			m.add(new JMenuItem(new DuplicateProjectAction(swingSession, project)));
		}
	}

	private void addFolderMenuItems(JPopupMenu m, final PlFolder folder) {
		JMenu mm = new JMenu("New Project");
		mm.add(new JMenuItem(new NewProjectAction(swingSession, "New De-duping Project", Project.ProjectMode.FIND_DUPES)));
		mm.add(new JMenuItem(new NewProjectAction(swingSession, "New Cleansing Project",Project.ProjectMode.CLEANSE)));
		mm.add(new JMenuItem(new NewProjectAction(swingSession, "New X-ref Project", Project.ProjectMode.BUILD_XREF)));
		m.add(mm);
		
		// TODO: Implement the import and export functions and
		// replace this dummy action.
		m.add(new JMenuItem(new AbstractAction("Import") {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(owningFrame,
				"Import is not yet available. We apologize for the inconvenience");				
			}
		}));
//		m.add(new JMenuItem(new ProjectImportAction(swingSession, owningFrame)));
		
		m.add(new JMenuItem(new DeletePlFolderAction(swingSession,
				"Delete Folder", folder)));
	}

	/**
	 * Attaches a menu item for the actions of a translate group parent.
	 * 
	 * @param m
	 *            The popup menu that the menu item would be attached onto.
	 * @param folder
	 *            The current folder being right-clicked on.
	 */
	private void addTranslateMenuItems(JPopupMenu m,
			TranslateGroupParent translate) {
		m.add(new JMenuItem(new NewTranslateGroupAction(swingSession)));
	}

	/**
	 * Attaches a menu item for the actions of a translate group.
	 * 
	 * @param m
	 *            The popup menu that the menu item would be attached onto.
	 * @param folder
	 *            The current folder being right-clicked on.
	 */
	private void addTranslateGroupMenuItems(JPopupMenu m,
			MatchMakerTranslateGroup group) {
		m
				.add(new JMenuItem(new DeleteTranslateGroupAction(swingSession,
						group)));
	}

	/**
	 * This appears to set the editor component to the correct type of editor
	 * depending on the component you have selected in the tree.
	 * <p>
	 * This method should catch all exceptions and return gracefully. Otherwise
	 * you may end up seeing unusual behaviour in the Swing UI.
	 */
	public void valueChanged(TreeSelectionEvent e) {

		JTree tree = (JTree) e.getSource();
		if (tree.getSelectionPath() == null) {
			logger.debug("Nothing selected, so return.");
			return;
		}
		TreePath tp = e.getPath();
		if (tp != null) {
			Object o = tp.getLastPathComponent();
			try {
				if (o instanceof PlFolder) {
					FolderEditor editor = new FolderEditor(swingSession,
							(PlFolder) o);
					swingSession.setCurrentEditorComponent(editor);
				} else if (o instanceof Project) {

					ProjectEditor me;
					me = new ProjectEditor(swingSession, (Project) o,
							(PlFolder<Project>) ((Project) o).getParent());

					swingSession.setCurrentEditorComponent(me);

				} else if (o instanceof MungeProcess) {
					if (swingSession.getOldPane() instanceof MungeProcessEditor) {
						MungeProcessEditor originalPane = (MungeProcessEditor) swingSession.getOldPane();
						if (originalPane.getProcess() == o) {
							return;
						}
					}
					Project m = ((MungeProcess) o).getParentProject();
					MungeProcessEditor editor = new MungeProcessEditor(swingSession, m, (MungeProcess) o);
					logger.debug("Created new munge process editor "
							+ System.identityHashCode(editor));
					swingSession.setCurrentEditorComponent(editor);
				} else if (o instanceof MungeStep) {
					MungeProcess mp = (MungeProcess)((MungeStep) o).getParent();
					Project m = mp.getParentProject();
					if (swingSession.getOldPane() instanceof MungeProcessEditor) {
					    MungeProcessEditor originalPane = (MungeProcessEditor) swingSession.getOldPane();
					    if (originalPane.getProcess() == mp) {
					        originalPane.setSelectedStep((MungeStep) o);
					        return;
					    }
					}
					MungeProcessEditor editor = new MungeProcessEditor(swingSession, m, mp);
					logger.debug("Created new munge process editor " + System.identityHashCode(editor));
					swingSession.setCurrentEditorComponent(editor);
					editor.setSelectedStep((MungeStep) o);
				} else if (o instanceof MungeStepOutput) {
					MungeStep ms = (MungeStep)((MungeStepOutput) o).getParent();
					MungeProcess mp = (MungeProcess)(ms.getParent());
					Project m = mp.getParentProject();
					if (swingSession.getOldPane() instanceof MungeProcessEditor) {
						MungeProcessEditor originalPane = (MungeProcessEditor) swingSession.getOldPane();
						if (originalPane.getProcess() == mp) {
							originalPane.setSelectedStepOutput((MungeStepOutput) o);
							return;
						}
					}
					MungeProcessEditor editor = new MungeProcessEditor(swingSession, m, mp);
					logger.debug("Created new munge process editor " + System.identityHashCode(editor));
					swingSession.setCurrentEditorComponent(editor);
					editor.setSelectedStepOutput((MungeStepOutput) o);
				} else if (o instanceof MatchMakerFolder) {
					MatchMakerFolder f = (MatchMakerFolder) o;
					Project m = (Project) f.getParent();

					if (f.getName().equals(Project.MUNGE_PROCESSES_FOLDER_NAME)) {
						MungeProcessGroupEditor editor = new MungeProcessGroupEditor(swingSession, m);
						logger.debug("Created new munge process group editor "
								+ System.identityHashCode(editor));
						swingSession.setCurrentEditorComponent(editor);
					}
				} else if (o instanceof MatchActionNode) {
					MatchActionNode node = (MatchActionNode) o;
					if (node.getActionType() == MatchActionType.AUDIT_INFO) {
						swingSession
								.setCurrentEditorComponent(new ProjectInfoEditor(
										node.getProject()));
					}
				}
			} catch (Exception ex) {
				MMSUtils.showExceptionDialog(owningFrame,
						"Couldn't create editor for selected component", ex);
			}
		}
	}

}