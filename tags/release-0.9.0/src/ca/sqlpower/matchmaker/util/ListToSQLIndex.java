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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLIndex;

/**
 * This is a class that implements UserType and is used for
 * translating indices into the sourcetable
 *
 */
public class ListToSQLIndex implements UserType  {
    
    private static final Logger logger = Logger.getLogger(ListToSQLIndex.class);

	private static final int COLUMN_COUNT = 11;

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

	public Object deepCopy(Object value) throws HibernateException {
        if (!(value instanceof SQLIndex))
            return null;
        SQLIndex oldIndex = (SQLIndex) value;
        SQLIndex newIndex = new SQLIndex(
                oldIndex.getName(),
                oldIndex.isUnique(),
                oldIndex.getQualifier(),
                oldIndex.getType(),
                oldIndex.getFilterCondition());
        try {
			for (Object child : oldIndex.getChildren()) {
				SQLIndex.Column oldIndexColumn = (SQLIndex.Column) child;

				SQLIndex.Column c;
				if (oldIndexColumn.getColumn() != null) {
					c = newIndex.new Column(
							oldIndexColumn.getColumn(),
							oldIndexColumn.isAscending(),
							oldIndexColumn.isDescending());
				} else {
					c = newIndex.new Column(
							oldIndexColumn.getName(),
							oldIndexColumn.isAscending(),
							oldIndexColumn.isDescending());
				}
				newIndex.addChild(c);
			}
		} catch (ArchitectException e) {
			throw new ArchitectRuntimeException(e);
		}
        return newIndex;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) (deepCopy(value));
    }

    /**
     * Two SQLIndex objects have persistance equality iff 
     * Both indices have the same name, and all column names that
     * appear in one appear in both in the same order.
     */
	public boolean equals(Object x, Object y) throws HibernateException {
        if (x == null && y == null)
            return true;
        if (logger.isDebugEnabled()){
        	logger.debug("neither null");
        }
        if ((x instanceof SQLIndex) && (y instanceof SQLIndex)) {
        	SQLIndex indexX = (SQLIndex) x;
        	SQLIndex indexY = (SQLIndex) y;
        	try {
				if (indexX.getName() == null ? indexX.getName() != indexY.getName():!indexX.getName().equals(indexY.getName())){
					if (logger.isDebugEnabled()){
						logger.debug("different pk name was " + indexY.getName()+ " expecting "+ indexX.getName());
					}
					return false;
				} else if (indexX.getChildCount() != indexY.getChildCount()) {
					if (logger.isDebugEnabled()){
						logger.debug("different child count was "+ indexY.getChildCount()+ " expecting "+ indexX.getChildCount());
					}
					return false;
				} else {
					for (int i=0; i < indexX.getChildCount(); i++){
						if (indexX.getChild(i).getName() == null ? indexX.getChild(i).getName() != indexY.getChild(i).getName():!indexX.getChild(i).getName().equals(indexY.getChild(i).getName())){
							if (logger.isDebugEnabled()){
								logger.debug("different column name was " + indexY.getChild(i).getName()+ " expecting "+ indexX.getChild(i).getName());
							}
							return false;
						}
					}
					return true;
				}
			} catch (ArchitectException e) {
				throw new ArchitectRuntimeException(e);
			}
        } else {
            return false;
        }
    }

    public int hashCode(Object x) throws HibernateException {
        if (!(x instanceof SQLIndex)) {
            return 0;
        } else {
            return x.hashCode();
        }
    }

    public boolean isMutable() {
        return true;
    }

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
            throws HibernateException, SQLException {
        SQLIndex index = new SQLIndex();
        String pkName = rs.getString(names[0]);
        if (pkName != null) {
            index.setName(pkName);
            for (int i = 1; i < names.length; i++) {
                if (names[i] != null) {
                    final String columnName = rs.getString(rs.findColumn(names[i]));
                    if (columnName != null) {
                    	try {
							SQLIndex.Column c;
							c = index.new Column(columnName, false, false);
							index.addChild(c);
						} catch (ArchitectException e) {
							throw new ArchitectRuntimeException(e);
						}
                    }
                }
            }
            return index;
        } else {
            return null;
        }
    }

	public void nullSafeSet(PreparedStatement st, Object value, int index)
            throws HibernateException, SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("nullSafeSet(pstmt," + value + "," + index + ")");
        }
        if (value instanceof SQLIndex) {
            SQLIndex ind = (SQLIndex) value;
            int indexItemPos = index;
            if (logger.isDebugEnabled()) {
                logger.debug("           setting param " + indexItemPos + " to \"" + ind.getName() + "\"");
            }
            st.setString(indexItemPos, ind.getName());
            try {
				// It is required to increment the index by 1 since the inital
				// index is for the SQLIndex name. The index needs to increase
				// to synchronize with setting the values of the columns
				for (SQLIndex.Column c : (List<SQLIndex.Column>) ind.getChildren()) {
					indexItemPos++;
					if (logger.isDebugEnabled()) {
						logger.debug("           setting param " + indexItemPos + " to \"" + c.getName() + "\"");
					}
					st.setString(indexItemPos, c.getName());
				}
				// fill in the rest of the values
				for (int i = indexItemPos + 1; i < COLUMN_COUNT + index; i++) {
					if (logger.isDebugEnabled()) {
						logger.debug("           setting param " + i + " to \"" + null + "\"");
					}
					st.setNull(i, Types.VARCHAR);
				}
			} catch (ArchitectException e) {
				throw new ArchitectRuntimeException(e);
			}
        } else if (value == null) {
        	for (int i = index; i < COLUMN_COUNT + index; i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("           setting param " + i + " to \"" + null + "\"");
                }
                st.setNull(i, Types.VARCHAR);
            }
        } else {
            throw new HibernateException(
                    "Invalid object type, it must be a SQLIndex");
        }
    }

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return deepCopy(original);
	}

	public Class returnedClass() {
		return SQLIndex.class;
	}

	public int[] sqlTypes() {
        int[] types = new int[11];
        for (int i = 0; i < COLUMN_COUNT; i++) {
            types[i] = Types.VARCHAR;
        }
        return types;
    }

}
