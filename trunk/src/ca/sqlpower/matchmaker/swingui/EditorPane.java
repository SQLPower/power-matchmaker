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

	/** Perform the editor save.
     * 
     * <p><b>IMPORTANT NOTE:</b> Make sure this method does not blindly return true
     * just so that it has a valid return type, it is essiental that it
     * returns if the object is saved properly or not.  This is required
     * since if the save does fail, the swing session needs to know to restore
     * the interface back and reselect the lastTreePath in the JTree.  You have
     * officially been warned...
     * </p>
     * @return the success of the saving process (do not fake it!)
	 */                
	public boolean doSave();

	/** Retrieve the Editor's visual component */
	public JComponent getPanel();
}
