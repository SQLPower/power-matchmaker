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
 * All the information we get from the postal system about a particular postal code.
 * This record type is the real meat of the address validation database.
 */
@Entity
public class PostalCode {

    /**
     * The postal or ZIP code, formatted in all caps with no spaces (for a
     * Canadian postal code, this means exactly 6 characters).
     */
    @PrimaryKey
    private String postalCode;
    
    private AddressType addressType;
    
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String provinceCode;
    
    private String directoryAreaName;
    private String deliveryInstallationPostalCode;
    
    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String municipalityName;

    @SecondaryKey(relate=Relationship.MANY_TO_ONE)
    private String streetName;
    
    private String streetTypeCode;
    private String streetDirectionCode;
    private AddressSequenceType streetAddressSequenceType;
    private Integer streetAddressToNumber;
    private Integer streetAddressFromNumber;
    private String streetAddressNumberSuffixToCode;
    private String streetAddressNumberSuffixFromCode;

    private String routeServiceBoxToNumber;
    private String routeServiceBoxFromNumber;
    private String routeServiceTypeDescription;
    private String routeServiceNumber;

    private String suiteToNumber;
    private String suiteFromNumber;

    private String buildingName;
    private String buildingTypeCode; // XXX enum

    // government
    private String departmentName;
    private String branchName;
    private String languageCode;

    private String largeVolumeReceiverName;

    
    
    public String getPostalCode() {
        return postalCode;
    }



    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }



    public AddressType getAddressType() {
        return addressType;
    }



    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }



    public String getProvinceCode() {
        return provinceCode;
    }



    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }



    public String getDirectoryAreaName() {
        return directoryAreaName;
    }



    public void setDirectoryAreaName(String directoryAreaName) {
        this.directoryAreaName = directoryAreaName;
    }



    public String getDeliveryInstallationPostalCode() {
        return deliveryInstallationPostalCode;
    }



    public void setDeliveryInstallationPostalCode(
            String deliveryInstallationPostalCode) {
        this.deliveryInstallationPostalCode = deliveryInstallationPostalCode;
    }



    public String getMunicipalityName() {
        return municipalityName;
    }



    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }



    public String getStreetName() {
        return streetName;
    }



    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }



    public String getStreetTypeCode() {
        return streetTypeCode;
    }



    public void setStreetTypeCode(String streetTypeCode) {
        this.streetTypeCode = streetTypeCode;
    }



    public String getStreetDirectionCode() {
        return streetDirectionCode;
    }



    public void setStreetDirectionCode(String streetDirectionCode) {
        this.streetDirectionCode = streetDirectionCode;
    }



    public AddressSequenceType getStreetAddressSequenceType() {
        return streetAddressSequenceType;
    }



    public void setStreetAddressSequenceType(
            AddressSequenceType streetAddressSequenceType) {
        this.streetAddressSequenceType = streetAddressSequenceType;
    }



    public Integer getStreetAddressToNumber() {
        return streetAddressToNumber;
    }



    public void setStreetAddressToNumber(Integer streetAddressToNumber) {
        this.streetAddressToNumber = streetAddressToNumber;
    }



    public Integer getStreetAddressFromNumber() {
        return streetAddressFromNumber;
    }



    public void setStreetAddressFromNumber(Integer streetAddressFromNumber) {
        this.streetAddressFromNumber = streetAddressFromNumber;
    }



    public String getStreetAddressNumberSuffixToCode() {
        return streetAddressNumberSuffixToCode;
    }



    public void setStreetAddressNumberSuffixToCode(
            String streetAddressNumberSuffixToCode) {
        this.streetAddressNumberSuffixToCode = streetAddressNumberSuffixToCode;
    }



    public String getStreetAddressNumberSuffixFromCode() {
        return streetAddressNumberSuffixFromCode;
    }



    public void setStreetAddressNumberSuffixFromCode(
            String streetAddressNumberSuffixFromCode) {
        this.streetAddressNumberSuffixFromCode = streetAddressNumberSuffixFromCode;
    }



    public String getRouteServiceBoxToNumber() {
        return routeServiceBoxToNumber;
    }



    public void setRouteServiceBoxToNumber(String routeServiceBoxToNumber) {
        this.routeServiceBoxToNumber = routeServiceBoxToNumber;
    }



    public String getRouteServiceBoxFromNumber() {
        return routeServiceBoxFromNumber;
    }



    public void setRouteServiceBoxFromNumber(String routeServiceBoxFromNumber) {
        this.routeServiceBoxFromNumber = routeServiceBoxFromNumber;
    }



    public String getRouteServiceTypeDescription() {
        return routeServiceTypeDescription;
    }



    public void setRouteServiceTypeDescription(String routeServiceTypeDescription) {
        this.routeServiceTypeDescription = routeServiceTypeDescription;
    }



    public String getRouteServiceNumber() {
        return routeServiceNumber;
    }



    public void setRouteServiceNumber(String routeServiceNumber) {
        this.routeServiceNumber = routeServiceNumber;
    }



    public String getSuiteToNumber() {
        return suiteToNumber;
    }



    public void setSuiteToNumber(String suiteToNumber) {
        this.suiteToNumber = suiteToNumber;
    }



    public String getSuiteFromNumber() {
        return suiteFromNumber;
    }



    public void setSuiteFromNumber(String suiteFromNumber) {
        this.suiteFromNumber = suiteFromNumber;
    }



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
        StringBuilder sb = new StringBuilder();
        sb.append("PostalCode: ").append(postalCode);
        sb.append(" addressType: " + addressType);
        sb.append(" provinceCode: ").append(provinceCode);
        sb.append(" directoryAreaName: ").append(directoryAreaName);
        sb.append(" deliveryInstallationPostalCode: ").append(deliveryInstallationPostalCode);
        sb.append(" municipalityName: ").append(municipalityName);
        sb.append(" streetName: ").append(streetName);
        sb.append(" streetTypeCode: ").append(streetTypeCode);
        sb.append(" streetDirectionCode: ").append(streetDirectionCode);
        sb.append(" streetAddressSequenceType: ").append(streetAddressSequenceType);
        sb.append(" streetAddressToNumber: ").append(streetAddressToNumber);
        sb.append(" streetAddressFromNumber: ").append(streetAddressFromNumber);
        sb.append(" streetAddressNumberSuffixToCode: ").append(streetAddressNumberSuffixToCode);
        sb.append(" streetAddressNumberSuffixFromCode: ").append(streetAddressNumberSuffixFromCode);
        sb.append(" routeServiceBoxToNumber: ").append(routeServiceBoxToNumber);
        sb.append(" routeServiceBoxFromNumber: ").append(routeServiceBoxFromNumber);
        sb.append(" routeServiceTypeDescription: ").append(routeServiceTypeDescription);
        sb.append(" routeServiceNumber: ").append(routeServiceNumber);
        sb.append(" suiteToNumber: ").append(suiteToNumber);
        sb.append(" suiteFromNumber: ").append(suiteFromNumber);
        sb.append(" buildingName: ").append(buildingName);
        sb.append(" buildingTypeCode: ").append(buildingTypeCode);
        sb.append(" departmentName: ").append(departmentName);
        sb.append(" branchName: ").append(branchName);
        sb.append(" languageCode: ").append(languageCode);
        sb.append(" largeVolumeReceiverName: ").append(largeVolumeReceiverName);
        
        return sb.toString();
    }
}
