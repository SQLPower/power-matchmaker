package ca.sqlpower.matchmaker.swingui;

import javax.swing.event.ListDataEvent;

public interface FolderListChangeListener {
	public void folderAdded(ListDataEvent e);
	public void folderRemove(ListDataEvent e);
}
