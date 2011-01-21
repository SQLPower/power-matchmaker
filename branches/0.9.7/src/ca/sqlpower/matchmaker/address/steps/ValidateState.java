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

package ca.sqlpower.matchmaker.address.steps;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

public class ValidateState {

    /**
     * If true there will be a valid suggestion after the current validation
     * run.
     * <p>
     * Some suggestions don't increase the error count as they are not a severe
     * error but they still have a more valid suggestion. This tracks if there
     * is a valid suggestion and the parsed address is not completely correct
     */
    private boolean suggestionExists = false;
    
    /**
     * This list contains all of the errors the current address has.
     */
    private final List<ValidateResult> errorList = new ArrayList<ValidateResult>();
    
    /**
     * The number of errors in the current address based on the suggestion being created.
     */
    private int errorCount = 0;
    
    /**
     * True if the suggestion being created is valid itself.
     */
    private boolean isValid = true;

    /**
     * If the address parsed was a route only and the correct address is street
     * and route we only show the route address so missing or invalid street
     * information is not an actual error.<br>
     * Alternatively, if the address parsed was a street only and the correct
     * address is street and route we only show the street address so missing or
     * invalid route information is not an actual error.
     * <p>
     * This should default to count errors unless a specific step or part needs
     * to disable it.
     * TODO: Figure out if this is even used after the mass refactoring.
     */
    private boolean countErrors = true;
    
    /**
     * This value is used in generating suggestions. If true then the suggestion had to
     * modify the address in a way that was not an error but created a valid alternative.
     * This occurs in places where the parser has difficulty like additional information
     * coming after the delivery installation name (ie: RR 4 STN A 21 YONGE puts A 21 YONGE
     * as the delivery installation name).
     */
    private boolean reparsed = false;

    /**
     * This will add the error message with a fail status to the list of errors
     * with the current address. It will also increase the error count and state
     * a suggestion exists if the errors are being counted.
     */
    public void incrementErrorCount(String errorMessage) {
        addError(ValidateResult.createValidateResult(
                Status.FAIL, errorMessage));
        if (isCountErrors()) {
            increaseErrorCount();
            setSuggestionExists(true);
        }
    }
    
    /**
     * This will add the error message with a fail status to the list of errors
     * with the current address. It will also increase the error count and state
     * a suggestion exists if the errors are being counted.<br>
     * It will also set the isValid flag state for this validation run.
     */
    public void incrementErrorAndSetValidate(String errorMessage, boolean isValid) {
        setValid(isValid);
        incrementErrorCount(errorMessage);
    }
    
    public boolean isSuggestionExists() {
        return suggestionExists;
    }

    public void setSuggestionExists(boolean suggestionExists) {
        this.suggestionExists = suggestionExists;
    }
    
    public void addError(ValidateResult validateResult) {
        errorList.add(validateResult);
    }

    public List<ValidateResult> getErrorList() {
        return errorList;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public void increaseErrorCount() {
        errorCount++;
    }

    public void setCountErrors(boolean countErrors) {
        	this.countErrors = countErrors;
        
    }

    public boolean isCountErrors() {
        return countErrors;
    }

    public void setReparsed(boolean reparsed) {
        	this.reparsed = reparsed;
        
    }

    public boolean isReparsed() {
        return reparsed;
    }

}
