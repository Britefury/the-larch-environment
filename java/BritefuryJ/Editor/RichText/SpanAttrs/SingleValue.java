package BritefuryJ.Editor.RichText.SpanAttrs;

import BritefuryJ.Editor.RichText.SpanAttributes;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingleValue extends AttrValue {
	private Object value;

	public SingleValue(Object value) {
		this.value = value;
	}


	@Override
	public int size() {
		return 1;
	}

	@Override
	public Object get(int index) {
		if (index == 0) {
			return value;
		}
		else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public Iterator<Object> iterator() {
		return new Iterator<Object>() {
			private int index = 0;


			public boolean hasNext() {
				return index == 0;
			}

			public Object next() {
				if (index == 0) {
					index++;
					return value;
				}
				else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
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

	@Override
	public int hashCode() {
		return value != null  ?  value.hashCode()  :  0;
	}

	@Override
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

	@Override
	public AttrValue difference(AttrValue v) {
		if (v instanceof SingleValue) {
			SingleValue s = (SingleValue)v;

			if (value.equals(s.value)) {
				return null;
			}
			else {
				throw new RuntimeException("this must be a strict superset of v");
			}
		}
		else {
			throw new RuntimeException("SingleValues can only be differenced with SingleValues");
		}
	}
}
