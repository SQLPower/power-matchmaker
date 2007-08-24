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


package ca.sqlpower.matchmaker.swingui;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.TranslateGroupParent;

public class TranslationComboBoxModelTest extends TestCase {

    private final class ListDataEventCounter implements ListDataListener {
        int contentsChanged;

        int intervalAdded;

        int intervalRemoved;

        ListDataEvent lastEvent = null;

        public void contentsChanged(ListDataEvent e) {
            contentsChanged++;
            lastEvent = e;
        }

        public void intervalAdded(ListDataEvent e) {
            intervalAdded++;
            lastEvent = e;
        }

        public void intervalRemoved(ListDataEvent e) {
            intervalRemoved++;
            lastEvent = e;
        }
        
        public int getAllEvents(){
            return getContentsChanged()+ getIntervalAdded()+ getIntervalRemoved();
        }

        public int getContentsChanged() {
            return contentsChanged;
        }

        public void setContentsChanged(int contentsChanged) {
            this.contentsChanged = contentsChanged;
        }

        public int getIntervalAdded() {
            return intervalAdded;
        }

        public void setIntervalAdded(int intervalAdded) {
            this.intervalAdded = intervalAdded;
        }

        public int getIntervalRemoved() {
            return intervalRemoved;
        }

        public void setIntervalRemoved(int intervalRemoved) {
            this.intervalRemoved = intervalRemoved;
        }

        public ListDataEvent getLastEvent() {
            return lastEvent;
        }

        public void setLastEvent(ListDataEvent lastEvent) {
            this.lastEvent = lastEvent;
        }
    }

    TranslationComboBoxModel tcbm;
    TestingMatchMakerSession session;
    TranslateGroupParent tgp;
    ListDataEventCounter counter;
    private MatchMakerTranslateGroup tg;
    
    protected void setUp() throws Exception {
        super.setUp();
        session=new TestingMatchMakerSession();
        tgp = session.getTranslations();
        tg = new MatchMakerTranslateGroup();
        tg.setName("Translate Group 1");
        tgp.addChild(tg);
        // we want to start out with 0 events
        tcbm = new TranslationComboBoxModel(tgp);
        counter = new ListDataEventCounter();
        tcbm.addListDataListener(counter);
    }

    public void testChildInsertedPassedOnCorrectly(){
        MatchMakerTranslateGroup tg2 = new MatchMakerTranslateGroup();
        tg2.setName("Translate Group 2");
        
        tgp.addChild(tg2);
        assertEquals("Incorrect number of events fired ",1,counter.getAllEvents());
        assertEquals("Event fired to the wrong location ",1,counter.getIntervalAdded());
        assertEquals("Wrong Type of event ",ListDataEvent.INTERVAL_ADDED,counter.getLastEvent().getType());
        assertEquals("Wrong lower bound ", 1, counter.getLastEvent().getIndex0());
        assertEquals("Wrong Upper bound ", 1, counter.getLastEvent().getIndex1());
    }
    
    public void testChildRemovedPassedOnCorrectly(){
        tgp.removeChild(tg);
        assertEquals("Incorrect number of events fired ",1,counter.getAllEvents());
        assertEquals("Event fired to the wrong location ",1,counter.getIntervalRemoved());
        assertEquals("Wrong Type of event ",ListDataEvent.INTERVAL_REMOVED,counter.getLastEvent().getType());
        assertEquals("Wrong lower bound ", 0, counter.getLastEvent().getIndex0());
        assertEquals("Wrong Upper bound ", 0, counter.getLastEvent().getIndex1());
    }
    
    protected void tearDown() throws Exception {
        tgp.getChildren().clear();
    }
}
