package ca.sqlpower.matchmaker.swingui;

import javax.swing.JDialog;

public interface DBConnectionUniDialog {
	JDialog getNewConnectionDialog();

	void setNewConnectionDialog(JDialog d);
}
