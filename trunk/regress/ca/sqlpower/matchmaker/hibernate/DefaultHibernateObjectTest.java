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

	boolean fired1 = false, fired2 = false;
	public void testHierarchicals() {
		PropertyChangeListener mock1 = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("Mock1: " + evt);
				fired1 = true;
			}
			public String toString() { return "mock1"; }
		};
		PropertyChangeListener mock2 = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("Mock2: " + evt);
				fired2 = true;
			}
			public String toString() { return "mock2"; }
		};
		target.addHierarchicalChangeListener(mock1);
		assertEquals(1, target.hierachicalListeners.size());
		List<PropertyChangeListener> all = new ArrayList<PropertyChangeListener>();
		all.add(mock1);
		all.add(mock2);
		target.addAllHierarchicalChangeListeners(all);
		assertEquals(2, target.hierachicalListeners.size());

		System.out.println("List 1");
		List<PropertyChangeListener> hchl = target.getHierarchicalChangeListeners();
		for (PropertyChangeListener chl : hchl) {
			System.out.println(chl);
		}

		target.removeHierarchicalChangeListener(mock1);
		assertEquals(1, target.hierachicalListeners.size());

		System.out.println("List 2");
		List<PropertyChangeListener> hchl2 = target.getHierarchicalChangeListeners();
		for (PropertyChangeListener chl : hchl2) {
			System.out.println(chl);
		}

		target.firePropertyChange(new PropertyChangeEvent(this, "test", "old", "new"));
		assertTrue(fired2);
		assertFalse(fired1);

		target.removeAllHierarchicalChangeListeners(all);
		assertEquals(0, target.getHierarchicalChangeListeners().size());
	}
	@Override
	public String toString() {
		return "DefaultHibernateTest";
	}
}
