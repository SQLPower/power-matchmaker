package ca.sqlpower.matchmaker.util;
/**
 * A SQL Query
 * 
 * note this is a really dumb bean.
 *
 */
public class SQLQuery {

	/** The columns in the select statement as in SQL with no SELECT */
	private String select;
	/** the from clause of a SQL statment without FROM */
	private String from;
	/** A SQL where clause without where */
	private String where;
	

	public SQLQuery(String select, String from, String where) {
		super();
		this.select = select;
		this.from = from;
		this.where = where;
	}
	
	public SQLQuery() {
		super();
	}
	
	/**
	 * Combines the select,from and where fields and adds the appropriate keywords
	 * to make a SQL Statement
	 */
	public String getSQLStatement(){
		StringBuffer buf = new StringBuffer();
		buf.append("SELECT ").append(select);
		buf.append("\n	FROM ").append(from);
		buf.append("\n	WHERE ").append(where).append(";");
		return buf.toString();
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		if (this.from != from) {
			this.from = from;
		}
	}

	public String getSelect() {
		return select;
	}

	public void setSelect(String select) {
		if (this.select != select) {
			this.select = select;
		}
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(String where) {
		if (this.where != where) {
			this.where = where;
		}
	}
	
	
}
