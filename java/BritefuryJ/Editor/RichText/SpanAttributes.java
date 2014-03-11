package BritefuryJ.Editor.RichText;


import java.util.*;

public class SpanAttributes {
	public static class Intersection <T> {
		public T intersection, dIntersectionA, dIntersectionB;

		public Intersection(T intersection, T dIntersectionA, T dIntersectionB) {
			this.intersection = intersection;
			this.dIntersectionA = dIntersectionA;
			this.dIntersectionB = dIntersectionB;
		}


		@Override
		public boolean equals(Object other) {
			if (other instanceof Intersection) {
				Intersection<T> i = (Intersection<T>)other;

				if (intersection == null  &&  i.intersection == null  ||
						intersection != null && i.intersection != null && intersection.equals(i.intersection)) {
					if (dIntersectionA == null  &&  i.dIntersectionA == null  ||
							dIntersectionA != null && i.dIntersectionA != null && dIntersectionA.equals(i.dIntersectionA)) {
						if (dIntersectionB == null  &&  i.dIntersectionB == null  ||
								dIntersectionB != null && i.dIntersectionB != null && dIntersectionB.equals(i.dIntersectionB)) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}


	public static abstract class AttrValue {
		public abstract Intersection<? extends AttrValue> intersect(AttrValue v);
	}


	public static class SingleValue extends AttrValue {
		private Object value;

		public SingleValue(Object value) {
			this.value = value;
		}


		@Override
		public boolean equals(Object other) {
			if (other instanceof SingleValue) {
				SingleValue s = (SingleValue)other;
				if (value == null  &&  s.value == null  ||
						value != null && s.value != null && value.equals(s.value)) {
					return true;
				}
			}
			return false;
		}


		public Intersection<? extends AttrValue> intersect(AttrValue v) {
			if (v instanceof SingleValue) {
				SingleValue s = (SingleValue)v;

				if (value.equals(s.value)) {
					return new Intersection<SingleValue>(this, null, null);
				}
				else {
					return null;
				}
			}
			else {
				throw new RuntimeException("SingleValues can only be intersected with SingleValues");
			}

		}
	}


	public static class ValueStack extends AttrValue {
		private ArrayList<Object> stack = new ArrayList<Object>();

		public ValueStack() {
		}

		public ValueStack(List<Object> values) {
			stack.addAll(values);
		}

		public List<Object> getValues() {
			return stack;
		}


		@Override
		public boolean equals(Object other) {
			if (other instanceof ValueStack) {
				ValueStack s = (ValueStack)other;
				if (stack == null  &&  s.stack == null  ||
						stack != null && s.stack != null && stack.equals(s.stack)) {
					return true;
				}
			}
			return false;
		}


		public Intersection<? extends AttrValue> intersect(AttrValue v) {
			if (v instanceof ValueStack) {
				ValueStack s = (ValueStack)v;

				if (stack.size() == 0  ||  s.stack.size() == 0) {
					// One or both empty; no intersection
					return null;
				}
				else {
					int top = Math.min(stack.size(), s.stack.size());
					int lastEqual = -1;
					for (int i = 0; i < top; i++) {
						if (!stack.get(i).equals(s.stack.get(i))) {
							lastEqual = i;
						}
					}
					int firstDifferent = lastEqual + 1;


					if (firstDifferent > 0) {
						Intersection<ValueStack> inter = new Intersection<ValueStack>(new ValueStack(stack.subList(0, firstDifferent)), null, null);

						if (firstDifferent < stack.size()) {
							inter.dIntersectionA = new ValueStack(stack.subList(firstDifferent, stack.size()));
						}

						if (firstDifferent < s.stack.size()) {
							inter.dIntersectionB = new ValueStack(stack.subList(firstDifferent, s.stack.size()));
						}

						return inter;
					}
					else
					{
						return null;
					}
				}
			}
			else {
				throw new RuntimeException("ValueStack can only be intersected with ValueStack");
			}

		}
	}



	private HashMap<Object, AttrValue> table ;


	public SpanAttributes() {
		table = new HashMap<Object, AttrValue>();
	}

	public SpanAttributes(HashMap<Object, AttrValue> values) {
		this.table = values;
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
			ValueStack s2 = new ValueStack(stack.getValues());
			s2.getValues().add(value);
			values.put(key, stack);
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
