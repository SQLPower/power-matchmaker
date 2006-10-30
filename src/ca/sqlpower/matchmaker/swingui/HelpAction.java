package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * Intended to provide a help action for the main program;
 * for now it's just a placeholder.
 */
public class HelpAction extends AbstractAction {

	public HelpAction() {
		super.putValue(AbstractAction.NAME, "Help");
	}

	public void actionPerformed(ActionEvent e) {
		// XXX Hook up real help someday.
		JOptionPane.showMessageDialog(null,
				"Help is not yet available. We apologize for the inconvenience");
	}
};
