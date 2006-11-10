package ca.sqlpower.matchmaker;

import java.util.Random;

public class TestingAbstractMatchMakerObject extends AbstractMatchMakerObject {

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

}
