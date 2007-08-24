/*
 * Copyright (c) 2007, SQL Power Group Inc.
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


package ca.sqlpower.matchmaker.swingui;

import java.util.List;

import javax.swing.Action;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * This validator takes in an MatchMakerObject and does a check to ensure
 * that no duplicate children of the object is created.  It also checks that 
 * the child is not null but will only show it as warning instead of fail.
 *
 */
public class MMODuplicateValidator implements Validator {

    private static final Logger logger = Logger.getLogger(MMODuplicateValidator.class);
    
    /**
     * The parent of the object to lookup duplicates
     */
    private MatchMakerObject parent;
    
    
    /**
     * A list of actions to disable in certain failing cases
     */
    private List<Action> actionsToDisable;
    
    /**
     * The name of the JComponent to aid in displaying proper error messages
     */
    private final String fieldName;
    
    /**
     * The maximum characters the JComponent name allows, if no limit, it's a negative number
     */
    private final int maxCharacters;
    

    /**
     * This validator works by checking if the input is a duplicate of any child's name of
     * the passed in parent MatchMakerObject and checking on character size limits,
     * if there is no limit input a negative number for maxCharacters.  
     * The validator also gets a list of action to enable and disable when appropiate as well.
     * 
     * @param parent the parent MatchMakerObject of the validating MatchMakerObject
     * @param actionsToDisable a list of actions to disable under failing circumstances
     * @param fieldName the name of the JComponent being validated
     * @param maxCharacters the restriction on how long the JComponent could be, if no limit
     * specify a negative number
     */
    public MMODuplicateValidator(MatchMakerObject parent, List<Action> actionsToDisable,
            String fieldName){
        this(parent, actionsToDisable, fieldName, -1);
    }
    
    /**
     * This validator works by checking if the input is a duplicate of any child's name of
     * the passed in parent MatchMakerObject.  The validator also gets a list of action
     * to enable and disable when appropiate as well.  This constructor assumes that the field
     * can accept an infinite amount of characters
     * 
     * @param parent the parent MatchMakerObject of the validating MatchMakerObject
     * @param actionsToDisable a list of actions to disable under failing circumstances
     * @param fieldName the name of the JComponent being validated
     * specify a negative number
     */
    public MMODuplicateValidator(MatchMakerObject parent, List<Action> actionsToDisable,
            String fieldName, final int maxCharacters){
        this.parent = parent;
        this.actionsToDisable = actionsToDisable;
        this.fieldName = fieldName;
        this.maxCharacters = maxCharacters;
    }
    
    
    public ValidateResult validate(Object contents) {
        String value = (String)contents;
        if (value==null || value.trim().length() ==0){
            setComponentsEnabled(false);
            return ValidateResult.createValidateResult(Status.OK, 
                    "");
        }

        if (maxCharacters > 0){
            if (value.length() > maxCharacters){
                setComponentsEnabled(false);
                return ValidateResult.createValidateResult(Status.FAIL, fieldName + " cannot be longer than "
                        + maxCharacters + " characters long");
            }
        }
        if (!parent.allowsChildren()){
            return ValidateResult.createValidateResult(Status.FAIL, 
                    "Cannot add children to "+parent.getClass());
        }        
        for (MatchMakerObject mmo : (List<MatchMakerObject>)parent.getChildren()){
            if (mmo.getName().equals(value)){
                setComponentsEnabled(false);
                return ValidateResult.createValidateResult(Status.FAIL, 
                        "Cannot have duplicate " + fieldName);
            }
        }
        setComponentsEnabled(true);
        return ValidateResult.createValidateResult(Status.OK, "");
        
    }
    
    private void setComponentsEnabled(boolean enable){
        if (actionsToDisable != null){
            for (Action c : actionsToDisable){
                c.setEnabled(enable);
            }
        }
    }

}
