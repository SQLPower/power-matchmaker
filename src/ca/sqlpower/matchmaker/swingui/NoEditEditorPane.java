package ca.sqlpower.matchmaker.swingui;

import javax.swing.JComponent;
/**
 * Editor Pane for those panels that have nothing to edit.
 */
class NoEditEditorPane implements EditorPane {

	/**
	 * The non-editable display for this EditorPane.
	 */
	private JComponent panel;
	
	public NoEditEditorPane(JComponent panel) {
		this.panel =panel;
	}
	
	/**
	 * doSave() is supposed to return the succesfull-ness of a save operation.
	 * Since nothing changes, nothing needs to be saved, so we just say that
	 * saving worked.
	 */
	public boolean doSave() {
		return true;
	}

	/**
	 * Always returns false because, since nothing is being edited, there are
	 * never changes, nevermind changes that haven't been saved.
	 */
	public boolean hasUnsavedChanges() {
		return false;
	}

	public JComponent getPanel() {
		return panel;
	}
	
	public void setPanel(JComponent panel) {
		this.panel = panel;
	}
}
