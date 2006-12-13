package ca.sqlpower.matchmaker;

import java.util.Random;

import ca.sqlpower.matchmaker.event.MatchMakerEventSupport;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

public class TestingAbstractMatchMakerObject
				extends AbstractMatchMakerObject<TestingAbstractMatchMakerObject, MatchMakerObject> {

	int i;

	public TestingAbstractMatchMakerObject( ) {
		Random rand  = new Random();
		i = rand.nextInt();
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser("app user name");
		this.setSession(session);
	}

	@Override
	public boolean equals(Object obj) {
		return this==obj;
	}

	@Override
	public int hashCode() {
		return i;
	}

	/**
	 * Made public so test cases can fire specific events on demand.
	 */
	@Override
	public MatchMakerEventSupport
		<TestingAbstractMatchMakerObject, MatchMakerObject> getEventSupport() {
		return super.getEventSupport();
	}

	public boolean hasListener(MatchMakerListener<?,?> listener) {
		return getEventSupport().getListeners().contains(listener);
	}

	public TestingAbstractMatchMakerObject duplicate(MatchMakerObject parent, MatchMakerSession session) {
		return null;
	}
}
