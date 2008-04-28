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

package ca.sqlpower.matchmaker.util;


/**
 * A SQL Query
 * 
 * note this is a really dumb bean.
 *
 */
public class SQLQuery {

	/** The columns in the select statement as in SQL with no SELECT */
	private String select;
	/** the from clause of a SQL statment without FROM */
	private String from;
	/** A SQL where clause without where */
	private String where;
	

	public SQLQuery(String select, String from, String where) {
		super();
		this.select = select;
		this.from = from;
		this.where = where;
	}
	
	public SQLQuery() {
		super();
	}
	
	/**
	 * Combines the select,from and where fields and adds the appropriate keywords
	 * to make a SQL Statement
	 */
	public String getSQLStatement(){
		StringBuffer buf = new StringBuffer();
		buf.append("SELECT ").append(select);
		buf.append("\n	FROM ").append(from);
		buf.append("\n	WHERE ").append(where).append(";");
		return buf.toString();
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		if (this.from != from) {
			this.from = from;
		}
	}

	public String getSelect() {
		return select;
	}

	public void setSelect(String select) {
		if (this.select != select) {
			this.select = select;
		}
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(String where) {
		if (this.where != where) {
			this.where = where;
		}
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((from == null) ? 0 : from.hashCode());
		result = PRIME * result + ((select == null) ? 0 : select.hashCode());
		result = PRIME * result + ((where == null) ? 0 : where.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SQLQuery other = (SQLQuery) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (select == null) {
			if (other.select != null)
				return false;
		} else if (!select.equals(other.select))
			return false;
		if (where == null) {
			if (other.where != null)
				return false;
		} else if (!where.equals(other.where))
			return false;
		return true;
	}
	
}
