/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

public class AddressCorrectionSettings extends MatchMakerSettings {

	/**
	 * Truncate the candidate duplicate table
	 */
	private boolean clearAddressPool = false;

	public boolean isClearAddressPool() {
		return clearAddressPool;
	}

	public void setClearAddressPool(boolean clearAddressPool) {
		boolean oldValue = this.clearAddressPool;
		this.clearAddressPool = clearAddressPool;
		getEventSupport().firePropertyChange("clearAddressPool", oldValue,
				this.clearAddressPool);
	}
	
   @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[AddressCorrectionSettings: ");
        buf.append("clearAddressPool->"+clearAddressPool+", ");
        buf.append(super.toString());
        buf.append("]");
        return buf.toString();
    }

   @Override
   public boolean equals(Object obj) {
       if(!(obj instanceof AddressCorrectionSettings)){
           return false;
       }
       if (this == obj) return true;
       if (!super.equals(obj)) return false;
       if (getClass() != obj.getClass()) return false;
       final AddressCorrectionSettings other = (AddressCorrectionSettings) obj;

       if (clearAddressPool != other.clearAddressPool ) return false;
       
       return true;
   }
   
   @Override
   public int hashCode() {
       final int PRIME = 31;
       int result = super.hashCode();
       result = PRIME * result + (clearAddressPool ? 1 : 0);
       return result;
   }
   
    /**
     * duplicate all properties of the MungeSettings except parent
     * @return new MungeSettings instance with the same properties
     * except parent
     */
	public AddressCorrectionSettings duplicate(MatchMakerObject parent,MatchMakerSession s) {
		AddressCorrectionSettings settings = new AddressCorrectionSettings();
		settings.setAppendToLog(getAppendToLog());
		settings.setDebug(getDebug());
		settings.setDescription(getDescription()==null?null:new String(getDescription()));
		settings.setLastRunDate(getLastRunDate()==null?null:new Date(getLastRunDate().getTime()));
		settings.setLog(getLog()==null?null:new File(getLog().getPath()));
		settings.setName(getName()==null?null:new String(getName()));
		settings.setProcessCount(getProcessCount()==null?null:new Integer(getProcessCount()));
		settings.setSendEmail(getSendEmail());
		settings.setSession(s);
		settings.setClearAddressPool(isClearAddressPool());
		settings.setVisible(isVisible());
		return settings;
	}
}
