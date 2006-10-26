package ca.sqlpower.matchmaker.hibernate;
// Generated Oct 23, 2006 12:38:03 PM by Hibernate Tools 3.2.0.beta7


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * PlMatchTranslateGroup generated by hbm2java
 */
public class PlMatchTranslateGroup  implements java.io.Serializable {

    // Fields    

     private Long id;
     private String translateGroupName;
     private List<PlMatchTranslate> plMatchTranslations = new ArrayList<PlMatchTranslate>();
     private Set plMatchCriterias = new HashSet(0);

     // Constructors

    /** default constructor */
    public PlMatchTranslateGroup() {
    }

   
    // Property accessors
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long translateGroupOid) {
        this.id = translateGroupOid;
    }
    public String getTranslateGroupName() {
        return this.translateGroupName;
    }
    
    public void setTranslateGroupName(String translateGroupName) {
        this.translateGroupName = translateGroupName;
    }
    public List<PlMatchTranslate> getPlMatchTranslations() {
        return this.plMatchTranslations;
    }
    
    public void setPlMatchTranslations(List<PlMatchTranslate> plMatchTranslates) {
        this.plMatchTranslations = plMatchTranslates;
    }
    public Set getPlMatchCriterias() {
        return this.plMatchCriterias;
    }
    
    public void setPlMatchCriterias(Set plMatchCriterias) {
        this.plMatchCriterias = plMatchCriterias;
    }

    @Override
    public String toString() {
    	return translateGroupName;
    }


}


