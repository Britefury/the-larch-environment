package tests.Editor.RichText;

import BritefuryJ.Editor.RichText.SpanAttrs.Intersection;
import BritefuryJ.Editor.RichText.SpanAttrs.ValueStack;
import junit.framework.TestCase;

import java.util.Arrays;

public class SpanAttributes_StackValueTest extends TestCase {
	protected ValueStack stack(Object ... values) {
		return new ValueStack(Arrays.asList(values));
	}

	protected Intersection<ValueStack> intersection(Object inter[], Object da[], Object db[]) {
		return new Intersection<ValueStack>(
				inter != null  ?  new ValueStack(Arrays.asList(inter))  :  null,
				da != null  ?  new ValueStack(Arrays.asList(da))  :  null,
				db != null  ?  new ValueStack(Arrays.asList(db))  :  null
		);
	}


	public void testEquals() {
		ValueStack a = stack("a");
		ValueStack b = stack("a");
		ValueStack c = stack("c");

		assertEquals(a, b);
		assertFalse(a.equals(c));
	}

	public void testIntersection() {
		ValueStack a = stack("a");
		ValueStack b = stack("a", "b", "c");
		ValueStack c = stack("a", "b", "d", "e");
		ValueStack d = stack("d");

		assertNull(a.intersect(d));
		assertNull(d.intersect(a));

		assertEquals(a.intersect(b), intersection(new Object[] {"a"}, null, new Object[] {"b", "c"}));
		assertEquals(b.intersect(a), intersection(new Object[] {"a"}, new Object[] {"b", "c"}, null));
		assertEquals(b.intersect(c), intersection(new Object[] {"a", "b"}, new Object[] {"c"}, new Object[] {"d", "e"}));
	}
}
