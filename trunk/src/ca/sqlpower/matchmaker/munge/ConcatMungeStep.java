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


/**
 * A munge step that concatenates all string input values into a single output value.
 * There is an optional delimiter that can be inserted between each concatenated
 * value.  The delimiter is not inserted before the first value or after the last one.
 * <p>
 * If there is no delimiter specified and all input values are null, the output will
 * also be null.  However, if at least one input is not null, the output will not be
 * null.  Null input values are treated as the empty string for purposes of concatenation.
 * If you want a special value to stand for null in your concatenated string, pass the
 * inputs through NVL steps.
 */
public class ConcatMungeStep extends AbstractMungeStep {

    /**
     * The value of this parameter will be placed between each concatenated value.
     * When the delimiter is set to null, this step behaves as if the delimiter was
     * set to the empty string.
     */
    public static final String DELIMITER_PARAMETER_NAME = "delimiter";
    
	public ConcatMungeStep() {
		setName("Concat");
		//This might be overriden by hibernate when loading from database.
		MungeStepOutput<String> out = new MungeStepOutput<String>("concatOutput", String.class);
		addChild(out);
		InputDescriptor desc1 = new InputDescriptor("concat1", String.class);
		InputDescriptor desc2 = new InputDescriptor("concat2", String.class);
		super.addInput(desc1);
		super.addInput(desc2);
	}
	
	public int addInput(InputDescriptor desc) {
		return super.addInput(desc);
	}
	
	public void connectInput(int index, MungeStepOutput o) {
		if (o != null && o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
				"Concatenate munge step does not accept non-String inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	public Boolean call() throws Exception {
		super.call();
		MungeStepOutput<String> out = getOut();
        
		String delimiter = getDelimiter();
        if (delimiter == null) {
            delimiter = "";
        }
        
		boolean allNulls = true;
        boolean first = true;
		StringBuilder data = new StringBuilder();
		for (MungeStepOutput<String> in: getMSOInputs()) {
		    if (!first) {
                data.append(delimiter);
            }
			if (in != null && in.getData() != null) {
				data.append(in.getData());
				allNulls = false;
			}
            first = false;
		}
		out.setData(data.toString());
		if (allNulls && delimiter.length() == 0) {
			out.setData(null);
		}
		printOutputs();
		return true;
	}

	public boolean canAddInput() {
		return true;
	}
    
    public void setDelimiter(String delimiter) {
        setParameter(DELIMITER_PARAMETER_NAME, delimiter);
    }

    public String getDelimiter() {
        return getParameter(DELIMITER_PARAMETER_NAME);
    }
}
