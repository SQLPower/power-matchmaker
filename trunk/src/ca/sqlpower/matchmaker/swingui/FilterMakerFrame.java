package ca.sqlpower.matchmaker.swingui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FilterMakerFrame extends JFrame {

	private static Logger logger = Logger.getLogger(FilterMakerFrame.class);

	private JComboBox duplicate1;
	private JComboBox duplicate2;
	private JComboBox comparisonOperator;
	private JButton pasteButton;
	private JButton undoButton;
	private JButton andButton;
	private JButton orButton;
	private JButton notButton;
	private JButton testButton;
	private JButton helpButton;
	private JButton okButton;
	private JButton cancelButton;
	private JTextArea filterText;


	public FilterMakerFrame(JPanel parent){		
        buildUI();                
		setTitle("Filter:");        
	}

	public void buildUI(){

		FormLayout layout = new FormLayout(
				"4dlu,fill:min(70dlu;default), 4dlu, fill:150dlu:grow,4dlu, min(60dlu;default),4dlu",
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,20dlu,4dlu,fill:30dlu:grow,10dlu,pref,10dlu");

		CellConstraints cc = new CellConstraints();

		PanelBuilder pb;
		JPanel p = logger.isDebugEnabled()  ? new FormDebugPanel(layout) : new JPanel(layout);

		pb = new PanelBuilder(layout,p);

		duplicate1 = new JComboBox();
		duplicate2 = new JComboBox();
		comparisonOperator = new JComboBox();
		pasteButton = new JButton(pasteAction);
		undoButton = new JButton(undoAction);
		andButton = new JButton(andAction);
		andButton.setSize(new Dimension(1,1));
		orButton = new JButton (orAction);
		orButton.setSize(new Dimension(1,1));
		notButton = new JButton(notAction);
		notButton.setSize(new Dimension(1,1));
		testButton = new JButton(testAction);
		helpButton = new JButton (helpAction);
		okButton = new JButton(okAction);
		cancelButton = new JButton(cancelAction);
		filterText = new JTextArea();

		pb.add(new JLabel("Duplicate1:"), cc.xy(2,2,"l,c"));
		pb.add(duplicate1, cc.xy(4,2,"f,c"));
		pb.add(new JLabel("Comparison Operator:"), cc.xy(2,4,"l,c"));
		pb.add(comparisonOperator, cc.xy(4,4));
		pb.add(new JLabel("Duplicate2:"), cc.xy(2,6,"l,c"));
		pb.add(duplicate2, cc.xy(4,6));

		pb.add(pasteButton, cc.xy(6,2,"r,c"));
		pb.add(undoButton, cc.xy(6,4,"r,c"));

		ButtonBarBuilder syntaxBar = new ButtonBarBuilder();
		syntaxBar.addGridded(andButton);
		syntaxBar.addRelatedGap();
		syntaxBar.addGridded(orButton);
		syntaxBar.addRelatedGap();
		syntaxBar.addGridded(notButton);
		syntaxBar.addRelatedGap();

		pb.add(syntaxBar.getPanel(), cc.xyw(2,8,3));
		pb.add(new JScrollPane(filterText), cc.xyw(2,10,5, "f,f"));

		ButtonBarBuilder bottomButtons = new ButtonBarBuilder();

		bottomButtons.addGridded(testButton);
		bottomButtons.addRelatedGap();		
		bottomButtons.addGridded(helpButton);
		bottomButtons.addRelatedGap();
		bottomButtons.addGlue();
		bottomButtons.addGridded(okButton);
		bottomButtons.addRelatedGap();
		bottomButtons.addGlue();
		bottomButtons.addGridded(cancelButton);
		bottomButtons.addRelatedGap();
		bottomButtons.addGlue();

		pb.add(bottomButtons.getPanel(), cc.xyw(2,12,5,"f,f"));
	
		
		setupOperatorDropdown();

		getContentPane().add(pb.getPanel());
	}
    public static void main(String[] args) {

        final JFrame f = new FilterMakerFrame(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                f.pack();
                f.setVisible(true);
            }
        });
    }
    /*
     * Adds the operators into the setupOperator Dropdown
     */
    private void setupOperatorDropdown(){
        comparisonOperator.addItem(" = ");
        comparisonOperator.addItem(" like ");
        comparisonOperator.addItem(" in ");
        comparisonOperator.addItem(" is null ");
    }
    

    ////////////// The Action Variables//////////////////

	private Action pasteAction = new AbstractAction("Paste"){

		public void actionPerformed(ActionEvent e) {
		}
	};

	private Action undoAction = new AbstractAction("Undo"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

	};

	private Action andAction = new AbstractAction("AND"){

		public void actionPerformed(ActionEvent e) {
			filterText.append(" AND ");
			// TODO Auto-generated method stub
		}

	};

	private Action notAction = new AbstractAction("NOT"){

		public void actionPerformed(ActionEvent e) {
            filterText.append(" NOT ");
			// TODO Auto-generated method stub
		}
	};

	private Action orAction = new AbstractAction("OR"){

		public void actionPerformed(ActionEvent e) {
		    filterText.append(" OR ");
			// TODO Auto-generated method stub
		}
	};

	private Action testAction = new AbstractAction("Test"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
		}
	};

	private Action helpAction = new AbstractAction("Help"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
		}
	};

	private Action okAction = new AbstractAction("OK"){

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
		}
	};
	private Action cancelAction = new AbstractAction("Cancel"){

		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	};

}
