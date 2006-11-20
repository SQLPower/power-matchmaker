package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This is for actions that aren't written yet.
 * It will be removed when the application is completed;
 * only used in about half a dozen places now...
 */
public class DummyAction extends AbstractAction {

	private String label;
	private JFrame parent;

	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(parent,
			String.format("The %s action is not yet implemented", label),
			"Apologies",
			JOptionPane.INFORMATION_MESSAGE);
	}

	public DummyAction(JFrame parent, String label) {
		super(label);
		this.label = label;
		this.parent = parent;
	}

}
