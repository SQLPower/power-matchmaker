package ca.sqlpower.matchmaker.hibernate;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import com.diasparsoftware.util.junit.ValueObjectEqualsTest;
import com.gargoylesoftware.base.testing.EqualsTester;

public class PlFolderDetailIdTest extends ValueObjectEqualsTest {

	PlFolderDetailId targ = new PlFolderDetailId("folder", "someObjectType", "someOjectName");

	@Override
	protected List<String> keyPropertyNames() {
		return Arrays.asList(
				new String[] { "folderName", "objectType", "objectName"});
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPlFolderDetailIdStringStringString() {
		assertEquals("folder", targ.getFolderName());
		assertEquals("someObjectType",  targ.getObjectType());
		assertEquals("someOjectName", targ.getObjectName());
	}

	public void testCopyConstructor() {
		assertEquals("copy constructor test", targ, targ.copyOf());
	}

	public void testUsingEqualsTester() throws Throwable {

		// With default values
		PlFolderDetailId a = new PlFolderDetailId();
		PlFolderDetailId b = new PlFolderDetailId();
		PlFolderDetailId c = new PlFolderDetailId();
		c.setFolderName("different");
		PlFolderDetailId d = new PlFolderDetailId() { };
		new EqualsTester(a, b, c, d);

		// Again with non-default values
		PlFolderDetailId targ2 = new PlFolderDetailId("folder", "someObjectType", "someOjectName");
		PlFolderDetailId different = new PlFolderDetailId("foo", "bar", "retch");
		PlFolderDetailId subclass = new PlFolderDetailId() {
			// subclass;
		};
		BeanUtils.copyProperties(subclass, targ);	// an oft-overlooked step wtith non-default values
		new EqualsTester(targ, targ2, different, subclass);
	}

	// ValueObjectEqualsTest

	@Override
	protected Object createControlInstance() throws Exception {
		return new PlFolderDetailId("folder", "someObjectType", "someOjectName");
	}

	@Override
	protected Object createInstanceDiffersIn(String ktv) throws Exception {
		PlFolderDetailId differs = new PlFolderDetailId("folder", "someObjectType", "someOjectName");
		if ("folderName".equals(ktv)) {
			differs.setFolderName("fish");
			return differs;
		} else if ("objectType".equals(ktv)) {
			differs.setObjectType("fish");
			return differs;
		} else if ("objectName".equals(ktv)) {
			differs.setObjectName("fish");
			return differs;
		} else {
			throw new IllegalArgumentException(ktv);
		}
	}
}
