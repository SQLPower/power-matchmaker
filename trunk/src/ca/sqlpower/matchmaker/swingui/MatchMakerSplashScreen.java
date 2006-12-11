package ca.sqlpower.matchmaker.swingui;

import java.awt.Font;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.matchmaker.MatchMakerSession;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MatchMakerSplashScreen {

	private JPanel splashScreen;
	private MatchMakerSession session;
	public MatchMakerSplashScreen(MatchMakerSession session) throws SQLException {
		this.session = session;
		buildUI();
	}
	
	private void buildUI() throws SQLException{
		
		JLabel logo = new JLabel(new ImageIcon(getClass().getResource("/icons/matchmaker_huge.png")));
		JLabel welcome  = new JLabel("<html>" + "Power*MatchMaker" + "</html>");
		Font f = welcome.getFont();
		Font newf = new Font(f.getName(),f.getStyle(),f.getSize()*2);
		welcome.setFont(newf);
		final Connection con = session.getConnection();
		final DatabaseMetaData dbmd = con.getMetaData();
		
		JLabel databaseLabel = new JLabel("Database:");
		databaseLabel.setVerticalAlignment(JLabel.TOP);
		JLabel databaseInfo = new JLabel("<html>" + session.getDatabase().getName()+"</html>");
		JLabel dbUserLabel = new JLabel("Database Username:");
		dbUserLabel.setVerticalAlignment(JLabel.TOP);
		JLabel dbUserInfo = new JLabel("<html>" +session.getDBUser()+"</html>");
		JLabel sqlpower = new JLabel("<html><div align='center'>SQL Power Group Inc.<br>http://www.sqlpower.ca</div></html>");
		JLabel dbProdNameLabel = new JLabel("Database product name: ");
		dbProdNameLabel.setVerticalAlignment(JLabel.TOP);
		JLabel dbProdNameInfo = new JLabel("<html>" +dbmd.getDatabaseProductName()+"</html>");
		JLabel dbProdVersionLabel = new JLabel("Database product version: ");
		dbProdVersionLabel.setVerticalAlignment(JLabel.TOP);
		JLabel dbProdVersionInfo = new JLabel("<html>" +dbmd.getDatabaseProductVersion()+"</html>");
		JLabel dbDriverNameLabel = new JLabel("Database driver name: ");
		dbDriverNameLabel.setVerticalAlignment(JLabel.TOP);
		JLabel dbDriverNameInfo = new JLabel("<html>" +dbmd.getDriverName()+"</html>");
		JLabel dbDriverVersionLabel = new JLabel("Database driver version: ");
		dbDriverVersionLabel.setVerticalAlignment(JLabel.TOP);
		JLabel dbDriverVersionInfo = new JLabel("<html>" +dbmd.getDriverVersion()+"</html>");
        JLabel plSchemaVersionLabel = new JLabel("Power*Loader Schema Version: ");
        JLabel plSchemaVersionInfo = new JLabel("<html>" +session.getPLSchemaVersion()+"</html>");
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:100:grow, 4dlu ");
		int rowCount =0;
		PanelBuilder pb = new PanelBuilder(layout);
		CellConstraints c = new CellConstraints();
		CellConstraints c2 = new CellConstraints();
		pb.appendRow(new RowSpec("10dlu"));
		rowCount++;
		pb.appendRow(new RowSpec("pref"));
		rowCount++;
		pb.add(logo, c.xyw(2, rowCount, 3));
		logo.setHorizontalAlignment(JLabel.CENTER);
		pb.appendRow(new RowSpec("10dlu"));
		rowCount++;
		pb.appendRow(new RowSpec("pref"));
		rowCount++;
		pb.add(welcome, c.xyw(2, rowCount, 3));
		welcome.setHorizontalAlignment(JLabel.CENTER);
		pb.appendRow(new RowSpec("10dlu"));
		rowCount++;
		pb.appendRow(new RowSpec("fill:pref"));
		rowCount++;
		pb.add(databaseLabel,c.xy(2, rowCount),databaseInfo,c2.xy(4, rowCount));
		pb.appendRow(new RowSpec("4dlu"));
		rowCount++;
		pb.appendRow(new RowSpec("fill:pref"));
		rowCount++;
		pb.add(dbUserLabel,c.xy(2, rowCount),dbUserInfo,c2.xy(4, rowCount));
		pb.appendRow(new RowSpec("4dlu"));
		rowCount++;
		pb.appendRow(new RowSpec("fill:pref"));
		rowCount++;
		pb.add(dbProdNameLabel,c.xy(2, rowCount),dbProdNameInfo,c2.xy(4, rowCount));
		pb.appendRow(new RowSpec("4dlu"));
		rowCount++;
		pb.appendRow(new RowSpec("fill:pref"));
		rowCount++;
		pb.add(dbProdVersionLabel,c.xy(2, rowCount),dbProdVersionInfo,c2.xy(4, rowCount));
		pb.appendRow(new RowSpec("4dlu"));
		rowCount++;
        
		pb.appendRow(new RowSpec("fill:pref"));
		rowCount++;
		pb.add(dbDriverNameLabel,c.xy(2, rowCount),dbDriverNameInfo,c2.xy(4, rowCount));
        pb.appendRow(new RowSpec("4dlu"));
		rowCount++;
        
		pb.appendRow(new RowSpec("fill:pref"));
		rowCount++;
 		pb.add(dbDriverVersionLabel,c.xy(2, rowCount),dbDriverVersionInfo,c2.xy(4, rowCount));
		pb.appendRow(new RowSpec("4dlu"));
		rowCount++;
        
        pb.appendRow(new RowSpec("fill:pref"));
        rowCount++;
        pb.add(plSchemaVersionLabel,c.xy(2, rowCount), plSchemaVersionInfo,c2.xy(4, rowCount));
        pb.appendRow(new RowSpec("4dlu"));
        rowCount++;
        
		pb.appendRow(new RowSpec("pref:grow"));
		rowCount++;
		pb.add(sqlpower, c.xyw(2, rowCount, 3));
		sqlpower.setHorizontalAlignment(JLabel.CENTER);
		sqlpower.setVerticalAlignment(JLabel.BOTTOM);
		splashScreen = pb.getPanel();
		
	}

	public JPanel getSplashScreen() {
		return splashScreen;
	}
}
