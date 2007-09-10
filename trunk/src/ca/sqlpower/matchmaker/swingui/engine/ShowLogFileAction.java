package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JTextField;

import com.darwinsys.notepad.Notepad;

/**
 * An action for displaying the contents of a text file in a popup
 * window.  Which file to show is determined by the contents of a text
 * field.
 */
class ShowLogFileAction extends AbstractAction {

	/**
	 * The text field that contains the pathname of the file to view.
	 */
	private final JTextField filenameField;
	
	/**
	 * Creates a new action that tries to load and display the text file at
	 * the pathname in the text field given.
	 * 
	 * @param filenameField The field that contains the pathname of the file
	 * to read.  This text field will be asked for its contents every time the
	 * action is invoked, in case its contents change over time.
	 */
	public ShowLogFileAction(JTextField filenameField) {
		super("Show Log File...");
		this.filenameField = filenameField;
	}

	public void actionPerformed(ActionEvent e) {
		String logFileName = filenameField.getText();
		try {
			Notepad notepad = new Notepad(false);
			notepad.doLoad(logFileName);
		} catch (IOException e1) {
			throw new RuntimeException("Unable to view log file "
					+ logFileName, e1);
		}

	}
}