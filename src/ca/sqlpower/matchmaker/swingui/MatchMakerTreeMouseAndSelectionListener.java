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
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.MatchRule;
import ca.sqlpower.matchmaker.MatchRuleSet;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel.MatchActionNode;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel.MatchActionType;
import ca.sqlpower.matchmaker.swingui.action.DeleteMatchAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteMatchGroupAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteMatchRule;
import ca.sqlpower.matchmaker.swingui.action.DeletePlFolderAction;
import ca.sqlpower.matchmaker.swingui.action.DuplicateMatchAction;
import ca.sqlpower.matchmaker.swingui.action.NewMatchAction;
import ca.sqlpower.matchmaker.swingui.action.NewMatchGroupAction;
import ca.sqlpower.matchmaker.swingui.action.NewMergeRuleAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchExportAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchImportAction;
import ca.sqlpower.matchmaker.swingui.action.Refresh;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.matchmaker.swingui.engine.MatchEnginePanel;
import ca.sqlpower.matchmaker.swingui.engine.MergeEnginePanel;
import ca.sqlpower.swingui.SPSUtils;

/**
 * This appears to be a mouse event listener for the MatchMaker tree component of the GUI.
 * It creates pop-up menus when a popup event is triggered, (ex. right-clicking), and also
 * changes the MatchMaker's main editor component according to the selected item in the tree
 */
public class MatchMakerTreeMouseAndSelectionListener extends MouseAdapter implements TreeSelectionListener {

	private static final Logger logger = Logger.getLogger(MatchMakerTreeMouseAndSelectionListener.class);

    private final MatchMakerSwingSession swingSession;

    private final JFrame owningFrame;

    public MatchMakerTreeMouseAndSelectionListener(MatchMakerSwingSession swingSession) {
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
                if (o instanceof FolderParent ) {
                	MatchMakerTreeModel model = (MatchMakerTreeModel)t.getModel();
                	int index = model.getIndexOfChild(model.getRoot(), o);
                	/*** create folder under current only */
                	if ( index == 0 ) {
                		createNewFolderMenuItem(m);
                	}
                } else if (o instanceof PlFolder) {
                    addFolderMenuItems(m, (PlFolder) o);
                } else if (o instanceof Match) {
                    addMatchMenuItems(m, (Match) o);
                } else if (o instanceof MatchMakerFolder<?>) {
                    MatchMakerFolder<?> folder = (MatchMakerFolder<?>) o;
                    if (folder.getName().equals(Match.MATCH_RULES_FOLDER_NAME)) {
                        addMatchRulesFolderMenuItems(m, folder);
                    }
                    else if (folder.getName().equals(Match.MERGE_RULES_FOLDER_NAME)) {
                    	addMergeRulesFolderMenuItems(m, folder);
                    }
                } else if (o instanceof MatchRuleSet) {
                    addMatchGroupMenuItems(m, (MatchRuleSet) o);
                } else if (o instanceof MatchRule) {
                    addMatchCriteriaMenuItems(m, (MatchRule) o);
                }
            }
            m.show(t, e.getX(), e.getY());
        }
    }

    /**
     * Attaches a menu item for the actions of a match group.
     * 
     * @param m The popup menu that the menu item would be attached onto.
     * @param folder The current folder being right-clicked on.
     */
    private void addMatchRulesFolderMenuItems(JPopupMenu m, MatchMakerFolder<?> folder) {
        m.add(new JMenuItem(new NewMatchGroupAction(swingSession, (Match) folder.getParent())));
    }

    /**
     * Attaches a menu item for the actions of a merge rule.
     * 
     * @param m The popup menu that the menu item would be attached onto.
     * @param folder The current folder being right-clicked on.
     */
    private void addMergeRulesFolderMenuItems(JPopupMenu m, MatchMakerFolder<?> folder) {
        m.add(new JMenuItem(new NewMergeRuleAction(swingSession, (Match) folder.getParent())));
    }

    /**
     * Attaches a menu item for the actions of a match criteria.
     * 
     * @param m The popup menu that the menu item would be attached onto.
     * @param criteria The current folder being right-clicked on.
     */
    private void addMatchCriteriaMenuItems(JPopupMenu m, MatchRule criteria) {
        m.add(new JMenuItem(new DeleteMatchRule(swingSession,criteria)));
    }

    /**
     * Attaches a menu item for the actions of a match rule set.
     * 
     * @param m The popup menu that the menu item would be attached onto.
     * @param group The current folder being right-clicked on.
     */
	private void addMatchGroupMenuItems(JPopupMenu m, MatchRuleSet group) {
    	m.add(new JMenuItem(new DeleteMatchGroupAction(swingSession,group)));
    }

    private void createNewFolderMenuItem(JPopupMenu m) {
    	m.add(new JMenuItem(new AbstractAction("New Folder") {
            public void actionPerformed(ActionEvent e) {
            	PlFolder<Match> folder = new PlFolder<Match>();
            	FolderEditor editor = new FolderEditor(swingSession,folder);
            	swingSession.setCurrentEditorComponent(editor);
            }
        }));
    }

    private void addMatchMenuItems(JPopupMenu m, final Match match) {

        m.addSeparator();
        m.add(new JMenuItem(new NewMatchGroupAction(swingSession, match)));
        
        m.addSeparator();
        m.add(new JMenuItem(new AbstractAction("Run Match") {
            public void actionPerformed(ActionEvent e) {
                MatchEnginePanel f = new MatchEnginePanel(swingSession, match,
                        owningFrame);
                swingSession.setCurrentEditorComponent(f);
            }
        }));
        m.add(new JMenuItem(new AbstractAction("Run Merge") {
            public void actionPerformed(ActionEvent e) {
                MergeEnginePanel f = new MergeEnginePanel(swingSession, match,
                        owningFrame);
                swingSession.setCurrentEditorComponent(f);
            }
        }));

        m.addSeparator();
        m.add(new JMenuItem(new AbstractAction("Audit Information") {
            public void actionPerformed(ActionEvent e) {
            	swingSession.setCurrentEditorComponent(new MatchInfoEditor(match));
            }
        }));
        m.add(new JMenuItem(new ShowMatchStatisticInfoAction(swingSession,match, owningFrame)));

        m.addSeparator();
        m.add(new JMenuItem(new PlMatchExportAction(swingSession, owningFrame)));
        m.add(new JMenuItem(new PlMatchImportAction(swingSession, owningFrame)));

        m.addSeparator();
        m.add(new JMenuItem(new DeleteMatchAction(swingSession,match)));
        m.add(new JMenuItem(new DuplicateMatchAction(swingSession,match)));
        
    }

    private void addFolderMenuItems(JPopupMenu m, final PlFolder folder) {
        m.add(new JMenuItem(new NewMatchAction(swingSession, "New Match")));
        m.add(new JMenuItem(new PlMatchImportAction(swingSession, owningFrame)));
        m.add(new JMenuItem(new DeletePlFolderAction(swingSession,"Delete Folder",folder)));
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
		if ( tree.getSelectionPath() == null ) {
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
				} else if (o instanceof Match) {

					MatchEditor me;
					me = new MatchEditor(swingSession, (Match) o,
							(PlFolder<Match>) ((Match) o).getParent());

					swingSession.setCurrentEditorComponent(me);

				} else if (o instanceof MatchRuleSet) {
					Match m = ((MatchRuleSet) o).getParentMatch();
					MatchRuleSetEditor editor = new MatchRuleSetEditor(
							swingSession, m, (MatchRuleSet) o);
					logger.debug("Created new match group editor "
							+ System.identityHashCode(editor));
					swingSession.setCurrentEditorComponent(editor);
				} else if ( o instanceof MatchMakerFolder ) {
					MatchMakerFolder f = (MatchMakerFolder)o;
					Match m = (Match) f.getParent();
					
					if ( f.getName().equals(Match.MERGE_RULES_FOLDER_NAME) ) {
						MergeTableRuleEditor editor = 
							new MergeTableRuleEditor(swingSession,m);
						logger.debug("Created new merge table rules editor "
								+ System.identityHashCode(editor));
						swingSession.setCurrentEditorComponent(editor);
					}
				} else if ( o instanceof TableMergeRules ) {
					TableMergeRules f = (TableMergeRules)o;
					Match m = (Match) f.getParentMatch();
					
					try {
						MergeColumnRuleEditor editor = new MergeColumnRuleEditor(swingSession,m,f,null);
						logger.debug("Created new merge column rules editor "
								+ System.identityHashCode(editor));
						swingSession.setCurrentEditorComponent(editor);
					} catch (ArchitectException e1) {
						SPSUtils.showExceptionDialogNoReport(owningFrame, 
								"An exception occured while creating the merge column rules editor", e1);
					}
				} else if ( o instanceof ColumnMergeRules ) {
					TableMergeRules f = (TableMergeRules) ((ColumnMergeRules)o).getParent();
					Match m = (Match) f.getParentMatch();
					
					try {
						MergeColumnRuleEditor editor =
							new MergeColumnRuleEditor(swingSession,m,f,
									(ColumnMergeRules)o);
						logger.debug("Created new merge column rules editor "
								+ System.identityHashCode(editor));
						swingSession.setCurrentEditorComponent(editor);
					} catch (ArchitectException e1) {
						SPSUtils.showExceptionDialogNoReport(owningFrame, 
								"An exception occured while creating the merge column rules editor", e1);
					}
				} else if (o instanceof MatchActionNode) {
					MatchActionNode node = (MatchActionNode) o;
					if (node.getActionType() == MatchActionType.RUN_MATCH) {
						swingSession.setCurrentEditorComponent(new MatchEnginePanel(swingSession, node.getMatch(), owningFrame));
					} else if (node.getActionType() == MatchActionType.AUDIT_INFO) {
						swingSession.setCurrentEditorComponent(new MatchInfoEditor(node.getMatch()));
					} else if (node.getActionType() == MatchActionType.VALIDATE_MATCHES) {
			            if (node.getMatch().getResultTable() == null){
			                throw new Exception("Match result table does not exist!");
			            }
			            swingSession.setCurrentEditorComponent(new MatchResultVisualizer(node.getMatch()));
					} else if (node.getActionType() == MatchActionType.VALIDATION_STATUS) {
						swingSession.setCurrentEditorComponent(new MatchValidationStatus(swingSession, node.getMatch()));
					} else if (node.getActionType() == MatchActionType.RUN_MERGE) {
						swingSession.setCurrentEditorComponent(new MergeEnginePanel(swingSession, node.getMatch(), owningFrame));
					}
				}
			} catch (Exception ex) {
				MMSUtils.showExceptionDialog(owningFrame, "Couldn't create editor for selected component", ex);
			}
		}
	}

	

}