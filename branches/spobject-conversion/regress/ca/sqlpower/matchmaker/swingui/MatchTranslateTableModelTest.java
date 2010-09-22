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


package ca.sqlpower.matchmaker.swingui;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.object.ObjectDependentException;

public class MatchTranslateTableModelTest extends TestCase {

    private class TableModelEventCounter implements TableModelListener {
        private int tableChangedCount;
        private TableModelEvent lastEvent;
        
        public void tableChanged(TableModelEvent e) {
            tableChangedCount++;
            lastEvent = e;
        }

        public TableModelEvent getLastEvent() {
            return lastEvent;
        }

        public void setLastEvent(TableModelEvent lastEvent) {
            this.lastEvent = lastEvent;
        }

        public int getTableChangedCount() {
            return tableChangedCount;
        }

        public void setTableChangedCount(int tableChangedCount) {
            this.tableChangedCount = tableChangedCount;
        }
    }
    
    
    private JTable table;
    private TranslateWordsTableModel model;
    private MatchMakerTranslateGroup translateGroup;
    private MatchMakerTranslateWord tw;
    private TableModelEventCounter ec;
    
    protected void setUp() throws Exception {
        table = new JTable();
        translateGroup = new MatchMakerTranslateGroup();
        model = new TranslateWordsTableModel(translateGroup);
        tw = new MatchMakerTranslateWord();
        tw.setFrom("TW1");
        translateGroup.addChild(tw);
        table.setModel(model);
        ec = new TableModelEventCounter();
        model.addTableModelListener(ec);
    }
    
    public void testFireRowAdded(){
        MatchMakerTranslateWord tw2 = new MatchMakerTranslateWord();
        tw2.setFrom("tw2");
        translateGroup.addChild(tw2);
        assertEquals("Incorrect number of events fired ",1,ec.getTableChangedCount());
        assertEquals("Wrong Type of event ",TableModelEvent.INSERT,ec.getLastEvent().getType());
        assertEquals("Wrong lower bound ", 1, ec.getLastEvent().getFirstRow());
        assertEquals("Wrong Upper bound ", 1, ec.getLastEvent().getLastRow());
    }
    
    public void testFireRowRemoved(){
        try {
			translateGroup.removeChild(tw);
		} catch (ObjectDependentException e) {
			throw new RuntimeException(e);
		}
        assertEquals("Incorrect number of events fired ",1,ec.getTableChangedCount());
        assertEquals("Wrong Type of event ",TableModelEvent.DELETE,ec.getLastEvent().getType());
        assertEquals("Wrong lower bound ", 0, ec.getLastEvent().getFirstRow());
        assertEquals("Wrong Upper bound ", 0, ec.getLastEvent().getLastRow());
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    

}
