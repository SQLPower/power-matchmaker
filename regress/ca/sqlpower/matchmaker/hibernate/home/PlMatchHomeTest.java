package ca.sqlpower.matchmaker.hibernate.home;

import javax.swing.SwingUtilities;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.util.HibernateUtil;

public class PlMatchHomeTest extends TestCase {

	PlMatch match = new PlMatch();
	ArchitectDataSource dbcs = new ArchitectDataSource();
	
	public PlMatchHomeTest() {
		dbcs.setDriverClass("oracle.jdbc.driver.OracleDriver");
		dbcs.setName("newpl");
		dbcs.setPlSchema("newpl");
		dbcs.setPlDbType("Oracle");
		dbcs.setPass("newpl");
		dbcs.setUrl("jdbc:oracle:thin:@arthur:1521:TEST");
		dbcs.setUser("newpl");
		HibernateUtil.createRepositorySessionFactory(dbcs);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testSave(){
		match.setMatchType("SOME TYPE");
		match.setMatchId("THIS ID REALLY SHOULD NOT BE DUPLICATED IT IS FOR TESTING");

		final PlMatchHome home = new PlMatchHome();
		try {
			SwingUtilities.invokeAndWait(new Runnable(){

				public void run() {
					System.out.println("Saving....");
					home.saveOrUpdate(match);
					home.flush();
					System.out.println("Deleting...");
					home.delete(match);
					home.flush();
				}
				
			});
		} catch (Throwable e) {		
			e.printStackTrace();
			fail("See console output");
		} 
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
