/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.dao;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.dao.xml.ProjectDAOXML;


/**
 * Not actually timed for now; this class passes on reads and writes as a
 * workaround for the old Hibernate implementation.
 */
public class TimedGeneralDAO implements MatchMakerDAO {
	
	private MatchMakerSession session;
	
	public Timer delayedSave;
	
	private ProjectDAOXML converter;
	
	private Class DAOtype;
	
	// XXX: Testing only, not likely to work for other users
	private final String FILENAME = "/Users/Joe/Documents/out.html";
	
	/**
	 * The amount of time that passes between the most recent save call and the
	 * actual save.
	 */
	@SuppressWarnings("unused")
	private final long SAVE_DELAY = 20000;
	
	class Save extends TimerTask {

		@Override
		public void run() {
			// TODO: Make this save things
		}
		
	}
	
	public TimedGeneralDAO(Class typeDAO) {
		DAOtype = typeDAO;
	}
	
	@Override
	public void delete(Object deleteMe) {
		/*
		 * while (x != -1) {
            System.out.print((char) x);
            x = in.read();
            }
		 */
	}

	@Override
	public List findAll() {
		/*InputStream in = null;
		try {
			in = new FileInputStream(FILENAME);
			converter = new ProjectDAOXML(session, in);
			return converter.findAll();
		} catch (IOException e) {
			System.out.println("oops");
		} */
		
		return Collections.emptyList();
	}

	@Override
	public Class getBusinessClass() {
		return null;
	}

	@Override
	public void save(Object saveMe) {
		// TODO: Make this reset when save is called again
		// delayedSave.schedule(new Save(), SAVE_DELAY);
		/*OutputStream out = null;
		try {
			out = new FileOutputStream(FILENAME);
	        converter = new ProjectDAOXML(out);
	        Project p = SQLPowerUtils.getAncestor((SPObject)saveMe, Project.class);
	        converter.save(p);
		} catch (FileNotFoundException e) {
			System.out.println("OS X filesystems do not work that way!");
		} finally {
			if (out != null) try {
				out.close();
			} catch (Exception e) {
				System.out.println("Something has gone double wrong");
				throw new RuntimeException(e);
			}
		}*/
	}

	public Set<String> getProjectNamesUsingResultTable(String name,
			String catalogName, String schemaName, String tableName) {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}

	public MatchMakerTranslateGroup findByOID(Long valueOf) {
		return null;
	}
	
	public MatchMakerTranslateGroup findByUUID(Long valueOf) {
		return null;
	}
	
}
