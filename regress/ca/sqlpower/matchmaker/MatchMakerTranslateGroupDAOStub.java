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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
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
        // TODO Auto-generated method stub
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.findByName()");
        return null;
    }

    public void delete(MatchMakerTranslateGroup deleteMe) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.delete()");

    }

    public List<MatchMakerTranslateGroup> findAll() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.findAll()");
        return null;
    }

    public Class<MatchMakerTranslateGroup> getBusinessClass() {
        // TODO Auto-generated method stub
        logger
                .debug("Stub call: MatchMakerTranslateGroupDAOStub.getBusinessClass()");
        return null;
    }

    public void save(MatchMakerTranslateGroup saveMe) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: MatchMakerTranslateGroupDAOStub.save()");

    }

}
