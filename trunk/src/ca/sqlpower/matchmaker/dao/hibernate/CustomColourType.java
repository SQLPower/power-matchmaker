package ca.sqlpower.matchmaker.dao.hibernate;

import java.awt.Color;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * Maps hexidecimal strings representing 24-bit RGB colour values to and from
 * instances of java.awt.Color.
 */
public class CustomColourType implements UserType {

    /**
     * Not implemented.
     */
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return null;
    }

    /**
     * Not implemented.
     */
    public Serializable disassemble(Object value) throws HibernateException {
        return null;
    }

    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        } else {
            Color colorValue = (Color) value;
            return new Color(colorValue.getRGB());
        }
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == null) {
            return y == null;
        } else {
            return x.equals(y);
        }
    }

    public int hashCode(Object x) throws HibernateException {
        if (x == null) {
            return 0;
        } else {
            return x.hashCode();
        }
    }

    public boolean isMutable() {
        return true;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        String colorHexValue = rs.getString(names[0]);
        if (colorHexValue == null) {
            return null;
        } else {
            return Color.decode(colorHexValue);
        }
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            Color colorValue = (Color) value;
            st.setString(index, "#"+Integer.toHexString(colorValue.getRGB() & 0xFFFFFF));
        }
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }

    public Class returnedClass() {
        return Color.class;
    }

    public int[] sqlTypes() {
        return new int[] { Types.VARCHAR };
    }

}
