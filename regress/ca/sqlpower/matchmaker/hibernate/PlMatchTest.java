package ca.sqlpower.matchmaker.hibernate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.diasparsoftware.util.junit.ValueObjectEqualsTest;

public class PlMatchTest extends ValueObjectEqualsTest {

	/** Since PlMatch,equals() ONLY looks at MatchID, not fields, we must use this... */
	public static final String FAKE_MATCH_ID = "12345";

	public void testGetChildCount() {
		Set<PlMatchGroup> plMatchGroups = new TreeSet<PlMatchGroup>();
		PlMatch target = new PlMatch();

		target.addAllPlMatchGroups(plMatchGroups);
		assertEquals(0, target.getChildCount());
		plMatchGroups.add(new PlMatchGroup());
		target.addAllPlMatchGroups(plMatchGroups);
		assertEquals(1, target.getChildCount());
	}

	public void testEqualsObject() {
		PlMatch target = new PlMatch();
		assertEquals(target, target);
	}


	@Override
	protected Object createControlInstance() throws Exception {
		PlMatch target = new PlMatch();
		target.setMatchId(FAKE_MATCH_ID);
		return target;
	}

	@Override
	protected Object createInstanceDiffersIn(String arg0) throws Exception {
		PlMatch target = new PlMatch();
		target.setMatchId(FAKE_MATCH_ID);
		if ("matchId".equals(arg0)) {
			target.setMatchId("foo");
		}
		return target;
	}

	@Override
	protected List keyPropertyNames() {
		return Arrays.asList(new String[] { "matchId" });
	}
}
