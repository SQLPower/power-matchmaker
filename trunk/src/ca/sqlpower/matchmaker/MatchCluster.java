/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.NonProperty;

public class MatchCluster extends AbstractMatchMakerObject {

	private static Logger logger = Logger.getLogger(MatchCluster.class);

	/**
	 * Defines an absolute ordering of the child types of this class.
	 */
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(SourceTableRecord.class, PotentialMatchRecord.class)));
	
	private final List<SourceTableRecord> sourceTableRecords = new ArrayList<SourceTableRecord>();
	
	private final List<PotentialMatchRecord> potentialMatchRecords = new ArrayList<PotentialMatchRecord>();
	
	@Constructor
	public MatchCluster() {
	}
	
	@Override
	protected void addChildImpl(SPObject child, int index) {
		if (child instanceof SourceTableRecord) {
			addSourceTableRecord((SourceTableRecord)child, index);
		} else if (child instanceof PotentialMatchRecord) {
			addPotentialMatchRecord((PotentialMatchRecord)child, index);
		}
	}

	public void addSourceTableRecord(SourceTableRecord src, int index) {
		sourceTableRecords.add(index, src);
		fireChildAdded(SourceTableRecord.class, src, index);
	}

	public void addPotentialMatchRecord(PotentialMatchRecord pmr, int index) {
		potentialMatchRecords.add(index, pmr);
		fireChildAdded(PotentialMatchRecord.class, pmr, index);
	}
	
	public void addSourceTableRecord(SourceTableRecord src) {
		if(!sourceTableRecords.contains(src)) {
			addChild(src, sourceTableRecords.size());
		}
	}
	
	/**
	 * This function is for adding children that don't fire events. It is needed when creating
	 * the cluster cache for the database reading the community edition.
	 */
	public void putSourceTableRecord(SourceTableRecord src) {
		if(!sourceTableRecords.contains(src)) {
			src.setParent(this);
			sourceTableRecords.add(src);
		}
	}
	
	/**
	 * This function is for adding children that don't fire events. It is needed when creating
	 * the cluster cache for the database reading the community edition.
	 */
	public void putPotentialMatchRecord(PotentialMatchRecord pmr) {
		PotentialMatchRecord existing = potentialMatchRecords.contains(pmr) ? 
				potentialMatchRecords.get(potentialMatchRecords.indexOf(pmr)) :
					null;
    	if (existing != null) { 
    		logger.debug("Found duplicate match of " + pmr);
    		Integer otherPriority = existing.getMungeProcess().getMatchPriority();
    		Integer pmrPriority = pmr.getMungeProcess().getMatchPriority();
			if (pmrPriority == null || otherPriority != null && otherPriority <= pmrPriority) { 
    			logger.debug("other's priority is equal or higher, so NOT replacing with pmr");
    			return;
    		} else {
    			logger.debug("pmr's priority is higher, so removing other");
    			if(isMagicEnabled())
    				potentialMatchRecords.remove(existing);
    		}
    	}
    	
    	if (potentialMatchRecords.contains(pmr)) {
            throw new IllegalStateException("Potential match is already in pool (it should not be)");
        }
		
    	pmr.setParent(this);
		potentialMatchRecords.add(pmr);
		
    	logger.debug("put " + pmr + " in Cluster " + this);
    	
    	if (pmr.getMatchStatus() == null) {
    	    pmr.setMatchStatus(MatchType.UNMATCH);
    	}
	}
	
	
	public void addPotentialMatchRecord(PotentialMatchRecord pmr) {
		PotentialMatchRecord existing = potentialMatchRecords.contains(pmr) ? 
				potentialMatchRecords.get(potentialMatchRecords.indexOf(pmr)) :
					null;
    	if (existing != null) { 
    		logger.debug("Found duplicate match of " + pmr);
    		Integer otherPriority = existing.getMungeProcess().getMatchPriority();
    		Integer pmrPriority = pmr.getMungeProcess().getMatchPriority();
			if (pmrPriority == null || otherPriority != null && otherPriority <= pmrPriority) { 
    			logger.debug("other's priority is equal or higher, so NOT replacing with pmr");
    			return;
    		} else {
    			logger.debug("pmr's priority is higher, so removing other");
    			if(isMagicEnabled())
    				removePotentialMatchRecord(existing);
    		}
    	}
    	
    	if (potentialMatchRecords.contains(pmr)) {
            throw new IllegalStateException("Potential match is already in pool (it should not be)");
        }
    	addChild(pmr, potentialMatchRecords.size());
    	if(getPool() != null) {
    		getPool().recordChangedState(pmr);          // bootstraps the "decided record" cache entry
    	}
    	
    	logger.debug("added " + pmr + " to Cluster " + this);
    	
    	if (pmr.getMatchStatus() == null) {
    	    pmr.setMatchStatus(MatchType.UNMATCH);
    	}
	}
	
	@Override
	protected boolean removeChildImpl(SPObject child) {
		if (child instanceof SourceTableRecord) {
			return removeSourceTableRecord((SourceTableRecord)child);
		} else if (child instanceof PotentialMatchRecord) {
			return removePotentialMatchRecord((PotentialMatchRecord)child);
		}
		return false;
	}

	public boolean removeSourceTableRecord(SourceTableRecord child) {
		int index = sourceTableRecords.indexOf(child);
		boolean removed = sourceTableRecords.remove(child);
		if(removed) {
			fireChildRemoved(SourceTableRecord.class, child, index);
		}
		return removed;
	}

	public boolean removePotentialMatchRecord(PotentialMatchRecord child) {
		int index = potentialMatchRecords.indexOf(child);
		boolean removed = potentialMatchRecords.remove(child);
		if(removed) {
			fireChildRemoved(PotentialMatchRecord.class, child, index);
		}
		return removed;
	}
	
	@Override
	@NonProperty
	public List<? extends SPObject> getChildren() {
		List<SPObject> children = new ArrayList<SPObject>();
		children.addAll(sourceTableRecords);
		return Collections.unmodifiableList(children);
	}

	@Override
	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}

	@Override
	public MatchMakerObject duplicate(MatchMakerObject parent) {
		return null;
	}

	@NonProperty
	public List<SourceTableRecord> getSourceTableRecords() {
		return Collections.unmodifiableList(sourceTableRecords);
	}

	@NonProperty
	public List<PotentialMatchRecord> getPotentialMatchRecords() {
		return Collections.unmodifiableList(potentialMatchRecords);
	}

	@NonProperty
	public MatchPool getPool() {
		return (MatchPool) getParent();
	}
    
    /**
     * Attempts to look up the existing SourceTableRecord instance in
     * the cache, but makes a new one and puts it in the cache if not found.
     * 
     * @param diplayValues The values used to display this record in the UI
     * @param keyValues The values for this record's unique index
     * @return The source table record that corresponds with the given key values.
     * The return value is never null.
     */
    public SourceTableRecord makeSourceTableRecord(List<Object> displayValues, List<Object> keyValues) {
        SourceTableRecord node = getSourceTableRecord(keyValues);
        if (node == null) {
            node = new SourceTableRecord(getPool().getProject(), displayValues, keyValues);
            addSourceTableRecord(node);
        } else {
        	node.setDisplayValues(displayValues);
        }
        return node;
    }

	@NonProperty
    public SourceTableRecord getSourceTableRecord(List<? extends Object> key) {
    	for(SourceTableRecord str : sourceTableRecords) {
    		if(key.equals(str.getKeyValues())) {
    			return str;
    		}
    	}
    	return null;
    }
	
	@NonProperty
	protected List<PotentialMatchRecord> getOriginalMatchEdges(SourceTableRecord src) {
		List<PotentialMatchRecord> records = new ArrayList<PotentialMatchRecord>();
		for(PotentialMatchRecord p : potentialMatchRecords) {
			if(p.getOrigLHS().equals(src) || p.getOrigRHS().equals(src)) {
				records.add(p);
			}
		}
		return records.isEmpty() ? null : records;
	}
	
	@NonProperty
	protected PotentialMatchRecord getMatchRecordByOriginalAdjacentSourceTableRecord(SourceTableRecord src1, SourceTableRecord src2) {
		for(PotentialMatchRecord p : potentialMatchRecords) {
			if((p.getOrigLHS().equals(src1) && p.getOrigRHS().equals(src2)) ||
					(p.getOrigLHS().equals(src2) && p.getOrigRHS().equals(src1))) {
				return p;
			}
		}
		return null;
	}
	
	@NonProperty
    protected PotentialMatchRecord getMatchRecordByValidatedSourceTableRecord(SourceTableRecord src1, SourceTableRecord src2) {
        for (PotentialMatchRecord p : potentialMatchRecords) {
			if((p.getMasterRecord().equals(src1) && p.getDuplicate().equals(src2)) ||
					(p.getMasterRecord().equals(src2) && p.getDuplicate().equals(src1))) {
                return p;
            }
        }
        return null;
    }
	
	public boolean doesSourceRecordExist(List<Object> keyValues) {
		SourceTableRecord src = getSourceTableRecord(keyValues);
		return src == null ? false : true;
	}

	public void reset() throws IllegalArgumentException, ObjectDependentException {
		for (Iterator<PotentialMatchRecord> it = getPotentialMatchRecords().iterator(); it.hasNext(); ) {
        	PotentialMatchRecord pmr = it.next();
			if (pmr.isSynthetic()) {
				it.remove();
				removeChild(pmr);
				continue;
			}
			if (pmr.getMatchStatus() != MatchType.UNMATCH) {
				logger.debug("Unmatching " + pmr + " for resetting the pool.");
				pmr.setMatchStatus(MatchType.UNMATCH);
			}
		}
	}
	
	@Override
	public String toString() {
		String s = "";
		for(SourceTableRecord src : sourceTableRecords) {
			s += src.getKeyValues() + " ";
		}
		return s;
	}
}
