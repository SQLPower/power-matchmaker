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
