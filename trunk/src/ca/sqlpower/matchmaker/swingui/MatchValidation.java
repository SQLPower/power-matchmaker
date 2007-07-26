package ca.sqlpower.matchmaker.swingui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.RowSet;
import javax.sql.rowset.JoinRowSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.RowSetModel;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.rowset.CachedRowSetImpl;
import com.sun.rowset.JoinRowSetImpl;

public class MatchValidation {

    private static final Logger logger = Logger.getLogger(MatchValidation.class);

    private MatchMakerSwingSession swingSession;
    private Match match;
    private JFrame frame;

    private RowSet fullResult;
    private List<String> allMatchGroup;
    private List<String> allMatchPct;
    private List<BigDecimal> allMatchPctThreshold;
    private List<String> allMatchStatus;

    private final String ALL = "All";
    private SearchAction searchAction;

    private JTable sourceJTable;
    private JTable candidateJTable;
    private JTextArea filterTextArea;

    private CachedRowSetImpl sourceTableRowSet;

    private SQLTable matchSourceTable;
    private SQLIndex pk;
    private ItemListener filterMatchGrpListener;
    private FilterMakerDialog filterPanel;


    private static final int MAX_TABLE_COL_WIDTH= 400;

    /**
     * Changes the candidate JTable according to the selection of source table.
     */
    private ListSelectionListener sourceJTableListener = new ListSelectionListener(){

        private int getColumnByName(JTable table, String name) {
            for ( int i=0; i<table.getColumnCount(); i++ ) {
                if ( table.getColumnName(i).equals(name) )
                    return i;
            }
            return -1;
        }
        public void valueChanged(ListSelectionEvent e) {


            int row = sourceJTable.getSelectedRow();

            if ( row == -1 ) {
                candidateJTable = new JTable();
                return;
            }

            StringBuffer dupCandItems = new StringBuffer();
            StringBuffer dupCandWhereClause = new StringBuffer();
            StringBuffer where = new StringBuffer();
            List <Object> params = new ArrayList<Object>();

            dupCandItems.append("MATCH_PERCENT");
            for ( int i = 0; i<pk.getChildCount(); i++ ) {
            	if ( i > 0 ) {
            		dupCandWhereClause.append(" AND ");
            	}
            	dupCandItems.append(",dup_candidate_").append(i+10);
            	dupCandItems.append(",dup_candidate_").append(i+20);

            	dupCandWhereClause.append("((current_candidate_").append(i+10);
            	dupCandWhereClause.append(" IS NULL AND current_candidate_").append(i+20);
            	dupCandWhereClause.append(" IS NOT NULL)");
            	dupCandWhereClause.append(" OR ");
            	dupCandWhereClause.append("(current_candidate_").append(i+10);
            	dupCandWhereClause.append(" IS NOT NULL AND current_candidate_").append(i+20);
            	dupCandWhereClause.append(" IS NULL)");
            	dupCandWhereClause.append(" OR ");
            	dupCandWhereClause.append("(current_candidate_").append(i+10);
            	dupCandWhereClause.append(" <> current_candidate_").append(i+20).append("))");


            	String columnName = pk.getChild(i).getName();
            	int col = getColumnByName(sourceJTable,columnName);
            	if ( col < 0 ) {
            		SPSUtils.showExceptionDialog(frame,
            				"column "+columnName+" not found",
            				new IllegalStateException("column "+columnName+" not found"), new MatchMakerQFAFactory());
            		return;
            	}
            	if ( sourceJTable.getValueAt(row,col) == null ) {
            		where.append("dup_candidate_").append(i+20).append(" IS NULL");
            	} else {
            		where.append("dup_candidate_").append(i+20).append("=?");
            		params.add(sourceJTable.getValueAt(row,col));
            	}
            }

            StringBuffer sql = new StringBuffer();
            sql.append("SELECT ").append(dupCandItems).append(" FROM ");
            SQLTable resultTable = match.getResultTable();
			sql.append(DDLUtils.toQualifiedName(resultTable.getCatalogName(),
					resultTable.getSchemaName(),
					resultTable.getName()));
            sql.append(" WHERE ").append(where);
            if ( where != null && where.length() > 0 ) {
                sql.append(" AND ");
            }
            sql.append(dupCandWhereClause);

            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs =  null;
            try {
                con = swingSession.getConnection();
                logger.debug("About to execute SQL for result table: "+sql);
                pstmt = con.prepareStatement(sql.toString());
                int i = 1;
                for ( Object p : params ) {
                    pstmt.setObject(i++,p);
                }
                rs = pstmt.executeQuery();
                CachedRowSetImpl crset2 = new CachedRowSetImpl();
                crset2.populate(rs);

                JoinRowSet jrs = new JoinRowSetImpl();

                for ( i = 0; i<pk.getChildCount(); i++ ) {
                    SQLObject col = pk.getChild(i);
                    jrs.addRowSet(sourceTableRowSet,col.getName() );
                    jrs.addRowSet(crset2, "DUP_CANDIDATE_"+(i+10) );
                }

                final RowSetModel model = new RowSetModel(jrs);
                candidateJTable.setModel(new ResultTableModel(model));
                candidateJTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                TableUtils.fitColumnWidths(candidateJTable, MAX_TABLE_COL_WIDTH);

                // FIXME would like to set column widths to match upper table,
                //       but this lower table has two extra columns in front

            } catch (SQLException e1) {
                SPSUtils.showExceptionDialog(frame,
                        "Unknown SQL Error:"+sql.toString(), e1, new MatchMakerQFAFactory());
            } finally {
                try {
                    if ( rs != null )
                        rs.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close result set", e1);
                }
                try {
                    if ( pstmt != null )
                        pstmt.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close statement", e1);
                }
                try {
                    if (con != null)
                        con.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close connection", e1);
                }
            }

        }
    };

    public MatchValidation(MatchMakerSwingSession swingSession, Match match) throws HeadlessException, SQLException {
        frame = new JFrame("Validate Matches: " + match.getName());
        frame.setIconImage(swingSession.getSmallMMIcon().getImage());
        this.swingSession = swingSession;
        this.match = match;
        setup();
        buildUI();
        frame.pack();
        frame.setLocationRelativeTo(swingSession.getFrame());
    }

    /**
     * get a full list of all match group,percent and status
     * @param match
     * @return
     * @throws SQLException
     */
    private RowSet getMatchResult(Match match ) throws SQLException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs =  null;
        try {
            con = swingSession.getConnection();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT * FROM ");
            SQLTable resultTable = match.getResultTable();
            sql.append(DDLUtils.toQualifiedName(resultTable.getCatalogName(),
					resultTable.getSchemaName(),
					resultTable.getName()));
            sql.append(" ORDER BY MATCH_PERCENT DESC");
            pstmt = con.prepareStatement(sql.toString());
            rs = pstmt.executeQuery();
            CachedRowSetImpl crset = new CachedRowSetImpl();
            crset.populate(rs);
            return crset;
        } finally {
            try {
                if ( rs != null )
                    rs.close();
            } catch (SQLException e1) {
                logger.warn("Couldn't close result set", e1);
            }
            try {
                if ( pstmt != null )
                    pstmt.close();
            } catch (SQLException e1) {
                logger.warn("Couldn't close statement", e1);
            }
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e1) {
                logger.warn("Couldn't close connection", e1);
            }
        }
    }

    /**
     * get the source table content
     * @return
     */
    private CachedRowSetImpl getMatchSourceTable(String filter) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs =  null;
        CachedRowSetImpl crset;
        String lastSQL = null;
        try {
            con = swingSession.getConnection();

            StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            for ( int i=0; i<matchSourceTable.getColumns().size(); i++ ) {
                if ( i > 0 ) {
                    sql.append(",");
                }
                sql.append(matchSourceTable.getColumn(i).getName());
            }
            sql.append(" FROM ");
            SQLTable resultTable = match.getSourceTable();
            sql.append(DDLUtils.toQualifiedName(resultTable.getCatalogName(),
					resultTable.getSchemaName(),
					resultTable.getName()));
            if ( filter != null && filter.length() > 0 ) {
                sql.append(" WHERE ").append(filter);
            }

            lastSQL = sql.toString();
            pstmt = con.prepareStatement(lastSQL);
            rs = pstmt.executeQuery();
            crset = new CachedRowSetImpl();
            crset.populate(rs);
            return crset;
        } catch (SQLException e1) {
            crset = null;
            SPSUtils.showExceptionDialog(frame,
                    "SQL Error", "The SQL Statement that caused the error: " + lastSQL,
                    e1, new MatchMakerQFAFactory());
        } finally {
            try {
                if ( rs != null )
                    rs.close();
            } catch (SQLException e1) {
                logger.warn("Couldn't close result set", e1);
            }
            try {
                if ( pstmt != null )
                    pstmt.close();
            } catch (SQLException e1) {
                logger.warn("Couldn't close statement", e1);
            }
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e1) {
                logger.warn("Couldn't close connection", e1);
            }
        }
        return crset;
    }






    private void setup() throws SQLException {
        fullResult = getMatchResult(match);
        allMatchGroup = new ArrayList<String>();
        allMatchPct = new ArrayList<String>();
        allMatchPctThreshold = new ArrayList<BigDecimal>();
        allMatchStatus = new ArrayList<String>();

        allMatchGroup.add(ALL);
        allMatchPct.add(ALL);

        allMatchStatus.add(ALL);
        allMatchStatus.add("AUTO-MATCH");
        allMatchStatus.add("MATCH");
        allMatchStatus.add("NOMATCH");
        allMatchStatus.add("MERGED");
        allMatchStatus.add("unmatched");

        while ( fullResult.next() ) {
            if ( !allMatchGroup.contains(fullResult.getString("GROUP_ID")) ) {
                allMatchGroup.add(fullResult.getString("GROUP_ID"));
            }
            if ( !allMatchPct.contains(fullResult.getBigDecimal("MATCH_PERCENT").toPlainString()) ) {
                allMatchPct.add(fullResult.getBigDecimal("MATCH_PERCENT").toPlainString());
            }
            if ( !allMatchPctThreshold.contains(fullResult.getBigDecimal("MATCH_PERCENT")) ) {
                allMatchPctThreshold.add(fullResult.getBigDecimal("MATCH_PERCENT"));
            }
            if (!allMatchStatus.contains(fullResult.getString("MATCH_STATUS")) ) {
                allMatchStatus.add(fullResult.getString("MATCH_STATUS"));
            }
        }

        sourceJTable = new JTable();
        candidateJTable = new JTable();
        sourceJTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        matchSourceTable = match.getSourceTable();
        pk = match.getSourceTableIndex();
    }

    private void buildUI() {

        JComboBox autoMatchPctComboBox = new JComboBox(new DefaultComboBoxModel(allMatchPctThreshold.toArray()));
        final JComboBox filterMatchPctComboBox = new JComboBox(new DefaultComboBoxModel(allMatchPct.toArray()));
        final JComboBox filterMatchGrpComboBox = new JComboBox(new DefaultComboBoxModel(allMatchGroup.toArray()));
        JComboBox filterMatchStatusComboBox = new JComboBox(new DefaultComboBoxModel(allMatchStatus.toArray()));

        filterMatchGrpListener = new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                if ( e.getSource() == filterMatchGrpComboBox ) {
                    final ItemListener[] itemListeners = filterMatchPctComboBox.getItemListeners();
                    for ( ItemListener i : itemListeners ) {
                        filterMatchPctComboBox.removeItemListener(i);
                    }

                    String gid = (String)filterMatchGrpComboBox.getSelectedItem();
                    if ( gid.equalsIgnoreCase(ALL) ) {
                        filterMatchPctComboBox.setSelectedItem(ALL);
                    } else {
                        for ( MatchMakerCriteriaGroup g : match.getMatchGroups() ) {
                            if ( g.getName().equals(gid) ) {
                                filterMatchPctComboBox.setSelectedItem(String.valueOf(g.getMatchPercent().intValue()));
                            }
                        }
                    }
                    for ( ItemListener i : itemListeners ) {
                        filterMatchPctComboBox.addItemListener(i);
                    }
                } else if ( e.getSource() == filterMatchPctComboBox ) {
                    final ItemListener[] itemListeners = filterMatchGrpComboBox.getItemListeners();
                    for ( ItemListener i : itemListeners ) {
                        filterMatchGrpComboBox.removeItemListener(i);
                    }

                    String pctStr = (String)filterMatchPctComboBox.getSelectedItem();
                    if ( pctStr.equalsIgnoreCase(ALL) ) {
                        filterMatchGrpComboBox.setSelectedItem(ALL);
                    } else {
                        int pct = Integer.valueOf(pctStr);
                        for ( MatchMakerCriteriaGroup g : match.getMatchGroups() ) {
                            if ( g.getMatchPercent() == pct ) {
                                filterMatchGrpComboBox.setSelectedItem(g.getName());
                            }
                        }
                    }
                    for ( ItemListener i : itemListeners ) {
                        filterMatchGrpComboBox.addItemListener(i);
                    }
                }
            }};
            filterMatchGrpComboBox.addItemListener(filterMatchGrpListener);
            filterMatchPctComboBox.addItemListener(filterMatchGrpListener);


            // right hand panel
            FormLayout layout = new FormLayout(
                    "4dlu,pref,4dlu",
                    //   1    2                 3    4             5
                    "20dlu,12dlu,4dlu,12dlu,4dlu,pref,50dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,"+
//                    1     2     3    4     5    6     7     8     9    10    11   12    13   14
            "4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,10dlu,pref,4dlu,80dlu,30dlu,pref,4dlu,pref,4dlu");
//            15   16    17   18    19   20    21   22     23   24    25    26    27   28    29   30
            JPanel rightPanel = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
            PanelBuilder pb = new PanelBuilder(layout, rightPanel );
            CellConstraints cc = new CellConstraints();

            pb.add(new JLabel("Auto-Match Threshold:"), cc.xy(2,2,"l,c"));
            pb.add(autoMatchPctComboBox,cc.xy(2,4,"f,f"));

            ButtonBarBuilder bb = new ButtonBarBuilder();
            bb.addGridded(new JButton(new ApplyAutoMatchThresholdAction(autoMatchPctComboBox)));
            bb.addRelatedGap();
            bb.addGridded(new JButton(new ResetAutoMatchThresholdAction(autoMatchPctComboBox)));
            pb.add(bb.getPanel(), cc.xy(2,6,"l,c"));

            pb.add(new JLabel("How To Search:"), cc.xy(2,8,"l,c"));
            pb.add(new JLabel("Match Percent:"), cc.xy(2,10,"l,c"));
            pb.add(filterMatchPctComboBox,cc.xy(2,12,"f,f"));
            pb.add(new JLabel("Match Group:"), cc.xy(2,14,"l,c"));
            pb.add(filterMatchGrpComboBox,cc.xy(2,16,"f,f"));
            pb.add(new JLabel("Match Status:"), cc.xy(2,18,"l,c"));
            pb.add(filterMatchStatusComboBox,cc.xy(2,20,"f,f"));

            filterTextArea = new JTextArea();
            JScrollPane filterScrollPane = new JScrollPane(filterTextArea);
            filterScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            filterScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            filterTextArea.setWrapStyleWord(true);
            filterTextArea.setLineWrap(true);

            ButtonBarBuilder bb1 = new ButtonBarBuilder();
            bb1.addGridded(new JButton(columnFilterAction));
            bb1.addRelatedGap();
            searchAction = new SearchAction(match,filterMatchGrpComboBox,
                    filterMatchStatusComboBox,filterTextArea,sourceJTable);
            bb1.addGridded(new JButton(searchAction));
            pb.add(bb1.getPanel(), cc.xy(2,22,"c,c"));

            pb.add(filterScrollPane,cc.xy(2,24,"f,f"));

            Action validationStatusAction = new AbstractAction("View Validation Status") {
                public void actionPerformed(ActionEvent e) {
                    MatchValidationStatus p = new MatchValidationStatus(swingSession, match, frame);
                    p.pack();
                    p.setVisible(true);
                }};

                pb.add(new JButton(validationStatusAction), cc.xy(2,26,"c,c"));

                Action exit = new AbstractAction("Exit") {
                    public void actionPerformed(ActionEvent e) {
                        frame.dispose();
                    }};
                    pb.add(new JButton(exit), cc.xy(2,28,"c,c"));



                    layout = new FormLayout(
                            "4dlu,fill:400dlu:grow,4dlu,fill:min(default;150dlu), 4dlu",
                            //1    2                3    4            5
                    "4dlu,12dlu,4dlu,fill:200dlu:grow,4dlu,12dlu,4dlu, 120dlu,4dlu, 12dlu,4dlu");
                    //       1    2     3    4        5    6     7     8      9     10    11    12    13
                    //       sp   Btn   sp   tab      sp   btn   sp    tab    sp    btn   sp
                    JPanel topPanel = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
                    pb = new PanelBuilder(layout, topPanel);
                    cc = new CellConstraints();

                    pb.add(new JScrollPane(sourceJTable), cc.xy(2,4,"f,f"));
                    pb.add(new JScrollPane(candidateJTable), cc.xy(2,8,"f,f"));

                    pb.add(new JButton(upperMasterAction), cc.xy(2,2,"r,c"));
                    pb.add(new JButton(noMatchAction), cc.xy(2,6,"r,c"));
                    pb.add(new JButton(lowerMasterAction), cc.xy(2,10,"r,c"));

                    pb.add(rightPanel, cc.xywh(4,2,1,9,"f,f"));
                    frame.getContentPane().add(pb.getPanel());
                    filterPanel = new FilterMakerDialog(frame,
                            filterTextArea, matchSourceTable, true);
    }

    private class SourceTableModel extends AbstractTableModel {

        TableModel model;
        public SourceTableModel(TableModel model) {
            super();
            this.model = model;
        }
        public int getRowCount() {
            return model.getRowCount();
        }
        public int getColumnCount() {
        	return matchSourceTable.getColumns().size();
        }
        public Object getValueAt(int rowIndex, int columnIndex) {
            return model.getValueAt(rowIndex,columnIndex);
        }
        @Override
        public String getColumnName(int column) {
        	if ( column < matchSourceTable.getColumns().size() ) {
        		return matchSourceTable.getColumn(column).getName();
        	}
        	return super.getColumnName(column);
        }

    }

    private class ResultTableModel extends AbstractTableModel {

        TableModel model;
        public ResultTableModel(TableModel model) {
            super();
            this.model = model;
        }
        public int getRowCount() {
            return model.getRowCount();
        }
        public int getColumnCount() {
            return model.getColumnCount();
        }
        public Object getValueAt(int rowIndex, int columnIndex) {

        	if ( columnIndex == 0 ) {
        		return model.getValueAt(rowIndex,matchSourceTable.getColumns().size());
        	} else if ( columnIndex < pk.getChildCount()+1 ) {
        		return model.getValueAt(rowIndex,matchSourceTable.getColumns().size()+columnIndex);
        	} else {
        		return model.getValueAt(rowIndex,columnIndex-pk.getChildCount()-1);
        	}
        	return null;
        }
        @Override
        public String getColumnName(int column) {
        	if ( column == 0 ) {
        		return "Match Percent";
        	} else if ( column < pk.getChildCount()+1 ) {
        		return "Current Target "+pk.getChild(column-1).getName();
        	} else {
        		return matchSourceTable.getColumn(column-1-pk.getChildCount()).getName();
        	}
        	return super.getColumnName(column);
        }

    }

    private class SearchAction extends AbstractAction {
        private Match match;
        private JComboBox groupCB;
        private JComboBox matchStatusCB;
        private JTextArea columnFilter;
        private JTable output;

        public SearchAction(Match match, JComboBox groupCB, JComboBox matchStatusCB,
                JTextArea columnFilter, JTable output) {
            super("Search");
            this.match = match;
            this.groupCB = groupCB;
            this.matchStatusCB = matchStatusCB;
            this.columnFilter = columnFilter;
            this.output = output;
        }
        public void actionPerformed(ActionEvent e) {

            StringBuffer where = new StringBuffer();
            String gid = (String)groupCB.getSelectedItem();
            List<String>params = new ArrayList<String>();
            if ( gid != null && !gid.equalsIgnoreCase(ALL) ) {
                where.append("GROUP_ID=?");
                params.add(gid);
            }
            String stauts = (String)matchStatusCB.getSelectedItem();
            if ( stauts != null && stauts.equalsIgnoreCase("unmatched") ) {
                if ( where.length() > 0 ) {
                    where.append(" AND ");
                }
                where.append("MATCH_STATUS IS NULL");
            } else if ( stauts != null && !stauts.equalsIgnoreCase(ALL) ) {
                if ( where.length() > 0 ) {
                    where.append(" AND ");
                }
                where.append("MATCH_STATUS=?");
                params.add(stauts);
            }

            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs =  null;
            try {

                con = swingSession.getConnection();

                StringBuffer sql = new StringBuffer();
                StringBuffer dupCandItems = new StringBuffer();
                StringBuffer dupCandWhereClause = new StringBuffer();
                for ( int i = 0; i<pk.getChildCount(); i++ ) {
                    if ( i > 0 ) {
                        dupCandItems.append(",");
                        dupCandWhereClause.append(" AND ");
                    }
                    dupCandItems.append("dup_candidate_").append(i+10);

                    dupCandWhereClause.append("((current_candidate_").append(i+10);
                    dupCandWhereClause.append(" IS NULL AND current_candidate_").append(i+20);
                    dupCandWhereClause.append(" IS NOT NULL)");
                    dupCandWhereClause.append(" OR ");
                    dupCandWhereClause.append("(current_candidate_").append(i+10);
                    dupCandWhereClause.append(" IS NOT NULL AND current_candidate_").append(i+20);
                    dupCandWhereClause.append(" IS NULL)");
                    dupCandWhereClause.append(" OR ");
                    dupCandWhereClause.append("(current_candidate_").append(i+10);
                    dupCandWhereClause.append(" <> current_candidate_").append(i+20).append("))");
                }

                sql.append("SELECT DISTINCT ").append(dupCandItems);
                sql.append(" FROM ");
                SQLTable resultTable = match.getResultTable();
                sql.append(DDLUtils.toQualifiedName(resultTable.getCatalogName(),
    					resultTable.getSchemaName(),
    					resultTable.getName()));
                sql.append(" WHERE ").append(where);
                if ( where != null && where.length() > 0 ) {
                    sql.append(" AND ");
                }
                sql.append(dupCandWhereClause);

                pstmt = con.prepareStatement(sql.toString());
                int i = 1;
                for ( String p : params ) {
                    pstmt.setString(i++,p);
                }
                rs = pstmt.executeQuery();
                CachedRowSetImpl crset2 = new CachedRowSetImpl();
                crset2.populate(rs);
                JoinRowSet jrs = new JoinRowSetImpl();

                CachedRowSetImpl newSourceRowSet = getMatchSourceTable(columnFilter.getText().trim());
                if ( newSourceRowSet != null ) {
                    sourceTableRowSet = newSourceRowSet;
                }

                for ( i = 0; i<pk.getChildCount(); i++ ) {
                    SQLObject col = pk.getChild(i);
                    jrs.addRowSet(sourceTableRowSet,col.getName() );
                    jrs.addRowSet(crset2, "DUP_CANDIDATE_"+(i+10) );
                }


                RowSetModel model = new RowSetModel(jrs);
                output.getSelectionModel().removeListSelectionListener(sourceJTableListener);
                output.setModel(new SourceTableModel(model));
                output.getSelectionModel().addListSelectionListener(sourceJTableListener);
                output.getSelectionModel().setSelectionInterval(0,0);
                TableUtils.fitColumnWidths(output, MAX_TABLE_COL_WIDTH);
            } catch (SQLException e1) {
                SPSUtils.showExceptionDialog(frame,
                        "Unknown SQL Error", e1, new MatchMakerQFAFactory());
            } finally {
                try {
                    if ( rs != null )
                        rs.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close result set", e1);
                }
                try {
                    if ( pstmt != null )
                        pstmt.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close statement", e1);
                }
                try {
                    if (con != null)
                        con.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close connection", e1);
                }
            }


        }

    }

    private class ApplyAutoMatchThresholdAction extends AbstractAction {
        private JComboBox list;

        public ApplyAutoMatchThresholdAction(JComboBox list) {
            super("Apply");
            this.list = list;
        }

        public void actionPerformed(ActionEvent e) {

            BigDecimal pct = (BigDecimal) list.getSelectedItem();
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs =  null;
            try {
                con = swingSession.getConnection();

                StringBuffer sql = new StringBuffer();

                sql.append("UPDATE ");
                SQLTable resultTable = match.getResultTable();
                sql.append(DDLUtils.toQualifiedName(resultTable.getCatalogName(),
    					resultTable.getSchemaName(),
    					resultTable.getName()));
                sql.append(" SET MATCH_STATUS='AUTO-MATCH' WHERE MATCH_PERCENT>= ?");
                pstmt = con.prepareStatement(sql.toString());
                pstmt.setBigDecimal(1,pct);
                int rows = pstmt.executeUpdate();
                searchAction.actionPerformed(null);
                logger.debug("Apply Auto-match @"+pct.intValue()+"   "+rows+" Updated.");
            } catch (SQLException e1) {
                SPSUtils.showExceptionDialog(frame,
                        "Unknown SQL Error", e1, new MatchMakerQFAFactory());
            } finally {
                try {
                    if ( rs != null )
                        rs.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close result set", e1);
                }
                try {
                    if ( pstmt != null )
                        pstmt.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close statement", e1);
                }
                try {
                    if (con != null)
                        con.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close connection", e1);
                }
            }
        }
    }

    private class ResetAutoMatchThresholdAction extends AbstractAction {
        private JComboBox list;

        public ResetAutoMatchThresholdAction(JComboBox list) {
            super("Reset");
            this.list = list;
        }

        public void actionPerformed(ActionEvent e) {

            BigDecimal pct = (BigDecimal) list.getSelectedItem();
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs =  null;
            try {
                con = swingSession.getConnection();

                StringBuffer sql = new StringBuffer();

                sql.append("UPDATE ");
                SQLTable resultTable = match.getResultTable();
                sql.append(DDLUtils.toQualifiedName(resultTable.getCatalogName(),
                		resultTable.getSchemaName(),
                		resultTable.getName()));
                sql.append(" SET MATCH_STATUS=NULL WHERE MATCH_PERCENT>= ?");
                pstmt = con.prepareStatement(sql.toString());
                pstmt.setBigDecimal(1,pct);
                int rows = pstmt.executeUpdate();
                searchAction.actionPerformed(null);
                logger.debug("Reset Auto-match @"+pct.intValue()+"   "+rows+" Updated.");
            } catch (SQLException e1) {
                SPSUtils.showExceptionDialog(frame,
                        "Unknown SQL Error", e1, new MatchMakerQFAFactory());
            } finally {
                try {
                    if ( rs != null )
                        rs.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close result set", e1);
                }
                try {
                    if ( pstmt != null )
                        pstmt.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close statement", e1);
                }
                try {
                    if (con != null)
                        con.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close connection", e1);
                }
            }
        }
    }

    private Action columnFilterAction = new AbstractAction("Column Filter"){
        public void actionPerformed(ActionEvent e) {
            filterPanel.setFilterTextContent(filterTextArea);
            filterPanel.pack();
            filterPanel.setVisible(true);
            filterPanel.setFilterTextContent(filterTextArea);
            filterTextArea.setEditable(false);

        }
    };

    /**
     * Marks the upper selected row as definitely matching the lower selected rows, and as the master
     * (canonical) version of that record.
     */
    private Action upperMasterAction = new AbstractAction("Master") {

        public void actionPerformed(ActionEvent e) {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            String lastSQL = null;
            try {
                con = swingSession.getConnection();

                StringBuilder sql;

                // find the match output table record that corresponds with the top table's selected row
                int upperRowNum = sourceJTable.getSelectedRow();
                Object[] masterKey = new Object[match.getSourceTableIndex().getChildCount()];
                for (int i = 0; i < masterKey.length; i++) {
                    masterKey[i] = sourceJTable.getValueAt(upperRowNum, i);
                }

                // update that record in the match output table to mark it as a master record
                sql = new StringBuilder();
                sql.append("UPDATE " + DDLUtils.toQualifiedName(match.getResultTable()));
                sql.append("\n SET dup1_master_ind='Y'");
                sql.append("\n WHERE");
                for (int i = 0; i < masterKey.length; i++) {
                    if (i != 0) {
                        sql.append(" AND");
                    }
                    sql.append(" dup_candidate_1").append(i);
                    sql.append("=?");
                }
                lastSQL = sql.toString();
                pstmt = con.prepareStatement(lastSQL);
                for (int i = 0; i < masterKey.length; i++) {
                    pstmt.setObject(i+1, masterKey[i]);
                }
                int count = pstmt.executeUpdate();
                logger.debug("Updated "+count+" rows with "+lastSQL);
                pstmt.close();
                pstmt = null;

                // update all records in the bottom table to mark them duplicates of the new master

            } catch (Exception e1) {
                SPSUtils.showExceptionDialog(frame,
                        "Error While setting master record", e1, new MatchMakerQFAFactory());
            } finally {
                try {
                    if ( rs != null )
                        rs.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close result set", e1);
                }
                try {
                    if ( pstmt != null )
                        pstmt.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close statement", e1);
                }
                try {
                    if (con != null)
                        con.close();
                } catch (SQLException e1) {
                    logger.warn("Couldn't close connection", e1);
                }
            }

        }

    };

    /**
     * Marks the lower selected row as definitely matching the upper selected row, and as the master
     * (canonical) version of that record.
     *
     * <p>If there is more than one record selected in the lower table, this action will
     * disable itself. (not implemented yet)
     */
    private Action lowerMasterAction = new AbstractAction("Master") {

        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(frame,
                    "We cant do that yet, Dave");
        }

    };

    /**
     * Marks the pair of selected rows as definitely not matching.
     */
    private Action noMatchAction = new AbstractAction("No Match") {

        public void actionPerformed(ActionEvent e) {
            final int selectedRow = sourceJTable.getSelectedRow();
            if ( selectedRow == -1 )
                return;

            final int selectedRowCand = candidateJTable.getSelectedRow();
            if ( selectedRowCand == -1 )
                return;

        }

    };

    /**
     * Displays the frame and gives it focus.  It is safe to call this
     * method multiple times.
     */
    public void showGUI() {
        frame.setVisible(true);
        frame.requestFocus();
    }
}
