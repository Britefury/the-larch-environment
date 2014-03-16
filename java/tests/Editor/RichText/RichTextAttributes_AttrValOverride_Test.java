package tests.Editor.RichText;

import BritefuryJ.Editor.RichText.Attrs.AttrValOverride;
import BritefuryJ.Editor.RichText.Attrs.Intersection;
import junit.framework.TestCase;

public class RichTextAttributes_AttrValOverride_Test extends TestCase {
	public void testEquals() {
		AttrValOverride a = new AttrValOverride("hello");
		AttrValOverride b = new AttrValOverride("hello");
		AttrValOverride c = new AttrValOverride("world");

		assertEquals(a, b);
		assertFalse(a.equals(c));
	}

	public void testIntersection() {
		AttrValOverride a = new AttrValOverride("hello");
		AttrValOverride b = new AttrValOverride("hello");
		AttrValOverride c = new AttrValOverride("world");

		assertNull(a.intersect(c));

		Intersection<AttrValOverride> i =
				(Intersection<AttrValOverride>)a.intersect(b);

		assertNull(i.dIntersectionA);
		assertNull(i.dIntersectionB);
		assertEquals(i.intersection, a);
		assertEquals(i.intersection, b);
	}

	public void testDifference() {
		AttrValOverride a = new AttrValOverride("hello");
		AttrValOverride b = new AttrValOverride("hello");

		assertNull(a.difference(b));
	}

	public void testConcatenate() {
		AttrValOverride a = new AttrValOverride("hello");
		AttrValOverride b = new AttrValOverride("world");

		assertEquals(a.concatenate(b), b);
	}
}
