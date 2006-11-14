package ca.sqlpower.matchmaker.util;
/**
 * Specification for a view
 *
 * FIXME write me
 */
public class ViewSpec extends SQLQuery {
	/** the view's name */
	private String name;
	/** The jdbc catalog containing the view */
	private String catalog;
	/** the jdbc schema containing the view */
	private String schema;
	
	public ViewSpec(){
		
	}
	
	public ViewSpec(String select, String from, String where) {
		super(select,from,where);
	}

	private void create(){
		
	}
	
	private void drop(){
		
	}
	
	private void replace(){
		
	}
	
	private void verifyQuery(){
		
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		if (this.catalog != catalog) {
			this.catalog = catalog;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (this.name != name) {
			this.name = name;
		}
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		if (this.schema != schema) {
			this.schema = schema;
		}
	}

}
