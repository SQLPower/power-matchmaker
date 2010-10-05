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

import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;


/**
 * This munge step will substitute all occurrences of a given string to another for
 *  a alphabetical string inputs. This step supports using regular expressions as
 *  an option for the target string.
 */
public class StringSubstitutionMungeStep extends AbstractMungeStep {

	/**
	 * The string that will be replaced during this step.
	 */
	private String from;
	
	/**
	 * The string that will be the replacement during this step.
	 */
	private String to;
	
	/**
	 * Whether this step will use regular expressions.
	 */
	private boolean regex;
	
	/**
	 * This is the name of the parameter that decides whether this step will be
	 * case sensitive. The only values accepted by the parameter are "true" and
	 *  "false".
	 */
	private boolean caseSensitive;
	
	@Accessor
	public String getFrom() {
		return from;
	}

	@Mutator
	public void setFrom(String from) {
		String old = this.from;
		this.from = from;
		firePropertyChange("from", old, from);
	}

	@Accessor
	public String getTo() {
		return to;
	}

	@Mutator
	public void setTo(String to) {
		String old = this.to;
		this.to = to;
		firePropertyChange("to", old, to);
	}

	@Accessor
	public boolean isRegex() {
		return regex;
	}

	@Mutator
	public void setRegex(boolean regex) {
		boolean old = this.regex;
		this.regex = regex;
		firePropertyChange("regex", old, regex);
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

	@Constructor
	public StringSubstitutionMungeStep() {
		super("String Substitution", false);
		MungeStepOutput<String> out = new MungeStepOutput<String>("stringSubstitutionOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("stringSubstitution", String.class);
		setRegex(false);
		setCaseSensitive(true);
		super.addInput(desc);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("String substitution munge step does not support addInput()");
	}
	
	@Override
	public boolean removeInput(int index) {
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
	
	public Boolean doCall() throws Exception {

		String from = getFrom();
		String to = getTo();
		boolean useRegex = isRegex();
		boolean caseSensitive = isCaseSensitive();
		MungeStepOutput<String> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		String data = in.getData();
		if (in.getData() == null) {
			out.setData(null);
		} else if (from != null && to != null) {
			if (!useRegex) {
				// This block of code adds escape characters to each of
				// the regex special characters to be taken as literals
				String specialChars = "-+*?()[]{}|^<=";
				from = from.replaceAll("\\\\", "\\\\\\\\");
				from = from.replaceAll("\\$", "\\\\\\$");
				for (char letter : specialChars.toCharArray()) {
					from = from.replaceAll("\\" + letter, "\\\\" + letter); 
				}
				from = "(" + from + "){1}";
			}
			Pattern p;
			if (!caseSensitive) {
				p = Pattern.compile(from, Pattern.CASE_INSENSITIVE);
			} else {
				p = Pattern.compile(from);
			}
			Matcher m = p.matcher(data);
			data = m.replaceAll(to);
			out.setData(data);
		}

		return true;
	}
}
