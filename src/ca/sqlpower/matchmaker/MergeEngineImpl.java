package ca.sqlpower.matchmaker;


public class MergeEngineImpl extends AbstractCEngine {

	@Override
	public void checkPreconditions() throws EngineSettingException {
		throw new EngineSettingException("Merge engine integration is not implemented");
	}

	public String createCommandLine(MatchMakerSession session, Match match, boolean userPrompt) {
		// TODO Auto-generated method stub
		return null;
	}
}
