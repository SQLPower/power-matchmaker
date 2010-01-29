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

import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.dao.ColumnMergeRulesDAO;

public class ColumnMergeRulesDAOHibernate extends AbstractMatchMakerDAOHibernate<ColumnMergeRules> implements ColumnMergeRulesDAO {

    /**
     * Creates a new data access object for column merge rules in the given
     * hibernate session.
     */
    public ColumnMergeRulesDAOHibernate(MatchMakerHibernateSession matchMakerSession) {
        super(matchMakerSession);
    }

    public Class<ColumnMergeRules> getBusinessClass() {
        return ColumnMergeRules.class;
    }

}