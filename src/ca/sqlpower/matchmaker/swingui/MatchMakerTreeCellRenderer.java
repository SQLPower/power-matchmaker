package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerObject;

public class MatchMakerTreeCellRenderer extends DefaultTreeCellRenderer {

	final private Icon matchIcon = new ImageIcon(getClass().getResource("/icons/gears_16.png"));
	final private Icon groupIcon = new ImageIcon(getClass().getResource("/icons/gear_16.png"));

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

        String text;
        if (value instanceof MatchMakerObject) {
            text = (((MatchMakerObject) value).getName());
        } else {
            text = value.toString();
        }
        
		super.getTreeCellRendererComponent(tree, text, selected, expanded, leaf, row, hasFocus);

		if (value instanceof Match) {
			setIcon(matchIcon);
		} else if (value instanceof MatchMakerCriteriaGroup) {
			setIcon(groupIcon);
		}
		return this;
	}

}
