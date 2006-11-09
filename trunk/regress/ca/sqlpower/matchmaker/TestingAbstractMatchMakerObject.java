package ca.sqlpower.matchmaker;

import java.util.Random;

public class TestingAbstractMatchMakerObject extends AbstractMatchMakerObject {

	int i;
	
	public TestingAbstractMatchMakerObject(String appUserName) {
		super(appUserName);
		Random rand  = new Random();
		i = rand.nextInt();
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
