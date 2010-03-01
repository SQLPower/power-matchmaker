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

package ca.sqlpower.matchmaker.antbuild;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import ca.sqlpower.matchmaker.MatchMakerVersion;

public class MatchMakerVersionTask extends Task {

    /**
     * Executes this task by setting the build properties pertaining to the
     * current MatchMaker version number.
     */
    public void execute() throws BuildException {
        getProject().setNewProperty("app_ver_major", MatchMakerVersion.APP_VERSION_MAJOR);
        getProject().setNewProperty("app_ver_minor", MatchMakerVersion.APP_VERSION_MINOR);
        getProject().setNewProperty("app_ver_tiny", MatchMakerVersion.APP_VERSION_TINY);
        getProject().setNewProperty("app_ver_suffix", MatchMakerVersion.APP_VERSION_SUFFIX);
    }

}
