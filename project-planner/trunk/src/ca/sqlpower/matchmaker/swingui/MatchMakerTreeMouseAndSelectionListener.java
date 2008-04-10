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

import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel.MatchActionNode;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel.MatchActionType;
import ca.sqlpower.matchmaker.swingui.action.DeleteMergeRuleAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteMungeProcessAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteMungeStepAction;
import ca.sqlpower.matchmaker.swingui.action.DeletePlFolderAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteProjectAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteTranslateGroupAction;
import ca.sqlpower.matchmaker.swingui.action.DuplicateProjectAction;
import ca.sqlpower.matchmaker.swingui.action.NewMergeRuleAction;
import ca.sqlpower.matchmaker.swingui.action.NewMungeProcessAction;
import ca.sqlpower.matchmaker.swingui.action.NewProjectAction;
import ca.sqlpower.matchmaker.swingui.action.NewTranslateGroupAction;
import ca.sqlpower.matchmaker.swingui.action.Refresh;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.matchmaker.swingui.engine.CleanseEnginePanel;
import ca.sqlpower.matchmaker.swingui.engine.MatchEnginePanel;
import ca.sqlpower.matchmaker.swingui.engine.MergeEnginePanel;

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
					} else if (folder.getName().equals(
							Project.MERGE_RULES_FOLDER_NAME)) {
						addMergeRulesFolderMenuItems(m, folder);
					}
				} else if (o instanceof MungeProcess) {
					addMungeProcessMenuItems(m, (MungeProcess) o);
				} else if (o instanceof MungeStep) {
					addMungeStepMenuItems(m, (MungeStep) o);
				} else if (o instanceof TableMergeRules) {
					addMergeRulesMenuItems(m, (TableMergeRules) o);
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
	 * Attaches a menu item for the actions of a munge process.
	 * 
	 * @param m
	 *            The popup menu that the menu item would be attached onto.
	 * @param folder
	 *            The current folder being right-clicked on.
	 */
	private void addMungeProcessesFolderMenuItems(JPopupMenu m,
			MatchMakerFolder<?> folder) {
		m.add(new JMenuItem(new NewMungeProcessAction(swingSession,
				(Project) folder.getParent())));
	}

	/**
	 * Attaches a menu item for the actions of a merge rules folder.
	 * 
	 * @param m
	 *            The popup menu that the menu item would be attached onto.
	 * @param folder
	 *            The current folder being right-clicked on.
	 */
	private void addMergeRulesFolderMenuItems(JPopupMenu m,
			MatchMakerFolder<?> folder) {
		m.add(new JMenuItem(new NewMergeRuleAction(swingSession, (Project) folder
				.getParent())));
	}

	/**
	 * Attaches a menu item for the actions of a merge rule.
	 * 
	 * @param m
	 *            The popup menu that the menu item would be attached onto.
	 * @param folder
	 *            The current folder being right-clicked on.
	 */
	private void addMergeRulesMenuItems(JPopupMenu m, TableMergeRules mergeRule) {
		if (!mergeRule.getTableName().equals(mergeRule.getParentProject().getSourceTableName())) {
			m.add(new JMenuItem(new DeleteMergeRuleAction(swingSession,
					mergeRule)));
		}
	}

	/**
	 * Attaches a menu item for the actions of a munge step.
	 * 
	 * @param m
	 *            The popup menu that the menu item would be attached onto.
	 * @param rule
	 *            The current folder being right-clicked on.
	 */
	private void addMungeStepMenuItems(JPopupMenu m, MungeStep step) {
		m.add(new JMenuItem(new DeleteMungeStepAction(swingSession, step)));
	}

	/**
	 * Attaches a menu item for the actions of a munge process.
	 * 
	 * @param m
	 *            The popup menu that the menu item would be attached onto.
	 * @param mungeProcess
	 *            The current folder being right-clicked on.
	 */
	private void addMungeProcessMenuItems(JPopupMenu m, MungeProcess mungeProcess) {
		m.add(new JMenuItem(new DeleteMungeProcessAction(swingSession, mungeProcess)));
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
		m.add(new JMenuItem(new NewMungeProcessAction(swingSession, project)));

		if (project.getType() != ProjectMode.CLEANSE) {
			m.add(new JMenuItem(new NewMergeRuleAction(swingSession, project)));
		}
		
		m.addSeparator();
		if (project.getType() == ProjectMode.FIND_DUPES) {
			m.add(new JMenuItem(new AbstractAction("Run Match") {
				public void actionPerformed(ActionEvent e) {
					MatchEnginePanel f = swingSession.getMatchEnginePanel(project.getMatchingEngine(), project);
					swingSession.setCurrentEditorComponent(f);
				}
			}));
			m.add(new JMenuItem(new AbstractAction("Run Merge") {
				public void actionPerformed(ActionEvent e) {
					MergeEnginePanel f = swingSession.getMergeEnginePanel(project.getMergingEngine(), project);
					swingSession.setCurrentEditorComponent(f);
				}
			}));
		} else if (project.getType() == ProjectMode.CLEANSE) {
			m.add(new JMenuItem(new AbstractAction("Run Cleanse") {
				public void actionPerformed(ActionEvent e) {
					CleanseEnginePanel f = swingSession.getCleanseEnginePanel(project.getCleansingEngine(), project);
					swingSession.setCurrentEditorComponent(f);
				}
			}));
		}

		m.addSeparator();
		if (project.getType() != ProjectMode.CLEANSE) {
			m.add(new JMenuItem(new ShowMatchStatisticInfoAction(swingSession,
					project, owningFrame)));
		}
		m.add(new JMenuItem(new AbstractAction("Audit Information") {
			public void actionPerformed(ActionEvent e) {
				swingSession.setCurrentEditorComponent(new ProjectInfoEditor(
						project));
			}
		}));
		
		m.addSeparator();
		
		// TODO: Implement the import and export functions and
		// replace these dummy actions.
		m.add(new JMenuItem(new AbstractAction("Import") {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(owningFrame,
				"Import is not yet available. We apologize for the inconvenience");				
			}
		}));
		m.add(new JMenuItem(new AbstractAction("Export") {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(owningFrame,
				"Export is not yet available. We apologize for the inconvenience");				
			}
		}));
//		m.add(new JMenuItem(new ProjectExportAction(swingSession,
//						owningFrame)));
//		m.add(new JMenuItem(new ProjectImportAction(swingSession,
//						owningFrame)));

		m.addSeparator();
		m.add(new JMenuItem(new DeleteProjectAction(swingSession, project)));
		m.add(new JMenuItem(new DuplicateProjectAction(swingSession, project)));

        if (logger.isDebugEnabled()) {
            m.addSeparator();
            m.add(new JMenuItem(new AbstractAction("Show Result Table Columns...") {
                public void actionPerformed(ActionEvent e) {
                    try {
                        SQLTable resultTable = project.getResultTable();
                        logger.debug("Result table dump for project " + project.getName());
                        logger.debug("Table: " + resultTable);
                        logger.debug("Columns: " + resultTable.getColumns());
                        logger.debug("Result Table Catalog: " + project.getResultTableCatalog());
                        logger.debug("Result Table Schema:  " + project.getResultTableSchema());
                        logger.debug("Result Table Name:    " + project.getResultTableName());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }));
            m.add(new JMenuItem(new AbstractAction("Reset Result Table") {
                public void actionPerformed(ActionEvent e) {
                    try {
                        String cat = project.getResultTableCatalog();
                        String sch = project.getResultTableSchema();
                        String nam = project.getResultTableName();
                        project.setResultTableCatalog(null);
                        project.setResultTableSchema(null);
                        project.setResultTableName(null);
                        project.setResultTableCatalog(cat);
                        project.setResultTableSchema(sch);
                        project.setResultTableName(nam);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }));
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

					if (f.getName().equals(Project.MERGE_RULES_FOLDER_NAME)) {
						MergeTableRuleEditor editor = new MergeTableRuleEditor(swingSession, m);
						logger.debug("Created new merge table rules editor "
								+ System.identityHashCode(editor));
						swingSession.setCurrentEditorComponent(editor);
					} else if (f.getName().equals(Project.MUNGE_PROCESSES_FOLDER_NAME)) {
						MungeProcessGroupEditor editor = new MungeProcessGroupEditor(swingSession, m);
						logger.debug("Created new munge process group editor "
								+ System.identityHashCode(editor));
						swingSession.setCurrentEditorComponent(editor);
					}
				} else if (o instanceof TableMergeRules) {
					// Checks if the original pane is the same as the new one
					if (swingSession.getOldPane() instanceof MergeColumnRuleEditor) {
						MergeColumnRuleEditor originalPane = (MergeColumnRuleEditor) swingSession.getOldPane();
						if (originalPane.getMergeRule() == o) {
							return;
						}
					}
					TableMergeRules f = (TableMergeRules) o;
					Project m = (Project) f.getParentProject();
					MergeColumnRuleEditor editor = new MergeColumnRuleEditor(swingSession, m, f);
					logger.debug("Created new merge column rules editor " + System.identityHashCode(editor));
					swingSession.setCurrentEditorComponent(editor);
				} else if (o instanceof ColumnMergeRules) {
					ColumnMergeRules columnMergeRule = (ColumnMergeRules) o;
					// Checks if the original pane is the same as the new one
					if (swingSession.getOldPane() instanceof MergeColumnRuleEditor) {
						MergeColumnRuleEditor originalPane = (MergeColumnRuleEditor) swingSession.getOldPane();
						if (originalPane.getMergeRule() == columnMergeRule.getParent()) {
							originalPane.setSelectedColumn(columnMergeRule);
							return;
						}
					}
					TableMergeRules f = (TableMergeRules) ((ColumnMergeRules) o).getParent();
					Project m = (Project) f.getParentProject();
					MergeColumnRuleEditor editor = new MergeColumnRuleEditor(swingSession, m, f);
					logger.debug("Created new merge column rules editor " + System.identityHashCode(editor));
					swingSession.setCurrentEditorComponent(editor);
					editor.setSelectedColumn(columnMergeRule);
				} else if (o instanceof MatchActionNode) {
					MatchActionNode node = (MatchActionNode) o;
					if (node.getActionType() == MatchActionType.RUN_MATCH) {
						swingSession
								.setCurrentEditorComponent(swingSession.getMatchEnginePanel(node.getProject().getMatchingEngine(), node.getProject()));
					} else if (node.getActionType() == MatchActionType.AUDIT_INFO) {
						swingSession
								.setCurrentEditorComponent(new ProjectInfoEditor(
										node.getProject()));
					} else if (node.getActionType() == MatchActionType.VALIDATE_MATCHES) {
						if (node.getProject().getResultTable() == null) {
							throw new Exception(
									"Match result table does not exist!");
						}
						swingSession
								.setCurrentEditorComponent(new MatchResultVisualizer(
										node.getProject()));
					} else if (node.getActionType() == MatchActionType.VALIDATION_STATUS) {
						swingSession
								.setCurrentEditorComponent(new MatchValidationStatus(
										swingSession, node.getProject()));
					} else if (node.getActionType() == MatchActionType.RUN_MERGE) {
						swingSession.setCurrentEditorComponent(swingSession.getMergeEnginePanel(
								node.getProject().getMergingEngine(), node.getProject()));
					} else if (node.getActionType() == MatchActionType.RUN_CLEANSING) {
						swingSession.setCurrentEditorComponent(swingSession.getCleanseEnginePanel(node.getProject().getCleansingEngine(), node.getProject()));
					}
				} else if (o instanceof TranslateGroupParent) {
					swingSession
							.setCurrentEditorComponent(new TranslateGroupsEditor(
									swingSession));
				} else if (o instanceof MatchMakerTranslateGroup) {
					// Checks if the original pane is the same as the new one
					if (swingSession.getOldPane() instanceof TranslateWordsEditor) {
						TranslateWordsEditor originalPane = (TranslateWordsEditor) swingSession.getOldPane();
						if (originalPane.getGroup() == o) {
							return;
						}
					}
					MatchMakerTranslateGroup group = (MatchMakerTranslateGroup) o;
					TranslateWordsEditor editor = new TranslateWordsEditor(swingSession, group);
					logger.debug("Created new translate word editor " + System.identityHashCode(editor));
					swingSession.setCurrentEditorComponent(editor);
				} else if (o instanceof MatchMakerTranslateWord) {
					MatchMakerTranslateWord word = (MatchMakerTranslateWord) o;
					// Checks if the original pane is the same as the new one
					if (swingSession.getOldPane() instanceof TranslateWordsEditor) {
						TranslateWordsEditor originalPane = (TranslateWordsEditor) swingSession.getOldPane();
						if (originalPane.getGroup() == word.getParent()) {
							originalPane.setSelectedWord(word);
							return;
						}
					}
					MatchMakerTranslateGroup group = (MatchMakerTranslateGroup) word.getParent();
					TranslateWordsEditor editor = new TranslateWordsEditor(swingSession, group);
					logger.debug("Created new translate word editor " + System.identityHashCode(editor));
					swingSession.setCurrentEditorComponent(editor);
					editor.setSelectedWord(word);

				}

			} catch (Exception ex) {
				MMSUtils.showExceptionDialog(owningFrame,
						"Couldn't create editor for selected component", ex);
			}
		}
	}

}