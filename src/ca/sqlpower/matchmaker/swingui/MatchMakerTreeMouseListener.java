package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;
import java.awt.Menu;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.swingui.action.EditMatchGroupAction;

public class MatchMakerTreeMouseListener implements MouseListener {

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {


	}
	JPopupMenu m;
	Component source;
	public void mousePressed(MouseEvent e) {
		makePopup(e);
	}

	private void makePopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			m = new JPopupMenu();
			JTree t = (JTree) e.getSource();
			source = t;
			int row = t.getRowForLocation(e.getX(),e.getY());
			TreePath tp = t.getPathForRow(row);
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
		Component c = source;
		while(!(c instanceof Window ) && c !=null){
			c = c.getParent();
		}
		m.add(new JMenuItem(new EditMatchGroupAction(group,(Window) c)));
	}

	private void createMatchMenu(final PlMatch match) {
		m.add(new JMenuItem(new AbstractAction("Edit Match"){

			public void actionPerformed(ActionEvent e) {
				MatchEditor me = new MatchEditor(match);
				me.pack();
				me.setVisible(true);
			}}));
		m.add(new JMenuItem("New Match Group"));
	}

	private void createFolderMenu(PlFolder folder) {
		m.add(new JMenuItem("New Match"));
	}

	public void mouseReleased(MouseEvent e) {
		makePopup(e);

	}



}
