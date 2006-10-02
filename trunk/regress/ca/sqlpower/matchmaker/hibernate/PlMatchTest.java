package ca.sqlpower.matchmaker.hibernate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import com.darwinsys.testing.TestUtils;

//import com.diasparsoftware.util.junit.ValueObjectEqualsTest;

public class PlMatchTest extends /*ValueObjectEqualsTest*/TestCase {

	PlMatch target = new PlMatch();

	public void testGetChildCount() {
		Set<PlMatchGroup> plMatchGroups = new TreeSet<PlMatchGroup>();

		target.setPlMatchGroups(plMatchGroups);
		assertEquals(0, target.getChildCount());
		plMatchGroups.add(new PlMatchGroup());
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
		return target;
	}

	//@Override
	protected Object createInstanceDiffersIn(String arg0) throws Exception {
		// TODO Auto-generated method stub
		return target;
	}

	//@Override
	protected List keyPropertyNames() {
		// TODO Auto-generated method stub
		return Arrays.asList(new String[] { "foo", "bar"});
	}
}
