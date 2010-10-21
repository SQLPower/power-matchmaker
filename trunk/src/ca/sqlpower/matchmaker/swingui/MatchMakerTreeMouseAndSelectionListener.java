/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
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
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ProgressMonitor;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.address.AddressPool;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeResultStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel.ProjectActionType;
import ca.sqlpower.matchmaker.swingui.action.DeleteMergeRuleAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteMungeProcessAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteMungeStepAction;
import ca.sqlpower.matchmaker.swingui.action.DeletePlFolderAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteProjectAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteTranslateGroupAction;
import ca.sqlpower.matchmaker.swingui.action.DuplicateProjectAction;
import ca.sqlpower.matchmaker.swingui.action.ExportProjectAction;
import ca.sqlpower.matchmaker.swingui.action.ImportProjectAction;
import ca.sqlpower.matchmaker.swingui.action.NewMergeRuleAction;
import ca.sqlpower.matchmaker.swingui.action.NewMungeProcessAction;
import ca.sqlpower.matchmaker.swingui.action.NewProjectAction;
import ca.sqlpower.matchmaker.swingui.action.NewTranslateGroupAction;
import ca.sqlpower.matchmaker.swingui.action.ScriptAction;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.matchmaker.swingui.address.AddressPoolLoadingWorker;
import ca.sqlpower.matchmaker.swingui.engine.EngineSettingsPanel;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.FolderNode;
import ca.sqlpower.swingui.ProgressWatcher;

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

	/**
	 * Tracks whether or not this selection listener is in the middle of handling
	 * a selection change. Some changes can result in a recursive call to the
	 * valueChanged() method, and this flag helps break cyclical changes.
	 */
	private boolean alreadyHandlingEvent;
	
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
				} else if (o instanceof FolderNode) {
					FolderNode folder = (FolderNode) o;
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
				
				if (o instanceof MatchMakerObject) {
		            m.add(new ScriptAction(swingSession, (MatchMakerObject) o));
				}
			}
			m.show(t, e.getX(), e.getY());
		}
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
			FolderNode folder) {
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
		if (!mergeRule.getTableName().equals((mergeRule.getParent()).getSourceTableName())) {
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
		if (!(step instanceof SQLInputStep || step instanceof MungeResultStep)) {
			m.add(new JMenuItem(new DeleteMungeStepAction(swingSession, step)));
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
			FolderNode folder) {
		m.add(new JMenuItem(new NewMungeProcessAction(swingSession,
				(Project) folder.getParent())));
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
				PlFolder folder = new PlFolder();
				FolderEditor editor = new FolderEditor(swingSession, folder);
				swingSession.setCurrentEditorComponent(editor);
			}
		}));
	}

	private void addProjectMenuItems(JPopupMenu m, final Project project) {

		m.add(new JMenuItem(new NewMungeProcessAction(swingSession, project)));

		if (project.getType() == ProjectMode.FIND_DUPES) {
			m.add(new JMenuItem(new NewMergeRuleAction(swingSession, project)));
		}
		
		m.addSeparator();
		if (project.getType() == ProjectMode.FIND_DUPES) {
			m.add(new JMenuItem(new AbstractAction("Run Match") {
				public void actionPerformed(ActionEvent e) {
					EngineSettingsPanel f = swingSession.getMatchEnginePanel(project.getMatchingEngine(), project);
					swingSession.setCurrentEditorComponent(f);
				}
			}));
			m.add(new JMenuItem(new AbstractAction("Run Merge") {
				public void actionPerformed(ActionEvent e) {
					EngineSettingsPanel f = swingSession.getMergeEnginePanel(project.getMergingEngine(), project);
					swingSession.setCurrentEditorComponent(f);
				}
			}));
		} else if (project.getType() == ProjectMode.CLEANSE) {
			m.add(new JMenuItem(new AbstractAction("Run Cleanse") {
				public void actionPerformed(ActionEvent e) {
					EngineSettingsPanel f = swingSession.getCleanseEnginePanel(project.getCleansingEngine(), project);
					swingSession.setCurrentEditorComponent(f);
				}
			}));
		} else if (project.getType() == ProjectMode.ADDRESS_CORRECTION) {
			m.add(new JMenuItem(new AbstractAction("Run Address Correction") {
				public void actionPerformed(ActionEvent e) {
					EngineSettingsPanel f = swingSession.getAddressCorrectionEnginePanel(project.getAddressCorrectionEngine(), project);
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
		m.add(new JMenuItem(new ExportProjectAction(swingSession, owningFrame)));
		m.add(new JMenuItem(new ImportProjectAction(swingSession, owningFrame)));

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
// 		TODO: Implement Cross-referencing projects first before re-enabling this menu item
//		mm.add(new JMenuItem(new NewProjectAction(swingSession, "New X-ref Project", Project.ProjectMode.BUILD_XREF)));
		mm.add(new JMenuItem(new NewProjectAction(swingSession, "New Address Correction Project", Project.ProjectMode.ADDRESS_CORRECTION)));
		m.add(mm);
		
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
		if (alreadyHandlingEvent) {
			logger.debug("Already handling an event. Returning with no effect.", new Exception());
			tree.setSelectionPath(null);
			return;
		}
		
		if (tree.getSelectionPath() == null) {
			logger.debug("Nothing selected, so return.");
			return;
		}
		TreePath tp = e.getPath();
		if (tp != null) {
			Object o = tp.getLastPathComponent();
			try {
				alreadyHandlingEvent = true;
				if (o instanceof PlFolder) {
					FolderEditor editor = new FolderEditor(swingSession,
							(PlFolder) o);
					swingSession.setCurrentEditorComponent(editor);
				} else if (o instanceof Project) {
					AbstractAction cancelAction = new AbstractAction("Cancel") {
						public void actionPerformed(final ActionEvent e) {
							swingSession.setCurrentEditorComponent(null);
						}
					};
					
					ProjectEditor me;
					me = new ProjectEditor(swingSession, (Project) o,
							(PlFolder) ((Project) o).getParent(),
							cancelAction);

					swingSession.setCurrentEditorComponent(me);

				} else if (o instanceof MungeProcess) {
					if (swingSession.getOldPane() instanceof MungeProcessEditor) {
						MungeProcessEditor originalPane = (MungeProcessEditor) swingSession.getOldPane();
						if (originalPane.getProcess() == o) {
							return;
						}
					}
					Project m = ((MungeProcess) o).getParent();
					MungeProcessEditor editor = new MungeProcessEditor(swingSession, m, (MungeProcess) o);
					logger.debug("Created new munge process editor "
							+ System.identityHashCode(editor));
					swingSession.setCurrentEditorComponent(editor);
				} else if (o instanceof MungeStep) {
					MungeProcess mp = (MungeProcess)((MungeStep) o).getParent();
					Project m = mp.getParent();
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
					Project m = mp.getParent();
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
				} else if (o instanceof FolderNode) {
					FolderNode f = (FolderNode) o;
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
					Project m = (Project) f.getParent();
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
					Project m = (Project) f.getParent();
					MergeColumnRuleEditor editor = new MergeColumnRuleEditor(swingSession, m, f);
					logger.debug("Created new merge column rules editor " + System.identityHashCode(editor));
					swingSession.setCurrentEditorComponent(editor);
					editor.setSelectedColumn(columnMergeRule);
				} else if (o instanceof ProjectActionNode) {
					ProjectActionNode node = (ProjectActionNode) o;
					if (node.getActionType() == ProjectActionType.RUN_MATCH) {
						swingSession
								.setCurrentEditorComponent(swingSession.getMatchEnginePanel(node.getProject().getMatchingEngine(), node.getProject()));
					} else if (node.getActionType() == ProjectActionType.AUDIT_INFO) {
						swingSession
								.setCurrentEditorComponent(new ProjectInfoEditor(
										node.getProject()));
					} else if (node.getActionType() == ProjectActionType.VALIDATE_MATCHES) {
						if (node.getProject().getResultTable() == null) {
							throw new Exception(
									"Match result table does not exist!");
						}
						swingSession
								.setCurrentEditorComponent(new MatchResultVisualizer(
										node.getProject(), swingSession));
					} else if (node.getActionType() == ProjectActionType.VALIDATION_STATUS) {
						swingSession
								.setCurrentEditorComponent(new MatchValidationStatus(
										swingSession, node.getProject()));
					} else if (node.getActionType() == ProjectActionType.RUN_MERGE) {
						swingSession.setCurrentEditorComponent(swingSession.getMergeEnginePanel(
								node.getProject().getMergingEngine(), node.getProject()));
					} else if (node.getActionType() == ProjectActionType.RUN_CLEANSING) {
						swingSession.setCurrentEditorComponent(swingSession.getCleanseEnginePanel(node.getProject().getCleansingEngine(), node.getProject()));
					} else if (node.getActionType() == ProjectActionType.RUN_ADDRESS_CORRECTION) {
						swingSession.setCurrentEditorComponent(swingSession.getAddressCorrectionEnginePanel(node.getProject().getAddressCorrectionEngine(), node.getProject()));
					} else if (node.getActionType() == ProjectActionType.VALIDATE_ADDRESSES) {
						AddressPool pool = new AddressPool(node.getProject());
						ProgressMonitor monitor = new ProgressMonitor(owningFrame, "", "", 0, 100);
						AddressPoolLoadingWorker addressPoolLoadingWorker = new AddressPoolLoadingWorker(pool, swingSession);
						ProgressWatcher.watchProgress(monitor, addressPoolLoadingWorker);
						Thread workerThread = new Thread(addressPoolLoadingWorker);
						workerThread.start();
					} else if (node.getActionType() == ProjectActionType.COMMIT_VALIDATED_ADDRESSES) {
						swingSession.setCurrentEditorComponent(swingSession.getValidatedAddressCommittingEnginePanel(node.getProject().getAddressCommittingEngine(), node.getProject()));
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
			} finally {
				alreadyHandlingEvent = false;
			}
		}
	}

	
}