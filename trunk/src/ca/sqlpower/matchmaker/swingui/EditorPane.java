package ca.sqlpower.matchmaker.swingui;

import javax.swing.JComponent;

/**
 * The interface (that should be) implmented by all the Editors
 * that are to appear in the "right side" editor pane area.
 *
 */
public interface EditorPane {
	/** True if this Pane has any changes; will usually delegate
	 * to the Panel's Validator's hasValidated() method.
	 * @return
	 */
	public boolean hasUnsavedChanges();

	/** Perform the editor save */
	public boolean doSave();

	/** Retrieve the Editor's visual component */
	public JComponent getPanel();
}
