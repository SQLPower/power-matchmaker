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



package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.dao.AbstractMungeProcessDAOTestCase;
import ca.sqlpower.matchmaker.dao.MungeProcessDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;


public class MungeProcessDAOOracleTest extends AbstractMungeProcessDAOTestCase {
    
    private MungeProcess mungeProcess;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mungeProcess = createNewObjectUnderTest();
    }
    @Override
	public void resetSession() throws Exception {
		((TestingMatchMakerHibernateSession) getSession()).resetSession();
	}
    
	@Override
	public MungeProcessDAO getDataAccessObject() throws Exception {
		return new MungeProcessDAOHibernate(getSession());
	}

    @Override
    public MatchMakerHibernateSession getSession() throws Exception {
        return HibernateTestUtil.getOracleHibernateSession();
    }
}
