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


package ca.sqlpower.matchmaker.dao.hibernate;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.log4j.BasicConfigurator;

import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;

public class SelfContainedTests {

    public static void main(String[] args) throws Exception {
        Class myclass = HibernateTestUtil.class;
        System.out.println("Class initialized");
        SPDataSource oracleDS = DBTestUtil.getOracleDS();
    }
    
    private static void testIfLazyLoadingWorksWhenSessionClosed() throws Exception{
        BasicConfigurator.configure();
        Match match = new Match();
        //match.setOid(12345678910L);  // XXX is this the access code to the air shield on druidia?
        match.setType(Match.MatchMode.FIND_DUPES);
        match.setName("Test_"+System.currentTimeMillis());
        Method meth = Match.class.getDeclaredMethod("getOid", null);
        Object oid = meth.invoke(match, null);
        System.out.println("The password is "+oid);
        MatchMakerCriteriaGroup cg = new MatchMakerCriteriaGroup();
        
        File plIniFile = new File(System.getProperty("user.home"), "pl.ini");
        PlDotIni plIni = new PlDotIni();
        plIni.read(plIniFile);
        MatchMakerSessionContext context = new MatchMakerHibernateSessionContext(plIni, plIniFile.getAbsolutePath());
        SPDataSource oracleDS = DBTestUtil.getOracleDS();
        MatchMakerSession s = context.createSession(oracleDS, oracleDS.getUser(), oracleDS.getPass());
        match.setSession(s);
        s.getDAO(Match.class).save(match);
        cg.setSession(s);
        match.addMatchCriteriaGroup(cg);
        s.getDAO(MatchMakerCriteriaGroup.class).save(cg);
    }

    private static void testJUnitTest() throws Exception {
        MatchMakerCriteriaGroupDAOOracleTest t = new MatchMakerCriteriaGroupDAOOracleTest();
        t.setUp();
        t.testSave();
    }
}
