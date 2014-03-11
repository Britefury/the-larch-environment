package tests.Editor.RichText;

import BritefuryJ.Editor.RichText.SpanAttributes;
import junit.framework.TestCase;

public class SpanAttributes_SingleValueTest extends TestCase {
	public void testEquals() {
		SpanAttributes.SingleValue a = new SpanAttributes.SingleValue("hello");
		SpanAttributes.SingleValue b = new SpanAttributes.SingleValue("hello");
		SpanAttributes.SingleValue c = new SpanAttributes.SingleValue("world");

		assertEquals(a, b);
		assertFalse(a.equals(c));
	}

	public void testIntersection() {
		SpanAttributes.SingleValue a = new SpanAttributes.SingleValue("hello");
		SpanAttributes.SingleValue b = new SpanAttributes.SingleValue("hello");
		SpanAttributes.SingleValue c = new SpanAttributes.SingleValue("world");

		assertNull(a.intersect(c));

		SpanAttributes.Intersection<SpanAttributes.SingleValue> i =
				(SpanAttributes.Intersection<SpanAttributes.SingleValue>)a.intersect(b);

		assertNull(i.dIntersectionA);
		assertNull(i.dIntersectionB);
		assertEquals(i.intersection, a);
		assertEquals(i.intersection, b);
	}
}
