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

import java.math.BigDecimal;
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
 * This munge step will return the number of words in a given string based
 * on a given delimiter string. By default, the delimiter is interpreted as a list
 * of delimiter characters. Optionally, the step can also interpret the delimiter
 * input as a regular expression, as long as the {@link #USE_REGEX_PARAMETER_NAME}
 * parameter is set to "true". The output data in the MungeStepOutput will be of type 
 * {@link BigDecimal}.
 * <p>
 * Using the regular expression option will behave similarly to getting the array
 * size of the String[] result from calling {@link String#split} with the 
 * regular expression delimiter.
 */
public class WordCountMungeStep extends AbstractMungeStep {
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));

	/**
	 * The string that will be used as the delimiter for this munge step.
	 */
	private String delimiter;
	
	/**
	 * Whether to use regular expressions in this munge step.
	 */
	private boolean regex;
	
	/**
	 * Whether the effects of this munge step should be case sensitive.
	 */
	private boolean caseSensitive;
	
	@Constructor
	public WordCountMungeStep() {
		super("Word Count",false);
		setDelimiter(" ");
		setRegex(false);
		setCaseSensitive(true);
		
		MungeStepOutput<BigDecimal> out = new MungeStepOutput<BigDecimal>("wordCountOutput", BigDecimal.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("string", String.class);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Word count munge step does not support addInput()");
	}
	
	@Override
	public boolean removeInput(int index) {
		throw new UnsupportedOperationException("Word count munge step does not support removeInput()");
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
				"Word count munge step does not accept non-String inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	public Boolean doCall() throws Exception {
		String delimiter = getDelimiter();
		boolean useRegex = isRegex();
		boolean caseSensitive = isCaseSensitive();
		
		MungeStepOutput<BigDecimal> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		String data = in.getData();
		
		int wordCount = 0;
		if (data != null) {
			if (!useRegex) {
				// This block of code adds escape characters to each of
				// the regex special characters to be taken as literals
				String specialChars = "-+*?()[]{}|^<=";
				delimiter = delimiter.replaceAll("\\\\", "\\\\\\\\");
				delimiter = delimiter.replaceAll("\\$", "\\\\\\$");
				for (char letter : specialChars.toCharArray()) {
					delimiter = delimiter.replaceAll("\\" + letter, "\\\\" + letter);
				}
				
				delimiter = "[" + delimiter + "]+";
			} 
			Pattern p;
			if (!caseSensitive) {
				p = Pattern.compile(delimiter, Pattern.CASE_INSENSITIVE);
			} else {
				p = Pattern.compile(delimiter);
			}
			wordCount = p.split(data).length;
		}
		
		out.setData(new BigDecimal(wordCount));
		return true;
	}

	@Mutator
	public void setDelimiter(String delim) {
			String old = delimiter;
			delimiter = delim;
			firePropertyChange("delimiter", old, delim);
	}

	@Accessor
	public String getDelimiter() {
		return delimiter;
	}

	@Mutator
	public void setRegex(boolean regex) {
			boolean old = this.regex;
			this.regex = regex;
			firePropertyChange("regex", old, regex);
	}

	@Accessor
	public boolean isRegex() {
		return regex;
	}

	@Mutator
	public void setCaseSensitive(boolean caseSensitive) {
			boolean old = this.caseSensitive;
			this.caseSensitive = caseSensitive;
			firePropertyChange("caseSensitive", old, caseSensitive);
	}

	@Accessor
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
}
