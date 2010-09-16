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

/**
 * A collection of static methods that help with common operations
 * in the core MatchMaker API.
 */
public class MatchMakerUtils {
	
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
		void listenToHierarchy(MatchMakerListener<T,C> listener, MatchMakerObject root) {
		root.addMatchMakerListener(listener);
		logger.debug("listenToHierarchy: \"" + root.getName() + "\" (" +
				root.getClass().getName() + ") children: " + root.getChildren());
		for (MatchMakerObject obj : root.getChildren()) {
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
		void listenToShallowHierarchy(MatchMakerListener<T,C> listener, MatchMakerObject root) {
		root.addMatchMakerListener(listener);
		logger.debug("listenToShallowHierarchy: \"" + root.getName() + "\" (" +
				root.getClass().getName() + ") children: " + root.getChildren());
		for (MatchMakerObject obj : root.getChildren()) {
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
		void unlistenToHierarchy(MatchMakerListener listener, MatchMakerObject root) {
		root.removeMatchMakerListener(listener);
		for (MatchMakerObject obj : root.getChildren()) {
			unlistenToHierarchy(listener, obj);
		}
	}

}
