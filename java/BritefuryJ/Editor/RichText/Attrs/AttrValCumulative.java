package BritefuryJ.Editor.RichText.Attrs;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AttrValCumulative extends AttrValue {
	private ArrayList<Object> stack = new ArrayList<Object>();

	public AttrValCumulative() {
	}

	public AttrValCumulative(List<Object> values) {
		stack.addAll(values);
	}

	public AttrValCumulative(Object values[]) {
		stack.addAll(Arrays.asList(values));
	}



	public AttrValCumulative withValue(Object v) {
		AttrValCumulative s = new AttrValCumulative();
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
		if (other instanceof AttrValCumulative) {
			AttrValCumulative s = (AttrValCumulative)other;
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


	@Override
	public Intersection<? extends AttrValue> intersect(AttrValue v) {
		if (v instanceof AttrValCumulative) {
			AttrValCumulative s = (AttrValCumulative)v;

			if (stack.isEmpty()  ||  s.stack.isEmpty()) {
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
					Intersection<AttrValCumulative> inter = new Intersection<AttrValCumulative>(new AttrValCumulative(stack.subList(0, firstDifferent)), null, null);

					if (firstDifferent < stack.size()) {
						inter.dIntersectionA = new AttrValCumulative(stack.subList(firstDifferent, stack.size()));
					}

					if (firstDifferent < s.stack.size()) {
						inter.dIntersectionB = new AttrValCumulative(s.stack.subList(firstDifferent, s.stack.size()));
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
			throw new RuntimeException("AttrValCumulative can only be intersected with AttrValCumulative");
		}
	}

	@Override
	public AttrValue difference(AttrValue v) {
		if (v instanceof AttrValCumulative) {
			AttrValCumulative s = (AttrValCumulative)v;

			if (s.stack.size() > stack.size()) {
				throw new RuntimeException("The entries in other are not a prefix of this");
			}
			else if (s.stack.size() == stack.size()) {
				if (s.stack.equals(stack)) {
					// Same contents; empty difference
					return null;
				}
				else {
					throw new RuntimeException("The entries in other are not a prefix of this");
				}
			}
			else {
				// The stack in @s is shorter
				if (s.stack.isEmpty()) {
					return this;
				}
				else {
					// Ensure that v is a prefix of this
					if (!s.stack.equals(stack.subList(0, s.stack.size()))) {
						throw new RuntimeException("The entries in other are not a prefix of this");
					}

					AttrValCumulative diff = new AttrValCumulative();
					diff.stack.addAll(stack.subList(s.stack.size(), stack.size()));
					return diff;
				}
			}
		}
		else {
			throw new RuntimeException("AttrValCumulative can only be differenced with AttrValCumulative");
		}
	}

	@Override
	public AttrValue concatenate(AttrValue v) {
		if (v instanceof AttrValCumulative) {
			AttrValCumulative s = (AttrValCumulative)v;

			AttrValCumulative cat = new AttrValCumulative(stack);
			cat.stack.addAll(s.stack);
			return cat;
		}
		else {
			throw new RuntimeException("AttrValCumulative can only be accumulated with AttrValCumulative");
		}
	}



	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState) {
		Pres vals = new Column(stack);
		return stackBorder.surround(new Column(new Pres[] {stackLabel, vals.padX(5.0, 0.0)}));
	}

	private static final SolidBorder stackBorder = new SolidBorder(1.0, 3.0, 4.0, 4.0, new Color(0.3f, 0.5f, 0.7f), null);
	private static final Pres stackLabel = StyleSheet.style(Primitive.fontSize.as(10), Primitive.foreground.as(new Color(0.3f, 0.5f, 0.7f))).applyTo(new Label("Value stack"));




	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("cumulative[");
		boolean first = true;
		for (Object value: stack) {
			if (!first) {
				builder.append(", ");
			}

			builder.append(value);
		}
		builder.append("]");

		return builder.toString();
	}
}
