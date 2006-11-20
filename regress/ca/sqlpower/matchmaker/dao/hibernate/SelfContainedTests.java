package ca.sqlpower.matchmaker.dao.hibernate;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.log4j.BasicConfigurator;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.PlDotIni;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;

public class SelfContainedTests {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        Match match = new Match();
        //match.setOid(12345678910L);  // XXX is this the access code to the air shield on druidia?
        match.setType(Match.MatchType.FIND_DUPES);
        match.setName("Test_"+System.currentTimeMillis());
        Method meth = Match.class.getDeclaredMethod("getOid", null);
        Object oid = meth.invoke(match, null);
        System.out.println("The password is "+oid);
        MatchMakerCriteriaGroup cg = new MatchMakerCriteriaGroup();
        
        File plIniFile = new File(System.getProperty("user.home"), "pl.ini");
        PlDotIni plIni = new PlDotIni();
        plIni.read(plIniFile);
        MatchMakerSessionContext context = new MatchMakerHibernateSessionContext(plIni, plIniFile.getAbsolutePath());
        ArchitectDataSource oracleDS = HibernateTestUtil.getOracleDS();
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
