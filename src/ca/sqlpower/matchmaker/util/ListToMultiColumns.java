package ca.sqlpower.matchmaker.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

public class ListToMultiColumns implements ParameterizedType, UserType {
    
    /**
     * The number of columns in the database table that contribute to
     * the Java List.  This has to be set before this UserType will work.
     */
    private int columnCount;
    private int[] types;
    
    public int getColumnCount() {
        return columnCount;
    }
    
    public void setColumnCount(int numColumns) {
        this.columnCount = numColumns;
        types = new int[numColumns];
        for (int i = 0; i < numColumns; i++) {
            types[i] = Types.VARCHAR;
        }
    }

    // ParameterizedType implementation is below this line

    /**
     * Sets the columnCount property.
     */
    public void setParameterValues(Properties parameters) {
        String columnCountStr = parameters.getProperty("columnCount");
        if (columnCountStr != null) {
            setColumnCount(Integer.valueOf(columnCountStr));
        }
    }

    // UserType implementation is below this line
    
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }
    
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    public Object deepCopy(Object value) throws HibernateException {
        return new ArrayList<String>((List<String>) value);
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) return true;
        if (x==null || y== null) return false;
        else return x.equals(y);
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public boolean isMutable() {
        return true;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        if (names.length != getColumnCount()) {
            throw new HibernateException(
                    "The column name list for this user type is not" +
                    " the same length as the numColumns setting.  " +
                    "(names.length="+names.length+"; numColumns="
                    +getColumnCount()+")");
        }
        List<String> list = new ArrayList<String>(getColumnCount());
        for (int i = 0; i < names.length; i++) {
            String val = rs.getString(names[i]);
            if (val == null) break;
            list.add(val);
        }
        return list;
    }

    public void nullSafeSet(PreparedStatement st, Object value, int firstIndex) throws HibernateException, SQLException {
        List<String> list = (List<String>) value;
        if (list == null) list = Collections.emptyList();
        int index = firstIndex;
        for (String val : list) {
            st.setString(index++, val);
        }
        for (; (index - firstIndex) < getColumnCount(); index++) {
            st.setNull(index, Types.VARCHAR);
        }
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }

    public Class returnedClass() {
       return List.class;
    }

    public int[] sqlTypes() {
        return types;
    }
}
