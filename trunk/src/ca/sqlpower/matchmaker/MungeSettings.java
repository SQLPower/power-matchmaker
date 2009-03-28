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

package ca.sqlpower.matchmaker;

import java.io.File;
import java.util.Date;

import ca.sqlpower.matchmaker.address.AddressValidator;

/**
 * Settings that are specific to the Match engine
 */
public class MungeSettings extends MatchMakerSettings {

	/**
	 * An enumeration of settings for which records to store in a result table.
	 */
	public enum PoolFilterSetting {
		/**
		 * Include all records into the address result table
		 */
		EVERYTHING("Include all records"),
		/**
		 * Include any records that are either invalid, or have
		 * suggestions with a different format from the original source addresses
		 */
		INVALID_OR_DIFFERENT_FORMAT("Include SERP invalid or different format"),
		/**
		 * Include only records that are invalid 
		 */
		INVALID_ONLY("Include only SERP invalid records");
		
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
		NOTHING("Do not auto-validate"),
		/**
		 * Only validate addresses that are SERP correctable
		 * (that is, the {@link AddressValidator} provides a
		 * valid suggestion)
		 */
		SERP_CORRECTABLE("Auto-validate only SERP correctable addresses"),
		/**
		 * Validate any address with only one suggestion 
		 */
		EVERYTHING_WITH_ONE_SUGGESTION("Auto-validate any address with only one suggestion"),
		/**
		 * Validate any address with at least one suggestion
		 */
		EVERYTHING_WITH_SUGGESTION("Auto-validate any address with a suggestion");
		
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
        result = PRIME * result + ((serpAutocorrect == true) ? 1 : 0);
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
        
        if (serpAutocorrect != other.serpAutocorrect) return false;
        
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
	 * If true, then the Address Correction Process will automatically correct
	 * any address that is SERP correctable.
	 * <p>
	 * If false, then the Address Correction process will not automatically
	 * correct any addresses.
	 */
	private boolean serpAutocorrect = false;
	
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
		getEventSupport().firePropertyChange("useBatchExecution", oldValue,
				this.useBatchExecution);
	}
	
	public boolean isSerpAutocorrect() {
		return serpAutocorrect;
	}

	public void setSerpAutocorrect(boolean serpAutocorrect) {
		boolean oldValue = this.serpAutocorrect;
		this.serpAutocorrect = serpAutocorrect;
		getEventSupport().firePropertyChange("serpAutocorrect", oldValue,
				this.serpAutocorrect);
	}
	
	public void setClearMatchPool(boolean clearMatchPool) {
		boolean oldValue = this.clearMatchPool;
		this.clearMatchPool = clearMatchPool;
		getEventSupport().firePropertyChange("clearMatchPool", oldValue,
				this.clearMatchPool);
	}

	public Short getAutoMatchThreshold() {
		return autoMatchThreshold;
	}

	public void setAutoMatchThreshold(Short autoMatchThreshold) {
		Short oldValue = this.autoMatchThreshold;
		this.autoMatchThreshold = autoMatchThreshold;
		getEventSupport().firePropertyChange("autoMatchThreshold", oldValue,
				this.autoMatchThreshold);
	}

	public Long getLastBackupNo() {
		return lastBackupNo;
	}

	public void setLastBackupNo(Long lastBackupNo) {
		Long oldValue = this.lastBackupNo;
		this.lastBackupNo = lastBackupNo;
		getEventSupport().firePropertyChange("lastBackupNo", oldValue,
				this.lastBackupNo);
	}
	
    public PoolFilterSetting getPoolFilterSetting() {
		return poolFilterSetting;
	}

	public void setPoolFilterSetting(PoolFilterSetting poolFilterSetting) {
		PoolFilterSetting oldValue = this.poolFilterSetting;
		this.poolFilterSetting = poolFilterSetting;
		getEventSupport().firePropertyChange("poolFilterSetting", oldValue, poolFilterSetting);
	}

	public AutoValidateSetting getAutoValidateSetting() {
		return autoValidateSetting;
	}

	public void setAutoValidateSetting(AutoValidateSetting autoValidateSetting) {
		AutoValidateSetting oldValue = this.autoValidateSetting;
		this.autoValidateSetting = autoValidateSetting;
		getEventSupport().firePropertyChange("autoValidateSetting", oldValue, autoValidateSetting);
	}

	@Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[MungeSettings: ");
        buf.append("autoMatchThreshold->"+autoMatchThreshold+", ");
        buf.append("lastBackupNo->"+lastBackupNo+", ");
        buf.append("clearMatchPool->"+clearMatchPool+", ");
        buf.append("useBatchExecution->"+useBatchExecution+", ");
        buf.append("skipValidation->"+serpAutocorrect+", ");
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
		settings.setSerpAutocorrect(isSerpAutocorrect());
		settings.setPoolFilterSetting(getPoolFilterSetting());
		settings.setAutoValidateSetting(getAutoValidateSetting());
		return settings;
	}
}