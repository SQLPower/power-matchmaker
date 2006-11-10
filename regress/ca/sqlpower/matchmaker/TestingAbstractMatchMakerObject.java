package ca.sqlpower.matchmaker;

import java.util.Random;

public class TestingAbstractMatchMakerObject extends AbstractMatchMakerObject {

	int i;
	
	public TestingAbstractMatchMakerObject( ) {
		Random rand  = new Random();
		i = rand.nextInt();
		this.setAppUserName("Test User");
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
