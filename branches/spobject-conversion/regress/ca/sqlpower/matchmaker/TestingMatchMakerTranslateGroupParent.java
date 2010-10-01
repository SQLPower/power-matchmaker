/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

public class TestingMatchMakerTranslateGroupParent extends TranslateGroupParent {

	public TestingMatchMakerTranslateGroupParent(MatchMakerSession session) {
		super(session);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public MatchMakerTranslateGroup getChildByUUID(String uuid) {
    	MatchMakerTranslateGroup translateGroup = new MatchMakerTranslateGroup();
		MatchMakerTranslateWord test = new MatchMakerTranslateWord();
		test.setFrom("ab");
		test.setTo("12");
		translateGroup.addChild(test);
		MatchMakerTranslateWord test2 = new MatchMakerTranslateWord();
		test2.setFrom("cd");
		test2.setTo("34");
		translateGroup.addChild(test2);
		MatchMakerTranslateWord test3 = new MatchMakerTranslateWord();
		test3.setFrom("x*y");
		test3.setTo("-");
		translateGroup.addChild(test3);
		return translateGroup;
	}
}
