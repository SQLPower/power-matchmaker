/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.munge;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * This munge step will only retain the given characters in the input string.
 */
public class RetainCharactersMungeStep extends AbstractMungeStep {

	private MungeStepOutput<String> out;
	
	/**
	 * This is the name of the parameter that decides whether this step will be
	 * case sensitive. The only values accepted by the parameter are "true" and
	 *  "false".
	 */
	public static final String CASE_SENSITIVE_PARAMETER_NAME = "caseSensitive";
	
	/**
	 * This is the name of the parameter that decides whether this step will use
	 * regular expression to replace words. The only values accepted by the parameter
	 * are "true" and "false".
	 */
	public static final String USE_REGEX_PARAMETER_NAME = "useRegex";
	
	/**
	 * This is the name of the parameter containing the list of characters to 
	 * retain. The parameter would be interpreted as a regular expression if
	 * the option is set to true.
	 */
	public static final String RETAIN_CHARACTERS_PARAMETER_NAME = "retainChars";

	/**
	 * Case sensitive is set to true and use regex is set to false for this
	 * munge step as defaults.
	 */
	public RetainCharactersMungeStep() {
		setName("Retain Chars");
		out = new MungeStepOutput<String>("retainCharactersOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("retainCharacters", String.class);
		setParameter(CASE_SENSITIVE_PARAMETER_NAME, "true");
		setParameter(USE_REGEX_PARAMETER_NAME, "false");
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Retain characters munge step does not support addInput()");
	}
	
	@Override
	public void removeInput(int index) {
		throw new UnsupportedOperationException("Retain characters munge step does not support removeInput()");
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
				"Retain characters munge step does not accept non-String inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	/**
	 * Regex can not be used in combination with case insensitivity, this should
	 * be disabled in the UI.
	 */
	public Boolean call() throws Exception {
		super.call();

		MungeStepOutput<String> in = getInputs().get(0);
		String caseSensitive = getParameter(CASE_SENSITIVE_PARAMETER_NAME);
		String useRegex = getParameter(USE_REGEX_PARAMETER_NAME);
		String retainChars = getParameter(RETAIN_CHARACTERS_PARAMETER_NAME);
		String data = in.getData();
		StringBuilder result = new StringBuilder();
		
		if (in.getData() != null) {
			if (useRegex.equals("true")) {
				Pattern p = Pattern.compile(retainChars);
				for (Character letter: data.toCharArray()) {
					Matcher m = p.matcher(letter.toString());
					if (m.matches()) {
						result.append(letter);
					}
				}
			} else {
				if (caseSensitive.equals("false")) {
					String retainLowerCase = retainChars.toLowerCase();
					String retainUpperCase = retainChars.toUpperCase();
					retainChars = retainLowerCase + retainUpperCase;
				}
				for (Character letter : data.toCharArray()) {
					if (retainChars.contains(letter.toString())) {
						result.append(letter);
					}
				}
			}
			out.setData(result.toString());
		} else {
			out.setData(null);
		}
		return true;
	}

	public boolean canAddInput() {
		return false;
	}
}
