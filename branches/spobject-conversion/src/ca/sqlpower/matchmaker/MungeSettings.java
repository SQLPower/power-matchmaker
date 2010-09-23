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

package ca.sqlpower.matchmaker;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.sqlpower.matchmaker.address.AddressValidator;
import ca.sqlpower.object.SPObject;

/**
 * Settings that are specific to the Match engine
 */
public class MungeSettings extends MatchMakerSettings {

	public static final List<Class<? extends SPObject>> allowedChildTypes =
        Collections.emptyList();
	/**
	 * An enumeration of settings for which records to store in a result table.
	 */
	public enum PoolFilterSetting {
		/**
		 * Include all records into the address result table
		 */
		EVERYTHING("Include all records"),
		/**
		 * Include all invalid or valid records. Currently, this would have the
		 * same behaviour as as 'EVERYTHING'.
		 */
		VALID_OR_INVALID("Include all records"),
		/**
		 * Currently, this has the same behaviour as 'VALID_ONLY'
		 */
		VALID_OR_DIFFERENT_FORMAT("Include all valid records"),
		/**
		 * Include any records that are either invalid, or have
		 * suggestions with a different format from the original source addresses
		 */
		INVALID_OR_DIFFERENT_FORMAT("Include invalid or valid but different format"),
		/**
		 * Include only records that are valid
		 */
		VALID_ONLY("Include only valid records"),
		/**
		 * Include only records that are valid but have suggestions printed in a 
		 * different format
		 */
		DIFFERENT_FORMAT_ONLY("Include only valid records with different formats"),
		/**
		 * Include only records that are invalid 
		 */
		INVALID_ONLY("Include only invalid records"),
		NOTHING("Include nothing");
		
		/**
		 * A longer description of this setting
		 */
		private String longDescription;
		
		private PoolFilterSetting(String longDescription) {
			this.longDescription = longDescription;
		}
		
		public String getLongDescription() {
			return longDescription;
		}
	}
	
	/**
	 * An enumeration of settings which addresses to
	 * automatically validate
	 */
	public enum AutoValidateSetting {
		/**
		 * Do no auto validate at all
		 */
		NOTHING("No auto-correction"),
		/**
		 * Only validate addresses that are correctable according to
		 * the applicable postal authority (currently only SERP)
		 * (that is, the {@link AddressValidator} provides a
		 * valid suggestion)
		 */
		SERP_CORRECTABLE("Auto-correct for postal authority certification"),
		/**
		 * Validate any address with only one suggestion 
		 */
		EVERYTHING_WITH_ONE_SUGGESTION("Auto-correct any with a single suggestion"),
		/**
		 * Validate any address with at least one suggestion
		 */
		EVERYTHING_WITH_SUGGESTION("Auto-correct any with at least one suggestion");
		
		/**
		 * A longer description of particular setting
		 */
		private String longDescription;
		
		private AutoValidateSetting(String longDescription) {
			this.longDescription = longDescription;
		}
		
		public String getLongDescription() {
			return longDescription;
		}
	}
	
	@Override
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((autoMatchThreshold == null) ? 0 : autoMatchThreshold.hashCode());
        result = PRIME * result + ((lastBackupNo == null) ? 0 : lastBackupNo.hashCode());
        result = PRIME * result + ((clearMatchPool == true) ? 345 : 456);
        result = PRIME * result + ((useBatchExecution == true) ? 1 : 0);
        result = PRIME * result + ((autoWriteAutoValidatedAddresses == true) ? 1 : 0);
        result = PRIME * result + ((poolFilterSetting == null) ? 0 : poolFilterSetting.hashCode());
        result = PRIME * result + ((autoMatchThreshold == null) ? 0 : autoValidateSetting.hashCode());
        return result;
    }

	@Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MungeSettings)){
            return false;
        }
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        final MungeSettings other = (MungeSettings) obj;
        if (autoMatchThreshold == null) {
            if (other.autoMatchThreshold != null) return false;
        } else if (!autoMatchThreshold.equals(other.autoMatchThreshold)) {
            return false;
        }
        
        if (lastBackupNo == null) {
            if (other.lastBackupNo != null) return false;
        } else if (!lastBackupNo.equals(other.lastBackupNo)) {
            return false;
        }
        
        if (clearMatchPool != other.clearMatchPool ) return false;
        
        if (useBatchExecution != other.useBatchExecution ) return false;
        
        if (autoWriteAutoValidatedAddresses != other.autoWriteAutoValidatedAddresses) return false;
        
        if (poolFilterSetting != other.poolFilterSetting) return false;
        
        if (autoValidateSetting != other.autoValidateSetting) return false;
        
        return true;
    }

	/**
	 * The threshold above which matches are automatically resolved
	 */
	private Short autoMatchThreshold;

	/**
	 * The number of the last backup
	 */
	private Long lastBackupNo;

	/**
	 * Truncate the candidate duplicate table
	 */
	private boolean clearMatchPool = false;

	/**
	 * Set whether or not to use batch updates when running SQL statements to
	 * update the Match/Address Pool
	 */
	private boolean useBatchExecution = false;

	/**
	 * Currently, setting is specific to the Address Correction Engine
	 * <p>
	 * If true, then the Address Correction Process will automatically write
	 * back automatically validated addresses back to the source table.
	 * <p>
	 * If false, then the Address Correction process will not automatically
	 * write anything to the source table until the commit phase.
	 */
	private boolean autoWriteAutoValidatedAddresses = false;
	
	private PoolFilterSetting poolFilterSetting = PoolFilterSetting.INVALID_OR_DIFFERENT_FORMAT;
	
	private AutoValidateSetting autoValidateSetting = AutoValidateSetting.EVERYTHING_WITH_ONE_SUGGESTION;
	
	public boolean getClearMatchPool() {
		return clearMatchPool;
	}

	public boolean isClearMatchPool() {
		return clearMatchPool;
	}

	public boolean isUseBatchExecution() {
		return useBatchExecution;
	}
	
	public void setUseBatchExecution(boolean useBatchExecute) {
		boolean oldValue = this.useBatchExecution;
		this.useBatchExecution = useBatchExecute;
		firePropertyChange("useBatchExecution", oldValue,
				this.useBatchExecution);
	}
	
	public boolean isAutoWriteAutoValidatedAddresses() {
		return autoWriteAutoValidatedAddresses;
	}

	public void setAutoWriteAutoValidatedAddresses(boolean autoWriteAutoValidatedAddresses) {
		boolean oldValue = this.autoWriteAutoValidatedAddresses;
		this.autoWriteAutoValidatedAddresses = autoWriteAutoValidatedAddresses;
		firePropertyChange("autoWriteAutoValidatedAddresses", oldValue,
				this.autoWriteAutoValidatedAddresses);
	}
	
	public void setClearMatchPool(boolean clearMatchPool) {
		boolean oldValue = this.clearMatchPool;
		this.clearMatchPool = clearMatchPool;
		firePropertyChange("clearMatchPool", oldValue,
				this.clearMatchPool);
	}

	public Short getAutoMatchThreshold() {
		return autoMatchThreshold;
	}

	public void setAutoMatchThreshold(Short autoMatchThreshold) {
		Short oldValue = this.autoMatchThreshold;
		this.autoMatchThreshold = autoMatchThreshold;
		firePropertyChange("autoMatchThreshold", oldValue,
				this.autoMatchThreshold);
	}

	public Long getLastBackupNo() {
		return lastBackupNo;
	}

	public void setLastBackupNo(Long lastBackupNo) {
		Long oldValue = this.lastBackupNo;
		this.lastBackupNo = lastBackupNo;
		firePropertyChange("lastBackupNo", oldValue,
				this.lastBackupNo);
	}
	
    public PoolFilterSetting getPoolFilterSetting() {
		return poolFilterSetting;
	}

	public void setPoolFilterSetting(PoolFilterSetting poolFilterSetting) {
		PoolFilterSetting oldValue = this.poolFilterSetting;
		this.poolFilterSetting = poolFilterSetting;
		firePropertyChange("poolFilterSetting", oldValue, poolFilterSetting);
	}

	public AutoValidateSetting getAutoValidateSetting() {
		return autoValidateSetting;
	}

	public void setAutoValidateSetting(AutoValidateSetting autoValidateSetting) {
		AutoValidateSetting oldValue = this.autoValidateSetting;
		this.autoValidateSetting = autoValidateSetting;
		firePropertyChange("autoValidateSetting", oldValue, autoValidateSetting);
	}

	@Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[MungeSettings: ");
        buf.append("autoMatchThreshold->"+autoMatchThreshold+", ");
        buf.append("lastBackupNo->"+lastBackupNo+", ");
        buf.append("clearMatchPool->"+clearMatchPool+", ");
        buf.append("useBatchExecution->"+useBatchExecution+", ");
        buf.append("autoWriteAutoValidatedAddresses->"+autoWriteAutoValidatedAddresses+", ");
        buf.append("poolFilterSetting->" + poolFilterSetting + ", ");
        buf.append("autoValidateSetting->" + autoValidateSetting + ", ");
        buf.append(super.toString());
        buf.append("]");
        return buf.toString();
    }

    /**
     * duplicate all properties of the MungeSettings except parent
     * @return new MungeSettings instance with the same properties
     * except parent
     */
	public MungeSettings duplicate(MatchMakerObject parent,MatchMakerSession s) {
		MungeSettings settings = new MungeSettings();
		settings.setAppendToLog(getAppendToLog());
		settings.setAutoMatchThreshold(getAutoMatchThreshold()==null?null:new Short(getAutoMatchThreshold()));
		settings.setDebug(getDebug());
		settings.setDescription(getDescription()==null?null:new String(getDescription()));
		settings.setLastBackupNo(getLastBackupNo()==null?null:new Long(getLastBackupNo()));
		settings.setLastRunDate(getLastRunDate()==null?null:new Date(getLastRunDate().getTime()));
		settings.setLog(getLog()==null?null:new File(getLog().getPath()));
		settings.setName(getName()==null?null:new String(getName()));
		settings.setProcessCount(getProcessCount()==null?null:new Integer(getProcessCount()));
		settings.setSendEmail(getSendEmail());
		settings.setSession(s);
		settings.setClearMatchPool(isClearMatchPool());
		settings.setVisible(isVisible());
		settings.setUseBatchExecution(isUseBatchExecution());
		settings.setAutoWriteAutoValidatedAddresses(isAutoWriteAutoValidatedAddresses());
		settings.setPoolFilterSetting(getPoolFilterSetting());
		settings.setAutoValidateSetting(getAutoValidateSetting());
		return settings;
	}

	@Override
	public List<? extends SPObject> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}
}