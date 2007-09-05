package ca.sqlpower.matchmaker.swingui.engine;

import java.io.File;

import org.apache.log4j.Logger;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * A validator that checks if the engine path points to an actual file.
 * If the file doesn't exist, the validation error message will be:
 * <p>
 * <b><i>fileTerm</i> not found at given location</b>.
 */
public class FileExistsValidator implements Validator {
	
	private static final Logger logger = Logger
			.getLogger(FileExistsValidator.class);
	
	/**
	 * The term used to refer to the file we're validating.
	 */
	private final String fileTerm;

	public FileExistsValidator(String fileTerm) {
		this.fileTerm = fileTerm;
	}

	/**
	 * Checks that the given object is the path name of a file that
	 * exists.
	 * 
	 * @param contents The path name to check.  Must be of type String.
	 */
	public ValidateResult validate(Object contents) {
		
		String fileLocation = (String) contents;
		File file = new File(fileLocation);
		if (!file.exists()) {
			return ValidateResult.createValidateResult(
					Status.FAIL,
					fileTerm + " not found at given location");
		}

		return ValidateResult.createValidateResult(Status.OK, "");
	}

}