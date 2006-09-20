package ca.sqlpower.matchmaker.swingui;

import javax.swing.JDialog;

public interface NewDatabaseConnectionParent {
	JDialog getNewConnectionDialog();

	void setNewConnectionDialog(JDialog d);
}
