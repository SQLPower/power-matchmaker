/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
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
		super("Retain Chars",false);
		MungeStepOutput<String> out = new MungeStepOutput<String>("retainCharactersOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("retainCharacters", String.class);
		setParameter(CASE_SENSITIVE_PARAMETER_NAME, true);
		setParameter(USE_REGEX_PARAMETER_NAME, false);
		setParameter(RETAIN_CHARACTERS_PARAMETER_NAME, "");
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Retain characters munge step does not support addInput()");
	}
	
	@Override
	public boolean removeInput(int index) {
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
	
	public Boolean doCall() throws Exception {
	
		MungeStepOutput<String> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		boolean caseSensitive = getBooleanParameter(CASE_SENSITIVE_PARAMETER_NAME);
		boolean useRegex = getBooleanParameter(USE_REGEX_PARAMETER_NAME);
		String retainChars = getParameter(RETAIN_CHARACTERS_PARAMETER_NAME);
		String data = in.getData();
		StringBuilder result = new StringBuilder();
		
		if (in.getData() != null) {
			Pattern p;
			if (!useRegex) {
				// This block of code adds escape characters to each of
				// the regex special characters to be taken as literals
				String specialChars = "-+*?()[]{}|^<=";
				retainChars = retainChars.replaceAll("\\\\", "\\\\\\\\");
				retainChars = retainChars.replaceAll("\\$", "\\\\\\$");
				for (char letter : specialChars.toCharArray()) {
					retainChars = retainChars.replaceAll("\\" + letter, "\\\\" + letter);
				}
				
				retainChars = "[" + retainChars + "]+";
			}
			if (!caseSensitive) {
				p = Pattern.compile(retainChars, Pattern.CASE_INSENSITIVE);
			} else {
				p = Pattern.compile(retainChars);
			}
			
			for (Character letter: data.toCharArray()) {
				Matcher m = p.matcher(letter.toString());
				if (m.matches()) {
					result.append(letter);
				}
			}
			out.setData(result.toString());
		} else {
			out.setData(null);
		}
		return true;
	}
}
