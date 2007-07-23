package ca.sqlpower.matchmaker;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.PLSchemaException;
import ca.sqlpower.sql.SchemaVersionFormatException;

public class TestingMatchMakerContext implements MatchMakerSessionContext {
	List<ArchitectDataSource> dataSources;
	String emailEngineLocation;
	String matchEngineLocation;
	DataSourceCollection plDotIni;
	MatchMakerSession session;
	
	public List<ArchitectDataSource> getDataSources() {
		return dataSources;
	}

	public void setDataSources(List<ArchitectDataSource> dataSources) {
		this.dataSources = dataSources;
	}

	public String getEmailEngineLocation() {
		return emailEngineLocation;
	}

	public void setEmailEngineLocation(String emailEngineLocation) {
		this.emailEngineLocation = emailEngineLocation;
	}

	public String getMatchEngineLocation() {
		return matchEngineLocation;
	}

	public void setMatchEngineLocation(String matchEngineLocation) {
		this.matchEngineLocation = matchEngineLocation;
	}

	public DataSourceCollection getPlDotIni() {
		return plDotIni;
	}

	public void setPlDotIni(DataSourceCollection plDotIni) {
		this.plDotIni = plDotIni;
	}

	public MatchMakerSession getSession() {
		return session;
	}

	public void setSession(MatchMakerSession session) {
		this.session = session;
	}

	public MatchMakerSession createSession(ArchitectDataSource ds,
			String username, String password) throws PLSecurityException,
			SQLException, IOException,
			SchemaVersionFormatException, PLSchemaException {
		return session;
	}


}
