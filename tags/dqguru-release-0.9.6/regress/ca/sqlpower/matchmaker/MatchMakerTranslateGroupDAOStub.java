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

import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.dao.MatchMakerTranslateGroupDAO;

public class MatchMakerTranslateGroupDAOStub implements
        MatchMakerTranslateGroupDAO {

    private static final Logger logger = Logger
            .getLogger(MatchMakerTranslateGroupDAOStub.class);
    
    public MatchMakerTranslateGroup findByName(String name) {
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.findByName()");
        return null;
    }

    public void delete(MatchMakerTranslateGroup deleteMe) {
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.delete()");
    }

    public List<MatchMakerTranslateGroup> findAll() {
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.findAll()");
        return null;
    }

    public Class<MatchMakerTranslateGroup> getBusinessClass() {
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.getBusinessClass()");
        return null;
    }

    public void save(MatchMakerTranslateGroup saveMe) {
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.save()");

    }

    /**
     * Returns a default translate group, ignoring the given oid.
     */
	public MatchMakerTranslateGroup findByOID(long oid) {
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
