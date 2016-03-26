//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText.Attrs;


import java.util.*;

public class RichTextAttributes {
	private HashMap<Object, AttrValue> table;


	public RichTextAttributes() {
		table = new HashMap<Object, AttrValue>();
	}

	private RichTextAttributes(HashMap<Object, AttrValue> values) {
		this.table = values;
	}


	public RichTextAttributes copy() {
		HashMap<Object, AttrValue> table = new HashMap<Object, AttrValue>();
		table.putAll(this.table);
		return new RichTextAttributes(table);
	}

	public void replaceContentsWith(RichTextAttributes attrs) {
		table.clear();
		table.putAll(attrs.table);
	}



	@Override
	public boolean equals(Object other) {
		if (other instanceof RichTextAttributes) {
			RichTextAttributes sa = (RichTextAttributes)other;
			return table.equals(sa.table);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return table.hashCode();
	}



	public int size() {
		return table.size();
	}

	public boolean isEmpty() {
		return table.isEmpty();
	}

	public boolean containsKey(Object key) {
		return table.containsKey(key);
	}

	public AttrValue getAttrVal(Object key) {
		return table.get(key);
	}

	public Object getValue(Object key, int index) {
		AttrValue av = table.get(key);
		if (av != null) {
			return av.get(index);
		}
		else {
			return null;
		}
	}

	public void putOverride(Object key, Object value) {
		AttrValue existing = table.get(key);
		if (existing != null  &&  !(existing instanceof AttrValOverride)) {
			throw new RuntimeException("Attempting to override non-overrideable value (not an instance of AttrValOverride)");
		}
		table.put(key, new AttrValOverride(value));
	}

	public void putCumulative(Object key, Object value) {
		AttrValCumulative cumulative = null;
		AttrValue existing = table.get(key);
		if (existing != null) {
			if (existing instanceof AttrValCumulative) {
				cumulative = (AttrValCumulative)existing;
				cumulative = cumulative.withValue(value);
			}
			else {
				throw new RuntimeException("Attempting to accumulate into non-cumulative value (not an instance of AttrValCumulative)");
			}
		}
		else {
			cumulative = new AttrValCumulative(new Object[] {value});
		}
		table.put(key, cumulative);
	}

	public AttrValue remove(Object key) {
		return table.remove(key);
	}


	public void clear() {
		table.clear();
	}

	public Set<Object> keySet() {
		return table.keySet();
	}

	public Set<Map.Entry<Object, AttrValue>> entrySet() {
		return table.entrySet();
	}


	public RichTextAttributes withOverride(Object key, Object value) {
		HashMap<Object, AttrValue> values = new HashMap<Object, AttrValue>();
		values.putAll(table);
		values.put(key, new AttrValOverride(value));
		return new RichTextAttributes(values);
	}

	public RichTextAttributes withCumulative(Object key, Object value) {
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
				throw new RuntimeException("withCumulative: existing value for key " + key + " is not a stack value");
			}
		}
		return new RichTextAttributes(values);
	}


	public Intersection<RichTextAttributes> intersect(RichTextAttributes other) {
		RichTextAttributes i = new RichTextAttributes(), da = new RichTextAttributes(), db = new RichTextAttributes();

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

			return new Intersection<RichTextAttributes>(i, da, db);
		}
	}



	public RichTextAttributes difference(RichTextAttributes other) {
		RichTextAttributes diff = new RichTextAttributes();

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



	public RichTextAttributes concatenate(RichTextAttributes other) {
		HashMap<Object, AttrValue> map = new HashMap<Object, AttrValue>();
		map.putAll(this.table);

		for (Map.Entry<Object, AttrValue> e: other.table.entrySet()) {
			AttrValue existing = map.get(e.getKey());
			if (existing == null) {
				// No existing value; just insert
				map.put(e.getKey(), e.getValue());
			}
			else {
				map.put(e.getKey(), existing.concatenate(e.getValue()));
			}
		}

		return new RichTextAttributes(map);
	}





	public static RichTextAttributes fromValues(Map<Object, Object> overrideValues, Map<Object, List<Object>> cumulativeValues) {
		HashMap<Object, AttrValue> table = new HashMap<Object, AttrValue>();

		if (overrideValues != null) {
			for (Map.Entry<Object, Object> e: overrideValues.entrySet()) {
				table.put(e.getKey(), new AttrValOverride(e.getValue()));
			}
		}

		if (cumulativeValues != null) {
			for (Map.Entry<Object, List<Object>> e: cumulativeValues.entrySet()) {
				table.put(e.getKey(), new AttrValCumulative(e.getValue()));
			}
		}

		return new RichTextAttributes(table);
	}



	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("{");
		boolean first = true;
		for (Map.Entry<Object, AttrValue> e: table.entrySet()) {
			if (!first) {
				builder.append(", ");
			}

			builder.append(e.getKey());
			builder.append("=");
			builder.append(e.getValue());

			first = false;
		}
		builder.append("}");

		return builder.toString();
	}
}
