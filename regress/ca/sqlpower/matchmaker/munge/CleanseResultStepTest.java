/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.munge;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTestCase;
import ca.sqlpower.object.SPObject;

public class CleanseResultStepTest extends MatchMakerTestCase<CleanseResultStep> {

    public CleanseResultStepTest(String name) {
		super(name);
	}

	private static final Logger logger = Logger.getLogger(CleanseResultStepTest.class);
    
    /**
     * The result step that we're testing the refresh behaviour of.
     */
    private CleanseResultStep resultStep;
    
    /**
     * Sets up the cleanse result step.
     */
    @Override
    protected void setUp() throws Exception {
        resultStep = new CleanseResultStep();
        super.setUp();
    }

	@Override
	protected CleanseResultStep getTarget() {
		return resultStep;
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return null;
	}
	@Override
	public void testAllowedChildTypesField() throws Exception {
		// Do nothing. This is in AbstractMungeStep
	}
}
