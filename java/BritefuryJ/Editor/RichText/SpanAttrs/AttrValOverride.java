package BritefuryJ.Editor.RichText.SpanAttrs;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class AttrValOverride extends AttrValue {
	private Object value;

	public AttrValOverride(Object value) {
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
		if (other instanceof AttrValOverride) {
			AttrValOverride s = (AttrValOverride)other;
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
		if (v instanceof AttrValOverride) {
			AttrValOverride s = (AttrValOverride)v;

			if (value.equals(s.value)) {
				return new Intersection<AttrValOverride>(this, null, null);
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
		if (v instanceof AttrValOverride) {
			AttrValOverride s = (AttrValOverride)v;

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

	@Override
	public AttrValue concatenate(AttrValue v) {
		if (v instanceof AttrValOverride) {
			// Override this
			return v;
		}
		else {
			throw new RuntimeException("SingleValues can only be accumulated with SingleValues");
		}
	}

	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState) {
		return Pres.coerce(value);
	}



	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
