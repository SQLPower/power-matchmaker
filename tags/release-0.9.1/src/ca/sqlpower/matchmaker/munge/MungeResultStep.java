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

import java.util.List;

/**
 * This MungeStep is used to store the final results of a MungeProcess.
 * As such, it contains no outputs, and only inputs. For each row
 * that gets processed in a MungeProcess, this step will store the
 * result. 
 * <p>
 * This step makes an important assumption that the input step that it takes in
 * contains MungeStepOutputs corresponding to the source table's unique key, and
 * that each MungeStepOutput's name is the same as the corresponding column's name.
 * If the MungeStepOutputs have different names, then this step will not be able
 * to find them, as it uses the source table's index key to find them.
 */
public interface MungeResultStep extends MungeStep {
	
	/**
	 * Adds or sets an input step to the result step. 
	 */
	public void addInputStep(SQLInputStep inputStep);
	
	/**
	 * Returns the munged results of this step.
	 */
	public List<MungeResult> getResults();
}
