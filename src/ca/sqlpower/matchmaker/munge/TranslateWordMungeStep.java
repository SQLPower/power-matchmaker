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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.MatchMakerEngine.EngineMode;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Mutator;


/**
 * This munge step will substitute all occurrences of a given group of strings to another for
 *  a alphabetical string input according to a mapping defined in a given MatchMakerTranslateGroup. 
 *  This step supports using regular expressions as an option for the target string.
 */
public class TranslateWordMungeStep extends AbstractMungeStep {
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));

	/**
	 * This is the translate group that holds all the target and replacement strings.
	 */
	private MatchMakerTranslateGroup translateGroup;
	
	/**
	 * The UUID of the translate group for this munge step.
	 * XXX This can now be the actual translate group. This is only a string
	 * due to the old implementation of using a string map to reference all parameters.
	 */
	private String translateGroupUuid;
	
	/**
	 * Whether to use regular expressions in this munge step.
	 */
	private boolean regex;
	
	/**
	 * Whether the effects of this munge step should be case sensitive.
	 */
	private boolean caseSensitive;
	
	
	public TranslateWordMungeStep() {
		super("Translate Words",false);
		MungeStepOutput<String> out = new MungeStepOutput<String>("translateWordOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("translateWord", String.class);
		super.addInput(desc);
		setRegex(false);
		setCaseSensitive(true);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Translate word munge step does not support addInput()");
	}
	
	@Override
	public boolean removeInput(int index) {
		throw new UnsupportedOperationException("Translate word munge step does not support removeInput()");
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
				"Translate word munge step does not accept non-String inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	/**
	 * This will throw a NullPointerException if the
	 * translate group has not been set.
	 */
	public Boolean doCall() throws Exception {

		String from;
		String to;
		boolean useRegex = isRegex();
		boolean caseSensitive = isCaseSensitive();
		MungeStepOutput<String> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		String data = in.getData();
		
		if (data != null) {
			for (MatchMakerTranslateWord translateWord : translateGroup.getChildren()) {
				from = translateWord.getFrom();
				to = translateWord.getTo();
				
				if (from != null && to != null) {
					// This block of code adds escape characters to each of
					// the regex special characters to be taken as literals
					if (!useRegex) {
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
				}
			}
		}
		
		out.setData(data);
		return true;
	}

	
	/**
	 * This munge step overrides the open() method to set its translate group from
	 * the parameter.
	 */
	@Override
	public void doOpen(EngineMode mode, Logger logger) throws Exception {
		refresh(logger);
		if (translateGroup == null) {
			throw new NullPointerException("Translate Word transformer was called " +
					"without a translate group selected. Check your Translate Word " +
					"transformer settings");
		}
	}
	
	@Override
	public void refresh(Logger logger) throws Exception {
		String uuid = getTranslateGroupUuid();
		
		translateGroup = getSession().getTranslations().getChildByUUID(uuid);
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
	public void setTranslateGroupUuid(String translateGroupUuid) {
			String old = this.translateGroupUuid;
			this.translateGroupUuid = translateGroupUuid;
			firePropertyChange("translateGroupUuid", old, translateGroupUuid);
	}

	@Accessor
	public String getTranslateGroupUuid() {
		return translateGroupUuid;
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
	
	@Override
	protected void copyPropertiesForDuplicate(MungeStep copy) {
		TranslateWordMungeStep step = (TranslateWordMungeStep) copy;
		step.setCaseSensitive(isCaseSensitive());
		step.setRegex(isRegex());
		step.setTranslateGroupUuid(getTranslateGroupUuid());
	}
}
