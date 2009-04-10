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

import org.hibernate.Session;

import ca.sqlpower.matchmaker.MatchMakerSession;

public interface MatchMakerHibernateSession extends MatchMakerSession {
    
    /**
     * Opens a new Hibernate session.  You have to close it when you're done.
     * 
     * @return A new hibernate session which is connected to the same database as
     * this session's SQLDatabase.
     */
    public Session openSession();
    
}
