package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.swingui.TranslatePanel;

public class EditTranslateAction extends AbstractAction {

	private Window parentWindow;

	public EditTranslateAction(Window parentWindow) {
		super("Translate Words Manager");
		this.parentWindow = parentWindow;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		JDialog dialog = ArchitectPanelBuilder.createSingleButtonArchitectPanelDialog(new TranslatePanel(), parentWindow, "Words in Translate","Close");
		dialog.setVisible(true);
	}

}
