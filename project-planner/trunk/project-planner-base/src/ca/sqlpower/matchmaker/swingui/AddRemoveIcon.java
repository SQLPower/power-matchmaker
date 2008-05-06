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

package ca.sqlpower.matchmaker.swingui;

/*
 * Copyright (c) 2007, Jonathan Fuerth
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Jonathan Fuerth nor the names of other
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Created on Jan 11, 2007
 *
 * This code belongs to Jonathan Fuerth
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.Icon;

public class AddRemoveIcon implements Icon {

   public static enum Type {
       ADD, REMOVE;
   }

   /**
    * This icon's type (add or remove).
    */
   private final Type type;

   /**
    * This icon's width and height in pixels.
    */
   private final int size = 8;

   /**
    * The width of a stroke (the horizontal and/or vertical line this
    * icon draws) in pixels.
    */
   private final float strokeWidth = 1.999f;

   public AddRemoveIcon(Type type) {
       this.type = type;
   }

   public int getIconHeight() {
       return size;
   }

   public int getIconWidth() {
       return size;
   }

   /**
    * Paints a "+" or "-" symbol, depending on this icon's type.
    */
   public void paintIcon(Component c, Graphics g, int x, int y) {
       float xf = x;
       float yf = y;
       Graphics2D g2 = (Graphics2D) g.create();
       g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
RenderingHints.VALUE_FRACTIONALMETRICS_ON);
       g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
RenderingHints.VALUE_ANTIALIAS_ON);
       g2.setColor(Color.BLACK);
       g2.setStroke(new BasicStroke(strokeWidth,
BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
       Line2D horiz = new Line2D.Float(
               xf,      yf+(size/2f),
               xf+size, yf+(size/2f));
       g2.draw(horiz);
       if (type == Type.ADD) {
           Line2D vert = new Line2D.Float(
                   xf+(size/2f), yf,
                   xf+(size/2f), yf+size);
           g2.draw(vert);
       }
       g2.dispose();
   }

}