package ca.sqlpower.matchmaker;

import junit.framework.TestCase;

public class ExternalEngineUtilsTest extends TestCase {

	public void testGetProgramPath() {
		String mmPath = ExternalEngineUtils.getProgramPath(EnginePath.MATCHMAKER);
		System.out.println("Path is " + mmPath);
		assertNotNull(mmPath);
	}

}
