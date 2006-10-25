package ca.sqlpower.matchmaker.hibernate;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

/**
 * There is no point in testing the methods that delegate
 * to the PropertyChangeListener, since (a) they seem to have
 * been generated mechanically and (b) we can probably assume
 * that PropertyChangeListener works correctly.
 * <p>
 * Instead, concentrate on testing the other methods.
 */
public class DefaultHibernateObjectTest extends TestCase {

	DefaultHibernateObject target = new DefaultHibernateObject() {

		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}

		@Override
		public int hashCode() {
			return pcs.hashCode() * 3;
		}
	};

	public void testChildren() {
		assertEquals(target.getChildren(),  Collections.emptyList());
		assertEquals(0, target.getChildCount());
	}

	public void testHierarchicals() {
		PropertyChangeListener mock1 = new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println(evt);
			}

		};
		PropertyChangeListener mock2 = new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println(evt);
			}

		};
		target.addHierarchicalChangeListener(mock1);
		assertEquals(1, target.hierachicalListeners.size());
		List<PropertyChangeListener> all = new ArrayList<PropertyChangeListener>();
		all.add(mock1);
		all.add(mock2);
		target.addAllHierarchicalChangeListeners(all);
		assertEquals(2, target.hierachicalListeners.size());
//		int i = 0;
//		for (PropertyChangeListener chl : all) {
//			assertSame(chl, target.getHierarchicalChangeListeners().get(i++));
//		}

		target.removeHierarchicalChangeListener(mock1);
		assertEquals(1, target.hierachicalListeners.size());

		target.removeAllHierarchicalChangeListeners(all);
		assertEquals(0, target.getHierarchicalChangeListeners().size());
	}
}
