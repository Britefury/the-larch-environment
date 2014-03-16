package tests.Editor.RichText;

import BritefuryJ.Editor.RichText.Attrs.AttrValCumulative;
import BritefuryJ.Editor.RichText.Attrs.Intersection;
import junit.framework.TestCase;

import java.util.Arrays;

public class RichTextAttributes_AttrValCumulative_Test extends TestCase {
	protected AttrValCumulative stack(Object ... values) {
		return new AttrValCumulative(Arrays.asList(values));
	}

	protected Intersection<AttrValCumulative> intersection(Object inter[], Object da[], Object db[]) {
		return new Intersection<AttrValCumulative>(
				inter != null  ?  new AttrValCumulative(Arrays.asList(inter))  :  null,
				da != null  ?  new AttrValCumulative(Arrays.asList(da))  :  null,
				db != null  ?  new AttrValCumulative(Arrays.asList(db))  :  null
		);
	}


	public void testEquals() {
		AttrValCumulative a = stack("a");
		AttrValCumulative b = stack("a");
		AttrValCumulative c = stack("c");

		assertEquals(a, b);
		assertFalse(a.equals(c));
	}

	public void testIntersection() {
		AttrValCumulative a = stack("a");
		AttrValCumulative b = stack("a", "b", "c");
		AttrValCumulative c = stack("a", "b", "d", "e");
		AttrValCumulative d = stack("d");

		assertNull(a.intersect(d));
		assertNull(d.intersect(a));

		assertEquals(a.intersect(b), intersection(new Object[] {"a"}, null, new Object[] {"b", "c"}));
		assertEquals(b.intersect(a), intersection(new Object[] {"a"}, new Object[] {"b", "c"}, null));
		assertEquals(b.intersect(c), intersection(new Object[]{"a", "b"}, new Object[]{"c"}, new Object[]{"d", "e"}));
	}

	public void testDifference() {
		AttrValCumulative a = stack("a");
		AttrValCumulative abc = stack("a", "b", "c");
		AttrValCumulative bc = stack("b", "c");

		assertNull(a.difference(a));

		assertEquals(abc.difference(a), bc);
	}

	public void testConcatenate() {
		AttrValCumulative a = stack("a");
		AttrValCumulative abc = stack("a", "b", "c");
		AttrValCumulative bc = stack("b", "c");

		assertEquals(a.concatenate(bc), abc);
	}
}
