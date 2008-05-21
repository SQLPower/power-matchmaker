/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Frame;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.SPSUtils;

/**
 * A set of basic functionality that all actions in the MatchMaker
 * rely on. Not all actions currently rely on this method but
 * it is useful for future actions.
 */
public abstract class AbstractMatchMakerAction extends AbstractAction {
	
	private static final int ICON_SIZE = 16;

    protected final Frame frame;
    protected final MatchMakerSwingSession session;
    
    /**
     * Helper constructor that all MatchMaker action subclasses that use an icon will call.
     * Ensures that the session, and its frame are non-null.
     * 
     * @param session The session that this action will operate on. Must not be null.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * @param iconResourceName The resource name of the icon. See
     * {@link SPSUtils#createIcon(String, String)} for details.
     */
    public AbstractMatchMakerAction(
    		MatchMakerSwingSession session,
            String actionName,
            String actionDescription,
            String iconResourceName) {
        
        this(session, actionName, actionDescription,
                iconResourceName == null ?
                        (Icon) null :
                        SPSUtils.createIcon(iconResourceName, actionName, ICON_SIZE));
    }

    /**
     * Helper constructor that all architect action subclasses that use an icon will call.
     * Ensures that the session, and its frame are non-null.
     * 
     * @param session The session that this action will operate on. Must not be null.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * @param icon The icon to use.  Null means no icon.
     */
    public AbstractMatchMakerAction(
    		MatchMakerSwingSession session,
            String actionName,
            String actionDescription,
            Icon icon) {
        
        super(actionName, icon);
        putValue(SHORT_DESCRIPTION, actionDescription);
        
        this.session = session;
        if (session == null) throw new NullPointerException("Null session");

        this.frame = session.getFrame();
        if (frame == null) throw new NullPointerException("Null parentFrame");
        
    }

    /**
     * Helper constructor that all MatchMaker action subclasses that do not
     * use an icon will call. Ensures that the session, and its frame
     * are non-null.
     * 
     * @param session The session that this action will operate on. Must not be null.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     */
    public AbstractMatchMakerAction(
            MatchMakerSwingSession session,
            String actionName,
            String actionDescription) {
        this(session, actionName, actionDescription, (Icon) null);
    }
}
