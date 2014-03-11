package BritefuryJ.Editor.RichText.SpanAttrs;

public abstract class AttrValue implements Iterable<Object> {
	public abstract int size();
	public abstract Object get(int index);
	public abstract Intersection<? extends AttrValue> intersect(AttrValue v);
}
