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

import ca.sqlpower.matchmaker.MatchMakerSession;


/**
 * This munge step will substitute all occurences of a given string to another for
 *  a alphabetical string inputs. This step supports using regular expressions as
 *  an option for the target string.
 */
public class StringSubstitutionMungeStep extends AbstractMungeStep {

	private MungeStepOutput<String> out;
	
	/**
	 * This is the name of the parameter containing the string to replace. The 
	 * parameter would be interpreted as a regular expression if the option is 
	 * set to true.
	 */
	public static final String FROM_PARAMETER_NAME = "from";
	
	public static final String TO_PARAMETER_NAME = "to";
	
	/**
	 * This is the name of the parameter that decides whether this step will use
	 * regular expression to replace words. The only values accepted by the parameter
	 * are "true" and "false".
	 */
	public static final String USE_REGEX_PARAMETER_NAME = "useRegex";
	
	public StringSubstitutionMungeStep(MatchMakerSession session) {
		super(session);
		setName("String Substitution");
		out = new MungeStepOutput<String>("stringSubstitutionOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("stringSubstitution", String.class);
		setParameter(USE_REGEX_PARAMETER_NAME, false);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("String substitution munge step does not support addInput()");
	}
	
	@Override
	public void removeInput(int index) {
		throw new UnsupportedOperationException("String substitution munge step does not support removeInput()");
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
				"String substitution munge step does not accept non-String inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	public Boolean call() throws Exception {
		super.call();

		String from = getParameter(FROM_PARAMETER_NAME);
		String to = getParameter(TO_PARAMETER_NAME);
		boolean useRegex = getBooleanParameter(USE_REGEX_PARAMETER_NAME);
		
		MungeStepOutput<String> in = getInputs().get(0);
		String data = in.getData();
		if (in.getData() == null) {
			out.setData(null);
		} else if (from != null && to != null) {
			if (useRegex) {
				Pattern p = Pattern.compile(from);
				Matcher m = p.matcher(data);
				data = m.replaceAll(to);
			} else {
				data = data.replace(from, to);
			}
			out.setData(data);
		}

		printOutputs();
		return true;
	}

	public boolean canAddInput() {
		return false;
	}
}
