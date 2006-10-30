package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JFrame;

/**
 * Intended to provide a help action for the main program;
 * for now it's just a placeholder.
 */
public class HelpAction extends AbstractAction {
	JFrame parent;
	public HelpAction(JFrame parent) {
		this.parent = parent;
		super.putValue(AbstractAction.NAME, "Help");
	}

	public void actionPerformed(ActionEvent e) {
		// XXX Hook up real help someday.
		JOptionPane.showMessageDialog(parent,
				"Help is not yet available. We apologize for the inconvenience");
	}
};
