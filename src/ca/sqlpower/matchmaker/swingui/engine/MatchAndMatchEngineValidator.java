package ca.sqlpower.matchmaker.swingui.engine;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.EngineSettingException;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchEngineImpl;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * A Validator to check the preconditions of the engine.
 */
class MatchAndMatchEngineValidator implements Validator {
	
	private static final Logger logger = Logger
			.getLogger(MatchAndMatchEngineValidator.class);

	private Match match;
	private MatchMakerSession session;

	public MatchAndMatchEngineValidator(MatchMakerSession session, Match match) {
		this.match = match;
		this.session = session;
	}

	public ValidateResult validate(Object contents) {
		MatchEngineImpl matchEngine = new MatchEngineImpl(session, match);
		try {
			matchEngine.checkPreconditions();
		} catch (EngineSettingException ex) {
			return ValidateResult.createValidateResult(Status.FAIL,
					ex.getMessage());
		} catch (Exception ex) {
            logger.warn("Unexpected exception while checking engine preconditions", ex);
            return ValidateResult.createValidateResult(Status.FAIL,
                    "Unexpected exception: "+ex.getMessage());
        }
		return ValidateResult.createValidateResult(Status.OK, "");
	}

}