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

package ca.sqlpower.matchmaker.server;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.dao.ProjectDAO;
import ca.sqlpower.matchmaker.dao.xml.MatchMakerXMLSession;
import ca.sqlpower.matchmaker.dao.xml.MatchmakerXMLSessionContext;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.SwingSessionContext;
import ca.sqlpower.matchmaker.swingui.SwingSessionContextImpl;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;

/**
 * A set of utility methods used by the server from requests of the client.
 */
public class ProjectPlannerServerUtils {
	
	/**
     * The width of all thumbnails generated from the munge pen on a save.
     */
    private static final int THUMBNAIL_WIDTH = 400;
    
    /**
     * The height of all thumbnails generated from the munge pen on a save.
     */
    private static final int THUMBNAIL_HEIGHT = 400;
	
	/**
	 * Generates a small image of the munge pen from the first munge process to
	 * be used as thumbnail. This "image" is in byte array form so to be stored
	 * in the database.
	 * 
	 * @param projectId
	 *            a string representation of the project's id
	 * @param projectXML
	 *            the xml represtation of the project's contents
	 */
	public static BufferedImage generateProjectThumbnail(String projectId, String projectXML)
			throws IOException, ClassNotFoundException, ArchitectException {
		
		MatchmakerXMLSessionContext context = new MatchmakerXMLSessionContext();
		SwingSessionContext sscontext = new SwingSessionContextImpl(Preferences
				.userNodeForPackage(MatchMakerXMLSession.class), context);
		ProjectPlannerServerSession sessionDelegate = new ProjectPlannerServerSession(context);
		MatchMakerSwingSession session = new MatchMakerSwingSession(sscontext,
				sessionDelegate);

		long projectIdLong = Long.parseLong(projectId);

		ProjectDAO projectDAO = (ProjectDAO) session.getDAO(Project.class);
		Project project = new Project();
		project.setOid(projectIdLong);
		project.setSession(sessionDelegate);
		
		ServerIOHandler ioHandler = (ServerIOHandler) sessionDelegate.getIoHandler();
		ioHandler.addProject(projectIdLong, projectXML);
		projectDAO.refresh(project);
		
		BufferedImage imageBuffer;
		if (project.getMungeProcesses().size() == 0) {
			imageBuffer = ImageIO.read(ProjectPlannerServerUtils.class.getResource("/icons/no_thumbnail.png"));
		} else {
			MungePen mungePen = new MungePen(project.getMungeProcesses().get(0),
					null, session);

			// weird hack to get the thumbnail to actually display
			final JFrame f = new JFrame();
			f.add(mungePen);
			f.pack();

			Dimension mungePenArea = mungePen.getUsedArea();
			double scaleFactor = Math.max(THUMBNAIL_WIDTH/mungePenArea.getWidth(), THUMBNAIL_HEIGHT/mungePenArea.getHeight());
			imageBuffer = new BufferedImage((int)(scaleFactor * mungePenArea.getWidth()), (int)(scaleFactor * mungePenArea.getHeight()), BufferedImage.TYPE_INT_RGB);

			Graphics2D g = (Graphics2D)imageBuffer.getGraphics();
			g.scale(scaleFactor, scaleFactor);
			mungePen.paint(g);
			g.dispose();
		}
    	
    	return imageBuffer;
	}
}
