package tests.Editor.RichText;

import BritefuryJ.Editor.RichText.SpanAttributes;
import junit.framework.TestCase;

import java.util.Arrays;

public class SpanAttributes_StackValueTest extends TestCase {
	protected SpanAttributes.ValueStack stack(Object ... values) {
		return new SpanAttributes.ValueStack(Arrays.asList(values));
	}

	protected SpanAttributes.Intersection<SpanAttributes.ValueStack> intersection(Object inter[], Object da[], Object db[]) {
		return new SpanAttributes.Intersection<SpanAttributes.ValueStack>(
				inter != null  ?  new SpanAttributes.ValueStack(Arrays.asList(inter))  :  null,
				da != null  ?  new SpanAttributes.ValueStack(Arrays.asList(da))  :  null,
				db != null  ?  new SpanAttributes.ValueStack(Arrays.asList(db))  :  null
		);
	}


	public void testEquals() {
		SpanAttributes.ValueStack a = stack("a");
		SpanAttributes.ValueStack b = stack("a");
		SpanAttributes.ValueStack c = stack("c");

		assertEquals(a, b);
		assertFalse(a.equals(c));
	}

	public void testIntersection() {
		SpanAttributes.ValueStack a = stack("a");
		SpanAttributes.ValueStack b = stack("a", "b", "c");
		SpanAttributes.ValueStack c = stack("a", "b", "d", "e");
		SpanAttributes.ValueStack d = stack("d");

		assertNull(a.intersect(d));
		assertNull(d.intersect(a));

		assertEquals(a.intersect(b), intersection(new Object[] {"a"}, null, new Object[] {"b", "c"}));
		assertEquals(b.intersect(a), intersection(new Object[] {"a"}, new Object[] {"b", "c"}, null));
		assertEquals(b.intersect(c), intersection(new Object[] {"a", "b"}, new Object[] {"c"}, new Object[] {"d", "e"}));
	}
}
