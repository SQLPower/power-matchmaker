package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

/**
 * This action creates a PLMatchGroupPanel and puts it in a popup dialog.
 */
public class EditMatchGroupAction extends AbstractAction {

    private final MatchMakerSwingSession swingSession;
    private final MatchMakerCriteriaGroup matchGroup;
	private final Window window;

	public EditMatchGroupAction(
            MatchMakerSwingSession swingSession,
            MatchMakerCriteriaGroup matchGroup,
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
		throw new UnsupportedOperationException("need code");
		/*try {
			JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
                    new MatchMakerCriteriaGroupEditor(swingSession, 
                    		matchGroup.getParentMatch(),matchGroup),
                    window, "Edit Match Group", "Save Match Group");
			d.setVisible(true);
		} catch (ArchitectException e1) {
			throw new ArchitectRuntimeException(e1);
		}*/
	}

}
