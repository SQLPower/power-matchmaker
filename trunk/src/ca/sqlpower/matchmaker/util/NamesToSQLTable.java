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
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;

public class NamesToSQLTable implements UserType {
	/**
	 * Stub class to let us get a table from a connection
	 * rather than a ArchitectDataSource
	 *
	 *	You must call set connection with a valid connection before
	 *  you call get connection.
	 */
	private class ConnectionSQLDatabase extends SQLDatabase {
		Connection connection;

		public Connection getConnection() {
			return connection;
		}

		public void setConnection(Connection connection) {
			this.connection = connection;
		}
		
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) deepCopy(value);
	}

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return deepCopy(cached);
	}

	/**
	 * Deep copy seems to be a misnomer.  The deep copy stops at any collections.
	 * For now, it only accepts SQLTable as the parameter or throws HibernateException other wise
	 * It creates a new SQLTable and puts in the same property as the incoming SQLTable
	 * and references it to the same parent object.
	 * 
	 * @param value the SQLTable to be copied
	 * @throws HibernateException if the value is not a SQLTable
	 */
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
		if (x == null && y == null) return true;
		if (y != null && x!= null && x.getClass() == y.getClass()) {
			return x.equals(y);
		} else {
			return false;
		}
	}

	public int hashCode(Object x) throws HibernateException {
		if ( !(x instanceof SQLTable)) {
			return 0;
		} else {
			return x.hashCode();
		}
	}

	/**
	 * SQL Tables are always mutable even if changes are rare
	 */
	public boolean isMutable() {
		return true;
	}

	/**
	 * Return a new sql table.  The table is loaded from the same source as the result set.
	 * 
	 * @param rs a JDBC Resultset
	 * @param names
	 *            an array of 1st Catalog column name, 2nd schema column Name
	 *            and 3rd table column name
	 * @param owner not used by this class
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
		/* XXX Gets the table from the connection passed in by the 
		 * Result set.  This is highly unefficient and should be fixed 
		 */
		Connection con = rs.getStatement().getConnection();		
		ConnectionSQLDatabase db = new ConnectionSQLDatabase();
		db.setPopulated(false);
		db.setConnection(con);
		try {
			db.populate();
			return db.getTableByName(catalogName,schemaName,tableName);
		} catch (ArchitectException e){
			throw new HibernateException(e);
		}
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
