/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

public class SortWordsMungeStep extends AbstractMungeStep {
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));

    /**
     * The delimiters for this step.
     */
    private String delimiter;

    /**
     * Whether the step will use regular expressions.
     */
    private boolean regex;

    /**
     * The parameter that specifies whether the delimiter should be case
     * sensitive. The only values accepted by the parameter are "true" and
     * "false".
     */
    private boolean caseSensitive;
    
    /**
     * The parameter that holds the delimiter that separates words in the
     * output.
     */
    private String resultDelim;

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

    @Accessor
	public String getResultDelim() {
		return resultDelim;
	}

    @Mutator
	public void setResultDelim(String resultDelim) {
    	String old = this.resultDelim;
		this.resultDelim = resultDelim;
		firePropertyChange("resultDelim", old, resultDelim);
	}

    @Accessor
	public String getDelimiter() {
		return delimiter;
	}
	
    @Mutator
	public void setDelimiter(String delimiter) {
    	String old = this.delimiter;
		this.delimiter = delimiter;
		firePropertyChange("delimiter", old, delimiter);
	}

	@Constructor
    public SortWordsMungeStep() {
        super("Sort Words", false);
        setDelimiter("\\p{Space}+");
        setResultDelim(" ");
        setRegex(true);
        setCaseSensitive(false);
        MungeStepOutput<String> out = new MungeStepOutput<String>("sortedWordsOutput", String.class);
        addChild(out);
        InputDescriptor desc = new InputDescriptor("words", String.class);
        super.addInput(desc);
    }
    
    @Override
    public int addInput(InputDescriptor desc) {
        throw new UnsupportedOperationException("Sort words munge step does not support addInput()");
    }
    
    @Override
    public boolean removeInput(int index) {
        throw new UnsupportedOperationException("Sort words munge step does not support removeInput()");
    }
    
    public Boolean doCall() throws Exception {
        String delimiter = getDelimiter();
        boolean delimIsRegex = isRegex();
        boolean delimCaseSensitive = isCaseSensitive();
        String resultDelim = getResultDelim();
        
        MungeStepOutput<String> out = getOut();
        MungeStepOutput<String> in = getMSOInputs().get(0);
        String data = in.getData();
        if (data != null) {
            int regexPatternFlags = 0;
            if (!delimIsRegex) {
                regexPatternFlags |= Pattern.LITERAL;
            }
            if (!delimCaseSensitive) {
                regexPatternFlags |= Pattern.CASE_INSENSITIVE;
            }
            Pattern p = Pattern.compile(delimiter, regexPatternFlags);
            String[] words = p.split(data);
            Arrays.sort(words);

            StringBuilder results = new StringBuilder();
            boolean firstWord = true;
            for (String word : words) {
                if (word.equals("")) {
                    continue;
                }
                if (!firstWord) {
                    results.append(resultDelim);
                }
                results.append(word);
                firstWord = false;
            }
            out.setData(results.toString());
        } else {
            out.setData(null);
        }
        
        return true;
    }

}
