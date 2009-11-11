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
		String repositoryDSName = null;
		String username = null;
		String password = null;
		String projectName = null;
		String logfilePath = null;
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("--repository") || arg.equals("-r")) {
				arg = args[i + 1];
				repositoryDSName = arg;
				i++;
			} else if (arg.equals("--username") || arg.equals("-u")) {
				arg = args[i + 1];
				username = arg;
				i++;
			} else if (arg.equals("--password") || arg.equals("-P")) {
				arg = args[i + 1];
				password = arg;
				i++;
			} else if (arg.equals("--project") || arg.equals("-p")) {
				arg = args[i + 1];
				projectName = arg;
				i++;
			} else if (arg.equals("--log") || arg.equals("-l")) {
				arg = args[i + 1];
				logfilePath = arg;
				i++;
			} else {
				if (!arg.equals("--help") && !arg.equals("-h")) {
					System.out.println("Cannot recognize argument '" + arg + "'");
				}
				printUsage();
				System.exit(1);
			}
		}

		if (repositoryDSName == null || username == null || password == null || projectName == null) {
			printUsage();
			System.exit(1);
		}

		Preferences prefs = Preferences.userNodeForPackage(MatchMakerSessionContext.class);

		String plDotIniPath = prefs.get(MatchMakerSessionContext.PREFS_PL_INI_PATH, null);
		DataSourceCollection<JDBCDataSource> plIni= readPlDotIni(plDotIniPath);
		MatchMakerSessionContext context = new MatchMakerHibernateSessionContext(prefs, plIni);
		
		JDBCDataSource repositoryDS = plIni.getDataSource(repositoryDSName);
		if (repositoryDS == null) {
			System.out.println("Could not find repository '" + repositoryDSName + "'");
			System.exit(1);
		}
		
		MatchMakerSession session = context.createSession(repositoryDS, username, password);
		Project project = session.getProjectByName(projectName);
		
		if (project == null) throw new NullPointerException("Could not find project '" + projectName + "'");
		
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
		
		if (logfilePath != null) {
			project.getMungeSettings().setLog(new File(logfilePath));
		}
		
		engine.call();
	}
	
	private static void printUsage() {
		System.out.println("Usage: java -jar dqguru-engine-runner.jar [JVM options] [args...]");
		System.out.println("Mandatory arguments include:");
		System.out.println("\t--repository | -r <repository name>\tName of Repository Datasource");
		System.out.println("\t--username | -u <username>\t\tRepository Username");
		System.out.println("\t--password | -P <password>\t\tRepository Password");
		System.out.println("\t--project | -p <project>\t\tDQguru Project Name");
		System.out.println("\nOptional arguments include:");
		System.out.println("\t--log | -l <log path>\t\t\tPath of the engine log");
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
