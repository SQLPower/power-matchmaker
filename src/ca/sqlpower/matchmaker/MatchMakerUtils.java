/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sqlobject.SQLDatabase;

/**
 * A collection of static methods that help with common operations
 * in the core MatchMaker API.
 */
public class MatchMakerUtils {

	/**
	 * The name of the table that will group the keys of source table entries
	 * into groups that make up graphs. This will be stored in Derby in the .mm
	 * directory.
	 */
	public static final String GRAPH_TABLE_NAME = "validate_graph_table";
	
	/**
	 * The column name for the graph table that groups source key entries into
	 * a graph.
	 */
	public static final String GRAPH_ID_COL_NAME = "graph_id";
	
	/**
	 * The column name prefix for primary key columns in the graph table.
	 * The key columns together define the source entries that make up a graph.
	 */
	public static final String PK_KEY_PREFIX = "key_";
	
	private static final Logger logger = Logger.getLogger(MatchMakerUtils.class);

    /**
     * You can't make instances of this class.
     */
	private MatchMakerUtils() {
	}

	/**
	 * Adds the given listener to the given root MatchMakerObject and each
	 * MatchMakerObject descendant reachable from it.
	 * @param <T> The type of the root object
	 * @param <C> The type of children the root contains
	 * @param listener The listener that should receive MatchMakerEvents from
	 * <tt>root</tt> and its descendants.
	 * @param root The root object to add listener to.  Doesn't necessarily
	 * have to be the real ultimate root of the hierarchy (it can have ancestor
	 * nodes; they simply won't be listened to)
	 */
	public static <T extends MatchMakerObject, C extends MatchMakerObject>
		void listenToHierarchy(MatchMakerListener<T,C> listener, MatchMakerObject<T,C> root) {
		root.addMatchMakerListener(listener);
		logger.debug("listenToHierarchy: \"" + root.getName() + "\" (" +
				root.getClass().getName() + ") children: " + root.getChildren());
		for (MatchMakerObject<T,C> obj : root.getChildren()) {
			listenToHierarchy(listener, obj);
		}
	}
	
	/**
	 * This method is similar to listenToHierarchy but only listens to the 
	 * first two levels in the tree, i.e. the listener is not added to the 
	 * grand children of the root. See 
	 * {@link #lisenToHierachy(MatchMakerListener<T,C> listener, MatchMakerObject<T,C> root)}
	 */
	public static <T extends MatchMakerObject, C extends MatchMakerObject>
		void listenToShallowHierarchy(MatchMakerListener<T,C> listener, MatchMakerObject<T,C> root) {
		root.addMatchMakerListener(listener);
		logger.debug("listenToShallowHierarchy: \"" + root.getName() + "\" (" +
				root.getClass().getName() + ") children: " + root.getChildren());
		for (MatchMakerObject<T,C> obj : root.getChildren()) {
			obj.addMatchMakerListener(listener);
		}
	}

	/**
	 * Removes the given listener from the given root MatchMakerObject and each
	 * MatchMakerObject descendant reachable from it.
	 * @param <T> The type of the root object
	 * @param <C> The type of children the root contains
	 * @param listener The listener that should no longer receive MatchMakerEvents from
	 * <tt>root</tt> and its descendants.
	 * @param root The root object to remove listener from.  Doesn't necessarily
	 * have to be the real ultimate root of the hierarchy (it can have ancestor
	 * nodes; they simply won't be unlistened to)
	 */
	public static <T extends MatchMakerObject, C extends MatchMakerObject>
		void unlistenToHierarchy(MatchMakerListener<T,C> listener, MatchMakerObject<T,C> root) {
		root.removeMatchMakerListener(listener);
		for (MatchMakerObject<T,C> obj : root.getChildren()) {
			unlistenToHierarchy(listener, obj);
		}
	}

	/**
	 * Creates a Derby database in the .mm directory in the user's home folder
	 * to store the graph table. The database name is based on the project's
	 * name which is sufficient since no two projects can have the same name.
	 * 
	 * @param session
	 *            Used to retrieve the data source collection and data source
	 *            types.
	 * @param project
	 *            Used to name the table.
	 */
	public static SQLDatabase createProjectGraphDataSource(MatchMakerSession session, Project project) {
		JDBCDataSource ds = new JDBCDataSource(session.getContext().getPlDotIni());
        ds.setName(project.getName() + "'s graph database");
        ds.setUser("");
        ds.setPass("");
        ds.setUrl("jdbc:derby:"+ makeGraphDBLocation(project) + ";create=true");

        // find Derby parent type
        JDBCDataSourceType derbyType = null;
        for (JDBCDataSourceType type : session.getContext().getPlDotIni().getDataSourceTypes()) {
        	if (type.getName() != null && type.getName().startsWith("Derby")) {
        		derbyType = type;
        		break;
        	}
        }
        if (derbyType == null) {
        	throw new RuntimeException("Derby database type is missing in pl.ini. " +
        			"Please create a database type that starts with Derby.");
        }
        ds.setParentType(derbyType);
        SQLDatabase db = new SQLDatabase(ds);
		return db;
	}
	
	public static String makeGraphDBLocation(Project project) {
		return System.getProperty("user.home")+"/.mm/" + project.getParent().getName() + "/" + project.getName();
	}
}
