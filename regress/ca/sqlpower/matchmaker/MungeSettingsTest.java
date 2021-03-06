/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker;

import java.util.Date;

import ca.sqlpower.object.SPObject;

public class MungeSettingsTest extends MatchMakerTestCase<MungeSettings> {

	public MungeSettingsTest(String name) {
		super(name);
	}

	MungeSettings ms;
	protected void setUp() throws Exception {
		super.setUp();
		Project project = (Project) createNewValueMaker(
				getRootObject(), null).makeNewValue(Project.class, null, "Parent project");
		ms = project.getMungeSettings();
	}

	@Override
	protected MungeSettings getTarget() {
		return ms;
	}

    public void testSetLastRunDateDefensive() {
        Date myDate = new Date();
        ms.setLastRunDate(myDate);
        assertEquals(myDate, ms.getLastRunDate());
        assertNotSame(myDate, ms.getLastRunDate());
    }

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return null;
	}

	@Override
	public SPObject getSPObjectUnderTest() {
		return getTarget();
	}
	
	@Override
	public void testPersisterCreatesNewObjects() throws Exception {
		//Since this class is only ever added as a final object to a project
		//we cannot test if it is correctly added through an add child method
		//as it is not allowed.
	}
}
