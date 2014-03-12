package tests.Editor.RichText;

import BritefuryJ.Editor.RichText.SpanAttrs.Intersection;
import BritefuryJ.Editor.RichText.SpanAttrs.SingleValue;
import junit.framework.TestCase;

public class SpanAttributes_SingleValueTest extends TestCase {
	public void testEquals() {
		SingleValue a = new SingleValue("hello");
		SingleValue b = new SingleValue("hello");
		SingleValue c = new SingleValue("world");

		assertEquals(a, b);
		assertFalse(a.equals(c));
	}

	public void testIntersection() {
		SingleValue a = new SingleValue("hello");
		SingleValue b = new SingleValue("hello");
		SingleValue c = new SingleValue("world");

		assertNull(a.intersect(c));

		Intersection<SingleValue> i =
				(Intersection<SingleValue>)a.intersect(b);

		assertNull(i.dIntersectionA);
		assertNull(i.dIntersectionB);
		assertEquals(i.intersection, a);
		assertEquals(i.intersection, b);
	}

	public void testDifference() {
		SingleValue a = new SingleValue("hello");
		SingleValue b = new SingleValue("hello");

		assertNull(a.difference(b));
	}
}
