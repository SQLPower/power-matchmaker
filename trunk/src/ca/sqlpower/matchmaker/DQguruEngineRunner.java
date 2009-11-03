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

package ca.sqlpower.matchmaker;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.prefs.Preferences;

import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSessionContext;
import ca.sqlpower.matchmaker.dao.hibernate.RepositoryVersionException;
import ca.sqlpower.matchmaker.swingui.SwingSessionContextImpl;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.util.UnknownFreqCodeException;

/**
 * Contains a main method that can be used to run an Engine on a predefined
 * DQguru project from a command-line interface
 */
public class DQguruEngineRunner {
	
	/**
	 * @param args
	 * @throws MatchMakerConfigurationException 
	 * @throws SQLObjectException 
	 * @throws SQLException 
	 * @throws UnknownFreqCodeException 
	 * @throws PLSecurityException 
	 * @throws RepositoryVersionException 
	 * @throws IOException 
	 * @throws SourceTableException 
	 * @throws EngineSettingException 
	 */
	public static void main(String[] args) throws RepositoryVersionException,
			PLSecurityException, SQLException, SQLObjectException,
			MatchMakerConfigurationException, IOException, EngineSettingException, SourceTableException {
		// TODO: Get args from the commandline
		String repositoryDSName = args[0];
		String username = args[1];
		String password = args[2];
		String projectName = args[3];

		Preferences prefs = Preferences.userNodeForPackage(MatchMakerSessionContext.class);

		String plDotIniPath = prefs.get(MatchMakerSessionContext.PREFS_PL_INI_PATH, null);
		DataSourceCollection<JDBCDataSource> plIni= readPlDotIni(plDotIniPath);
		MatchMakerSessionContext context = new MatchMakerHibernateSessionContext(prefs, plIni);
		
		JDBCDataSource repositoryDS = plIni.getDataSource(repositoryDSName);
		MatchMakerSession session = context.createSession(repositoryDS, username, password);
		Project project = session.getProjectByName(projectName);

		ProjectMode type = project.getType();
		
		MatchMakerEngine engine = null;
		
		switch(type) {
		case ADDRESS_CORRECTION:
			engine = project.getAddressCorrectionEngine();
			break;
		case BUILD_XREF:
			throw new UnsupportedOperationException("Cross-reference (Xref) engine not yet supported");
		case CLEANSE:
			engine = project.getCleansingEngine();
			break;
		case FIND_DUPES:
			engine = project.getMatchingEngine();
			break;
		default:
			throw new UnsupportedOperationException("No supported engine for project type " + type);
		}
		engine.call();
	}
	
	private static DataSourceCollection<JDBCDataSource> readPlDotIni(String plDotIniPath) throws IOException {
        if (plDotIniPath == null) {
            return null;
        }
        File pf = new File(plDotIniPath);
        if (!pf.exists() || !pf.canRead()) {
            return null;
        }

        DataSourceCollection pld = new PlDotIni();
        // First, read the defaults
        pld.read(SwingSessionContextImpl.class.getClassLoader().getResourceAsStream("ca/sqlpower/sql/default_database_types.ini"));
        // Now, merge in the user's own config
        pld.read(pf);
        return pld;
    }
}
