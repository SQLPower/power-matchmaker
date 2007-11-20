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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.DateToStringMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the component for a date to string munge step. It has three options,
 * combo boxes for the format, date style and time style.
 */
public class DateToStringMungeComponent extends AbstractMungeComponent {

	private JComboBox format;
	private JComboBox dateStyle;
	private JComboBox timeStyle;
	
	public DateToStringMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler, session);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel(new FormLayout(
				"4dlu,pref,4dlu,pref,4dlu",
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu"));
		final DateToStringMungeStep temp = (DateToStringMungeStep) getStep();
		final String[] FORMATS = DateToStringMungeStep.FORMATS.toArray(new String[]{});
		final String[] STYLES = DateToStringMungeStep.STYLES.toArray(new String[]{});
		
		format = new JComboBox(FORMATS);
		format.setSelectedItem(temp.getFormat());
		format.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				temp.setFormat((String) e.getItem());
			}
		});
		dateStyle = new JComboBox(STYLES);
		dateStyle.setSelectedItem(temp.getDateStyle());
		dateStyle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				temp.setDateStyle((String) e.getItem());
			}
		});
		timeStyle = new JComboBox(STYLES);
		timeStyle.setSelectedItem(temp.getTimeStyle());
		timeStyle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				temp.setTimeStyle((String) e.getItem());
			}
		});
		
		CellConstraints cc = new CellConstraints();
		int row = 2;
		content.add(new JLabel("Format: "), cc.xy(2, row));
		content.add(format, cc.xy(4, row));
		row += 2;
		content.add(new JLabel("Date Style: "), cc.xy(2, row));
		content.add(dateStyle, cc.xy(4, row));
		row += 2;
		content.add(new JLabel("Time Style: "), cc.xy(2, row));
		content.add(timeStyle, cc.xy(4, row));
		
		return content;
	}
}
