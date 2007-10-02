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
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.dao.MatchMakerTranslateGroupDAO;


/**
 * This munge step will substitute all occurences of a given group of strings to another for
 *  a alphabetical string input according to a mapping defined in a given MatchMakerTranslateGroup. 
 *  This step supports using regular expressions as an option for the target string.
 */
public class TranslateWordMungeStep extends AbstractMungeStep {

	private MungeStepOutput<String> out;
	
	/**
	 * This is the translate group that holds all the target and replacement strings.
	 */
	private MatchMakerTranslateGroup translateGroup;
	
	
	/**
	 * The session is used in this munge step to get the DAO that would allow us to
	 * find the translate group by its oid.
	 */
	private MatchMakerSession session;
	
    /**
     * The name of the parameter that specifies the OID of the translate group used
     * by this step.
     */
	public static final String TRANSLATE_GROUP_PARAMETER_NAME = "translateGroupOid";
	
	/**
	 * This is the name of the parameter that decides whether this step will use
	 * regular expression to replace words. The only values accepted by the parameter
	 * are "true" and "false".
	 */
	public static final String USE_REGEX_PARAMETER_NAME = "useRegex";
	
	public TranslateWordMungeStep(MatchMakerSession session) {
		setName("Translate Words");
		this.session = session;
		out = new MungeStepOutput<String>("translateWordOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("translateWord", String.class);
		super.addInput(desc);
		setParameter(USE_REGEX_PARAMETER_NAME, false);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Translate word munge step does not support addInput()");
	}
	
	@Override
	public void removeInput(int index) {
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
	public Boolean call() throws Exception {
		super.call();

		String from;
		String to;
		boolean useRegex = getBooleanParameter(USE_REGEX_PARAMETER_NAME);
		
		MungeStepOutput<String> in = getInputs().get(0);
		String data = in.getData();
		
		if (data != null) {
			for (MatchMakerTranslateWord translateWord : translateGroup.getChildren()) {
				from = translateWord.getFrom();
				to = translateWord.getTo();
				
				if (from != null && to != null) {
					if (useRegex) {
						Pattern p = Pattern.compile(from);
						Matcher m = p.matcher(data);
						data = m.replaceAll(to);
					} else {
						data = data.replace(from, to);
					}
				}
			}
		}
		out.setData(data);
		return true;
	}

	public boolean canAddInput() {
		return false;
	}
	
	/**
	 * This munge step overrides the open() method to set its translate group from
	 * the parameter.
	 */
	@Override
	public void open() throws Exception {
		String oid = getParameter(TRANSLATE_GROUP_PARAMETER_NAME);
		MatchMakerTranslateGroupDAO groupDAO = (MatchMakerTranslateGroupDAO) (session.getDAO(MatchMakerTranslateGroup.class));
		translateGroup = groupDAO.findByOID(Long.valueOf(oid));
		if(translateGroup == null) {
			throw new NullPointerException("Translate group with " + oid + " not found");
		}
		super.open();
	}
}
