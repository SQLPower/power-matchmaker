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
    
    private MatchMakerObject parent;
    private List<Action> actionsToDisable;
    private String dupErrorMessage;
    
    public MMODuplicateValidator(MatchMakerObject parent, List<Action> actionsToDisable,
    		String dupErrorMessage){
        this.parent = parent;
        this.actionsToDisable = actionsToDisable;
        this.dupErrorMessage = dupErrorMessage;
    }
    
    
    public ValidateResult validate(Object contents) {
        String value = (String)contents;
        if (value==null || value.trim().length() ==0){
            setComponentsEnabled(false);
            return ValidateResult.createValidateResult(Status.OK, 
                    "");
        }
        
        if (!parent.allowsChildren()){
            return ValidateResult.createValidateResult(Status.FAIL, 
                    "Cannot add children to "+parent.getClass());
        }        
        for (MatchMakerObject mmo : (List<MatchMakerObject>)parent.getChildren()){
            if (mmo.getName().equals(value)){
                setComponentsEnabled(false);
                return ValidateResult.createValidateResult(Status.FAIL, 
                        dupErrorMessage);
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
