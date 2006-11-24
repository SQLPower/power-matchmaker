package ca.sqlpower.matchmaker.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * A SQL Query
 * 
 * note this is a really dumb bean.
 *
 */
public class StringsToViewSpec implements UserType {


	
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return deepCopy(cached);
	}

	public Object deepCopy(Object value) throws HibernateException {
		if ( !( value instanceof ViewSpec) ) {
			return null;
		}
		ViewSpec oldValue = (ViewSpec)value;
		return new ViewSpec(oldValue.getSelect(),
				oldValue.getFrom(),oldValue.getWhere());
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable)deepCopy(value);
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if ( x == null && y != null ) {
			return false;
		} else if ( x != null && y == null ) {
			return false;
		} else if ( x == null && y == null ) {
			return true;
		} else {
			return x.equals(y);
		}
	}

	public int hashCode(Object x) throws HibernateException {
		return (x==null)?0:x.hashCode();
	}

	public boolean isMutable() {
		return true;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		if (names.length != sqlTypes().length ) {
            throw new HibernateException(
                    "The column name list for this user type is not" +
                    " the same length as the ViewSpec properties setting.  " +
                    "(names.length="+names.length+"; number of properties="
                    +sqlTypes().length+")");
        }
        if (rs.getString(names[0]) == null &&
                rs.getString(names[1]) == null &&
                rs.getString(names[2]) == null) {
            return null;
        } else {
            return new ViewSpec(rs.getString(names[0]),
                                rs.getString(names[1]),
                                rs.getString(names[2]));
        }
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if ( value == null ) {
			st.setString(index,null);
			st.setString(index+1,null);
			st.setString(index+2,null);
		} else {
			st.setString(index,((ViewSpec)value).getSelect());
			st.setString(index+1,((ViewSpec)value).getFrom());
			st.setString(index+2,((ViewSpec)value).getWhere());
		}
		
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return deepCopy(original);
	}

	public Class returnedClass() {
		return ViewSpec.class;
	}

	public int[] sqlTypes() {
		int [] type = {Types.VARCHAR,Types.VARCHAR,Types.VARCHAR};
		return type;
	}
	
	
}
