package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.hibernate.Transaction;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroupId;
import ca.sqlpower.matchmaker.util.HibernateUtil;

public class NewMatchGroupPanel implements ArchitectPanel {

	PlMatch parent;
	Window window;
	JTextField groupName = new JTextField();
	Color oldBackground = groupName.getBackground();
	JPanel p = new JPanel(new BorderLayout());

	public NewMatchGroupPanel(PlMatch parent, Window window) {
		this.parent = parent;
		this.window = window;
		KeyListener listener = new KeyListener(){

			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				validateForm();
			}

			public void keyTyped(KeyEvent e) {
				validateForm();
			}

		};
		groupName.addKeyListener(listener);
		p.add(new JLabel("Group Name"),BorderLayout.WEST);
		p.add(groupName, BorderLayout.CENTER);
	}

	public boolean applyChanges() {
		// TODO Auto-generated method stub
		if (validateForm()){
			PlMatchGroup matchGroup = new PlMatchGroup(new PlMatchGroupId(parent.getMatchId(),groupName.getText()),parent);
			matchGroup.getPlMatch().getPlMatchGroups().add(matchGroup);
			Transaction tx = HibernateUtil.primarySession().beginTransaction();
			HibernateUtil.primarySession().save(matchGroup);
			tx.commit();
			JDialog d;
			try {
				d = ArchitectPanelBuilder.createArchitectPanelDialog(new PlMatchGroupPanel(matchGroup), window, "Edit Match Group", "Save Match Group");
			} catch (ArchitectException e) {
				throw new ArchitectRuntimeException(e);
			}
			d.setVisible(true);
			return true;
		}
		return false;
	}

	private boolean validateForm() {
		if (groupName.getText().equals("")){
			groupName.setBackground(Color.red);
			return false;
		}
		for (PlMatchGroup g: parent.getPlMatchGroups()){
			if (groupName.getText().equals(g.getId().getGroupId())){
				groupName.setBackground(Color.red);
				return false;
			}
		}
		groupName.setBackground(oldBackground);
		return true;
	}

	public void discardChanges() {

	}

	public JComponent getPanel() {
		return p;
	}

}
