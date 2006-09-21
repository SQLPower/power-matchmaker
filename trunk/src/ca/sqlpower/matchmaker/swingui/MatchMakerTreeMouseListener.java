package ca.sqlpower.matchmaker.swingui;

import java.awt.Menu;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;

public class MatchMakerTreeMouseListener implements MouseListener {

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {


	}
	JPopupMenu m;
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			JTree t = (JTree) e.getSource();
			int row = t.getRowForLocation(e.getX(),e.getY());
			TreePath tp = t.getPathForRow(row);
			Object o = tp.getLastPathComponent();
			m = new JPopupMenu();
			if(o instanceof PlFolder){
				createFolderMenu((PlFolder) o);
			} else if (o instanceof PlMatch){
				createMatchMenu((PlMatch) o);
			} else if (o instanceof PlMatchGroup){
				createMatchGroupMenu((PlMatchGroup) o);
			}
			m.show(t, e.getX(), e.getY());
		}
	}

	private void createMatchGroupMenu(PlMatchGroup group) {
		m.add(new JMenuItem("Edit Match Group"));
	}

	private void createMatchMenu(PlMatch match) {
		m.add(new JMenuItem("Edit Match"));
		m.add(new JMenuItem("New Match Group"));
	}

	private void createFolderMenu(PlFolder folder) {
		m.add(new JMenuItem("New Match"));
	}

	public void mouseReleased(MouseEvent e) {


	}

}
