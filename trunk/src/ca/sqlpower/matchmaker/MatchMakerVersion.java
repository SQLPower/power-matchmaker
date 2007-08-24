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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class MatchMakerVersion extends Task {

    // The method executing the task
    public void execute() throws BuildException {
        getProject().setNewProperty(
                "app_ver_major",
                Integer.toString(MatchMakerSessionContext.APP_VERSION.getMajor()) );
        getProject().setNewProperty(
                "app_ver_minor",
                Integer.toString(MatchMakerSessionContext.APP_VERSION.getMinor()) );
        getProject().setNewProperty(
                "app_ver_tiny",
                Integer.toString(MatchMakerSessionContext.APP_VERSION.getTiny()) );
    }

    // The setter for the "message" attribute
    public void setMessage(String msg) {
    	// NOTUSED
    }
}
