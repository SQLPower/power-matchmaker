package ca.sqlpower.matchmaker;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;

public class ColumnMergeRules extends AbstractMatchMakerObject<ColumnMergeRules,MatchMakerObject> {

	public enum MergeActionType {
		UNKNOWN, IGNORE, AUGMENT, CONCAT, MIN, MAX, SUM, AVG;
		
		public static MergeActionType getActionTypeFromString(String type) {
			if ("IGNORE".equals(type)){
				return IGNORE;
			} else if ("AUGMENT".equals(type)){
				return AUGMENT;
			} else if ("CONCAT".equals(type)){
				return CONCAT;
			} else if ("MIN".equals(type)){
				return MIN;
			} else if ("MAX".equals(type)){
				return MAX;
			} else if ("SUM".equals(type)){
				return SUM;
			} else if ("AVG".equals(type)){
				return AVG;
			} else {
				return UNKNOWN;
			} 
		}
		
		@Override
		public String toString() {
			switch (this) {
			case IGNORE:
				return "Ignore";
			case AUGMENT:
				return "AUGMENT";
			case CONCAT:
				return "CONCAT";
			case MIN:
				return "MIN";
			case MAX:
				return "MAX";
			case SUM:
				return "SUM";
			case AVG:
				return "AVG";
			case UNKNOWN:
				return "UNKNOWN";
			default:
				throw new IllegalStateException("Invalid enumeration");
			}
		}
	}

	private static final Logger logger = Logger.getLogger(ColumnMergeRules.class);
	
	private Long oid;
	
	private SQLColumn column;
	
	private boolean updateAction;
	
	private MergeActionType actionType;
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((column == null) ? 0 : column.hashCode());
		result = PRIME * result + ((getParent() == null) ? 0 : getParent().hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ColumnMergeRules))
			return false;
		final ColumnMergeRules other = (ColumnMergeRules) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (getParent() == null) {
			if (other.getParent() != null)
				return false;
		} else if (!getParent().equals(other.getParent()))
			return false;
		return true;
	}

	
	public ColumnMergeRules duplicate(MatchMakerObject parent,
			MatchMakerSession session) {
		ColumnMergeRules columnRule = new ColumnMergeRules();
		columnRule.setParent(parent);
		columnRule.setSession(session);
		columnRule.setName(getName());
		columnRule.setActionType(actionType);
		columnRule.setUpdateAction(updateAction);
		columnRule.setColumn(column);
		return columnRule;
	}


	public MergeActionType getActionType() {
		return actionType;
	}


	public void setActionType(MergeActionType actionType) {
		MergeActionType oldValue = this.actionType;
		this.actionType = actionType;
		getEventSupport().firePropertyChange("actionType", oldValue, this.actionType);
	}

	@Override
	public String getName() {
		if (this.getColumn() == null) {
			return null;
		} else {
			return this.getColumn().getName();
		}
		
	}

	public SQLColumn getColumn() {
		return column;
	}


	public void setColumn(SQLColumn column) {
		SQLColumn oldValue = this.getColumn();
		this.column = column;
		getEventSupport().firePropertyChange("column", oldValue, this.column);
	}


	public boolean isUpdateAction() {
		return updateAction;
	}


	public void setUpdateAction(boolean updateAction) {
		boolean oldValue = this.updateAction;
		this.updateAction = updateAction;
		getEventSupport().firePropertyChange("updateAction", oldValue, this.updateAction);
	}

}
