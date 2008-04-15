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


package ca.sqlpower.matchmaker.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.munge.MungeProcess;

/**
 * This is a class for different utilities used in testing.
 */
public class MMTestUtils {

	Logger logger = Logger.getLogger(MMTestUtils.class);
	
	private MMTestUtils() {
		//This is a utilities class for testing and should not have instances created.
	}

	/**
	 * Creates the result table used to store information about the graphs
	 * created by
	 * {@link #createTestingPool(MatchMakerSession, Project, MungeProcess, MungeProcess)}.
	 * This table is only required if we want to test against a database.
	 */
	public static void createResultTable(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("CREATE TABLE pl.match_results ("
				+ "\n DUP_CANDIDATE_10 varchar(50) not null,"
				+ "\n DUP_CANDIDATE_20 varchar(50) not null,"
				+ "\n CURRENT_CANDIDATE_10 varchar(50),"
				+ "\n CURRENT_CANDIDATE_20 varchar(50)," 
				+ "\n DUP_ID0 varchar(50),"
				+ "\n MASTER_ID0 varchar(50),"
				+ "\n CANDIDATE_10_MAPPED varchar(1),"
				+ "\n CANDIDATE_20_MAPPED varchar(1),"
				+ "\n MATCH_PERCENT integer," 
				+ "\n GROUP_ID varchar(60),"
				+ "\n MATCH_DATE timestamp," 
				+ "\n MATCH_STATUS varchar(60),"
				+ "\n MATCH_STATUS_DATE timestamp,"
				+ "\n MATCH_STATUS_USER varchar(60),"
				+ "\n DUP1_MASTER_IND  varchar(1)" + "\n)");
		stmt.close();
	}

	/**
	 * Removes the result table created by {@link #createResultTable(Connection)}.
	 */
	public static void dropResultTable(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("DROP TABLE pl.match_results");
		stmt.close();
	}
	
	/**
	 * Creates the source table for testing the MatchPool. See
	 * ({@link MatchPoolTest})
	 * This table is only required if we want to test against a database.
	 */
	public static void createSourceTable(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("CREATE TABLE pl.source_table ("
				+ "\n PK1 varchar(50) not null,"
				+ "\n FOO varchar(10),"
				+ "\n BAR varchar(10)," 
				+ "\n CONSTRAINT SOURCE_TABLE_PK PRIMARY KEY (PK1)" + "\n)");
		stmt.close();
	}
	
	/**
	 * Removes the source table created by {@link #createSourceTable(Connection)}.
	 */
	public static void dropSourceTable(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.executeUpdate("DROP TABLE pl.source_table");
		stmt.close();
	}
}
