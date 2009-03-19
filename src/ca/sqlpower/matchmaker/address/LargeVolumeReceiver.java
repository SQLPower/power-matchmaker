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

package ca.sqlpower.matchmaker.address;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/**
 * Large volume receivers are given a specific postal code and have
 * special rules. These values come from the dcr file.
 */
@Entity
public class LargeVolumeReceiver {
	
	public static enum LVRRecordType {
		BUILDING_NAME("A"),
		LVR_NAME_STREET("B"),
		GOVERNMENT_NAME_STREET("C"),
		LVR_NAME_LOCK_BOX("D"),
		GOVERNMENT_NAME_LOCK_BOX("E"),
		GENERAL_DELIVERY_NAME("F");
		
		private final String code;

		private LVRRecordType(String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
		
		public static LVRRecordType forCode(String code) {
			for (LVRRecordType type : values()) {
				if (type.getCode().equals(code)) {
					return type;
				}
			}
			throw new IllegalStateException("Unknown code for LVR record type " + code);
		}
		
	}
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE)
	private String lvrRecordType;
	
    private String buildingName;
    private String buildingTypeCode; // XXX enum

    // government
    private String departmentName;
    private String branchName;
    private String languageCode;

    private String largeVolumeReceiverName;
    
    private String generalDeliveryDescription;
    
    @PrimaryKey
    private String postalCode;
    
    public String getBuildingName() {
        return buildingName;
    }



    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }



    public String getBuildingTypeCode() {
        return buildingTypeCode;
    }



    public void setBuildingTypeCode(String buildingTypeCode) {
        this.buildingTypeCode = buildingTypeCode;
    }



    public String getDepartmentName() {
        return departmentName;
    }



    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }



    public String getBranchName() {
        return branchName;
    }



    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }



    public String getLanguageCode() {
        return languageCode;
    }



    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }



    public String getLargeVolumeReceiverName() {
        return largeVolumeReceiverName;
    }



    public void setLargeVolumeReceiverName(String largeVolumeReceiverName) {
        this.largeVolumeReceiverName = largeVolumeReceiverName;
    }
    
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" buildingName: ").append(buildingName);
        sb.append(" buildingTypeCode: ").append(buildingTypeCode);
        sb.append(" departmentName: ").append(departmentName);
        sb.append(" branchName: ").append(branchName);
        sb.append(" languageCode: ").append(languageCode);
        sb.append(" largeVolumeReceiverName: ").append(largeVolumeReceiverName);
        return sb.toString();
    }



	public void setLVRRecordType(String lvrRecordType) {
			this.lvrRecordType = lvrRecordType;
		
	}
	
	public void setLVRRecordType(LVRRecordType lvrRecordType) {
		this.lvrRecordType = lvrRecordType.getCode();
	
	}



	public LVRRecordType getLVRRecordType() {
		return LVRRecordType.forCode(lvrRecordType);
	}



	public void setGeneralDeliveryDescription(String generalDeliveryDescription) {
			this.generalDeliveryDescription = generalDeliveryDescription;
		
	}



	public String getGeneralDeliveryDescription() {
		return generalDeliveryDescription;
	}



	public void setPostalCode(String postalCode) {
			this.postalCode = postalCode;
		
	}



	public String getPostalCode() {
		return postalCode;
	}

}
