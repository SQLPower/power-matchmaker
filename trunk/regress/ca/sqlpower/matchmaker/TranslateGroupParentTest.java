package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class TranslateGroupParentTest extends TestCase {
    PlFolder<MatchMakerObject> folder;
    Match match;
    MatchMakerCriteriaGroup cg;
    MatchMakerCriteria c;
    TranslateGroupParent tgp;
    TestingMatchMakerSession session;

    protected void setUp() throws Exception {
        folder = new PlFolder<MatchMakerObject>();
        match = new Match();
        folder.addChild(match);
        cg = new MatchMakerCriteriaGroup();
        match.addMatchCriteriaGroup(cg);
        c = new MatchMakerCriteria();
        cg.addChild(c);
        session = new TestingMatchMakerSession(); 
        List<PlFolder> folders = new ArrayList<PlFolder>();
        folders.add(folder);
        session.setFolders(folders);
        tgp = new TranslateGroupParent(session);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsUseInBusinessModelTGFound() {
        MatchMakerTranslateGroup tg = new MatchMakerTranslateGroup();
        tg.setName("tg");
        c.setTranslateGroup(tg);
        assertTrue("Couldn't find the translate group in the business model",tgp.isInUseInBusinessModel(tg));
    }
    
    public void testIsUseInBusinessModelTGNotFound() {
        MatchMakerTranslateGroup tg = new MatchMakerTranslateGroup();
        tg.setName("tg");
        assertFalse("Oops found the translate group in the business model",tgp.isInUseInBusinessModel(tg));
    }

}
