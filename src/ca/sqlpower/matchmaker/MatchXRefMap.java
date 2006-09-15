package ca.sqlpower.matchmaker;


public class MatchXRefMap {

    private String matchID;
    private int mappingSeqNo;
    private String sourceColumnName;
    private String targeColumnName;
    private boolean  indexColumn;

    public void MatchXRefMap(){

    }

    public boolean isIndexColumn() {
        return indexColumn;
    }

    public void setIndexColumn(boolean indexColumn) {
        this.indexColumn = indexColumn;
    }

    public int getMappingSeqNo() {
        return mappingSeqNo;
    }

    public void setMappingSeqNo(int mappingSeqNo) {
        this.mappingSeqNo = mappingSeqNo;
    }

    public String getMatchID() {
        return matchID;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    public String getSourceColumnName() {
        return sourceColumnName;
    }

    public void setSourceColumnName(String sourceColumnName) {
        this.sourceColumnName = sourceColumnName;
    }

    public String getTargeColumnName() {
        return targeColumnName;
    }

    public void setTargeColumnName(String targeColumnName) {
        this.targeColumnName = targeColumnName;
    }
}
