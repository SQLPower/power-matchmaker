/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.SPSUtils;

public class MMSUtils {
	
	private static final Logger logger = Logger.getLogger(MMSUtils.class);
	
    /**
     * Returns an icon that is suitable for use as a frame icon image
     * in the MatchMaker.
     */
    public static ImageIcon getFrameImageIcon() {
        return SPSUtils.createIcon("pp_24", "Project Planner Logo");
    }

    /**
     * 
     * Displays a dialog box with the given message and exception,
     * allowing the user to examine the stack trace.  The dialog will
     * not have a parent component so it will be displayed on top of 
     * everything.
     * 
     * @deprecated This method will create a dialog, but because it
     * has no parent component, it will stay over everything.
     * 
     * @param message A user visible string that should explain the problem
     * @param t The exception that warranted a dialog
     */
    public static JDialog showExceptionDialogNoReport(String message, Throwable t) {
        JFrame f = new JFrame();
        f.setIconImage(getFrameImageIcon().getImage());
        return SPSUtils.showExceptionDialogNoReport(f, message, t);
    }

    /**
	 * Displays a dialog box with the given message and exception, allowing the
	 * user to examine the stack trace.
	 * <p>
	 * Also attempts to post an anonymous description of the error to a central
	 * reporting server.
	 * 
	 * @param parent
	 *            The parent window to the error window that this method makes
	 * @param message
	 *            A user visible string that should describe the problem
	 * @param t
	 *            The exception that warranted a dialog
	 */
    public static JDialog showExceptionDialog(Component parent, String message, Throwable t) {
        return SPSUtils.showExceptionDialogNoReport(parent, message, t);
    }
    
    /**
	 * Searches the given tree's selection path for a Node of the given type.
	 * Returns the first one encountered, or null if there are no selected
	 * nodes of the given type.
	 */
	public static <T extends Object> T getTreeObject(JTree tree, Class<T> type) {
		TreePath[] paths = tree.getSelectionPaths();
		if (paths == null || paths.length == 0) {
			return null;
		}
		for (int i = 0; i < paths.length; i++) {
			TreePath path = paths[i];
			for (Object o : path.getPath()) {
				if (o.getClass().equals(type)) return (T) o;
			}
		}
		return null;
	}

}
