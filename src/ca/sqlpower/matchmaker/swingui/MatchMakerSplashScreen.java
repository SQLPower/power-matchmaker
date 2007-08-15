package ca.sqlpower.matchmaker.swingui;

import java.awt.Font;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MatchMakerSplashScreen {

    private static final Logger logger = Logger.getLogger(MatchMakerSplashScreen.class);

	private JPanel splashScreen;
	private MatchMakerSession session;
	public MatchMakerSplashScreen(MatchMakerSession session) {
		this.session = session;
		buildUI();
	}

	private void buildUI() {

		JLabel spgLogo = new JLabel(new ImageIcon(getClass().getResource("/icons/sqlpower_transparent.png")));
		JLabel mmLogo = new JLabel(new ImageIcon(getClass().getResource("/icons/matchmaker_huge.png")));
		JLabel welcome  = new JLabel("<html>" + "Power*MatchMaker " + MatchMakerSessionContext.APP_VERSION + "</html>");
		Font f = welcome.getFont();
		Font newf = new Font(f.getName(),f.getStyle(),f.getSize()*2);
		welcome.setFont(newf);
        
        StringBuilder summary = new StringBuilder();
        summary.append("<html><table><tr>");
        summary.append("<td>Database:</td><td>").append(session.getDatabase().getName()).append("</td>");
        summary.append("</tr><tr>");
        summary.append("<td>Database User Name:</td><td>").append(session.getDBUser()).append("</td>");
        summary.append("</tr><tr>");
        
        Connection con = null;
        try {
            con = session.getConnection();
            DatabaseMetaData dbmd = con.getMetaData();
            summary.append("<td>Database Product Name:</td><td>").append(dbmd.getDatabaseProductName()).append("</td>");
            summary.append("</tr><tr>");
            summary.append("<td>Database Product Version:</td><td>").append(dbmd.getDatabaseProductVersion()).append("</td>");
            summary.append("</tr><tr>");
            summary.append("<td>Database Driver Name:</td><td>").append(dbmd.getDriverName()).append("</td>");
            summary.append("</tr><tr>");
            summary.append("<td>Database Driver Version:</td><td>").append(dbmd.getDriverVersion()).append("</td>");
        } catch (SQLException e) {
            logger.error("Couldn't get database metadata!", e);
            summary.append("<td colspan=2>Database information not available: ")
                    .append(e.getMessage()).append("</td>");
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                logger.warn("Couldn't close connection", ex);
            }
        }
        
        summary.append("</tr><tr>");
        summary.append("<td>Power*Loader Schema Version:</td><td>").append(session.getPLSchemaVersion()).append("</td>");
        summary.append("</tr></table></html>");
        JLabel summaryLabel = new JLabel(summary.toString());
        
        JLabel sqlpower =
			new JLabel("<html><div align='center'>SQL Power Group Inc.<br>http://www.sqlpower.ca/</div></html>");

        FormLayout layout = new FormLayout(
				"4dlu, pref, 4dlu, fill:100:grow, 4dlu, fill:100:grow, 4dlu ");
		int rowCount =0;
		PanelBuilder pb = new PanelBuilder(layout);
		CellConstraints c = new CellConstraints();
		pb.appendRow(new RowSpec("10dlu"));
		rowCount++;
		pb.appendRow(new RowSpec("pref"));
		rowCount++;
		pb.add(spgLogo, c.xyw(2, rowCount, 3));
		spgLogo.setHorizontalAlignment(JLabel.CENTER);
		pb.add(mmLogo, c.xyw(6, rowCount, 1));
		mmLogo.setHorizontalAlignment(JLabel.CENTER);
		pb.appendRow(new RowSpec("10dlu"));
		rowCount++;
		pb.appendRow(new RowSpec("pref"));
		rowCount++;
		pb.add(welcome, c.xyw(2, rowCount, 5));
		welcome.setHorizontalAlignment(JLabel.CENTER);
		pb.appendRow(new RowSpec("10dlu"));
		rowCount++;
		pb.appendRow(new RowSpec("fill:pref"));
		rowCount++;
		pb.add(summaryLabel,c.xyw(2, rowCount, 3));
		pb.appendRow(new RowSpec("4dlu"));
        rowCount++;

		pb.appendRow(new RowSpec("pref:grow"));
		rowCount++;
		pb.add(sqlpower, c.xyw(2, rowCount, 5));
		sqlpower.setHorizontalAlignment(JLabel.CENTER);
		sqlpower.setVerticalAlignment(JLabel.BOTTOM);
		splashScreen = pb.getPanel();

	}

	public JPanel getSplashScreen() {
		return splashScreen;
	}
}
