package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.TranslatePanel;

/**
 * This action creates a TranslatePanel and puts it in a popup dialog with an OK button.
 */
public class EditTranslateAction extends AbstractAction {

    private final MatchMakerSwingSession swingSession;
	private final Window parentWindow;

	public EditTranslateAction(MatchMakerSwingSession swingSession, Window parentWindow) {
		super("Translate Words Manager");
        this.swingSession = swingSession;
		this.parentWindow = parentWindow;
	}
	
	public void actionPerformed(ActionEvent e) {
		JDialog dialog = ArchitectPanelBuilder.createSingleButtonArchitectPanelDialog(
                new TranslatePanel(swingSession), parentWindow, "Words in Translate","Close");
		dialog.setVisible(true);
	}

}
