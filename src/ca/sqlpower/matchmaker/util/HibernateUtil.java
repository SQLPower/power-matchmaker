package ca.sqlpower.matchmaker.util;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import ca.sqlpower.sql.SPDataSourceType;


/**
 * A collection of static methods that make it easier to work with Hibernate.
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

    /**
     * This method shouldn't exist. We should put hibernate dialect information in the
     * data source type section of the PL.INI, and not do any cross-referencing.
     */
	public static String guessHibernateDialect(SPDataSourceType dsType){
		if (dsType == null ) throw new IllegalArgumentException("No dialect for a null database type");
		String dbString = dsType.getName().toLowerCase();

		if( dbString.startsWith("oracle")) {
			return "org.hibernate.dialect.OracleDialect";
		} else if (dbString.contains("sql server")) {
			return "org.hibernate.dialect.SQLServerDialect";
		} else if (dbString.contains("db2")) {
			return "org.hibernate.dialect.DB2Dialect";
		} else if (dbString.startsWith("postgres")) {
			return "org.hibernate.dialect.PostgreSQLDialect";
		} else if (dbString.equals("hsqldb")) {
			return "org.hibernate.dialect.HSQLDialect";
		} else if (dbString.contains("derby")) {
			return "org.hibernate.dialect.DerbyDialect";
		} else {
			throw new IllegalArgumentException("I don't know the hibernate dialect for " + dbString);
		}

	}
}
