package ca.sqlpower.matchmaker;

import ca.sqlpower.matchmaker.event.MatchMakerListener;

public class MatchMakerUtils {

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
		for (MatchMakerObject<T,C> obj : root.getChildren()) {
			listenToHierarchy(listener, obj);
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

}
