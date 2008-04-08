/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.swingui.LabelPane;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;

/**
 * This class is used to add a new Label to the MungePen.
 */
public class AddLabelAction extends AbstractAction {
	
	/**
	 * This is the MungePen related to this action.
	 */
	private MungePen mp;
	
	/**
	 * This is the point at which the user clicked in the munge pen.
	 */
	private Point p;

	public AddLabelAction(MungePen mp, Point p) {
		this.mp=mp;
		this.p = p;
	}
	
	/**
	 * When we add the Label, we have to make sure it is on a lower Layer on the 
	 * Munge pen such that it does not interfere with the other MungeComponents
	 */
	public void actionPerformed(ActionEvent arg0) {
		LabelPane label = new LabelPane(mp, new Color(0xc5, 0xdd, 0xf8).darker(), p);
		mp.add(label);
		mp.setLayer(label, mp.findHighestLabelLayer());
		mp.getLabels().add(label);
	}

}
