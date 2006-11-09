package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.PlMatchGroupPanel;

/**
 * This action creates a PLMatchGroupPanel and puts it in a popup dialog.
 */
public class EditMatchGroupAction extends AbstractAction {

    private final MatchMakerSwingSession swingSession;
    private final PlMatchGroup matchGroup;
	private final Window window;

	public EditMatchGroupAction(
            MatchMakerSwingSession swingSession,
            PlMatchGroup matchGroup,
            Window parentWindow) {
		super("Edit Match Group");
        this.swingSession = swingSession;
		this.matchGroup = matchGroup;
		this.window = parentWindow;
	}

	/**
	 * Creates and shows the dialog
	 */
	public void actionPerformed(ActionEvent e)  {
		try {
			JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
                    new PlMatchGroupPanel(swingSession, matchGroup),
                    window, "Edit Match Group", "Save Match Group");
			d.setVisible(true);
		} catch (ArchitectException e1) {
			throw new ArchitectRuntimeException(e1);
		}
	}

}
