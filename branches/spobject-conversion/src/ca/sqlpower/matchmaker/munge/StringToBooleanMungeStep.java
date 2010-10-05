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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;



/**
 * This munge step will take in a string and return a boolean. The options 
 * of true list and false list are a comma separated list of Strings, or 
 * a regular expression that if passed in will set the output to true or 
 * false respectively. Use regular expression can be set to "true" or "false"
 * and indicates if the true and false lists are regular expressions. The 
 * neither option gives a default value to those that were not matched, options
 * for the default are "true", "false", "null".
 * TODO Implement "halt" as an allowed value.
 * 
 * In either of the comma separated lists a \, can be use to indicate 
 * that this comma is part of the string you are looking for.
 * 
 * All nulls will be passed through and not changed.
 * 
 * Note: The list of trues will be checked first.
 */
public class StringToBooleanMungeStep extends AbstractMungeStep {
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));

	/**
	 * Whether this step is case sensitive.
	 */
	private boolean caseSensitive;

	/**
	 * Whether this step uses regular expressions.
	 */
	private boolean useRegex;

	/**
	 * The values to convert to true
	 */
	private String trueList;

	/**
	 * The values to convert to false
	 */
	private String falseList;

	/**
	 * The value to set to a string that does not get matched. Options for this are 
	 * "true", "false", "null" 
	 */
	private Boolean neither;

	/**
	 * Case sensitive is set to true and use regex is set to false for this
	 * munge step as defaults.
	 */
	@Constructor
	public StringToBooleanMungeStep() {
		super("String to Boolean",false);
		MungeStepOutput<Boolean> out = new MungeStepOutput<Boolean>("StringToBooleanOutput", Boolean.class);
		addChild(out);

		InputDescriptor desc = new InputDescriptor("retainCharacters", String.class);
		setCaseSensitive(true);
		setUseRegex(false);
		setTrueList("");
		setFalseList("");
		setNeither(null);
		
		super.addInput(desc);
	}

	@Accessor
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Mutator
	public void setCaseSensitive(boolean caseSensitive) {
		boolean old = this.caseSensitive;
		this.caseSensitive = caseSensitive;
		firePropertyChange("caseSensitive", old, caseSensitive);
	}

	@Accessor
	public boolean isUseRegex() {
		return useRegex;
	}

	@Mutator
	public void setUseRegex(boolean useRegex) {
		boolean old = this.useRegex;
		this.useRegex = useRegex;
		firePropertyChange("useRegex", old, useRegex);
	}

	@Accessor
	public String getTrueList() {
		return trueList;
	}

	@Mutator
	public void setTrueList(String trueList) {
		String old = this.trueList;
		this.trueList = trueList;
		firePropertyChange("trueList", old, trueList);
	}

	@Accessor
	public String getFalseList() {
		return falseList;
	}

	@Mutator
	public void setFalseList(String falseList) {
		String old = this.falseList;
		this.falseList = falseList;
		firePropertyChange("falseList", old, falseList);
	}

	@Accessor
	public Boolean getNeither() {
		return neither;
	}

	@Mutator
	public void setNeither(Boolean neither) {
		Boolean old = this.neither;
		this.neither = neither;
		firePropertyChange("neither", old, neither);
	}

	public Boolean doCall() throws Exception {
		MungeStepOutput<Boolean> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		boolean caseSensitive = isCaseSensitive();
		boolean useRegex = isUseRegex();
		String trueList = getTrueList();
		String falseList = getFalseList();
		Boolean neither = getNeither();

		if (in.getData() != null) {
			String data = in.getData();
			Pattern t = getPattern(useRegex, caseSensitive, trueList);
			Pattern f = getPattern(useRegex, caseSensitive, falseList);

			if (t.matcher(data).matches()) {				
				out.setData(true);
			} else if (f.matcher(data).matches()) {
				out.setData(false);
			} else {
				out.setData(neither);
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

