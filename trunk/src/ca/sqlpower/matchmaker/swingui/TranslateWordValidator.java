package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
    
    public TranslateWordValidator(JTable table){
        this.table = table;
    }
    public ValidateResult validate(Object contents) {
        MatchTranslateTableModel model = (MatchTranslateTableModel)table.getModel();
        List<MatchMakerObject> childrenList = new ArrayList<MatchMakerObject>();
        for (int i =0; i < table.getRowCount(); i++){
            childrenList.add(model.getMatchMakerObject(i));
        }
        Set<MatchMakerObject> childrenSet = new TreeSet<MatchMakerObject>(childrenList);
        if (childrenSet.size() < childrenList.size()){
            return ValidateResult.createValidateResult(Status.FAIL, 
                    "No Duplicate Translations Allowed");
        } 
        return ValidateResult.createValidateResult(Status.OK, "");
    }

}
