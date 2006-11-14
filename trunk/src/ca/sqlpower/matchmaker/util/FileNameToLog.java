package ca.sqlpower.matchmaker.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import ca.sqlpower.matchmaker.util.log.Level;
import ca.sqlpower.matchmaker.util.log.Log;
import ca.sqlpower.matchmaker.util.log.LogFactory;

public class FileNameToLog implements UserType {

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return deepCopy(cached);
	}

	public Object deepCopy(Object value) throws HibernateException {
		if (value == null) return null;
		return LogFactory.getLogger(Level.DEBUG,((Log)value).getConstraint());
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) deepCopy(value);
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == null && y == null) return true;
		if (y != null && x!= null && x.getClass() == y.getClass()) {
			return x.equals(y);
		} else {
			return false;
		}
	}

	public int hashCode(Object x) throws HibernateException {
		if ( !(x instanceof Log)) {
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
		String fileName = rs.getString(names[0]);
		if (fileName != null) {
			return LogFactory.getLogger(Level.DEBUG,fileName);
		} else {
			return null;
		}
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		if (value != null) {
			st.setString(index, (String) ((Log) value).getConstraint());
		} else {
			st.setString(index, null);
		}
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	public Class returnedClass() {
		return Log.class;
	}

	public int[] sqlTypes() {
		int[] types = {Types.VARCHAR };
		return types;
	}

}
