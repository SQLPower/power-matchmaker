package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;

public class MatchMakerTreeCellRenderer extends DefaultTreeCellRenderer {

	final private Icon matchIcon = new ImageIcon(getClass().getResource("/icons/gears_16.png"));
	final private Icon groupIcon = new ImageIcon(getClass().getResource("/icons/gear_16.png"));

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		if ( value instanceof MatchMakerTreeModel.MMTreeNode ) {
			if ( ((MatchMakerTreeModel.MMTreeNode)value).isRoot() ) {
				setText("");
			} else {
				setText(((MatchMakerTreeModel.MMTreeNode)value).getName());
			}
		}
		if (value instanceof PlMatch) {
			setIcon(matchIcon);
		} else if (value instanceof PlMatchGroup) {
			setIcon(groupIcon);
		}
		return this;
	}

}
