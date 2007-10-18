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

package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.dao.StubMatchMakerDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.StubMatchMakerSession;
import ca.sqlpower.matchmaker.util.MMTestUtils;
import ca.sqlpower.sql.SPDataSource;

public class PreMergeDataFudgerTest extends TestCase {

	Logger logger = Logger.getLogger(PreMergeDataFudgerTest.class);
	
	private MatchPool pool;

	private Connection con;
	private SQLDatabase db;
	private SQLTable sourceTable;
	private SQLTable resultTable;
	private Project project;
	
	private MatchMakerSession session = new StubMatchMakerSession() {
		@Override
		public Connection getConnection() {
			try {
				return db.getConnection();
			} catch (ArchitectException e) {
				throw new ArchitectRuntimeException(e);
			}
		}
		
		@Override
		public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
	        return new StubMatchMakerDAO<T>(businessClass);
	    }
	};

	private PreMergeDataFudger fudger;
	
	@Override
	protected void setUp() throws Exception {
		SPDataSource dataSource = DBTestUtil.getHSQLDBInMemoryDS();
		db = new SQLDatabase(dataSource);
		con = db.getConnection();

		MMTestUtils.createResultTable(con);

		SQLSchema plSchema = db.getSchemaByName("pl");

		resultTable = db.getTableByName(null, "pl", "match_results");
		
		project = new Project();
		project.setSession(session);
		project.setResultTable(resultTable);
		
		MungeProcess groupOne = new MungeProcess();
		groupOne.setName("Group_One");
		project.addMungeProcess(groupOne);

		MungeProcess groupTwo = new MungeProcess();
		groupOne.setName("Group_Two");
		project.addMungeProcess(groupTwo);
		
		pool = MMTestUtils.createTestingPool(session, project, groupOne, groupTwo);
		
		fudger = new PreMergeDataFudger(session, pool);
	}
	
	protected void tearDown() throws Exception {
		MMTestUtils.dropResultTable(con);
		con.close();
	}
	
	public void testChainLengthThree() throws Exception {
		fudger.fudge();
		
		SourceTableRecord n1 = pool.getSourceTableRecord(Collections.singletonList("n1"));
		SourceTableRecord n2 = pool.getSourceTableRecord(Collections.singletonList("n2"));
		SourceTableRecord n3 = pool.getSourceTableRecord(Collections.singletonList("n3"));
		SourceTableRecord n4 = pool.getSourceTableRecord(Collections.singletonList("n4"));
		
		PotentialMatchRecord n1n4 = n1.getMatchRecordByValidatedSourceTableRecord(n4);
		assertNotNull(n1n4);
		assertSame(n1n4.getRuleSet(), fudger.getRuleSet());

		PotentialMatchRecord n2n4 = n2.getMatchRecordByValidatedSourceTableRecord(n4);
		assertNotNull(n2n4);
		assertSame(n2n4.getRuleSet(), fudger.getRuleSet());

		// this one's different because there is also a direct user-validated edge belonging to GroupOne
		Collection<PotentialMatchRecord> n3Edges = n3.getOriginalMatchEdges();
		assertEquals(2, n3Edges.size());
		PotentialMatchRecord n3n4 = null;
		for (PotentialMatchRecord edge : n3Edges) {
			if (edge.getRuleSet() == fudger.getRuleSet()) {
				assertNull("Only one fudged edge should exist on n3", n3n4);
				n3n4 = edge;
			}
		}
		assertNotNull(n3n4);
		assertSame(n3n4.getMaster(), n4);
	}

	public void testUnfudgeChainLengthThree() throws Exception {
		fudger.fudge();
		
		SourceTableRecord n1 = pool.getSourceTableRecord(Collections.singletonList("n1"));
		SourceTableRecord n2 = pool.getSourceTableRecord(Collections.singletonList("n2"));
		SourceTableRecord n3 = pool.getSourceTableRecord(Collections.singletonList("n3"));
		SourceTableRecord n4 = pool.getSourceTableRecord(Collections.singletonList("n4"));
		
		PotentialMatchRecord n1n4 = n1.getMatchRecordByValidatedSourceTableRecord(n4);
		assertNotNull(n1n4);
		assertSame(n1n4.getRuleSet(), fudger.getRuleSet());
		
		fudger.unfudge();
		
		n1n4 = n1.getMatchRecordByValidatedSourceTableRecord(n4);
		assertNull(n1n4);
	}
	
	public void testUnmatchedEdgesDontGetMatched() throws Exception{
		fudger.fudge();
		
		SourceTableRecord c1 = pool.getSourceTableRecord(Collections.singletonList("c1"));
		SourceTableRecord c2 = pool.getSourceTableRecord(Collections.singletonList("c2"));
		SourceTableRecord c3 = pool.getSourceTableRecord(Collections.singletonList("c3"));
		
		PotentialMatchRecord c1c3 = c1.getMatchRecordByValidatedSourceTableRecord(c3);
		assertNull(c1c3);
		
		PotentialMatchRecord c2c3 = c2.getMatchRecordByValidatedSourceTableRecord(c3);
		assertNull(c2c3);
	}
	
	public void testNoMatchedEdgesDontGetMatched() throws Exception{
		fudger.fudge();
		
		SourceTableRecord o1 = pool.getSourceTableRecord(Collections.singletonList("o1"));
		SourceTableRecord o2 = pool.getSourceTableRecord(Collections.singletonList("o2"));
		SourceTableRecord o3 = pool.getSourceTableRecord(Collections.singletonList("o3"));
		
		PotentialMatchRecord o1o3 = o1.getMatchRecordByValidatedSourceTableRecord(o3);
		assertNull(o1o3);
		
		PotentialMatchRecord o1o2 = o1.getMatchRecordByValidatedSourceTableRecord(o2);
		assertNull(o1o2);
	}

	public void testFlipCandidates() throws Exception {
		fudger.fudge();
		
		Statement stmt = con.createStatement();
		
		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pl.match_results WHERE " +
										"(MATCH_STATUS='MATCH' OR MATCH_STATUS='AUTO-MATCH') AND " +
										"(DUP1_MASTER_IND='N')");
		assertTrue(rs.next());
		assertEquals(0, rs.getInt(1));
	}
}
