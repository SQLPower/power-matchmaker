package ca.sqlpower.matchmaker.swingui;

import java.util.List;

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
    
    private MatchMakerObject parent;
    
    public MMODuplicateValidator(MatchMakerObject parent){
        this.parent = parent;
    }
    
    
    public ValidateResult validate(Object contents) {
        String value = (String)contents;
        if (value==null || value.trim().length() ==0){
            return ValidateResult.createValidateResult(Status.WARN, 
                    "The group name should not be empty");
        }
        
        if (!parent.allowsChildren()){
            return ValidateResult.createValidateResult(Status.FAIL, 
                    "Cannot add children to this object");
        }        
        for (MatchMakerObject mmo : (List<MatchMakerObject>)parent.getChildren()){
            if (mmo.getName().equals(value)){
                return ValidateResult.createValidateResult(Status.FAIL, 
                        "Cannot have duplicate object name");
            }
        }
        return ValidateResult.createValidateResult(Status.OK, "");
        
    }

}
