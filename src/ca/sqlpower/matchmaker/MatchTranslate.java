package ca.sqlpower.matchmaker;

import java.util.List;


public class MatchTranslate {

    private String groupName;
    private long seqNo;
    private String fromWord;
    private String toWord;


    public void MatchTranslate(){

    }

    public String getFromWord() {
        return fromWord;
    }

    public void setFromWord(String fromWord) {
        this.fromWord = fromWord;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(long seqNo) {
        this.seqNo = seqNo;
    }

    public String getToWord() {
        return toWord;
    }

    public void setToWord(String toWord) {
        this.toWord = toWord;
    }

}
