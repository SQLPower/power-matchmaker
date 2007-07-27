package ca.sqlpower.matchmaker.swingui;

import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.SPSUtils;

public class AboutPanel extends JPanel implements DataEntryPanel {

	public JLabel content;

	public AboutPanel() {
		initComponents();
	}

	public void initComponents() {
		setLayout(new FlowLayout());

        // Include the product's 128x128 Icon
        String realPath = "/icons/matchmaker_128.png";
        java.net.URL imgURL = SPSUtils.class.getResource(realPath);

        if (imgURL != null) {
            ImageIcon imageIcon = new ImageIcon(imgURL, "MatchMaker Logo");
            add(new JLabel(imageIcon));
        } else {
        	System.err.println("ICON IS NULL");
        }
        String message =
            "<html>" +
            "<h1>Power*MatchMaker</h1>" +
            "<p>Version " + MatchMakerSessionContext.APP_VERSION + "</p>" +
            "<p>Copyright 2006 SQL Power Group Inc.</p>" +
            "</html>";
		content = new JLabel(message);
		add(content);
	}

	public boolean applyChanges() {
		return true;
        // nothing to apply
	}

	public void discardChanges() {
        // nothing to discard
	}

	public JPanel getPanel() {
		return this;
	}
}