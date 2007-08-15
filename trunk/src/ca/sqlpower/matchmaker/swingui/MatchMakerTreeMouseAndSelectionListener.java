package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
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
import ca.sqlpower.matchmaker.MatchMakerCriteria;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerFolder;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.swingui.action.DeleteMatchAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteMatchCriteria;
import ca.sqlpower.matchmaker.swingui.action.DeleteMatchGroupAction;
import ca.sqlpower.matchmaker.swingui.action.DeletePlFolderAction;
import ca.sqlpower.matchmaker.swingui.action.DuplicateMatchAction;
import ca.sqlpower.matchmaker.swingui.action.NewMatchAction;
import ca.sqlpower.matchmaker.swingui.action.NewMatchGroupAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchExportAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchImportAction;
import ca.sqlpower.matchmaker.swingui.action.Refresh;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
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
                } else if (o instanceof MatchMakerCriteriaGroup) {
                    addMatchGroupMenuItems(m, (MatchMakerCriteriaGroup) o);
                } else if (o instanceof MatchMakerCriteria) {
                    addMatchCriteriaMenuItems(m, (MatchMakerCriteria) o);
                }
            }
            m.show(t, e.getX(), e.getY());
        }
    }

    private void addMatchRulesFolderMenuItems(JPopupMenu m, MatchMakerFolder<?> folder) {
        m.add(new JMenuItem(new NewMatchGroupAction(swingSession, (Match) folder.getParent())));
    }

    private void addMatchCriteriaMenuItems(JPopupMenu m, MatchMakerCriteria criteria) {
        m.add(new JMenuItem(new DeleteMatchCriteria(swingSession,criteria)));
    }

	private void addMatchGroupMenuItems(JPopupMenu m, MatchMakerCriteriaGroup group) {
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
                RunMatchDialog f = new RunMatchDialog(swingSession, match,
                        owningFrame);
                f.pack();
                f.setVisible(true);
            }
        }));

        m.addSeparator();
        m.add(new JMenuItem(new AbstractAction("Audit Information") {
            public void actionPerformed(ActionEvent e) {
                MatchInfoPanel p = new MatchInfoPanel(match);
                JDialog d = DataEntryPanelBuilder
                        .createSingleButtonDataEntryPanelDialog(p, owningFrame,
                                "Audit Information", "OK");
                d.pack();
                d.setVisible(true);
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

				} else if (o instanceof MatchMakerCriteriaGroup) {
					Match m = ((MatchMakerCriteriaGroup) o).getParentMatch();
					MatchMakerCriteriaGroupEditor editor = new MatchMakerCriteriaGroupEditor(
							swingSession, m, (MatchMakerCriteriaGroup) o);
					logger.debug("Created new match group editor "
							+ System.identityHashCode(editor));
					swingSession.setCurrentEditorComponent(editor);
				} else if ( o instanceof MatchMakerFolder ) {
					MatchMakerFolder f = (MatchMakerFolder)o;
					Match m = (Match) f.getParent();
					
					if ( f.getName().equals(m.MERGE_RULES_FOLDER_NAME) ) {
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
				}
			} catch (Exception ex) {
				MMSUtils.showExceptionDialog(owningFrame, "Couldn't create editor for selected component", ex);
			}
		}
	}

	

}