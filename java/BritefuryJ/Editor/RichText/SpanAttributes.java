package BritefuryJ.Editor.RichText;


import BritefuryJ.Editor.RichText.SpanAttrs.AttrValue;
import BritefuryJ.Editor.RichText.SpanAttrs.Intersection;
import BritefuryJ.Editor.RichText.SpanAttrs.SingleValue;
import BritefuryJ.Editor.RichText.SpanAttrs.ValueStack;

import java.util.*;

public class SpanAttributes implements Map<Object, AttrValue> {
	private HashMap<Object, AttrValue> table ;


	public SpanAttributes() {
		table = new HashMap<Object, AttrValue>();
	}

	public SpanAttributes(HashMap<Object, AttrValue> values) {
		this.table = values;
	}



	@Override
	public boolean equals(Object other) {
		if (other instanceof SpanAttributes) {
			SpanAttributes sa = (SpanAttributes)other;
			return table.equals(sa.table);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return table.hashCode();
	}



	@Override
	public int size() {
		return table.size();
	}

	@Override
	public boolean isEmpty() {
		return table.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return table.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return table.containsValue(value);
	}

	@Override
	public AttrValue get(Object key) {
		return table.get(key);
	}

	@Override
	public AttrValue put(Object key, AttrValue value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public AttrValue remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<?, ? extends AttrValue> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Object> keySet() {
		return table.keySet();
	}

	@Override
	public Collection<AttrValue> values() {
		return table.values();
	}

	@Override
	public Set<Entry<Object, AttrValue>> entrySet() {
		return table.entrySet();
	}


	public SpanAttributes withAttr(Object key, Object value) {
		HashMap<Object, AttrValue> values = new HashMap<Object, AttrValue>();
		values.putAll(table);
		values.put(key, new SingleValue(value));
		return new SpanAttributes(values);
	}

	public SpanAttributes withAppend(Object key, Object value) {
		HashMap<Object, AttrValue> values = new HashMap<Object, AttrValue>();
		values.putAll(table);
		ValueStack stack = (ValueStack)values.get(key);
		if (stack == null) {
			stack = new ValueStack(Arrays.asList(new Object[] {value}));
			values.put(key, stack);
		}
		else {
			ValueStack s2 = stack.withValue(value);
			values.put(key, s2);
		}
		return new SpanAttributes(values);
	}


	public Intersection<SpanAttributes> intersect(SpanAttributes other) {
		SpanAttributes i = new SpanAttributes(), da = new SpanAttributes(), db = new SpanAttributes();

		for (Map.Entry<Object, AttrValue> e_a: table.entrySet()) {
			AttrValue b = other.table.get(e_a.getKey());

			if (b != null) {
				// Key common to both
				Intersection<? extends AttrValue> valueIntersection = e_a.getValue().intersect(b);

				if (valueIntersection.intersection != null) {
					i.table.put(e_a.getKey(), valueIntersection.intersection);
				}

				if (valueIntersection.dIntersectionA != null) {
					da.table.put(e_a.getKey(), valueIntersection.dIntersectionA);
				}

				if (valueIntersection.dIntersectionB != null) {
					db.table.put(e_a.getKey(), valueIntersection.dIntersectionB);
				}
			}
			else {
				// Key only in A
				da.table.put(e_a.getKey(), e_a.getValue());
			}
		}

		for (Map.Entry<Object, AttrValue> e_b: other.table.entrySet()) {
			if (!table.containsKey(e_b.getKey())) {
				db.table.put(e_b.getKey(), e_b.getValue());
			}
		}

		if (i.table.size() == 0) {
			return null;
		}
		else {
			if (da.table.size() == 0) {
				da = null;
			}

			if (db.table.size() == 0) {
				db = null;
			}

			return new Intersection<SpanAttributes>(i, da, db);
		}
	}





	public static SpanAttributes fromValues(Map<Object, Object> values) {
		HashMap<Object, AttrValue> table = new HashMap<Object, AttrValue>();
		for (Map.Entry<Object, Object> e: values.entrySet()) {
			table.put(e.getKey(), new SingleValue(e.getValue()));
		}
		return new SpanAttributes(table);
	}
}
