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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 * This test creates a simple JFrame with a JTextArea in it to make 
 * sure the undo and redo button works on the JTextArea.
 * 
 * <p>This is not a JUnit test because JUnit tests for visible swing
 * components don't work very well.  You have to run this test manually
 * and try it for yourself.
 */
public class TestJTextAreaUndoWrapper {
    
    public static void main(String[] args) {
        JTextArea textArea = new JTextArea(24,56);
        JTextAreaUndoWrapper target = new JTextAreaUndoWrapper(textArea);
        final JFrame frame = new JFrame();
        frame.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println(frame.getSize());
            }
        });
        frame.setContentPane(target);
        frame.pack();
        frame.setVisible(true);   
        System.out.println(target.getMinimumSize());
    }
}
