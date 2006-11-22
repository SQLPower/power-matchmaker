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
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.swingui.action.NewMatchAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchImportAction;
import ca.sqlpower.matchmaker.swingui.action.Refresh;
import ca.sqlpower.matchmaker.swingui.action.ShowMatchStatisticInfoAction;

public class MatchMakerTreeMouseListener extends MouseAdapter {

    private final MatchMakerSwingSession swingSession;

    private final JFrame owningFrame;

    private JPopupMenu m;

    public MatchMakerTreeMouseListener(MatchMakerSwingSession swingSession) {
        this.swingSession = swingSession;
        this.owningFrame = swingSession.getFrame();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (MouseEvent.BUTTON1 == e.getButton()) {
            JTree t = (JTree) e.getSource();
            int row = t.getRowForLocation(e.getX(), e.getY());
            TreePath tp = t.getPathForRow(row);
            if (tp != null) {
                Object o = tp.getLastPathComponent();
                if (o instanceof Match) {

                    MatchEditor me;
                    try {
                        me = new MatchEditor(swingSession,(Match) o,
                        		(PlFolder<Match>)((Match) o).getParent());
                    } catch (ArchitectException e1) {
                        throw new ArchitectRuntimeException(e1);
                    }

                    swingSession.setCurrentEditorComponent(me.getPanel());

                } else if (o instanceof MatchMakerCriteriaGroup ) {
                	Match m = ((MatchMakerCriteriaGroup)o).getParentMatch();
                    try {
                        MatchMakerCriteriaGroupEditor editor = 
                        	new MatchMakerCriteriaGroupEditor(
                        			swingSession,
                        			m, (MatchMakerCriteriaGroup) o);
                        swingSession.setCurrentEditorComponent(editor.getPanel());
                    } catch (ArchitectException e1) {
                        throw new ArchitectRuntimeException(e1);
                    }
                }
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
                if (o instanceof PlFolder) {
                    createFolderMenu((PlFolder) o);
                } else if (o instanceof Match) {
                    createMatchMenu((Match) o);
                } else if (o instanceof MatchMakerCriteriaGroup) {
                    createMatchGroupMenu((MatchMakerCriteriaGroup) o);
                }
            }
            m.show(t, e.getX(), e.getY());
        }
    }

    private void createMatchGroupMenu(MatchMakerCriteriaGroup group) {
        m.add(new JMenuItem("need code"));
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
        m.add(new JMenuItem(new ShowMatchStatisticInfoAction(match, owningFrame)));
        m.addSeparator();
        // TODO add this back in m.add(new JMenuItem(new PlMatchExportAction(match)));
        m.add(new JMenuItem(new PlMatchImportAction(swingSession, owningFrame)));
    }

    private void createFolderMenu(final PlFolder folder) {
        m.add(new JMenuItem(new NewMatchAction(swingSession, "New Match", folder)));
        m.add(new JMenuItem(new PlMatchImportAction(swingSession, owningFrame)));
    }

}