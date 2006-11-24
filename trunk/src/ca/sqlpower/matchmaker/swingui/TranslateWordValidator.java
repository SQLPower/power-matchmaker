package ca.sqlpower.matchmaker.swingui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JTable;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * This validator is used to check the translate words in the JTable.
 * It gets the translations from the model and adds it to the list.  The list
 * is then put into a Set.  If the set and list size are not equal, this indicates
 * that there is a duplicate translations in which case the status becomes fail.
 */
public class TranslateWordValidator implements Validator {

    private JTable table;
    private List<Action> actions;
    
    public TranslateWordValidator(JTable table,List<Action> actions){
        this.table = table;
        this.actions = actions;
    }
    public ValidateResult validate(Object contents) {
        MatchTranslateTableModel model = (MatchTranslateTableModel)table.getModel();
        Set<MatchMakerObject> childrenSet = new HashSet<MatchMakerObject>();
        for (int i =0; i < model.getRowCount(); i++){
            childrenSet.add(model.getMatchMakerObject(i));
        }
        if (childrenSet.size() != model.getRowCount()){
            setComponentsEnabled(false);
            return ValidateResult.createValidateResult(Status.FAIL, 
                    "No Duplicate Translations Allowed");
        } 
        setComponentsEnabled(true);
        return ValidateResult.createValidateResult(Status.OK, "");
    }
    
    private void setComponentsEnabled(boolean enable){
        if (actions != null){
            for (Action c : actions){
                c.setEnabled(enable);
            }
        }
    }

}
