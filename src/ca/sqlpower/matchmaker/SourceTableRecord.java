package ca.sqlpower.matchmaker;

import java.util.List;
import java.util.Set;

public class SourceTableRecord {
    
    /**
     * The values of the unique index columns in the same order as the
     * Index Column objects in the source table's index.  This lets us
     * select the entire match source record when we need it.
     */
    private List<Object> keyValues;
    private Set<PotentialMatchRecord> potentialMatches;
    
    public List<Object> getKeyValues() {
        return keyValues;
    }


    public void setKeyValues(List<Object> keyValues) {
        this.keyValues = keyValues;
    }


    /**
     * Two source table records are equal if their primary key values are all the 
     * same
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SourceTableRecord)) {
            return false;
        } 
        SourceTableRecord other = (SourceTableRecord) obj;
        for (int i=0; i<keyValues.size(); i++){
            Object otherKeyValue = other.getKeyValues().get(i);
            if (otherKeyValue != keyValues.get(i)){
                if (keyValues != null) {
                    if (!keyValues.equals(otherKeyValue)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
      
        return true;
    }

}
