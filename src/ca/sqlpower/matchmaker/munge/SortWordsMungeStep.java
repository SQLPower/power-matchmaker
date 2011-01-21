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

import java.util.Arrays;
import java.util.regex.Pattern;

public class SortWordsMungeStep extends AbstractMungeStep {

    /**
     * The parameter that specifies the delimiter for dividing the input String
     * into words. It will be interpreted either as a literal string or a
     * regular expression, depending on the USE_REGEX_PARAMETER_NAME parameter.
     */
    public static final String DELIMITER_PARAMETER_NAME = "delimiter";

    /**
     * The parameter that decides whether this step will use regular expression
     * to interpret the delimiter. The only values accepted by the parameter are
     * "true" and "false".
     */
    public static final String USE_REGEX_PARAMETER_NAME = "useRegex";

    /**
     * The parameter that specifies whether the delimiter should be case
     * sensitive. The only values accepted by the parameter are "true" and
     * "false".
     */
    public static final String CASE_SENSITIVE_PARAMETER_NAME = "caseSensitive";
    
    /**
     * The parameter that holds the delimiter that separates words in the
     * output.
     */
    public static final String RESULT_DELIM_PARAMETER_NAME = "resultDelim";

    public SortWordsMungeStep() {
        super("Sort Words", false);
        setParameter(DELIMITER_PARAMETER_NAME, "\\p{Space}+");
        setParameter(RESULT_DELIM_PARAMETER_NAME, " ");
        setParameter(USE_REGEX_PARAMETER_NAME, true);
        setParameter(CASE_SENSITIVE_PARAMETER_NAME, false);
        MungeStepOutput<String> out = new MungeStepOutput<String>("sortedWordsOutput", String.class);
        addChild(out);
        InputDescriptor desc = new InputDescriptor("words", String.class);
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
    
    public Boolean doCall() throws Exception {
        String delimiter = getParameter(DELIMITER_PARAMETER_NAME);
        boolean delimIsRegex = getBooleanParameter(USE_REGEX_PARAMETER_NAME);
        boolean delimCaseSensitive = getBooleanParameter(CASE_SENSITIVE_PARAMETER_NAME);
        String resultDelim = getParameter(RESULT_DELIM_PARAMETER_NAME);
        
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

    public void setDelimiter(String delimiter) {
        setParameter(DELIMITER_PARAMETER_NAME, delimiter);
    }
    
    public void setDelimiterCaseSensitive(boolean v) {
        setParameter(CASE_SENSITIVE_PARAMETER_NAME, v);
    }
    
    public void setDelimiterRegex(boolean v) {
        setParameter(USE_REGEX_PARAMETER_NAME, v);
    }
    
    public void setResultDelimiter(String resultDelimiter) {
        setParameter(RESULT_DELIM_PARAMETER_NAME, resultDelimiter);
    }
}
