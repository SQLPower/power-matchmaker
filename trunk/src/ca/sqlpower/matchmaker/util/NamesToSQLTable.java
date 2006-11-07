package ca.sqlpower.matchmaker.util;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;

public class NamesToSQLTable implements UserType {

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) deepCopy(value);
	}

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return deepCopy(cached);
	}

	public Object deepCopy(Object value) throws HibernateException {
		if (value == null)
			return null;

		if (value instanceof SQLTable) {
			SQLTable oldTable = (SQLTable) value;
			SQLTable t = null;
	
			try {
				if (oldTable.getParent() != null) {
					t = new SQLTable(oldTable.getParent(), oldTable.getName(), oldTable
							.getRemarks(), oldTable.getObjectType(), oldTable.isPopulated());
				} else {
					t = new SQLTable();
					t.setName(oldTable.getName());
					t.setRemarks(oldTable.getRemarks());
					t.setObjectType(oldTable.getObjectType());
					t.setPopulated(oldTable.isPopulated());
					t.setPhysicalName(oldTable.getPhysicalName());
					t.setPhysicalPrimaryKeyName(oldTable.getPhysicalPrimaryKeyName());
					t.setPrimaryKeyName(oldTable.getPrimaryKeyName());
				}
				// Replace the children
				while( t.getChildCount() >0){
					t.removeChild(0);
				}
				System.out.println(t.getChildCount());
				for(Object o:oldTable.getChildren()){
					t.addChild((SQLObject)o);
				}
								
				return t;
			} catch (ArchitectException e) {
				throw new HibernateException(e);
			}

		} else {
			throw new HibernateException(
					"Invalid type expecting SQLTAble or null");
		}
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		// TODO Auto-generated method stub
		return false;
	}

	public int hashCode(Object x) throws HibernateException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * SQL Tables are always mutable even if changes are rare
	 */
	public boolean isMutable() {
		return true;
	}

	/**
	 * 
	 * @param names
	 *            an array of 1st Catalog column name, 2nd schema column Name
	 *            and 3rd table column name
	 */
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		rs.next();
		if (names.length != 3) {
			throw new HibernateException("Invalid number of names should be 3");
		}
		String catalogName = rs.getString(rs.findColumn(names[0]));
		String schemaName = rs.getString(rs.findColumn(names[1]));
		String tableName = rs.getString(rs.findColumn(names[2]));
		//XXX 
		Connection con = rs.getStatement().getConnection();

		return null;
	}

	/**
	 * Adds the names of the catalog, schema and table to a prepared statement
	 * if the catalog or schema are the empty string it substitutes null in its
	 * place.
	 * 
	 * @param st
	 *            a sql prepared statement
	 * @param value
	 *            this can either be null or a sql table
	 * @param index
	 *            the first index to store the results
	 */
	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		if (value instanceof SQLTable) {
			SQLTable t = (SQLTable) value;
			if (t.getCatalogName().equals("")) {
				st.setNull(index, Types.VARCHAR);
			} else {
				st.setString(index, t.getCatalogName());
			}
			index++;
			if (t.getSchemaName().equals("")) {
				st.setNull(index, Types.VARCHAR);
			} else {
				st.setString(index, t.getSchemaName());
			}
			index++;
			st.setString(index, t.getName());
		} else if (value == null) {
			st.setNull(index, Types.VARCHAR);
			index++;
			st.setNull(index, Types.VARCHAR);
			index++;
			st.setNull(index, Types.VARCHAR);
		} else {
			throw new HibernateException("Invalid type expecting SQLTable");
		}
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return deepCopy(original);
	}

	public Class returnedClass() {
		return SQLTable.class;
	}

	public int[] sqlTypes() {
		int[] types = { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR };
		return types;
	}

}
