package ca.sqlpower.matchmaker;

import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectDataSource;

public class MatchMakerSessionContextTest extends TestCase {

	private MatchMakerSessionContext ctx;

	public void testCreateSession() throws Exception {
		List<ArchitectDataSource> sources = ctx.getDataSources();
		ArchitectDataSource ds =  sources.get(0);
		MatchMakerSession session = ctx.createSession(ds, "test", "test");
		assertNotNull(session);
	}
}
