package ca.sqlpower.matchmaker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.swingui.ASUtils.LabelValueBean;
import ca.sqlpower.matchmaker.Match.MatchMode;

/**
 * this class imports match from xml export file.
 */
public class MatchImportor {

	private final static Logger logger = Logger.getLogger(MatchImportor.class);

	/**
	 * import match from xml export file.
	 * @param match -- the match to load to
	 * @param in     -- input from
	 *
	 * @return      -- true if nothing wrong.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public boolean load(Match match, InputStream in) throws ParserConfigurationException, SAXException, IOException {

		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(in,new MatchExportFileHandler(match));
		return true;
	}

	private class MatchExportFileHandler extends DefaultHandler {


		StringBuffer buf = new StringBuffer();
		List<LabelValueBean> properties = new ArrayList<LabelValueBean>();
		Match match;

		public MatchExportFileHandler(Match match) {
			this.match = match;
		}

		@Override
		public void startElement(String uri, String localName,
							String qName, Attributes attributes)
							throws SAXException {
			buf = new StringBuffer();


			if ( qName.equalsIgnoreCase("PL_MATCH")) {
				properties = new ArrayList<LabelValueBean>();
			} else if ( qName.equalsIgnoreCase("PL_MATCH_GROUP")) {
				properties = new ArrayList<LabelValueBean>();
			} else if ( qName.equalsIgnoreCase("PL_MATCH_CRITERIA")) {
				properties = new ArrayList<LabelValueBean>();
			} else if ( qName.equalsIgnoreCase("PL_FOLDER")) {
				properties = new ArrayList<LabelValueBean>();
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			buf.append(ch,start,length);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {

			try {
				if ( qName.equalsIgnoreCase("PL_MATCH")) {
					setMatchMaketProperties(match,match,properties);
				} else if ( qName.equalsIgnoreCase("PL_MATCH_GROUP")) {
					setMatchMaketProperties(match,new MatchMakerCriteriaGroup(),properties);
				} else if ( qName.equalsIgnoreCase("PL_MATCH_CRITERIA")) {
					setMatchMaketProperties(match,new MatchMakerCriteria(),properties);
				} else if ( qName.equalsIgnoreCase("PL_FOLDER")) {
					setMatchMaketProperties(match,new PlFolder<Match>(),properties);
				} else {

					if ( qName.equalsIgnoreCase("DATE")) {
						// search the last date column
						for ( int i=properties.size()-1; i>=0; i--) {
							LabelValueBean bean = properties.get(i);
							if (bean.getLabel().equalsIgnoreCase("CREATE_DATE") ||
									bean.getLabel().equalsIgnoreCase("LAST_UPDATE_DATE") ||
									bean.getLabel().equalsIgnoreCase("MATCH_LAST_RUN_DATE") ||
									bean.getLabel().equalsIgnoreCase("MERGE_LAST_RUN_DATE") ) {
								bean.setValue(buf.toString());
							}
						}
					} else {
						final LabelValueBean bean = new LabelValueBean(qName,buf.toString());
						properties.add(bean);
					}
				}
			} catch (ParseException e) {
				final SAXException exception = new SAXException("Parse Error:"+e.getMessage());
				exception.setStackTrace(e.getStackTrace());
				throw exception;
			}
		}

		private void setMatchMaketProperties(Match parentMatch,
				MatchMakerObject mmo,List<LabelValueBean> properties) throws ParseException {
			if ( mmo instanceof Match ) {
				Match match = (Match) mmo;
				for ( LabelValueBean bean : properties ) {
					String value = (String) bean.getValue();
					logger.debug("setting:["+bean.getLabel()+"] to ["+ value+"]");
					if ( bean.getLabel().equalsIgnoreCase("MATCH_ID")) {
						match.setName(value);
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_DESC")) {
						match.getMatchSettings().setDescription(value);
					} else if ( bean.getLabel().equalsIgnoreCase("TABLE_OWNER")) {
						match.setSourceTableSchema(value);
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_TABLE")) {
						match.setSourceTableName(value);
					} else if ( bean.getLabel().equalsIgnoreCase("PK_COLUMN")) {
						SQLIndex idx = new SQLIndex();
						idx.setName(value);
						match.setSourceTableIndex(idx);
					} else if ( bean.getLabel().equalsIgnoreCase("FILTER")) {
						match.setFilter(value);
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_LAST_RUN_DATE")) {
						if (value != null && value.length() > 0 ) {
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							Date date = df.parse(value);
							match.getMatchSettings().setLastRunDate(date);
						}
					} else if ( bean.getLabel().equalsIgnoreCase("AUTO_MATCH_THRESHOLD")) {
						match.getMatchSettings().setAutoMatchThreshold(MyShort.valueOf(value));
					} else if ( bean.getLabel().equalsIgnoreCase("MERGE_DESC")) {
						match.getMergeSettings().setDescription(value);
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_LOG_FILE_NAME")) {
						match.getMatchSettings().setLog(new File(value));
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_APPEND_TO_LOG_IND")) {
						match.getMatchSettings().setAppendToLog((value).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_PROCESS_CNT")) {
						match.getMatchSettings().setProcessCount(MyInt.parseInt(value));
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_SHOW_PROGRESS_FREQ")) {
						match.getMatchSettings().setShowProgressFreq(MyLong.parseLong(value));
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_DEBUG_MODE_IND")) {
						match.getMatchSettings().setDebug((value).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_ROLLBACK_SEGMENT_NAME")) {
						match.getMatchSettings().setRollbackSegmentName(value);
					} else if ( bean.getLabel().equalsIgnoreCase("MERGE_LOG_FILE_NAME")) {
						match.getMergeSettings().setLog(new File(value));
					} else if ( bean.getLabel().equalsIgnoreCase("MERGE_APPEND_TO_LOG_IND")) {
						match.getMergeSettings().setAppendToLog((value).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("MERGE_PROCESS_CNT")) {
						match.getMergeSettings().setProcessCount(MyInt.parseInt(value));
					} else if ( bean.getLabel().equalsIgnoreCase("MERGE_SHOW_PROGRESS_FREQ")) {
						match.getMergeSettings().setShowProgressFreq(MyLong.parseLong(value));
					} else if ( bean.getLabel().equalsIgnoreCase("MERGE_DEBUG_MODE_IND")) {
						match.getMergeSettings().setDebug((value).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("MERGE_ROLLBACK_SEGMENT_NAME")) {
						match.getMergeSettings().setRollbackSegmentName(value);
					} else if ( bean.getLabel().equalsIgnoreCase("MERGE_AUGMENT_NULL_IND")) {
						match.getMergeSettings().setAugmentNull((value).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("MERGE_LAST_RUN_DATE")) {
						if (value != null && value.length() > 0 ) {
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							Date date = df.parse(value);
							match.getMergeSettings().setLastRunDate(date);
							match.getMergeSettings().setDebug((value).equalsIgnoreCase("y"));
						}
					} else if ( bean.getLabel().equalsIgnoreCase("SELECT_CLAUSE")) {
						match.getView().setSelect(value);
					} else if ( bean.getLabel().equalsIgnoreCase("FROM_CLAUSE")) {
						match.getView().setFrom(value);
					} else if ( bean.getLabel().equalsIgnoreCase("WHERE_CLAUSE")) {
						match.getView().setWhere(value);
					} else if ( bean.getLabel().equalsIgnoreCase("RESULTS_TABLE")) {
						match.setResultTableName(value);
					} else if ( bean.getLabel().equalsIgnoreCase("RESULTS_TABLE_OWNER")) {
						match.setResultTableSchema(value);
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_BREAK_IND")) {
						match.getMatchSettings().setBreakUpMatch((value).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_TYPE")) {
						match.setType(MatchMode.FIND_DUPES);
					} else if ( bean.getLabel().equalsIgnoreCase("MERGE_TABLES_BACKUP_IND")) {
						match.getMergeSettings().setBackUp((value).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("LAST_BACKUP_NO")) {
						match.getMatchSettings().setLastBackupNo(MyLong.parseLong(value));
					} else if ( bean.getLabel().equalsIgnoreCase("TRUNCATE_CAND_DUP_IND")) {
						match.getMatchSettings().setTruncateCandDupe((value).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_SEND_EMAIL_IND")) {
						match.getMatchSettings().setSendEmail((value).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("MERGE_SEND_EMAIL_IND")) {
						match.getMergeSettings().setSendEmail((value).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("XREF_OWNER")) {
						match.setXrefTableSchema(value);
					} else if ( bean.getLabel().equalsIgnoreCase("XREF_TABLE_NAME")) {
						match.setXrefTableName(value);
					} else if ( bean.getLabel().equalsIgnoreCase("XREF_CATALOG")) {
						match.setXrefTableCatalog(value);
					} else if ( bean.getLabel().equalsIgnoreCase("TABLE_CATALOG")) {
						match.setSourceTableCatalog(value);
					} else if ( bean.getLabel().equalsIgnoreCase("RESULTS_TABLE_CATALOG")) {
						match.setResultTableCatalog(value);
					}

				}
			} else if ( mmo instanceof MatchMakerCriteriaGroup ) {
				MatchMakerCriteriaGroup group = (MatchMakerCriteriaGroup) mmo;
				for ( LabelValueBean bean : properties ) {
					logger.debug("setting:["+bean.getLabel()+"] to ["+ bean.getValue()+"]");
					if ( bean.getLabel().equalsIgnoreCase("DESCRIPTION")) {
						group.setDesc((String)bean.getValue());
					} else if ( bean.getLabel().equalsIgnoreCase("GROUP_ID")) {
						group.setName((String)bean.getValue());
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_PERCENT")) {
						group.setMatchPercent(MyShort.valueOf((String) bean.getValue()));
					} else if ( bean.getLabel().equalsIgnoreCase("FILTER_CRITERIA")) {
						group.setFilter((String) bean.getValue());
					} else if ( bean.getLabel().equalsIgnoreCase("ACTIVE_IND")) {
						group.setActive(((String) bean.getValue()).equalsIgnoreCase("y"));
					}
				}
System.out.println("\n\n***\nadding group:"+group.getName());
				parentMatch.addMatchCriteriaGroup(group);
			} else if ( mmo instanceof MatchMakerCriteria ) {
				MatchMakerCriteria criteria = (MatchMakerCriteria) mmo;
				String groupName = null;
				String criteriaName = null;
				for ( LabelValueBean bean : properties ) {
					if ( bean.getLabel().equalsIgnoreCase("GROUP_ID")) {
						groupName = (String) bean.getValue();
					} else if ( bean.getLabel().equalsIgnoreCase("COLUMN_NAME")) {
						criteriaName = (String) bean.getValue();
					}
				}
				if (groupName == null) {
					throw new ParseException(
							"Group ID is missing from the match criteria [" +
							criteriaName +
							"]",
							0);
				}
				MatchMakerCriteriaGroup group = parentMatch.getMatchCriteriaGroupByName(groupName);
				if (group == null ) {
					throw new ParseException(
							"Group ID [" +
							groupName +
							"] is missing from the match [" +
							parentMatch.getName() +
							"]",
							0);
				}
				group.addChild(criteria);
				criteria.setParent(group);

				for ( LabelValueBean bean : properties ) {
					logger.debug("setting:["+bean.getLabel()+"] to ["+ bean.getValue()+"]");
					if ( bean.getLabel().equalsIgnoreCase("ALLOW_NULL_IND")) {
						criteria.setAllowNullInd(((String) bean.getValue()).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("CASE_SENSITIVE_IND")) {
						criteria.setCaseSensitiveInd(((String) bean.getValue()).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("FIRST_N_CHAR_BY_WORD_IND")) {
						criteria.setFirstNCharByWordInd(((String) bean.getValue()).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_END")) {
						criteria.setMatchEnd(((String) bean.getValue()).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_FIRST_PLUS_ONE_IND")) {
						criteria.setMatchFirstPlusOneInd(((String) bean.getValue()).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("MATCH_START")) {
						criteria.setMatchStart(((String) bean.getValue()).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("REORDER_IND")) {
						criteria.setReorderInd(((String) bean.getValue()).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("COUNT_WORDS_IND")) {
						criteria.setCountWordsInd(((String) bean.getValue()).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("SOUND_IND")) {
						criteria.setSoundInd(((String) bean.getValue()).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("COLUMN_NAME")) {
						criteria.setName((String) bean.getValue());
						criteria.setColumnName((String) bean.getValue());
					} else if ( bean.getLabel().equalsIgnoreCase("REMOVE_SPECIAL_CHARS")) {
						criteria.setRemoveSpecialChars(((String) bean.getValue()).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("FIRST_N_CHAR")) {
						if ( bean.getValue() != null && ((String) bean.getValue()).length() > 0 ) {
							criteria.setFirstNChar(MyLong.parseLong((String) bean.getValue()));
						}
					} else if ( bean.getLabel().equalsIgnoreCase("FIRST_N_CHAR_BY_WORD")) {
						criteria.setFirstNCharByWord(MyLong.parseLong((String) bean.getValue()));
					} else if ( bean.getLabel().equalsIgnoreCase("MIN_WORDS_IN_COMMON")) {
						criteria.setMinWordsInCommon(MyLong.parseLong((String) bean.getValue()));
					} else if ( bean.getLabel().equalsIgnoreCase("SEQ_NO")) {
						Long x = MyLong.parseLong((String) bean.getValue());
						if ( x != null ) {
							criteria.setSeqNo(new BigDecimal(x));
						}
					} else if ( bean.getLabel().equalsIgnoreCase("REPLACE_WITH_SPACE")) {
						criteria.setReplaceWithSpace((String) bean.getValue());
					} else if ( bean.getLabel().equalsIgnoreCase("REPLACE_WITH_SPACE_IND")) {
						criteria.setReplaceWithSpaceInd(((String) bean.getValue()).equalsIgnoreCase("y"));
					} else if ( bean.getLabel().equalsIgnoreCase("SUPPRESS_CHAR")) {
						criteria.setSuppressChar((String) bean.getValue());
					} else if ( bean.getLabel().equalsIgnoreCase("WORDS_IN_COMMON_NUM_WORDS")) {
						criteria.setWordsInCommonNumWords(MyLong.parseLong((String) bean.getValue()));
					} else if ( bean.getLabel().equalsIgnoreCase("VARIANCE_TYPE")) {
						criteria.setVarianceType((String) bean.getValue());
					} else if ( bean.getLabel().equalsIgnoreCase("VARIANCE_AMT")) {
						Long x = MyLong.parseLong((String) bean.getValue());
						if ( x != null ) {
							criteria.setVarianceAmt(new BigDecimal(x));
						}
					}
				}
			} else if ( mmo instanceof PlFolder ) {
				PlFolder folder = (PlFolder) mmo;
				folder.addChild(parentMatch);
				for ( LabelValueBean bean : properties ) {
					logger.debug("setting:["+bean.getLabel()+"] to ["+ bean.getValue()+"]");
					if ( bean.getLabel().equalsIgnoreCase("FOLDER_NAME")) {
						folder.setName((String)bean.getValue());
					} else if ( bean.getLabel().equalsIgnoreCase("FOLDER_DESC")) {
						folder.setFolderDesc((String)bean.getValue());
					}
				}
			}
		}
	}

	private static class MyLong {
		public static Long parseLong(String s) {
			if ( s != null && s.length() > 0 && !s.equalsIgnoreCase("null") ) {
				return Long.parseLong(s);
			}
			return null;
		}
	}

	private static class MyInt {
		public static Integer parseInt(String s) {
			if ( s != null && s.length() > 0 && !s.equalsIgnoreCase("null") ) {
				return Integer.parseInt(s);
			}
			return null;
		}
	}

	private static class MyShort {
		public static Short valueOf(String s) {
			if ( s != null && s.length() > 0 && !s.equalsIgnoreCase("null") ) {
				return Short.parseShort(s);
			}
			return null;
		}

	}
}
