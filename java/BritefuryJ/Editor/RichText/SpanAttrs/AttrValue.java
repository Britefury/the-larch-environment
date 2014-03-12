package BritefuryJ.Editor.RichText.SpanAttrs;

import BritefuryJ.DefaultPerspective.Presentable;

public abstract class AttrValue implements Iterable<Object>, Presentable {
	public abstract int size();
	public abstract Object get(int index);

	public abstract Intersection<? extends AttrValue> intersect(AttrValue v);
	// v should be a prefix of this or equal to this, otherwise RuntimeException is thrown
	public abstract AttrValue difference(AttrValue v);
	public abstract AttrValue concatenate(AttrValue v);
}
