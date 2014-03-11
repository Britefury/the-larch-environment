package BritefuryJ.Editor.RichText.SpanAttrs;

import BritefuryJ.Editor.RichText.SpanAttributes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ValueStack extends AttrValue {
	private ArrayList<Object> stack = new ArrayList<Object>();

	public ValueStack() {
	}

	public ValueStack(List<Object> values) {
		stack.addAll(values);
	}



	public ValueStack withValue(Object v) {
		ValueStack s = new ValueStack();
		s.stack.addAll(this.stack);
		s.stack.add(v);
		return s;
	}



	@Override
	public int size() {
		return stack.size();
	}

	@Override
	public Object get(int index) {
		return stack.get(index);
	}

	@Override
	public Iterator<Object> iterator() {
		return stack.iterator();
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

	@Override
	public int hashCode() {
		return stack.hashCode();
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
				int firstDifferent = top;
				for (int i = 0; i < top; i++) {
					if (!stack.get(i).equals(s.stack.get(i))) {
						firstDifferent = i;
						break;
					}
				}


				if (firstDifferent > 0) {
					Intersection<ValueStack> inter = new Intersection<ValueStack>(new ValueStack(stack.subList(0, firstDifferent)), null, null);

					if (firstDifferent < stack.size()) {
						inter.dIntersectionA = new ValueStack(stack.subList(firstDifferent, stack.size()));
					}

					if (firstDifferent < s.stack.size()) {
						inter.dIntersectionB = new ValueStack(s.stack.subList(firstDifferent, s.stack.size()));
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
