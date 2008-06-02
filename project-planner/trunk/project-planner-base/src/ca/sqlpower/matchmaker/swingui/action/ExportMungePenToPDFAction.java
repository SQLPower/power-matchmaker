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
package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerVersion;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.WorkflowEditor;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.MonitorableImpl;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This action will export the currently selected munge pen to a 
 * user defined PDF file. Before the export starts the user will be
 * prompted to specify a file to export to.
 * 
 * Note: A munge pen must be selected before running this action
 * otherwise the munge pen will not exist.
 */
public class ExportMungePenToPDFAction extends ProgressAction {
    private static final Logger logger = Logger.getLogger(ExportMungePenToPDFAction.class);

    /**
     * A key to denote which file we are exporting to.
     */
    private static final String FILE_KEY = "FILE_KEY";
    
    /**
     * The padding around the PDF export.
     */
    private static int OUTSIDE_PADDING = 10; 
    
    /**
     * This is the paintComponent method on the JComponent
     * class retrieved through reflection. If this parameter
     * is null then the {@link #paintComponent(JComponent, Graphics)}
     * method has not been called yet to initialize this variable.
     */
	private static Method paintComponent;
	
	/**
     * This is the paintBorder method on the JComponent
     * class retrieved through reflection. If this parameter
     * is null then the {@link #paintBorder(JComponent, Graphics)}
     * method has not been called yet to initialize this variable.
     */
	private static Method paintBorder;
    
    public ExportMungePenToPDFAction(MatchMakerSwingSession session) {
        super(session, "Export Munge Pen to PDF", "Export Munge Pen to PDF");

        try {
        	paintComponent = JComponent.class.getDeclaredMethod("paintComponent", Graphics.class);
        	paintComponent.setAccessible(true);
        } catch (SecurityException e) {
        	e.printStackTrace();
        	throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
        	e.printStackTrace();
        	throw new RuntimeException(e);
        }

        try {
        	paintBorder = JComponent.class.getDeclaredMethod("paintBorder", Graphics.class);
        	paintBorder.setAccessible(true);
        } catch (SecurityException e) {
        	e.printStackTrace();
        	throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
        	e.printStackTrace();
        	throw new RuntimeException(e);
        }
    }

    /**
     *  When an action is performed on this it pops up the save dialog
     *  and requests a file to save to. When it gets that it draws the
     *  munge pen to a PDF file on a seperate thread.
     */
    public boolean setup(MonitorableImpl monitor, Map<String,Object> properties) {
        monitor.setStarted(true);
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(SPSUtils.PDF_FILE_FILTER);
        if (!(session.getOldPane() instanceof WorkflowEditor)) {
        	JOptionPane.showMessageDialog(session.getFrame(), "We only allow PDF exports of the munge pen at current.", "Cannot Export MungePen", JOptionPane.WARNING_MESSAGE);
        	return false;
        }
        monitor.setJobSize(((WorkflowEditor) session.getOldPane()).getMungePen().getComponentCount());
        
        File file = null;
        while (true) {
            int response = chooser.showSaveDialog(session.getFrame());

            if (response != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            file = chooser.getSelectedFile();
            String fileName = file.getName();

            if (!fileName.endsWith(".pdf")) {
                file = new File(file.getPath()+".pdf");
            }

            if (file.exists()) {
                response = JOptionPane.showConfirmDialog(
                        null,
                        "The file\n" + file.getPath() + "\nalready exists. Do you want to overwrite it?",
                        "File Exists", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    break;
                }
            } else {
                break;
            }
        }

        logger.debug("Saving to file: "+file.getName()+" (" +file.getPath()+")");
    
        properties.put(FILE_KEY,file);
        
        return true;
    }
    
    @Override
    public void cleanUp(MonitorableImpl monitor) {
        // TODO might have to cleanup here
    }

    @Override
    public void doStuff(MonitorableImpl monitor, Map<String, Object> properties) {
        if (!(session.getOldPane() instanceof WorkflowEditor)) {
        	JOptionPane.showMessageDialog(session.getFrame(), "We only allow PDF exports of the munge pen at current.", "Cannot Export MungePen", JOptionPane.WARNING_MESSAGE);
        	return;
        }

        MungePen mungePen = ((WorkflowEditor) session.getOldPane()).getMungePen();
        
        /* We translate the graphics to (OUTSIDE_PADDING, OUTSIDE_PADDING) 
         * so nothing is drawn right on the edge of the document. So
         * we multiply by 2 so we can accomodate the translate and ensure
         * nothing gets drawn outside of the document size.
         */
        final int width = mungePen.getBounds().width + 2*OUTSIDE_PADDING;
        final int height = mungePen.getBounds().height + 2*OUTSIDE_PADDING;
        final Rectangle ppSize = new Rectangle(width, height);
        
        OutputStream out = null;
        Document d = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream((File)properties.get(FILE_KEY)));
            d = new Document(ppSize);
            
            d.addTitle("Project Planner Munge Pen PDF Export");
            d.addAuthor(System.getProperty("user.name"));
            d.addCreator("Project Planner version "+MatchMakerVersion.APP_VERSION);
            
            PdfWriter writer = PdfWriter.getInstance(d, out);
            d.open();
            PdfContentByte cb = writer.getDirectContent();
            Graphics2D g = cb.createGraphicsShapes(width, height);
            // ensure a margin
            g.translate(OUTSIDE_PADDING, OUTSIDE_PADDING);
            
            mungePen.paintComponent(g);

            int j = 0;
            //paint each component individually to show progress
            for (int i = mungePen.getComponentCount() - 1; i >= 0; i--) {
            	JComponent mpc = (JComponent) mungePen.getComponent(i);
            	
            	//set text and foreground as paintComponent
            	//does not normally do this
            	g.setColor(mpc.getForeground());
            	g.setFont(mpc.getFont());
                
                logger.debug("Printing " + mpc.getName() + " to PDF");
                paintComponentAndChildren(mpc, g);
                
                monitor.setProgress(j);
                j++;
            }
            g.dispose();
        } catch (Exception ex) {
            SPSUtils.showExceptionDialogNoReport(session.getFrame(), 
                    "Could not export the munge pen", 
                    ex);
        } finally {
            if (d != null) {
                try {
                    d.close();
                } catch (Exception ex) {
                    SPSUtils.showExceptionDialogNoReport(session.getFrame(),
                            "Could not close document for exporting munge pen", 
                            ex);
                }
            }
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException ex) {
                    SPSUtils.showExceptionDialogNoReport(session.getFrame(),
                        "Could not close pdf file for exporting munge pen", 
                        ex);
                }
            }
        }
    }
    
    /**
	 * This will paint the given component and all of its children. Painting of
	 * the child components will be done with a recursive call to this method
	 * and painting will be done using reflection to call paintComponent.
	 * <p>
	 * NOTE: This method will not paint any children that are not subclasses
	 * of JComponent.
	 */
    private void paintComponentAndChildren(JComponent jc, Graphics g) {
    	
    	g.translate(jc.getLocation().x, jc.getLocation().y);
    	if (logger.isDebugEnabled()) {
    		g.drawRect(0, 0, jc.getWidth(), jc.getHeight());
    	}
    	
    	paintComponent(jc, g);
    	paintBorder(jc, g);
    	logger.debug("Painting " + jc.getName() + " at location " + jc.getLocation() + " with dimensions " + jc.getWidth() + ", " + jc.getHeight());
    	for (Component child : jc.getComponents()){
    		if (child instanceof JComponent) {
    			paintComponentAndChildren((JComponent) child, g.create());
    		}
    	}
    	logger.debug("Finished Painting " + jc.getName() + " at location " + jc.getLocation() + " with dimensions " + jc.getWidth() + ", " + jc.getHeight());
       	g.translate(-jc.getLocation().x, -jc.getLocation().y);
    }

    @Override
    public String getDialogMessage() {
        return "Creating PDF";
    }
    
    @Override
    public String getButtonText() {
        return "Run in Background";
    }
    
    /**
     * This method calls the JComponent's paintComponent method
     * through reflection. This way we can paint any specific
     * component we need to a graphics object. To increase performance
     * the paintComponent method is only retrieved once and stored
     * locally.
     */
    public static void paintComponent(JComponent jc, Graphics g) {
    	try {
			paintComponent.invoke(jc, g);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
    
    /**
     * This method calls the JComponent's paintBorder method
     * through reflection. This way we can paint any specific
     * component we need to a graphics object. To increase performance
     * the paintComponent method is only retrieved once and stored
     * locally.
     */
    public static void paintBorder(JComponent jc, Graphics g) {
    	try {
			paintBorder.invoke(jc, g);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
}
