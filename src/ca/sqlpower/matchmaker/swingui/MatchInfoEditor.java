package ca.sqlpower.matchmaker.swingui;

import java.awt.Font;
import java.awt.HeadlessException;
import java.text.DateFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.DateFormatAllowsNull;
import ca.sqlpower.matchmaker.Match;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The MatchInfoEditor is used to display information about a match such
 * as its ID, type and history. This is not editable, so it extends {@link NoEditEditorPane}
 */
public class MatchInfoEditor extends NoEditEditorPane {

	private static final Logger logger = Logger.getLogger(MatchInfoEditor.class);
	private Match match;

	public MatchInfoEditor(Match match) throws HeadlessException {
		super(null);
		this.match = match;
		super.setPanel(buildUI());
	}

	/**
	 * Returns a panel that displays all of the audit information that we have
	 * about the parent match.
	 */
	private JPanel buildUI() {

		DateFormat df = new DateFormatAllowsNull();

		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:pref:grow, 4dlu ", // columns
				"10dlu,  pref,4dlu,pref,4dlu,pref,4dlu,pref, 12dlu,   pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref, 12dlu,    pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,10dlu"); // rows

		PanelBuilder pb;

		JPanel panel = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, panel);
		CellConstraints cc = new CellConstraints();

		pb.add(new JLabel("Match ID:"), cc.xy(2,2,"r,c"));
		pb.add(new JLabel("Folder:"), cc.xy(2,4,"r,c"));
		pb.add(new JLabel("Description:"), cc.xy(2,6,"r,t"));
		pb.add(new JLabel("Type:"), cc.xy(2,8,"r,c"));

		String folderName = null;

		 if ( match.getParent() != null) {
   			folderName = match.getParent().getName();
		}

		pb.add(new JLabel(match.getName()), cc.xy(4,2));
		pb.add(new JLabel(folderName), cc.xy(4,4));
		JTextArea descriptionText = new JTextArea(match.getMatchSettings().getDescription(), 3, 3);
		descriptionText.setEditable(false);
		pb.add(new JScrollPane(descriptionText), cc.xy(4,6,"f,f"));
		pb.add(new JLabel(match.getType().toString()), cc.xy(4,8));

		pb.add(new JLabel("Logged on As:"), cc.xy(2,10,"r,c"));
		pb.add(new JLabel("Last Updated Date:"), cc.xy(2,12,"r,c"));
		pb.add(new JLabel("Last Updated User:"), cc.xy(2,14,"r,c"));
		pb.add(new JLabel("Last Run Date:"), cc.xy(2,16,"r,c"));

		pb.add(new JLabel(match.getName()), cc.xy(4,10));
		pb.add(new JLabel(df.format(match.getLastUpdateDate())), cc.xy(4,12,"f,f"));
		pb.add(new JLabel(match.getLastUpdateAppUser()), cc.xy(4,14));
		pb.add(new JLabel(df.format(match.getMatchSettings().getLastRunDate())), cc.xy(4,16,"f,f"));

		JLabel checkout = new JLabel("Checkout Information");
		Font f = checkout.getFont();
		f = f.deriveFont(Font.BOLD,f.getSize()+2);
		checkout.setFont(f);
		pb.add(checkout, cc.xy(2,20,"l,c"));

		pb.add(new JLabel("Checked out date:"), cc.xy(2,22,"r,c"));
		pb.add(new JLabel("Checked out user:"), cc.xy(2,24,"r,c"));
		pb.add(new JLabel("Checked out osuser:"), cc.xy(2,26,"r,c"));
		
		return pb.getPanel();
	}
}