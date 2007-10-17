/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.playground;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class StupidDialogTest {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				new StupidDialogTest();
			}
		});
	}
	
	public StupidDialogTest() {
		final JFrame f = new JFrame();
		
		JPanel p = new JPanel();
		p.add(new JButton(new AbstractAction("Show Dialog"){
			public void actionPerformed(ActionEvent e) {
				showDialog(f);
			}
		}));
		
		f.setSize(new Dimension(200,200));
		f.setContentPane(p);
		f.setVisible(true);
	}
	
	public void showDialog(JFrame owner) {
		final JDialog dialog = new JDialog(owner, "Choose Process Type");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		Action cancelAction = new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		};
        
		Panel p = new Panel(new FlowLayout());

		p.add (new JTextField(30));
		p.add(new JButton(cancelAction));
		
		dialog.setContentPane(p);
	    dialog.pack();
		dialog.setVisible(true);
		
	}
}
