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

package ca.sqlpower.matchmaker.munge;

import java.math.BigDecimal;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import ca.sqlpower.matchmaker.MatchMakerTestCase;
import ca.sqlpower.object.SPObject;

public class GoogleAddressLookupMungeStepTest extends MatchMakerTestCase<GoogleAddressLookupMungeStep> {

    public GoogleAddressLookupMungeStepTest(String name) {
		super(name);
	}

	private GoogleAddressLookupMungeStep step;

    private MungeStepOutput<String> testInput;

    private final Logger logger = Logger.getLogger("testLogger");

    protected void setUp() throws Exception {
        super.setUp();
        logger.addAppender(new ConsoleAppender());
        logger.setLevel(Level.ALL);
        step = new GoogleAddressLookupMungeStep();
        step.setParameter(GoogleAddressLookupMungeStep.GOOGLE_MAPS_API_KEY, "ABQIAAAAC68VN0JS8nvnA3-VgSg5dRSPtI6ZTKhKKG8kdYEzcTLFAXRiHhS13bHpYAqAQNo1st7t_FZ7-22PWw");
        testInput = new MungeStepOutput<String>("Testing address values", String.class);
        step.connectInput(0, testInput);
    }

    public void testSimpleLookupCanada() throws Exception {
        step.open(logger);
        testInput.setData("4950 Yonge St, Toronto");  // SQLP WWHQ!
        step.call();
        assertEquals(BigDecimal.valueOf(200), step.getOutputByName("Lookup Status").getData());
        assertEquals("CA", step.getOutputByName("Country Code").getData());
        assertEquals("ON", step.getOutputByName("Administrative Area").getData());
        assertEquals("Toronto Division", step.getOutputByName("Sub-Administrative Area").getData());
        assertEquals("Toronto", step.getOutputByName("Locality").getData());
        assertEquals("4950 Yonge St", step.getOutputByName("Street Address").getData());
        assertEquals("M2N", step.getOutputByName("Postal Code").getData());
        assertTrue(43.76477 < ((BigDecimal)(step.getOutputByName("Latitude").getData())).doubleValue());
        assertTrue(43.7648 > ((BigDecimal)(step.getOutputByName("Latitude").getData())).doubleValue());
        assertTrue(-79.412235 < ((BigDecimal)(step.getOutputByName("Longitude").getData())).doubleValue());
        assertTrue(-79.412225 > ((BigDecimal)(step.getOutputByName("Longitude").getData())).doubleValue());
        assertEquals(BigDecimal.valueOf(8), step.getOutputByName("Accuracy Code").getData());
        
        step.mungeCommit();
        step.mungeClose();
    }

    public void testInvalidLookupCanada() throws Exception {
        step.open(logger);
        testInput.setData("1234 StreetDoesn't Exist at all, definitely, I'm sure about it");
        
        class ErrorCounter extends AppenderSkeleton {

            /**
             * The number of times a logging event with the level of ERROR
             * has been delivered to this appender.
             */
            int errorCount;
            
            @Override
            protected void append(LoggingEvent evt) {
                if (evt.getLevel() == Level.ERROR) {
                    errorCount++;
                }
            }

            public void close() {
                // nothing to do
            }

            public boolean requiresLayout() {
                return false;
            }

        };
        
        ErrorCounter ec = new ErrorCounter();
        logger.addAppender(ec);
        try {
            // call should not throw an exception, but it must log an error
            step.call();
            assertEquals(1, ec.errorCount);
        } finally {
            step.mungeCommit();
            step.mungeClose();
            logger.removeAppender(ec);
        }
    }

	@Override
	protected GoogleAddressLookupMungeStep getTarget() {
		return step;
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return MungeStepOutput.class;
	}
    
	@Override
	public void testAllowedChildTypesField() throws Exception {
		// Already in AbstractMungeStep
	}
	
	@Override
	public void testDuplicate() throws Exception {
		// do nothing
	}
}
