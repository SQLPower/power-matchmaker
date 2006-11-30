package ca.sqlpower.matchmaker;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import ca.sqlpower.architect.ArchitectException;

/**
 * this class imports match from xml export file.
 */
public class MatchImportor {

	private final static Logger logger = Logger.getLogger(MatchImportor.class);

	private Digester setupDigester(Match match) {
        Digester d = new Digester();
        d.setValidating(false);
        d.push(match);


        d.addSetProperties("EXPORT/PL_MATCH");
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_ID", "setName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/FILTER", "setFilter", 0);

        d.addObjectCreate("EXPORT/PL_MATCH_GROUP", MatchMakerCriteriaGroup.class);
        d.addSetProperties("EXPORT/PL_MATCH_GROUP");
        d.addSetNext("EXPORT/PL_MATCH_GROUP", "addMatchCriteriaGroup");

        d.addCallMethod("EXPORT/PL_MATCH_GROUP/GROUP_ID", "setName", 0);

        d.addObjectCreate("EXPORT/PL_MATCH_CRITERIA", MatchMakerCriteria.class);
        d.addSetProperties("EXPORT/PL_MATCH_CRITERIA");
        d.addSetNext("EXPORT/PL_MATCH_CRITERIA", "addChild");

        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/COLUMN_NAME", "setName", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/CASE_SENSITIVE_IND", "setCaseSensitiveInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/SUPPRESS_CHAR", "setSuppressChar", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/SOUND_IND", "setSoundInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/FIRST_N_CHAR", "setFirstNChar", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/LAST_UPDATE_DATE/DATE", "setLastUpdateDate", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/LAST_UPDATE_USER", "setLastUpdateUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/SEQ_NO", "setSeqNo", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/MATCH_START", "setMatchStart", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/MATCH_END", "setMatchEnd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/VARIANCE_AMT", "setVarianceAmt", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/VARIANCE_TYPE", "setVarianceType", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/LAST_UPDATE_OS_USER", "setLastUpdateOsUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/ALLOW_NULL_IND", "setAllowNullInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/TRANSLATE_GROUP_NAME", "setTranslateGroupName", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/REMOVE_SPECIAL_CHARS", "setRemoveSpecialChars", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/REORDER_IND", "setReorderInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/FIRST_N_CHAR_BY_WORD_IND", "setFirstNCharByWordInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/REPLACE_WITH_SPACE", "setReplaceWithSpace", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/REPLACE_WITH_SPACE_IND", "setReplaceWithSpaceInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/FIRST_N_CHAR_BY_WORD", "setFirstNCharByWord", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/MIN_WORDS_IN_COMMON", "setMinWordsInCommon", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/MATCH_FIRST_PLUS_ONE_IND", "setMatchFirstPlusOneInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/WORDS_IN_COMMON_NUM_WORDS", "setWordsInCommonNumWords", 0);

        //TODO: restore the merge import
         return d;
    }

	/**
	 * import match from xml export file.
	 * @param match -- the match to load to
	 * @param in     -- input from
	 * @return      -- true if nothing wrong.
	 * @throws ArchitectException
	 */
	public boolean load(Match match, InputStream in) throws ArchitectException {

		// use digester to read from file
        try {
            setupDigester(match).parse(in);
        } catch (SAXException ex) {
            logger.error("SAX Exception in config file parse!", ex);
            throw new ArchitectException("Syntax error in export file", ex);
        } catch (IOException ex) {
            logger.error("IO Exception in config file parse!", ex);
            throw new ArchitectException("I/O Error", ex);
        } catch (Exception ex) {
            logger.error("General Exception in config file parse!", ex);
            throw new ArchitectException("Unexpected Exception", ex);
        }


		return true;
	}
}
