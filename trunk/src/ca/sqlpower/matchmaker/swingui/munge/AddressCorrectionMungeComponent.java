/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.munge.AddressCorrectionMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.BrowserUtil;
import ca.sqlpower.validation.swingui.FormValidationHandler;

public class AddressCorrectionMungeComponent extends AbstractMungeComponent {
	
	private static Logger logger = Logger.getLogger(AddressCorrectionMungeComponent.class);
	private String addressDataURL;
	private JLabel addressDataRequired;
	
	private MouseAdapter HyperlinkTextSelectedListener;

	private JButton showAllButton;
	private JButton hideAllButton;
	
	private PreferenceChangeListener preferenceListener = new PreferenceChangeListener() {
		public void preferenceChange(PreferenceChangeEvent evt) {
			if (MatchMakerSessionContext.ADDRESS_CORRECTION_DATA_PATH.equals(evt.getKey())) {
				AddressCorrectionMungeStep step = (AddressCorrectionMungeStep) getStep();
				step.validateDatabase();
				if (step.doesDatabaseExist()) {
					addressDataRequired.setVisible(false);
				} else {
					addressDataRequired.setVisible(true);
				}
			}
		}
	};
	private final MatchMakerSession session;
	
	public AddressCorrectionMungeComponent(MungeStep step,
			FormValidationHandler handler, MatchMakerSession s) {
		super(step, handler, s);
		this.session = s;
		s.getContext().addPreferenceChangeListener(preferenceListener);
	}

	@Override
	protected JPanel buildUI() {
		if (addressDataRequired != null) {
			addressDataRequired
					.removeMouseListener(HyperlinkTextSelectedListener);
		}

		if (HyperlinkTextSelectedListener == null) {
			HyperlinkTextSelectedListener = new MouseAdapter() {

				public void mouseEntered(MouseEvent e) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}

				public void mousePressed(MouseEvent e) {
					try {
						BrowserUtil.launch(addressDataURL);
					} catch (IOException e1) {
						SPSUtils.showExceptionDialogNoReport(
								AddressCorrectionMungeComponent.this,
								"Invalid URL", e1);
					}
				}

				public void mouseExited(MouseEvent e) {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			};
		}

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(
				"center:pref:grow", "fill:pref, pref"));

		addressDataURL = "http://www.sqlpower.ca/page/matchmaker_address_data";
		addressDataRequired = new JLabel(
				"<html> <p align =\"center\">You cannot continue without having a valid address database to compare against!<br> Please set the path to your address database in the User Preferences... <br> To purchase one, click on the following link:<br> <a href=\""
						+ addressDataURL
						+ "\">http://www.sqlpower.ca/page/matchmaker_address_data</a><html>");
		addressDataRequired.addMouseListener(HyperlinkTextSelectedListener);

		builder.add(addressDataRequired);
		builder.nextLine();
		addressDataRequired.setVisible(false);

		if (!((AddressCorrectionMungeStep) getStep()).doesDatabaseExist()) {
			addressDataRequired.setVisible(true);
		}

		showAllButton = new JButton(new HideShowAllLabelsAction("Show All",
				true, true, true));
		hideAllButton = new JButton(new HideShowAllLabelsAction("Hide All",
				true, true, false));
		JPanel content = new JPanel(new FlowLayout());
		content.add(showAllButton);
		content.add(hideAllButton);

		builder.append(content);

		setOutputShowNames(true);
		setInputShowNames(true);

		return builder.getPanel();
	}
	
	@Override
	protected void remove() {
		super.remove();
		session.getContext().removePreferenceChangeListener(preferenceListener);
		addressDataRequired.removeMouseListener(HyperlinkTextSelectedListener);
	}

}
