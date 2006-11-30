package ca.sqlpower.sql;

import java.sql.Connection;
import java.sql.SQLException;

import ca.sqlpower.matchmaker.MatchMakerSession;

public class TestingDefParamsObject extends DefaultParameters {
	
	String emailServerName = null;
	String emailReturnAddress = null;

	public TestingDefParamsObject(Connection arg0) throws SQLException, PLSchemaException {
		super(arg0);
	}

	public TestingDefParamsObject(MatchMakerSession session) throws SQLException, PLSchemaException {
		super(session.getConnection());
	}

	@Override
	public String get(String arg0) {
		return super.get(arg0);
	}

	public void setEmailReturnAddress(String emailReturnAddress) {
		this.emailReturnAddress = emailReturnAddress;
	}

	public void setEmailServerName(String emailServerName) {
		this.emailServerName = emailServerName;
	}

	@Override
	public String getEmailReturnAddress() {
		return emailReturnAddress;
	}

	@Override
	public String getEmailServerName() {
		return emailServerName;
	}

}
