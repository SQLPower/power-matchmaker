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
                SQLIndex.Column c = newIndex.new Column(
                        oldIndexColumn.getName(),
                        oldIndexColumn.isAscending(),
                        oldIndexColumn.isDescending());
                newIndex.addChild(c);
            }
        } catch (ArchitectException e) {
            throw new HibernateException(e);
        }
        return newIndex;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) (deepCopy(value));
    }

	public boolean equals(Object x, Object y) throws HibernateException {
        if (x == null && y == null)
            return true;
        if (x != null && y != null && (x instanceof SQLIndex)
                && (y instanceof SQLIndex)) {
            return x.equals(y);
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
                        SQLIndex.Column c = index.new Column();
                        c.setName(columnName);
                        try {
                            index.addChild(c);
                        } catch (ArchitectException e) {
                            throw new HibernateException(e);
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
                // It is require to increment the index by 1 since the inital
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
                throw new HibernateException(e);
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
