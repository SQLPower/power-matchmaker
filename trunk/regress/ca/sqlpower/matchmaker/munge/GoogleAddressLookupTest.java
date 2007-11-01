/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.munge;

import java.math.BigDecimal;

import junit.framework.TestCase;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class GoogleAddressLookupTest extends TestCase {

    private GoogleAddressLookup step;

    private MungeStepOutput<String> testInput;

    private final Logger logger = Logger.getLogger("testLogger");

    protected void setUp() throws Exception {
        super.setUp();
        logger.addAppender(new ConsoleAppender());
        logger.setLevel(Level.ALL);
        step = new GoogleAddressLookup();
        step.setParameter(GoogleAddressLookup.GOOGLE_MAPS_API_KEY, "ABQIAAAAC68VN0JS8nvnA3-VgSg5dRSPtI6ZTKhKKG8kdYEzcTLFAXRiHhS13bHpYAqAQNo1st7t_FZ7-22PWw");
        testInput = new MungeStepOutput<String>("Testing address values", String.class);
        step.connectInput(0, testInput);
    }

    public void testSimpleLookupCanada() throws Exception {
        step.open(logger);
        testInput.setData("4950 Yonge St, Toronto");  // SQLP WWHQ!
        step.call();
        assertEquals(BigDecimal.valueOf(43.765073), step.getOutputByName("Latitude").getData());
        assertEquals(BigDecimal.valueOf(-79.411909), step.getOutputByName("Longitude").getData());
        step.close();
    }

    public void testInvalidLookupCanada() throws Exception {
        step.open(logger);
        testInput.setData("1234 StreetDoesn't Exist");
        
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
            step.close();
            logger.removeAppender(ec);
        }
    }
}
