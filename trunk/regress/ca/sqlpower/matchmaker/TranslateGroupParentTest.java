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

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.matchmaker.munge.TranslateWordMungeStep;

import junit.framework.TestCase;

public class TranslateGroupParentTest extends TestCase {
    PlFolder<MatchMakerObject> folder;
    Match match;
    MatchRuleSet cg;
    TranslateGroupParent tgp;
    TestingMatchMakerSession session;

    protected void setUp() throws Exception {
        folder = new PlFolder<MatchMakerObject>();
        match = new Match();
        folder.addChild(match);
        cg = new MatchRuleSet();
        match.addMatchRuleSet(cg);
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
        MatchMakerTranslateGroup tg = new MatchMakerTranslateGroup(new Long(1234));
        tg.setName("tg");
        TranslateWordMungeStep twMungeStep;
        twMungeStep = new TranslateWordMungeStep(session);
        twMungeStep.setParameter(TranslateWordMungeStep.TRANSLATE_GROUP_PARAMETER_NAME, String.valueOf(tg.getOid()));
        cg.addChild(twMungeStep);
        assertTrue("Couldn't find the translate group in the business model",tgp.isInUseInBusinessModel(tg));
    }
    
    public void testIsUseInBusinessModelTGNotFound() {
        MatchMakerTranslateGroup tg = new MatchMakerTranslateGroup();
        tg.setName("tg");
        assertFalse("Oops found the translate group in the business model",tgp.isInUseInBusinessModel(tg));
    }

}
