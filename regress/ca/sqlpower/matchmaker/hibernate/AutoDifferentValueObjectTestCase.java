package ca.sqlpower.matchmaker.hibernate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diasparsoftware.util.junit.ValueObjectEqualsTest;

public abstract class AutoDifferentValueObjectTestCase
	extends ValueObjectEqualsTest {

	/** Map from field names to class types; you MUST
	 * have a static initializer to fill in these values.
	 */
	static Map<String, Class> map  = new HashMap<String, Class>();

	@Override
	protected List<String> keyPropertyNames() {
		List<String> n = new ArrayList<String>();
		for (String s : map.keySet())
			n.add(s);
		return n;
	}

	/** Must be set to an default instance */
	Object target;

	@Override
	protected Object createInstanceDiffersIn(String fieldName) throws Exception {
		Class c = map.get(fieldName);
		Field f = getField(fieldName);
		f.setAccessible(true);  // bye-bye "private"
		if (c.equals(String.class)) {
			f.set(target, "testme");
		} else if (c.equals(BigDecimal.class)) {
			f.set(target, new BigDecimal(123));
		} else if (c.equals(boolean.class)) {
			f.set(target, Boolean.TRUE);
		} else if (c.equals(java.util.Date.class)) {
			f.set(target, new Date());
		} else if (c.equals(Integer.class)) {
			f.set(target, new Integer(111));
		} else if (c.equals(Long.class)) {
			f.set(target, new Long(111));
		} else throw new IllegalArgumentException(
			"Unhandled type " + c);
		return target;
	}

	Field[] fields = DefParam.class.getDeclaredFields();

	private Field getField(String name) {
		for (Field f : fields) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		throw new IllegalArgumentException(name + " is not a field");
	}
}
