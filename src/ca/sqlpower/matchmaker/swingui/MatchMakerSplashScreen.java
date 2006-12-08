package ca.sqlpower.matchmaker.swingui;

import java.awt.Font;

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
	public MatchMakerSplashScreen(MatchMakerSession session) {
		this.session = session;
		buildUI();
	}
	
	private void buildUI(){
		
		JLabel logo = new JLabel(new ImageIcon(getClass().getResource("/icons/matchmaker_huge.png")));
		JLabel welcome  = new JLabel("Welcome, "+session.getAppUser()+ ", to the Power*MatchMaker");
		Font f = welcome.getFont();
		Font newf = new Font(f.getName(),f.getStyle(),f.getSize()*2);
		welcome.setFont(newf);
		
		JLabel databaseLabel = new JLabel("Database:");
		JLabel databaseInfo = new JLabel(session.getDatabase().getName());
		JLabel dbUserLabel = new JLabel("Database Username:");
		JLabel dbUserInfo = new JLabel(session.getDBUser());
		JLabel sqlpower = new JLabel("<html><div align='center'>SQL Power Group Inc.<br>http://www.sqlpower.ca</div></html>");
		
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:pref:grow, 4dlu ");
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
		pb.appendRow(new RowSpec("pref"));
		rowCount++;
		pb.add(databaseLabel,c.xy(2, rowCount),databaseInfo,c2.xy(4, rowCount));
		pb.appendRow(new RowSpec("4dlu"));
		rowCount++;
		pb.appendRow(new RowSpec("pref"));
		rowCount++;
		pb.add(dbUserLabel,c.xy(2, rowCount),dbUserInfo,c2.xy(4, rowCount));
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
