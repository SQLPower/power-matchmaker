package ca.sqlpower.matchmaker.swingui;

import java.awt.Font;
import java.awt.HeadlessException;
import java.text.DateFormat;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.DateFormatAllowsNull;
import ca.sqlpower.architect.swingui.ArchitectPanel;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MatchInfoPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(MatchInfoPanel.class);
	private PlMatch match;
	private JPanel panel;

	public MatchInfoPanel(PlMatch match) throws HeadlessException {
		this.match = match;
		buildUI();
	}

	private void buildUI() {

		DateFormat df = new DateFormatAllowsNull();

		FormLayout layout = new FormLayout(
				"4dlu,80dlu,4dlu,fill:200dlu:grow, 4dlu ", // columns
				"10dlu,  12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu, 12dlu,   12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu, 12dlu,    12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,12dlu,4dlu,10dlu"); // rows

		PanelBuilder pb;

		panel = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, panel);
		CellConstraints cc = new CellConstraints();

		pb.add(new JLabel("Match ID:"), cc.xy(2,2,"r,c"));
		pb.add(new JLabel("Folder:"), cc.xy(2,4,"r,c"));
		pb.add(new JLabel("Description:"), cc.xy(2,6,"r,t"));
		pb.add(new JLabel("Type:"), cc.xy(2,8,"r,c"));

		String folderName = null;
		if ( match.getFolder() != null) {
			PlFolder f = (PlFolder) match.getFolder();
    		if ( f != null ) {
    			folderName = f.getFolderName();
    		}
		}

		pb.add(new JLabel(match.getMatchId()), cc.xy(4,2));
		pb.add(new JLabel(folderName), cc.xy(4,4));
		pb.add(new JLabel(match.getMatchDesc()), cc.xy(4,6,"f,f"));
		pb.add(new JLabel(match.getMatchType()), cc.xy(4,8));

		pb.add(new JLabel("Logged on As:"), cc.xy(2,10,"r,c"));
		pb.add(new JLabel("Created:"), cc.xy(2,12,"r,c"));
		pb.add(new JLabel("Last Updated Date:"), cc.xy(2,14,"r,c"));
		pb.add(new JLabel("Last Updated User:"), cc.xy(2,16,"r,c"));
		pb.add(new JLabel("Last Run Date:"), cc.xy(2,18,"r,c"));

		pb.add(new JLabel(match.getMatchId()), cc.xy(4,10));
		pb.add(new JLabel(df.format(match.getCreateDate())), cc.xy(4,12));
		pb.add(new JLabel(df.format(match.getLastUpdateDate())), cc.xy(4,14,"f,f"));
		pb.add(new JLabel(match.getLastUpdateUser()), cc.xy(4,16));
		pb.add(new JLabel(df.format(match.getLastUpdateDate())), cc.xy(4,18,"f,f"));

		JLabel checkout = new JLabel("Checkout Information");
		Font f = checkout.getFont();
		f = f.deriveFont(Font.BOLD,f.getSize()+2);
		checkout.setFont(f);
		pb.add(checkout, cc.xy(2,20,"l,c"));

		pb.add(new JLabel("Checked out date:"), cc.xy(2,22,"r,c"));
		pb.add(new JLabel("Checked out user:"), cc.xy(2,24,"r,c"));
		pb.add(new JLabel("Checked out osuser:"), cc.xy(2,26,"r,c"));

		pb.add(new JLabel(df.format(match.getCheckedOutDate())), cc.xy(4,22));
		pb.add(new JLabel(match.getCheckedOutUser()), cc.xy(4,24));
		pb.add(new JLabel(match.getCheckedOutOsUser()), cc.xy(4,26,"f,f"));
	}

	public static void main(String args[]) throws ArchitectException {

		PlMatch plMatch = new PlMatch();
		plMatch.setMatchId("TEST MATCH ID");
		plMatch.setMatchType("Test Match type");
		MatchInfoPanel p = new MatchInfoPanel(plMatch);
		MatchMakerFrame f = MatchMakerFrame.getMainInstance();
		final JDialog d = ArchitectPanelBuilder.createSingleButtonArchitectPanelDialog(
				p,f,"Audit Information","OK");

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	d.pack();
		    	d.setVisible(true);
		    }
		});
	}

	public boolean applyChanges() {
		// we don't care
		return false;
	}

	public void discardChanges() {
		// we don't care

	}

	public JComponent getPanel() {
		return panel;
	}


}
