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

package ca.sqlpower.matchmaker.munge;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.Project.ProjectMode;




public class MockJDBCCleanseTest extends SQLInputStepTest {

    protected ProjectMode getMatchType() {
		return ProjectMode.CLEANSE;
	}
	
	public void testDoNothing() {
		try {
			MungeStep mrs = step.getOuputStep();
			
			MungeProcess mungep = new MungeProcess();
			mungep.addChild(step);
			mungep.addChild(mrs);
			
			MungeProcessor mp = new MungeProcessor(mungep);
			mp.call();
			
			Connection con = db.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM table1");
			
			if (!rs.next()) {
				fail("NOTHING IN THE TABLE! :(");
			}
			
			
			assertEquals("row1,1", rs.getString(1));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(":(");
		}
	}
	
	public void testOneUpperCaseConnection() {	
		try {
			
			MungeStep mrs = step.getOuputStep();
			UpperCaseMungeStep ucms = new UpperCaseMungeStep(new TestingMatchMakerSession());
			ucms.connectInput(0, step.getChildren().get(0));
			mrs.connectInput(0, ucms.getChildren().get(0));
			
			MungeProcess mungep = new MungeProcess();
			mungep.addChild(step);
			mungep.addChild(mrs);
			mungep.addChild(ucms);
			
			MungeProcessor mp = new MungeProcessor(mungep);
			mp.call();
			
			Connection con = db.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM table1");
			
			if (!rs.next()) {
				fail("NOTHING IN THE TABLE! :(");
			}
			
			
			assertEquals("row1,1".toUpperCase(), rs.getString(1));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(":(");
		}
			
	}
	
}
