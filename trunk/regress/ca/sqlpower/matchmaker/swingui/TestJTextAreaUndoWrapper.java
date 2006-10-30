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
