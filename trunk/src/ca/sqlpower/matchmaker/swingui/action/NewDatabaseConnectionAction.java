package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.DBCSPanel;
import ca.sqlpower.architect.swingui.DBConnectionCallBack;
import ca.sqlpower.architect.swingui.action.DBCS_OkAction;
import ca.sqlpower.matchmaker.swingui.MatchMakerFrame;
import ca.sqlpower.matchmaker.swingui.NewDatabaseConnectionParent;

public class NewDatabaseConnectionAction extends AbstractAction {

	private NewDatabaseConnectionParent parent = null;
	private Component componentParent = null;
	private DBConnectionCallBack callBackParent = null;

	public NewDatabaseConnectionAction() {
		super();
	}

	public NewDatabaseConnectionAction(String name) {
		super(name);
	}

	public NewDatabaseConnectionAction(String name, Icon icon) {
		super(name, icon);
	}

	public void actionPerformed(ActionEvent e) {
		if ( parent != null ) {
			if (parent.getNewConnectionDialog() != null) {
				parent.getNewConnectionDialog().requestFocus();
				return;
			}
		}
		final DBCSPanel dbcsPanel = new DBCSPanel();
		dbcsPanel.setDbcs(new ArchitectDataSource());

		DBCS_OkAction okAction = new DBCS_OkAction(dbcsPanel,
				true,
				MatchMakerFrame.getMainInstance().getUserSettings().getPlDotIni());
		if ( callBackParent != null ) {
			okAction.setConnectionSelectionCallBack(callBackParent);
		}
		Action cancelAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dbcsPanel.discardChanges();
				if ( parent != null ) {
					parent.setNewConnectionDialog(null);
				}
			}
		};

		JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
				dbcsPanel, SwingUtilities.getWindowAncestor(
						componentParent),
						"New Database Connection",
						ArchitectPanelBuilder.OK_BUTTON_LABEL,
						okAction, cancelAction);

		okAction.setConnectionDialog(d);
		if ( parent != null ) {
			parent.setNewConnectionDialog(d);
		}
		d.setVisible(true);
	}

	public NewDatabaseConnectionParent getParent() {
		return parent;
	}

	public void setParent(NewDatabaseConnectionParent parent) {
		this.parent = parent;
	}

	public DBConnectionCallBack getCallBack() {
		return callBackParent;
	}

	public void setCallBack(DBConnectionCallBack callBack) {
		this.callBackParent = callBack;
	}

	public Component getComponentParent() {
		return componentParent;
	}

	public void setComponentParent(Component componentParent) {
		this.componentParent = componentParent;
	}

}
