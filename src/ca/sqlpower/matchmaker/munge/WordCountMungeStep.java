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

import java.math.BigDecimal;
import java.util.regex.Pattern;

import ca.sqlpower.matchmaker.MatchMakerSession;

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

	private MungeStepOutput<BigDecimal> out;
	
	/**
	 * The value of the String that will be used as the delimiter to determine
	 * what is used to divide the String into words
	 */
	public static final String DELIMITER_PARAMETER_NAME = "delimiter";
	
	/**
	 * This is the name of the parameter that decides whether this step will use
	 * regular expression to interpret the delimiter. The only values accepted by 
	 * the parameter are "true" and "false".
	 */
	public static final String USE_REGEX_PARAMETER_NAME = "useRegex";
	
	public WordCountMungeStep(MatchMakerSession session) {
		super(session);
		setName("Word Count");
		setParameter(DELIMITER_PARAMETER_NAME, " ");
		setParameter(USE_REGEX_PARAMETER_NAME, false);
		
		out = new MungeStepOutput<BigDecimal>("wordCountOutput", BigDecimal.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("string", String.class);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Word count munge step does not support addInput()");
	}
	
	@Override
	public void removeInput(int index) {
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
	
	public Boolean call() throws Exception {
		super.call();
		String delimiter = getParameter(DELIMITER_PARAMETER_NAME);
		boolean useRegex = getBooleanParameter(USE_REGEX_PARAMETER_NAME);
		
		MungeStepOutput<String> in = getInputs().get(0);
		String data = in.getData();
		
		int wordCount = 0;
		if (data != null) {
			if (!useRegex) {
				delimiter = "[" + delimiter + "]+";
			} 
			Pattern p = Pattern.compile(delimiter);
			wordCount = p.split(data).length;
		}
		out.setData(new BigDecimal(wordCount));
		return true;
	}

	public boolean canAddInput() {
		return false;
	}
}
