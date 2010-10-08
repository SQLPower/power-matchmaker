/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
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

import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.TranslateWordMungeStep;
import ca.sqlpower.object.SPObject;

public class TranslateGroupParentTest extends MatchMakerTestCase {
	
    public TranslateGroupParentTest(String name) {
		super(name);
	}

	PlFolder folder;
    Project project;
    MungeProcess cg;
    TranslateGroupParent tgp;
    TestingMatchMakerSession session;

    protected void setUp() throws Exception {
    	super.setUp();
        folder = new PlFolder();
        project = new Project();
        folder.addChild(project);
        cg = new MungeProcess();
        project.addChild(cg);
        session = new TestingMatchMakerSession(); 
        List<PlFolder> folders = new ArrayList<PlFolder>();
        folders.add(folder);
        session.setFolders(folders);
        tgp = new TranslateGroupParent();
        getRootObject().addChild(tgp, 0);
        tgp.setSession(session);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsUseInBusinessModelTGFound() {
        MatchMakerTranslateGroup tg = new MatchMakerTranslateGroup();
        tg.setName("tg");
        TranslateWordMungeStep twMungeStep;
        twMungeStep = new TranslateWordMungeStep();
        twMungeStep.setTranslateGroup(tg);

        cg.addChild(twMungeStep);
        assertTrue("Couldn't find the translate group in the business model",tgp.isInUseInBusinessModel(tg));
    }
    
    public void testIsUseInBusinessModelTGNotFound() {
        MatchMakerTranslateGroup tg = new MatchMakerTranslateGroup();
        tg.setName("tg");
        assertFalse("Oops found the translate group in the business model",tgp.isInUseInBusinessModel(tg));
    }

	@Override
	protected MatchMakerObject getTarget() {
		return tgp;
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return MatchMakerTranslateGroup.class;
	}
	
	@Override
	public void testDuplicate() throws Exception {
		// TranslateGroupParent does not duplicate.
	}
}
