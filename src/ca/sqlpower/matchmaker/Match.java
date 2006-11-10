package ca.sqlpower.matchmaker;

import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.util.SourceTable;
import ca.sqlpower.matchmaker.util.ViewSpec;

public class Match extends AbstractMatchMakerObject<MatchMakerFolder> {

	public enum MatchType {
		FIND_DUPES("Find Duplicates"), BUILD_XREF("Build Cross-Reference");

		String displayName;

		private MatchType(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	/** the oid for the match */
	Long oid;
	
	/** The name of this object. It must be unique across all match objects */
	String name;

	/** The type of match */
	MatchType type;

	/** A little note on this match object */
	String description;

	/**
	 * The table where we get the match data.
	 */
	SourceTable sourceTable;

	/**
	 * The table where the engine stores the results of a match
	 */
	SQLTable resultTable;

	/** The settings for the match engine */
	MatchSettings matchSettings;

	/** the settings for the merge engine */
	MergeSettings mergeSettings;

	/** a filter for the tables that are matched */
	String filter;

	/** FIXME can't remember what the view does */
	ViewSpec view;

	/** the folder containing this match.  
	 * Null if no folder containing this match
	 */
	PlFolder folder;
	
	/** The point above which matches are done automatically */
	int autoMatchThreshold;

	public Match( ) {
	}
	/**
	 * FIXME Implement me
	 *
	 */
	public void execute() {
		throw new NotImplementedException();
	}

	/**
	 * FIXME Implement me
	 *
	 */
	public boolean checkValid() {
		throw new NotImplementedException();
	}
	
	/**
	 * FIXME Implement me
	 *
	 */
	public void createResultTable() {
		throw new NotImplementedException();
	}
	
	/**
	 * FIXME Implement me
	 *
	 */
	public void createViewTable() {
		throw new NotImplementedException();
	}
	
	public int getAutoMatchThreshold() {
		return autoMatchThreshold;
	}

	public void setAutoMatchThreshold(int autoMatchThreshold) {
		int oldValue = this.autoMatchThreshold;
		this.autoMatchThreshold = autoMatchThreshold;
		getEventSupport().firePropertyChange("autoMatchThreshold", oldValue,
				this.autoMatchThreshold);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		String oldValue = this.description;
		this.description = description;
		getEventSupport().firePropertyChange("description", oldValue,
				this.description);
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		String oldValue = this.filter;
		this.filter = filter;
		getEventSupport().firePropertyChange("filter", oldValue, this.filter);
	}

	public MatchSettings getMatchSettings() {
		return matchSettings;
	}

	public void setMatchSettings(MatchSettings matchSettings) {
		MatchSettings oldValue = this.matchSettings;
		this.matchSettings = matchSettings;
		getEventSupport().firePropertyChange("matchSettings", oldValue,
				this.matchSettings);
	}

	public MergeSettings getMergeSettings() {
		return mergeSettings;
	}

	public void setMergeSettings(MergeSettings mergeSettings) {
		MergeSettings oldValue = this.mergeSettings;
		this.mergeSettings = mergeSettings;
		getEventSupport().firePropertyChange("mergeSettings", oldValue,
				this.mergeSettings);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		getEventSupport().firePropertyChange("name", oldValue, this.name);
	}

	public SQLTable getResultTable() {
		return resultTable;
	}

	public void setResultTable(SQLTable resultTable) {
		SQLTable oldValue = this.resultTable;
		this.resultTable = resultTable;
		getEventSupport().firePropertyChange("resultTable", oldValue,
				this.resultTable);
	}

	public SourceTable getSourceTable() {
		return sourceTable;
	}

	public void setSourceTable(SourceTable sourceTable) {
		SourceTable oldValue = this.sourceTable;
		this.sourceTable = sourceTable;
		getEventSupport().firePropertyChange("sourceTable", oldValue,
				this.sourceTable);
	}

	public MatchType getType() {
		return type;
	}

	public void setType(MatchType type) {
		MatchType oldValue = this.type;
		this.type = type;
		getEventSupport().firePropertyChange("type", oldValue, this.type);
	}

	public ViewSpec getView() {
		return view;
	}

	public void setView(ViewSpec view) {
		ViewSpec oldValue = this.view;
		this.view = view;
		getEventSupport().firePropertyChange("view", oldValue, this.view);
	}
	public PlFolder getFolder() {
		return folder;
	}
	public void setFolder(PlFolder folder) {
		PlFolder oldValue = this.folder;
		this.folder = folder;
		getEventSupport().firePropertyChange("folder", oldValue, this.folder);
	}
	
	/**
	 * Preconditions 
	 * 
	 * FIXME write this method
	 * @return
	 */
	public List<PlMatchGroup> getMatchGroups() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((oid == null) ? 0 : oid.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Match other = (Match) obj;
		if (oid == null) {
			if (other.oid != null)
				return false;
		} else if (!oid.equals(other.oid))
			return false;
		return true;
	}

}
