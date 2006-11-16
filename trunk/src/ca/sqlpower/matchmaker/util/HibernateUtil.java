package ca.sqlpower.matchmaker.util;

import org.apache.log4j.Logger;
import org.hibernate.Session;


/**
 * Some Utils to encapsulate common Hibernate operations.
 */
public class HibernateUtil {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(HibernateUtil.class);
    
    /**
     * @deprecated Use a MatchMakerSession instead of this static method.
     */
    public static Session primarySession() {
        throw new UnsupportedOperationException("You have to use MatchMakerSession now.");
    }

	public static String plDbType2Dialect(String plType){
		if (plType == null ) throw new IllegalArgumentException("No dialect for a null database");
		String dbString = plType.toLowerCase();

		if( dbString.equals("oracle")) {
			return "org.hibernate.dialect.OracleDialect";
		} else if (dbString.equals("sql server")) {
			return "org.hibernate.dialect.SQLServerDialect";
		} else if (dbString.equals("db2")) {
			return "org.hibernate.dialect.DB2Dialect";
		} else if (dbString.equals("postgres")) {
			return "org.hibernate.dialect.PostgreSQLDialect";
		} else if (dbString.equals("hsqldb")) {
			return "org.hibernate.dialect.HSQLDialect";
		} else if (dbString.equals("derby")) {
			return "org.hibernate.dialect.DerbyDialect";
		} else {
			return "org.hibernate.dialect.DerbyDialect";
		}

	}
}
