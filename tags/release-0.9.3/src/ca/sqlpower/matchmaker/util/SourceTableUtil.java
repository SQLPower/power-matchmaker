/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.diff.CompareSQL;
import ca.sqlpower.architect.diff.DiffChunk;
import ca.sqlpower.architect.diff.DiffType;
import ca.sqlpower.graph.DepthFirstSearch;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.munge.CleanseResultStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeProcessGraphModel;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.MungeProcessGraphModel.Edge;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.sql.SPDataSource;

/**
 * A utility class with methods used to fix a project if any changes has been
 * made to its source table.
 */
public class SourceTableUtil {

	private static final Logger logger = Logger.getLogger(SourceTableUtil.class);
	
	/**
	 * Checks the project's source table for changes and attempts to fix the
	 * project accordingly. If the source table cannot be found on the database,
	 * it just shows an error message because it's too hard to fix.
	 * 
	 * @param session
	 *            Used to find the source table on the database and display an
	 *            error message.
	 * @param project
	 *            The project to check and fix.
	 * @throws Exception
	 *             If anything went wrong in the process.
	 */
	public static void checkAndfixProject(MatchMakerSwingSession session, Project project) throws Exception {
		SQLTable oldSourceTable = project.getSourceTable();
		SQLTable newSourceTable = session.findPhysicalTableByName(
				oldSourceTable.getParentDatabase().getDataSource().getName(),
				oldSourceTable.getCatalogName(),
				oldSourceTable.getSchemaName(), oldSourceTable.getName());
		SQLIndex oldIndex = project.getSourceTableIndex();
		TableMergeRules oldSourceTmr = null;
		if (project.getType() == ProjectMode.FIND_DUPES) {
			oldSourceTmr = findSourceTableMergeRule(project);
		}
		
		// it's way too difficult to fix the project if the source table can't
		// be found on the database.
		if (newSourceTable == null) {
			JOptionPane.showMessageDialog(session.getFrame(), "The MatchMaker cannot find the project's" +
					" source table and does not know how to fix the project!", "Source Table Missing",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// use ComparSQL to find differences between the project's source table
		// and the actual physical table on the database.
		List<SQLTable> inMemory = new ArrayList<SQLTable>();
		inMemory.add(oldSourceTable);
		List<SQLTable> physical = new ArrayList<SQLTable>();
		physical.add(newSourceTable);
		CompareSQL compare = new CompareSQL(inMemory, physical);
		compare.setCompareIndices(true);
		List<DiffChunk<SQLObject>> tableDiffs = compare.generateTableDiffs();
		
		// examines through the CompareSQL diffs.
		boolean sourceTableChanged = false;
		boolean sourceTableIndexChanged = false;
		for (DiffChunk<SQLObject> diff : tableDiffs) {
			if (diff.getType() != DiffType.SAME) {
				sourceTableChanged = true;
				if (diff.getData().equals(oldIndex)) {
					sourceTableIndexChanged = true;
				}
			} 
		}
		
		// the source table has not been changed, weird....
		if (!sourceTableChanged) return;
		
		project.startCompoundEdit();
		session.setSelectNewChild(false);
		try {
			// updates the project business model to the new source table.
			project.setSourceTable(newSourceTable);
			
			fixSourceTableIndex(project, newSourceTable, oldIndex);
			
			fixInputSteps(project);
			
			if (project.getType() == ProjectMode.CLEANSE) {
				fixCleanseResultSteps(project);
			} else if (project.getType() == ProjectMode.FIND_DUPES) {
				
				fixMergeRules(project, oldSourceTmr, sourceTableChanged);
				
				if (sourceTableIndexChanged) {
					// fix the result table.
					SQLTable oldResultTable = project.getResultTable();
					SPDataSource dataSource = oldResultTable.getParentDatabase().getDataSource();
					MMSUtils.createResultTable(session.getFrame(), dataSource, project);
				}
			}
			
			session.save(project);
		} finally {
			
			project.endCompoundEdit();
			session.setSelectNewChild(true);
		}
	}

	/**
	 * Disconnects, removes and re-populates the outputs of each input munge
	 * step from the given project's munge processes.
	 * 
	 * @param project
	 *            The project to perform the fix on.
	 * @throws Exception
	 *             If a munge process has more than one input step or some
	 *             problem occurs while updating the input steps.
	 */
	private static void fixInputSteps(Project project) throws Exception {
		for (MungeProcess mp : project.getMungeProcesses()) {
			// sorts the steps so the input step is first.
			List<MungeStep> steps = new ArrayList<MungeStep>(mp.getChildren());
			MungeProcessGraphModel gm = new MungeProcessGraphModel(steps);
			DepthFirstSearch<MungeStep, Edge> dfs = new DepthFirstSearch<MungeStep, Edge>();
			dfs.performSearch(gm);
			List<MungeStep> processOrder = dfs.getFinishOrder();
			Collections.reverse(processOrder);
			
			// disconnect inputs connecting from the input step.
			List<MungeStepOutput> inputStepOuputs = null;
			MungeStep inputStep = null;
			for (MungeStep ms : processOrder) {
				if (ms.isInputStep()) {
					if (inputStep != null) throw new IllegalStateException("Found munge process with more than 1 input step!");
					// create a new list to avoid concurrent modification later.
					inputStepOuputs = new ArrayList<MungeStepOutput>(ms.getChildren());
					inputStep = ms;
				} else {
					for (MungeStepOutput mso : inputStepOuputs) {
						ms.disconnectInput(mso);
					}
				}
			}
			
			// remove the input step's outputs
			for (MungeStepOutput mso : inputStepOuputs) {
				inputStep.removeChild(mso);
			}
			
			// re-populate the input step's outputs
			inputStep.open(logger);
			inputStep.rollback();
			inputStep.close();
		}
	}

	/**
	 * Removes and re-populates the inputs of each cleanse result munge step
	 * from the given project's munge processes.
	 * 
	 * @param project
	 *            The project to perform the fix on.
	 * @throws Exception
	 *             If a munge process has more than one cleanse result step or
	 *             some problem occurs while updating the cleanse result steps.
	 */
	private static void fixCleanseResultSteps(Project project) throws Exception {
		for (MungeProcess mp : project.getMungeProcesses()) {
			MungeStep resultStep = null;
			for (MungeStep ms : mp.getChildren()) {
				if (ms instanceof CleanseResultStep) {
					if (resultStep != null) throw new IllegalStateException("Found munge process with more than 1 result step!");
					int inputsSize = ms.getMSOInputs().size();
					for (int i = 0; i < inputsSize; i++) {
						ms.removeInput(0);
					}
					
					// re-populate the inputs
					ms.open(logger);
					ms.rollback();
					ms.close();
					
					resultStep = ms;
				}
			}
		}
	}
	
	/**
	 * Finds and returns the given project's source table merge rule. Throws an
	 * IllegalStateException if none is found.
	 * 
	 * @param project
	 *            The project to perform the fix on.
	 */
	private static TableMergeRules findSourceTableMergeRule(Project project) {
		for (TableMergeRules tmr : project.getTableMergeRules()) {
			if (tmr.isSourceMergeRule()) return tmr;
		}
		throw new IllegalStateException("Found project without a source table merge rule!");
	}

	/**
	 * Attempts to fix the table merge rules by updating the source table merge
	 * rule and deleting the other merge rules if the source table's index has
	 * been changed.
	 * 
	 * @param project
	 *            The project to perform the fix on.
	 * @param sourceTmr
	 *            The old source table merge rule before the fix began.
	 * @param sourceTableIndexChanged
	 *            Whether the source table index has been changed. If true, all
	 *            merge rules except the source merge rule will be removed.
	 * @throws ArchitectException
	 *             If getting the project's source table index causes problems.
	 */
	private static void fixMergeRules(Project project, TableMergeRules sourceTmr, boolean sourceTableIndexChanged) throws ArchitectException {
		// removed all the old column merge rules.
		List<ColumnMergeRules> sourceCmrs = new ArrayList<ColumnMergeRules>(sourceTmr.getChildren());
		for (ColumnMergeRules cmr : sourceCmrs) {
			sourceTmr.removeChild(cmr);
		}

		// update to the new source table and source table index
		sourceTmr.setTable(project.getSourceTable());
		sourceTmr.setTableIndex(project.getSourceTableIndex());
		
		// re-populate the column merge rules.
		sourceTmr.deriveColumnMergeRules();
		for (ColumnMergeRules cmr : sourceTmr.getChildren()) {
			if (sourceTmr.getPrimaryKeyFromIndex().contains(cmr.getColumn())) {
				cmr.setActionType(MergeActionType.NA);
			}
		}

		if (sourceTableIndexChanged) {
			// a source table index change would probably warrant re-making all the merge rules...
			List<TableMergeRules> removeTmrs = new ArrayList<TableMergeRules>(project.getTableMergeRules());
			removeTmrs.remove(sourceTmr);
			for (TableMergeRules tmr : removeTmrs) {
				project.removeTableMergeRule(tmr);
			}
		}
	}

	/**
	 * Attempts to fix the source table index. First tries to find an index by
	 * the same name, then if none exists, it finds the first unique index
	 * available.
	 * 
	 * @param project
	 *            The project to fix.
	 * @param newSourceTable
	 *            The brand new source table.
	 * @param oldIndex
	 *            The previous source table index.
	 * @throws ArchitectException
	 */
	private static void fixSourceTableIndex(Project project, SQLTable newSourceTable,
			SQLIndex oldIndex) throws ArchitectException {
		SQLIndex newIndex = null;
		
		// no unique index in the new source table ... not good!
		if (newSourceTable.getUniqueIndices().size() == 0) {
			throw new IllegalArgumentException("The new source table does not have any unique index.");
		}
		
		// try to find an index with the same name as the old index.
		for (SQLIndex ind : newSourceTable.getUniqueIndices()) {
			if (ind.getName().equals(oldIndex.getName())) {
				newIndex = ind;
				break;
			}
		}
	
		if (newIndex != null) {
			// assume it's safe to keep using the "old index".
			project.setSourceTableIndex(newIndex);
			
		} else {
			// no index with same name, just use the first unique index.
			// if the user doesn't like that, it could always be changed later.
			project.setSourceTableIndex(newSourceTable.getUniqueIndices().get(0));
		}
	}
}
