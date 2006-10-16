package ca.sqlpower.matchmaker.hibernate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.darwinsys.testing.TestUtils;

import junit.framework.TestCase;


//import com.diasparsoftware.util.junit.ValueObjectEqualsTest;

public class PlMatchTest extends /*ValueObjectEqualsTest*/TestCase {

	/** Since PlMatch,equals() ONLY looks at MatchID, not fields, we must use this... */
	public static final String FAKE_MATCH_ID = "12345";

	PlMatch target = new PlMatch();

	public void testGetChildCount() {
		Set<PlMatchGroup> plMatchGroups = new TreeSet<PlMatchGroup>();

		target.addAllPlMatchGroups(plMatchGroups);
		assertEquals(0, target.getChildCount());
		plMatchGroups.add(new PlMatchGroup());
		target.addAllPlMatchGroups(plMatchGroups);
		assertEquals(1, target.getChildCount());
	}

	public void testEqualsObject() {
		assertEquals(target, target);
	}

	public void testCopyOf() {
		PlMatch target2 = target.copyOf();
		assertNotNull(target2);
		assertTrue(TestUtils.equals(target, target2));
		target2.setBatchFileName("fred");
		assertFalse(TestUtils.equals(target, target2));
	}

	//@Override
	protected Object createControlInstance() throws Exception {
		target.setMatchId(FAKE_MATCH_ID);
		return target;
	}

	//@Override
	protected Object createInstanceDiffersIn(String arg0) throws Exception {
		target.setMatchId(FAKE_MATCH_ID);
		if ("matchId".equals(arg0)) {
			target.setMatchId("foo");
		}
		return target;
	}

	//@Override
	protected List keyPropertyNames() {
		return Arrays.asList(new String[] { "matchId" });
	}
}
