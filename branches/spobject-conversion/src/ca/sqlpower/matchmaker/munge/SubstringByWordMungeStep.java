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
 * This munge step will return a output string containing the substrings 
 * of each of the individual words in the input. The words are defined by
 * given delimiter strings. Optionally, the step can also interpret the delimiter
 * input as a regular expression, as long as the {@link #USE_REGEX_PARAMETER_NAME}
 * parameter is set to "true". Each substring begins at the specified beginIndex
 * and extends to the character at index endIndex - 1 of the input words.
 * The substrings in the output are separated by {@link #RESULT_DELIM_PARAMETER_NAME}.
 */
public class SubstringByWordMungeStep extends AbstractMungeStep {

	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
	
	/**
	 * The begin index for the output substring of this munge step.
	 */
	private int begIndex;

	/**
	 * The end index for the output substring of this munge step.
	 */
	private int endIndex;
	
	/**
	 * Whether to use regular expressions in this munge step.
	 */
	private boolean regex;
	
	/**
	 * The string that will be used as the delimiter for this munge step.
	 */
	private String delimiter;
	
	/**
	 * The delimiter for the result of this munge step.
	 */
	private String resultDelim;
	
	/**
	 * Whether the effects of this munge step should be case sensitive.
	 */
	private boolean caseSensitive;
	
	@Constructor
	public SubstringByWordMungeStep() {
		super("Substring by Word",false);
		setDelimiter(" ");
		setResultDelim(" ");
		setRegex(false);
		setCaseSensitive(true);
		setBegIndex(0);
		setEndIndex(0);
		MungeStepOutput<String> out = new MungeStepOutput<String>("substringOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("substring", String.class);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Substring munge step does not support addInput()");
	}
	
	@Override
	public boolean removeInput(int index) {
		throw new UnsupportedOperationException("Substring munge step does not support removeInput()");
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
				"Substring munge step does not accept non-String inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	/**
	 * This call() throws an {@link IndexOutOfBoundsException} if the given
	 * indices were not in the range of the input
	 */
	public Boolean doCall() throws Exception {
		int beginIndex = getBegIndex();
		int endIndex = getEndIndex();
		
		String delimiter = getDelimiter();
		boolean useRegex = isRegex();
		boolean caseSensitive = isCaseSensitive();
		String resultDelim = getResultDelim();
		
		MungeStepOutput<String> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		String data = in.getData();
		StringBuilder results = new StringBuilder("");
		if (data != null) {
			if (beginIndex < 0) {
				throw new IndexOutOfBoundsException(
					"The begin index can not be less than 0.");
			}
			
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
			// This separates the input into an array of "words"
			// according to the specified delimiters.
			String [] words = p.split(data);
			
			// This for loop performs the substring on each word
			for (String word : words) {
				if (beginIndex >= word.length()) {
					results.append("");
				} else {
					if (endIndex > word.length()) {
						endIndex = word.length();
					}
					results.append(word.substring(beginIndex, endIndex));
					
					// This prevents adding a separator to the end of the output.
					if (data.lastIndexOf(word)!= data.length()-word.length()){
						results.append(resultDelim);
					}
				}
			}
			out.setData(results.toString());
		} else {
			out.setData(null);
		}
		
		return true;
	}

	@Mutator
	public void setBegIndex(int begIndex) {
			int old = this.begIndex;
			this.begIndex = begIndex;
			firePropertyChange("begIndex", old, begIndex);
	}

	@Accessor
	public int getBegIndex() {
		return begIndex;
	}

	@Mutator
	public void setEndIndex(int endIndex) {
			int old = this.endIndex;
			this.endIndex = endIndex;
			firePropertyChange("endIndex", old, endIndex);
	}

	@Accessor
	public int getEndIndex() {
		return endIndex;
	}

	@Mutator
	public void setRegex(boolean useRegex) {
			boolean old = this.regex;
			this.regex = useRegex;
			firePropertyChange("regex", old, regex);
	}

	@Accessor
	public boolean isRegex() {
		return regex;
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
	public void setResultDelim(String resultDelim) {
			String old = this.resultDelim;
			this.resultDelim = resultDelim;
			firePropertyChange("resultDelim", old, resultDelim);
	}

	@Accessor
	public String getResultDelim() {
		return resultDelim;
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
