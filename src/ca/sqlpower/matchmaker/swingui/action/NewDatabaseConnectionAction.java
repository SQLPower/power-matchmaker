package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.DBCSPanel;
import ca.sqlpower.architect.swingui.DBConnectionCallBack;
import ca.sqlpower.architect.swingui.action.DBCSOkAction;
import ca.sqlpower.matchmaker.swingui.DBConnectionUniDialog;
import ca.sqlpower.matchmaker.swingui.SwingSessionContext;

public class NewDatabaseConnectionAction extends AbstractAction {

	private DBConnectionUniDialog uniDialogParent = null;
	private Component componentParent = null;
	private DBConnectionCallBack callBackParent = null;
    private SwingSessionContext context;

	public NewDatabaseConnectionAction(SwingSessionContext context, String name) {
		super(name);
		this.context = context;
	}

	public void actionPerformed(ActionEvent e) {
		if ( uniDialogParent != null ) {
			if (uniDialogParent.getNewConnectionDialog() != null && uniDialogParent.getNewConnectionDialog().isVisible()) {
				uniDialogParent.getNewConnectionDialog().requestFocus();
				return;
			}
		}
		final DBCSPanel dbcsPanel = new DBCSPanel();
		dbcsPanel.setDbcs(new ArchitectDataSource());

		DBCSOkAction okAction = new DBCSOkAction(dbcsPanel,
				true,
				context.getPlDotIni());
		if ( callBackParent != null ) {
			okAction.setConnectionSelectionCallBack(callBackParent);
		}
		Action cancelAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				dbcsPanel.discardChanges();
				if ( uniDialogParent != null ) {
					uniDialogParent.setNewConnectionDialog(null);
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
		if ( uniDialogParent != null ) {
			uniDialogParent.setNewConnectionDialog(d);
		}
		d.setVisible(true);
	}

	public DBConnectionUniDialog getParent() {
		return uniDialogParent;
	}

	public void setParent(DBConnectionUniDialog parent) {
		this.uniDialogParent = parent;
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
