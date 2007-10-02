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
 * This munge step will return a output string containing the substrings 
 * of each of the individual words in the input. The words are defined by
 * given delimiter strings. Optionally, the step can also interpret the delimiter
 * input as a regular expression, as long as the {@link #USE_REGEX_PARAMETER_NAME}
 * parameter is set to "true". Each substring begins at the specified beginIndex
 * and extends to the character at index endIndex - 1 of the input words.
 * The substrings in the output are separated by {@link #RESULT_DELIM_PARAMETER_NAME}.
 */
public class SubstringByWordMungeStep extends AbstractMungeStep {

	private MungeStepOutput<String> out;
	
	/**
	 * This is the name of the parameter with the value of the beginIndex.
	 */
	public static final String BEGIN_PARAMETER_NAME = "beginIndex";

	/**
	 * This is the name of the parameter with the value of the endIndex.
	 */
	public static final String END_PARAMETER_NAME = "endIndex";
	
	/**
	 * This is the name of the parameter that decides whether this step will use
	 * regular expression to interpret the delimiter. The only values accepted by 
	 * the parameter are "true" and "false".
	 */
	public static final String USE_REGEX_PARAMETER_NAME = "useRegex";
	
	/**
	 * The value of the String that will be used as the delimiter to determine
	 * what is used to divide the String into words
	 */
	public static final String DELIMITER_PARAMETER_NAME = "delimiter";
	
	/**
	 * This is the name of the parameter with the value of the delimiter to use
	 * to separate words in the output.
	 */
	public static final String RESULT_DELIM_PARAMETER_NAME = "resultDelim";
	
	public SubstringByWordMungeStep() {
		setName("Substring by Word");
		setParameter(DELIMITER_PARAMETER_NAME, " ");
		setParameter(RESULT_DELIM_PARAMETER_NAME, " ");
		setParameter(USE_REGEX_PARAMETER_NAME, false);
		setParameter(BEGIN_PARAMETER_NAME, 0);
		setParameter(END_PARAMETER_NAME, 0);
		out = new MungeStepOutput<String>("substringOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("substring", String.class);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Substring munge step does not support addInput()");
	}
	
	@Override
	public void removeInput(int index) {
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
	public Boolean call() throws Exception {
		super.call();

		int beginIndex = getIntegerParameter(BEGIN_PARAMETER_NAME);
		int endIndex = getIntegerParameter(END_PARAMETER_NAME);
		
		String delimiter = getParameter(DELIMITER_PARAMETER_NAME);
		boolean useRegex = getBooleanParameter(USE_REGEX_PARAMETER_NAME);
		String resultDelim = getParameter(RESULT_DELIM_PARAMETER_NAME);
				
		MungeStepOutput<String> in = getInputs().get(0);
		String data = in.getData();
		StringBuilder results = new StringBuilder("");
		if (data != null) {
			if (beginIndex < 0) {
				throw new IndexOutOfBoundsException(
					"The begin index can not be less than 0.");
			}
			
			// This block separates the input into an array of words.
			if (!useRegex) {
				delimiter = "[" + delimiter + "]+";
			} 
			Pattern p = Pattern.compile(delimiter);
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

	public boolean canAddInput() {
		return false;
	}
}
