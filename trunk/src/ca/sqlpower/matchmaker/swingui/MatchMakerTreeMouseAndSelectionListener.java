package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

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
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteria;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.swingui.action.DeleteMatchAction;
import ca.sqlpower.matchmaker.swingui.action.DeleteMatchCriteria;
import ca.sqlpower.matchmaker.swingui.action.DeleteMatchGroupAction;
import ca.sqlpower.matchmaker.swingui.action.DeletePlFolderAction;
import ca.sqlpower.matchmaker.swingui.action.DuplicateMatchAction;
import ca.sqlpower.matchmaker.swingui.action.NewMatchAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchExportAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchImportAction;
import ca.sqlpower.matchmaker.swingui.action.Refresh;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;

public class MatchMakerTreeMouseAndSelectionListener extends MouseAdapter implements TreeSelectionListener {

	private static final Logger logger = Logger.getLogger(MatchMakerTreeMouseAndSelectionListener.class);

    private final MatchMakerSwingSession swingSession;

    private final JFrame owningFrame;

    private JPopupMenu m;

    public MatchMakerTreeMouseAndSelectionListener(MatchMakerSwingSession swingSession) {
        this.swingSession = swingSession;
        this.owningFrame = swingSession.getFrame();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

	private void itemSelected(TreeSelectionEvent e) {
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
					try {
						me = new MatchEditor(swingSession, (Match) o,
								(PlFolder<Match>) ((Match) o).getParent());
					} catch (ArchitectException e1) {
						throw new ArchitectRuntimeException(e1);
					}

					swingSession.setCurrentEditorComponent(me);

				} else if (o instanceof MatchMakerCriteriaGroup) {
					Match m = ((MatchMakerCriteriaGroup) o).getParentMatch();
					try {
						MatchMakerCriteriaGroupEditor editor = new MatchMakerCriteriaGroupEditor(
								swingSession, m, (MatchMakerCriteriaGroup) o);
						logger.debug("Created new match group editor "
								+ System.identityHashCode(editor));
						swingSession.setCurrentEditorComponent(editor);
					} catch (ArchitectException e1) {
						throw new ArchitectRuntimeException(e1);
					}
				}
			} catch (SQLException e1) {
				throw new RuntimeException(e1);
			}
		}
	}
	

    @Override
    public void mousePressed(MouseEvent e) {
        makePopup(e);

        JTree t = (JTree) e.getSource();
        int row = t.getRowForLocation(e.getX(), e.getY());
        TreePath tp = t.getPathForRow(row);
        if (tp != null) {
            t.setSelectionPath(tp);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        makePopup(e);
    }

    private void makePopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            m = new JPopupMenu();
            JTree t = (JTree) e.getSource();
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
                		createNewFolderMenuItem();
                	}
                } else if (o instanceof PlFolder) {
                    createFolderMenu((PlFolder) o);
                } else if (o instanceof Match) {
                    createMatchMenu((Match) o);
                } else if (o instanceof MatchMakerCriteriaGroup) {
                    createMatchGroupMenu((MatchMakerCriteriaGroup) o);
                } else if (o instanceof MatchMakerCriteria) {
                    createMatchCriteriaMenu((MatchMakerCriteria) o);
                }
            }
            m.show(t, e.getX(), e.getY());
        }
    }

    private void createMatchCriteriaMenu(MatchMakerCriteria criteria) {
    	m.add(new JMenuItem(new DeleteMatchCriteria(swingSession,criteria)));
    }

	private void createMatchGroupMenu(MatchMakerCriteriaGroup group) {
    	m.add(new JMenuItem(new DeleteMatchGroupAction(swingSession,group)));
    }

    private void createNewFolderMenuItem() {
    	m.add(new JMenuItem(new AbstractAction("New Folder") {
            public void actionPerformed(ActionEvent e) {
            	PlFolder<Match> folder = new PlFolder<Match>();
            	FolderEditor editor = new FolderEditor(swingSession,folder);
            	try {
					swingSession.setCurrentEditorComponent(editor);
				} catch (SQLException e1) {
					throw new RuntimeException(e1);
				}
            }
        }));
    }

    private void createMatchMenu(final Match match) {

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
                JDialog d = ArchitectPanelBuilder
                        .createSingleButtonArchitectPanelDialog(p, owningFrame,
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

    private void createFolderMenu(final PlFolder folder) {
        m.add(new JMenuItem(new NewMatchAction(swingSession, "New Match", folder)));
        m.add(new JMenuItem(new PlMatchImportAction(swingSession, owningFrame)));
        m.add(new JMenuItem(new DeletePlFolderAction(swingSession,"Delete Folder",folder)));
    }

	public void valueChanged(TreeSelectionEvent e) {
		itemSelected(e);
	}

	

}