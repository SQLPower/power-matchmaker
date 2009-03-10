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

package ca.sqlpower.matchmaker.swingui.address;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class AddressLabelBorderFactory {

	private CompoundBorder border;
	public CompoundBorder generateAddressLabelBorder(Color color, int thickness, final int inset, boolean rounded, EmptyBorder emptyBorder) {
		
		LineBorder lineBorder = new LineBorder(color, thickness, true) {
			private int inset1 = inset;
			@Override
			public Insets getBorderInsets(Component c) {
				return new Insets(inset1,inset1,inset1,inset1);
			}
			@Override
			public Insets getBorderInsets(Component c, Insets insets) {
				insets.top = insets.bottom = insets.left = insets.right = this.inset1 ;
				return insets;
			}
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		        Color oldColor = g.getColor();
		        int i;

		        g.setColor(lineColor);
		        for(i = 0; i < thickness; i++)  {
		        	g.drawRoundRect(x+i, y+i, width-i-i-1, height-i-i-1, 10*thickness, 10*thickness);
		        }
		        g.setColor(oldColor);
		    }
		};
		return border = new CompoundBorder(emptyBorder, lineBorder);
	}

}