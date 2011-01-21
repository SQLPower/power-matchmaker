/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui;

import javax.swing.JTable;

import org.apache.log4j.Logger;

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

    private static final Logger logger = Logger.getLogger(TranslateWordValidator.class);
	
    private JTable table;
    
    public TranslateWordValidator(JTable table){
        this.table = table;
    }
    public ValidateResult validate(Object contents) {
        TranslateWordsTableModel model = (TranslateWordsTableModel)table.getModel();
        for (int x = 0; x<model.getRowCount();x++) {
        	String val = (String) model.getValueAt(x, 0);
        	if (val.equals("")) {
                return ValidateResult.createValidateResult(Status.FAIL, 
                        "The from field can not be empty");
        	}
        	
        }
        return ValidateResult.createValidateResult(Status.OK, "");
    }

}
