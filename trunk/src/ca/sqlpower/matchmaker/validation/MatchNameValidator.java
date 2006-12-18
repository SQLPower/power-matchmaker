package ca.sqlpower.matchmaker.validation;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

public class MatchNameValidator implements Validator {

	private MatchMakerSwingSession session;
	private Match match;
    private static final int MAX_CHAR_MATCH_NAME = 80;

	public MatchNameValidator(MatchMakerSwingSession session, Match match) {
		this.session = session;
		this.match = match;
	}

	public ValidateResult validate(Object contents) {	    
		String value = (String)contents;
		if ( value == null || value.length() == 0 ) {
			return ValidateResult.createValidateResult(Status.FAIL,
					"Match name is required");
		} else if ( !value.equals(match.getName()) &&
					!session.isThisMatchNameAcceptable(value) ) {
			return ValidateResult.createValidateResult(Status.FAIL,
					"Match name is invalid or already exists.");
		} else if (value.length() > MAX_CHAR_MATCH_NAME){
		    return ValidateResult.createValidateResult(Status.FAIL, "Match ID cannot be more than "+
                    MAX_CHAR_MATCH_NAME + " characters long");
        }
		return ValidateResult.createValidateResult(Status.OK, "");
	}
}