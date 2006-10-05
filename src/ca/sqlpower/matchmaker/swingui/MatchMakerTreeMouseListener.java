package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.swingui.action.DeleteMatchGroupAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchExportAction;
import ca.sqlpower.matchmaker.swingui.action.PlMatchImportAction;
import ca.sqlpower.matchmaker.swingui.action.Refresh;

public class MatchMakerTreeMouseListener implements MouseListener {

	private JSplitPane splitPane;
	public MatchMakerTreeMouseListener(JSplitPane splitPane) {
		this.splitPane = splitPane;
	}
	public void mouseClicked(MouseEvent e) {
		
		if ( e.BUTTON1 == e.getButton()) {
			JTree t = (JTree) e.getSource();
			source = t;
			int row = t.getRowForLocation(e.getX(),e.getY());
			TreePath tp = t.getPathForRow(row);
			if (tp != null) {
				Object o = tp.getLastPathComponent();
				if (o instanceof PlMatch) {

					MatchEditor me;
					me = new MatchEditor((PlMatch) o,null,splitPane);
					
					splitPane.setRightComponent(me.getPanel());
				
				}else if (o instanceof PlMatchGroup){
					try {
						PlMatchGroupPanel panel = new PlMatchGroupPanel((PlMatchGroup) o);
						splitPane.setRightComponent(panel);
					} catch (ArchitectException e1) {
						throw new ArchitectRuntimeException(e1);
					}
				}
			}
		}

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {


	}
	JPopupMenu m;
	Component source;
	public void mousePressed(MouseEvent e) {
		makePopup(e);

		JTree t = (JTree) e.getSource();
		int row = t.getRowForLocation(e.getX(),e.getY());
		TreePath tp = t.getPathForRow(row);
		if ( tp != null ) {
			t.setSelectionPath(tp);
		}
	}

	private void makePopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			m = new JPopupMenu();
			JTree t = (JTree) e.getSource();
			source = t;
			int row = t.getRowForLocation(e.getX(),e.getY());
			TreePath tp = t.getPathForRow(row);
			m.add(new JMenuItem(new Refresh()));
			if (tp != null) {
				Object o = tp.getLastPathComponent();
				if(o instanceof PlFolder){
					createFolderMenu((PlFolder) o);
				} else if (o instanceof PlMatch){
					createMatchMenu((PlMatch) o);
				} else if (o instanceof PlMatchGroup){
					createMatchGroupMenu((PlMatchGroup) o);
				}
			}
			m.show(t, e.getX(), e.getY());
		}
	}

	private void createMatchGroupMenu(PlMatchGroup group) {
		Window c = getWindow();
		
		m.add(new JMenuItem(new DeleteMatchGroupAction(group,splitPane)));
	}

	private Window getWindow() {
		Component c = source;
		while(!(c instanceof Window ) && c !=null){
			c = c.getParent();
		}
		return (Window) c;
	}

	private void createMatchMenu(final PlMatch match) {



		m.add(new JMenuItem(new AbstractAction("Run Match"){

            public void actionPerformed(ActionEvent e) {
                RunMatchPanel f = new RunMatchPanel(match);
                f.pack();
                f.setVisible(true);
            }}));



		m.addSeparator();
		m.add(new JMenuItem(new AbstractAction("Audit Information"){
			public void actionPerformed(ActionEvent e) {
				MatchInfoPanel p = new MatchInfoPanel(match);
				JDialog d = ArchitectPanelBuilder.createSingleButtonArchitectPanelDialog(
						p,MatchMakerFrame.getMainInstance(),
						"Audit Information","OK");
				d.pack();
				d.setVisible(true);
			}}));
		m.addSeparator();
		m.add(new JMenuItem(new PlMatchExportAction(match)));
		m.add(new JMenuItem(new PlMatchImportAction()));
	}

	private void createFolderMenu(final PlFolder folder) {
        m.add(new JMenuItem(new AbstractAction("New Match"){

            public void actionPerformed(ActionEvent e) {
                MatchEditor me;
				me = new MatchEditor(null,folder,splitPane);
				me.pack();
				me.setVisible(true);
            }}));

        m.add(new JMenuItem(new PlMatchImportAction()));
	}

	public void mouseReleased(MouseEvent e) {
		makePopup(e);

	}



}