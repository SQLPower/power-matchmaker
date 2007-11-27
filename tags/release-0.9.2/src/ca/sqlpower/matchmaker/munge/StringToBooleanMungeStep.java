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

import java.util.regex.Pattern;



/**
 * This munge step will take in a string and return a boolean. The options 
 * of true list and false list are a comma separated list of Strings, or 
 * a regular expression that if passed in will set the output to true or 
 * false respectivly. Use regular expression can be set to "true" or "false"
 * and indicates if the true and false lists are regular expressions. The 
 * neither option gives a default value to those that were not matched, options
 * for the default are "true", "false", "null", "halt" where halt stops the 
 * operation.
 * 
 * In either of the comma separated lists a \, can be use to indicate 
 * that this comma is part of the string you are looking for.
 * 
 * All nulls will be passed through and not changed.
 * 
 * Note: The list of trues will be checked first.
 */
public class StringToBooleanMungeStep extends AbstractMungeStep {

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
	 * This is the name of the parameter containing the list of values to keep as true
	 */
	public static final String TRUE_LIST_PARAMETER_NAME = "true list";

	/**
	 * This is the name of the parameter containing the list of values to keep as false
	 */
	public static final String FALSE_LIST_PARAMETER_NAME = "false list";

	/**
	 * The value to set to a string that does not get matched. Options for this are 
	 * "true", "false", "null" 
	 */
	public static final String NEITHER_PARAMETER_NAME = "neighter true or false";

	/**
	 * Case sensitive is set to true and use regex is set to false for this
	 * munge step as defaults.
	 */
	public StringToBooleanMungeStep() {
		super("String to Boolean",false);
		MungeStepOutput<Boolean> out = new MungeStepOutput<Boolean>("StringToBooleanOutput", Boolean.class);
		addChild(out);

		InputDescriptor desc = new InputDescriptor("retainCharacters", String.class);
		setParameter(CASE_SENSITIVE_PARAMETER_NAME, true);
		setParameter(USE_REGEX_PARAMETER_NAME, false);
		setParameter(TRUE_LIST_PARAMETER_NAME, "");
		setParameter(FALSE_LIST_PARAMETER_NAME, "");
		setParameter(NEITHER_PARAMETER_NAME, "null");
		super.addInput(desc);
	}

	public Boolean doCall() throws Exception {
		MungeStepOutput<Boolean> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		boolean caseSensitive = getBooleanParameter(CASE_SENSITIVE_PARAMETER_NAME);
		boolean useRegex = getBooleanParameter(USE_REGEX_PARAMETER_NAME);
		String trueList = getParameter(TRUE_LIST_PARAMETER_NAME);
		String falseList = getParameter(FALSE_LIST_PARAMETER_NAME);
		String neither = getParameter(NEITHER_PARAMETER_NAME);

		if (in.getData() != null) {
			String data = in.getData();
			Pattern t = getPattern(useRegex, caseSensitive, trueList);
			Pattern f = getPattern(useRegex, caseSensitive, falseList);

			if (t.matcher(data).matches()) {				
				out.setData(true);
			} else if (f.matcher(data).matches()) {
				out.setData(false);
			} else {
				if (neither.equalsIgnoreCase("true")) {
					out.setData(true);
				} else if (neither.equalsIgnoreCase("false")) {
					out.setData(false);
				} else if (neither.equalsIgnoreCase("null")) {
					out.setData(null);
				} else {
					throw new IllegalArgumentException("Error converting \"" + data + 
							"\" to boolean, it did not match any of the given options");
				}
			}
		} else {
			out.setData(null);
		}
		return true;

	}

	private Pattern getPattern(boolean useRegex, boolean caseSensitive, String input) {
		Pattern p;
		String regex = input;
		int pos = 0;
		if (!useRegex) {
			regex = "";
			while (pos < input.length()) {
				String tmp = "";
				boolean bs = false;
				while (pos < input.length()) {
					if (bs || input.charAt(pos) != ',') {
						if (input.charAt(pos) == ',') {
							//this will remove the extra backslash
							tmp = tmp.substring(0,tmp.length()-1);
						}
						tmp += input.charAt(pos);
						bs = input.charAt(pos) == '\\';
					} else {
						pos++;
						break;
					}
					pos++;
				}

				// This block of code adds escape characters to each of
				// the regex special characters to be taken as literals
				String specialChars = "-+*?()[]{}|^<=";
				tmp = tmp.replaceAll("\\\\", "\\\\\\\\");
				tmp = tmp.replaceAll("\\$", "\\\\\\$");
				for (char letter : specialChars.toCharArray()) {
					tmp = tmp.replaceAll("\\" + letter, "\\\\" + letter);
				}

				if (regex.length() > 0) {
					regex += "|";
				}
				regex +=tmp;
			}

		}

		if (!caseSensitive) {
			p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		} else {
			p = Pattern.compile(regex);
		}

		return p;
	}

}

