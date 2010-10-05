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

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.MatchMakerEngine.EngineMode;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Mutator;


/**
 * This munge step will substitute all occurrences of a given group of strings to another for
 *  a alphabetical string input according to a mapping defined in a given MatchMakerTranslateGroup. 
 *  This step supports using regular expressions as an option for the target string.
 */
public class TranslateWordMungeStep extends AbstractMungeStep {

	/**
	 * This is the translate group that holds all the target and replacement strings.
	 */
	private MatchMakerTranslateGroup translateGroup;
	
    /**
     * The name of the parameter that specifies the OID of the translate group used
     * by this step.
     */
	public static final String TRANSLATE_GROUP_PARAMETER_NAME = "translateGroupOid";
	/**
	 * The UUID of the translate group for this munge step.
	 */
	private String translateGroupUuid;
	
	/**
	 * This is the name of the parameter that decides whether this step will use
	 * regular expression to replace words. The only values accepted by the parameter
	 * are "true" and "false".
	 */
	public static final String USE_REGEX_PARAMETER_NAME = "useRegex";
	/**
	 * Whether to use regular expressions in this munge step.
	 */
	private boolean regex;
	
	/**
	 * This is the name of the parameter that decides whether this step will be
	 * case sensitive. The only values accepted by the parameter are "true" and
	 *  "false".
	 */
	public static final String CASE_SENSITIVE_PARAMETER_NAME = "caseSensitive";
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
		setParameter(USE_REGEX_PARAMETER_NAME, false);
		setParameter(CASE_SENSITIVE_PARAMETER_NAME, true);
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
		boolean useRegex = getBooleanParameter(USE_REGEX_PARAMETER_NAME);
		boolean caseSensitive = getBooleanParameter(CASE_SENSITIVE_PARAMETER_NAME);
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
		String uuid = getParameter(TRANSLATE_GROUP_PARAMETER_NAME);
		
		translateGroup = getSession().getTranslations().getChildByUUID(uuid);
	}

	@Mutator
	public void setRegex(boolean useRegex) {
			this.regex = useRegex;
	}

	@Accessor
	public boolean isRegex() {
		return regex;
	}

	@Mutator
	public void setTranslateGroupUuid(String translateGroupUuid) {
			this.translateGroupUuid = translateGroupUuid;
	}

	@Accessor
	public String getTranslateGroupUuid() {
		return translateGroupUuid;
	}

	@Mutator
	public void setCaseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
	}
	
	@Accessor
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
}
