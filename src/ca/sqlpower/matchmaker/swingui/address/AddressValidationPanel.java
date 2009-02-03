/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.address;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.NoEditEditorPane;

public class AddressValidationPanel extends NoEditEditorPane {

    private static final Logger logger = Logger.getLogger(AddressValidationPanel.class);
    
    private final MatchMakerSwingSession session;

    private final Project project;
    
    public AddressValidationPanel(MatchMakerSwingSession session, Project project) {
        this.session = session;
        this.project = project;
        JList needsValidationList = new JList(new String[] { "Example 1", "Example 2", "Example 3" });
        needsValidationList.setPreferredSize(new Dimension(200, 50));
        setPanel(new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(needsValidationList),
                new JLabel("To begin address validation, please select an address from the list.", JLabel.CENTER)));
    }
    
    @Override
    public JSplitPane getPanel() {
        return (JSplitPane) super.getPanel();
    }
}
