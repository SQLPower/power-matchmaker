package ca.sqlpower.matchmaker.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;

/**
 * This is a class that implements UserType and is used for
 * translating indices into the sourcetable
 *
 */
public class ListToSQLIndex implements UserType  {


	private static final int columns = 11;

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return deepCopy(cached);
	}


	public Object deepCopy(Object value) throws HibernateException {
		if (!(value instanceof SQLIndex))return null;
		SQLIndex old = (SQLIndex) value;
		SQLIndex copyOf = new SQLIndex(old.getName(), old.isUnique(), 
				old.getQualifier(), old.getType(), old.getFilterCondition());
		try {
			for (Object child : old.getChildren()){
				SQLIndex.Column tempChild = (SQLIndex.Column)child;
				SQLIndex.Column c = copyOf.new Column(tempChild.getName(),
						tempChild.isAscending(), 
						tempChild.isDescending());
				if (((SQLObject)(child)).getChildCount() > 0){
					c.addChild(((SQLObject)child).getChild(0));
				}
				copyOf.addChild(c);
			}
		} catch (ArchitectException e){
			throw new HibernateException(e);
		}
		return copyOf;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable)(deepCopy(value));
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == null && y==null) return true;
		if (x!= null && y!=null && (x instanceof SQLIndex)&& 
				(y instanceof SQLIndex)){
			return x.equals(y);
		} else{
			return false;
		}
	}

	public int hashCode(Object x) throws HibernateException {
		if (!(x instanceof SQLIndex)){
			return 0;
		} else{
			return x.hashCode();
		}
	}

	public boolean isMutable() {
		return true;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		SQLIndex index = new SQLIndex();
		index.setName(rs.getString(names[0]));		
		for (int i=2; i < 12; i++){
			if (names[i-1] != null) {
				if (rs.getString(rs.findColumn(names[i-1])) != null){
					SQLIndex.Column c = index.new Column();
					c.setName(rs.getString(rs.findColumn(names[i-1])));
					try {
						index.addChild(c);
					} catch (ArchitectException e) {
						throw new HibernateException(e);
					}
				}
			}
		}
		return null;
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {	
		if (value instanceof SQLIndex){
			SQLIndex ind = (SQLIndex)value;
			st.setString(index, ind.getName());
			try {
				//It is require to increment the index by 1 since the initla
				//index is for the SQLIndex name.  The index needs to increase
				//to synchronize with setting the values of the columns
				for (SQLIndex.Column c : (List<SQLIndex.Column>)ind.getChildren()){
					index++;
					st.setString(index, c.getName());
					
				}
			} catch (ArchitectException e) {
				throw new HibernateException(e);
			}
		} else if (value == null) {
			for (int i=index; i < columns +index; i++) {
				st.setNull(i, Types.VARCHAR);
			}
		} else {
			throw new HibernateException("Invalid object type, it must be a SQLIndex");
		}

	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {		// 
		return deepCopy(original);
	}

	public Class returnedClass() {
		return SQLIndex.class;
	}

	public int[] sqlTypes() {
		int[] types = new int[11];
		for(int i=0; i <columns; i++){
			types[i]=Types.VARCHAR;
		}
		return types;
	}

}
