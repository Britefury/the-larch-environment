package BritefuryJ.Editor.RichText;


import BritefuryJ.Editor.RichText.SpanAttrs.AttrValCumulative;
import BritefuryJ.Editor.RichText.SpanAttrs.AttrValOverride;
import BritefuryJ.Editor.RichText.SpanAttrs.AttrValue;
import BritefuryJ.Editor.RichText.SpanAttrs.Intersection;

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
		return table.put(key, value);
	}

	@Override
	public AttrValue remove(Object key) {
		return table.remove(key);
	}

	@Override
	public void putAll(Map<?, ? extends AttrValue> m) {
		table.putAll(m);
	}

	public void putAll(SpanAttributes attrs) {
		table.putAll(attrs.table);
	}

	@Override
	public void clear() {
		table.clear();
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
		values.put(key, new AttrValOverride(value));
		return new SpanAttributes(values);
	}

	public SpanAttributes withAppend(Object key, Object value) {
		HashMap<Object, AttrValue> values = new HashMap<Object, AttrValue>();
		values.putAll(table);
		AttrValue attrVal = values.get(key);
		if (attrVal == null) {
			attrVal = new AttrValCumulative(Arrays.asList(new Object[] {value}));
			values.put(key, attrVal);
		}
		else {
			if (attrVal instanceof AttrValCumulative) {
				AttrValCumulative s2 = ((AttrValCumulative)attrVal).withValue(value);
				values.put(key, s2);
			}
			else {
				throw new RuntimeException("withAppend: existing value for key " + key + " is not a stack value");
			}
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

				if (valueIntersection == null) {
					da.table.put(e_a.getKey(), e_a.getValue());
					db.table.put(e_a.getKey(), b);
				}
				else {
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



	public SpanAttributes difference(SpanAttributes other) {
		SpanAttributes diff = new SpanAttributes();

		for (Map.Entry<Object, AttrValue> e_a: table.entrySet()) {
			AttrValue b = other.table.get(e_a.getKey());

			if (b != null) {
				// Key common to both
				AttrValue valueDiff = e_a.getValue().difference(b);

				if (valueDiff != null) {
					diff.table.put(e_a.getKey(), valueDiff);
				}
			}
			else {
				// Key only in A
				diff.table.put(e_a.getKey(), e_a.getValue());
			}
		}

		for (Map.Entry<Object, AttrValue> e_b: other.table.entrySet()) {
			if (!table.containsKey(e_b.getKey())) {
				throw new RuntimeException("difference: this must be a strict superset of other");
			}
		}

		return diff;
	}



	public SpanAttributes concatenate(SpanAttributes other) {
		SpanAttributes cat = new SpanAttributes(this.table);

		for (Map.Entry<Object, AttrValue> e: other.table.entrySet()) {
			AttrValue existing = cat.table.get(e.getKey());
			if (existing == null) {
				// No existing value; just insert
				cat.table.put(e.getKey(), e.getValue());
			}
			else {
				cat.table.put(e.getKey(), existing.concatenate(e.getValue()));
			}
		}

		return cat;
	}





	public static SpanAttributes fromValues(Map<Object, Object> values) {
		HashMap<Object, AttrValue> table = new HashMap<Object, AttrValue>();
		for (Map.Entry<Object, Object> e: values.entrySet()) {
			table.put(e.getKey(), new AttrValOverride(e.getValue()));
		}
		return new SpanAttributes(table);
	}
}
