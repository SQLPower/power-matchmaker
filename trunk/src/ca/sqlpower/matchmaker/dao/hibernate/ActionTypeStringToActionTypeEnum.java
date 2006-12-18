package ca.sqlpower.matchmaker.dao.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;

public class ActionTypeStringToActionTypeEnum implements UserType {

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return deepCopy(cached);
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
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
		if ( !(x instanceof MergeActionType)) {
			return 0;
		} else {
			return x.hashCode();
		}
	}
	/**
	 * Enums are not mutable
	 */
	public boolean isMutable() {
		return false;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		String typeString = rs.getString(names[0]);
		if (typeString != null) {
			return MergeActionType.getActionTypeFromString(typeString);
		} else {
			return null;
		}
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		if (value != null) {
			st.setString(index, ((MergeActionType) value).toString());
		} else {
			st.setString(index, null);
		}
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	public Class returnedClass() {
		return MergeActionType.class;
	}

	public int[] sqlTypes() {
		int[] types = {Types.VARCHAR };
		return types;
	}

}
