package ca.sqlpower.matchmaker.swingui;

import javax.swing.JComponent;
/**
 * Editor Pane for those panels that have nothing to edit
 */
class NoEditEditorPane implements EditorPane {

	private JComponent panel;
	
	public NoEditEditorPane(JComponent panel) {
		this.panel =panel;
	}
	public boolean doSave() {
		return true;
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return false;
	}

	public void setPanel(JComponent panel) {
		this.panel = panel;
	}
}
