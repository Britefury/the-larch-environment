package tests.Editor.RichText;

import BritefuryJ.Editor.RichText.SpanAttributes;
import BritefuryJ.Editor.RichText.SpanAttrs.Intersection;
import BritefuryJ.Editor.RichText.SpanAttrs.SingleValue;
import BritefuryJ.Editor.RichText.SpanAttrs.ValueStack;
import junit.framework.TestCase;

import java.util.Arrays;

public class SpanAttributesTest extends TestCase {
	protected Intersection<SpanAttributes> intersection(SpanAttributes inter, SpanAttributes da, SpanAttributes db) {
		return new Intersection<SpanAttributes>(inter, da, db);
	}

	public void testEquals() {
		SpanAttributes a0 = new SpanAttributes().withAttr("a", 1);
		SpanAttributes a1 = new SpanAttributes().withAttr("a", 1);
		SpanAttributes b = new SpanAttributes().withAttr("b", 2);
		SpanAttributes c0 = new SpanAttributes().withAppend("c", 3).withAppend("c", 4);
		SpanAttributes c1 = new SpanAttributes().withAppend("c", 3).withAppend("c", 4);

		assertEquals(a0, a1);
		assertEquals(c0, c1);
		assertFalse(a0.equals(b));
		assertFalse(b.equals(a0));
	}

	public void testWithAttr() {
		SpanAttributes a = new SpanAttributes().withAttr("a", 1);
		SpanAttributes ab = new SpanAttributes().withAttr("a", 1).withAttr("b", 2);
		SpanAttributes c0 = new SpanAttributes().withAppend("c", 3).withAppend("c", 4);

		assertEquals(a.get("a"), new SingleValue(1));
		assertEquals(ab.get("a"), new SingleValue(1));
		assertEquals(ab.get("b"), new SingleValue(2));
		assertEquals(c0.get("c"), new ValueStack(Arrays.asList(new Object[] {3, 4})));
	}

	public void testIntersection() {
		SpanAttributes a0 = new SpanAttributes().withAttr("a", 1);
		SpanAttributes a1 = new SpanAttributes().withAttr("a", 1);
		SpanAttributes abcd = new SpanAttributes().withAttr("a", 1).withAttr("b", 2).withAttr("c", 3).withAttr("d", 4);
		SpanAttributes abef = new SpanAttributes().withAttr("a", 1).withAttr("b", 2).withAttr("e", 5).withAttr("f", 6);
		SpanAttributes ab = new SpanAttributes().withAttr("a", 1).withAttr("b", 2);
		SpanAttributes cd = new SpanAttributes().withAttr("c", 3).withAttr("d", 4);
		SpanAttributes ef = new SpanAttributes().withAttr("e", 5).withAttr("f", 6);

		SpanAttributes wxz = new SpanAttributes().withAttr("w", 0).withAttr("x", 1).withAppend("z", 3).withAppend("z", 4).withAppend("z", 5).withAppend("z", 10).withAppend("z", 15);
		SpanAttributes wyz = new SpanAttributes().withAttr("w", 0).withAttr("y", 2).withAppend("z", 3).withAppend("z", 4).withAppend("z", 100).withAppend("z", 200);
		SpanAttributes wqz = new SpanAttributes().withAttr("w", 0).withAttr("q", -2).withAppend("z", 3).withAppend("z", 1000).withAppend("z", 5).withAppend("z", 10).withAppend("z", 15);

		SpanAttributes wxz_wyz_int = new SpanAttributes().withAttr("w", 0).withAppend("z", 3).withAppend("z", 4);
		SpanAttributes wxz_wyz_da = new SpanAttributes().withAttr("x", 1).withAppend("z", 5).withAppend("z", 10).withAppend("z", 15);
		SpanAttributes wxz_wyz_db = new SpanAttributes().withAttr("y", 2).withAppend("z", 100).withAppend("z", 200);

		SpanAttributes wxz_wqz_int = new SpanAttributes().withAttr("w", 0).withAppend("z", 3);
		SpanAttributes wxz_wqz_da = new SpanAttributes().withAttr("x", 1).withAppend("z", 4).withAppend("z", 5).withAppend("z", 10).withAppend("z", 15);
		SpanAttributes wxz_wqz_db = new SpanAttributes().withAttr("q", -2).withAppend("z", 1000).withAppend("z", 5).withAppend("z", 10).withAppend("z", 15);


		assertEquals(a0.intersect(a1), intersection(a0, null, null));
		assertEquals(abcd.intersect(abef), intersection(ab, cd, ef));
		assertEquals(wxz.intersect(wyz), intersection(wxz_wyz_int, wxz_wyz_da, wxz_wyz_db));
		assertEquals(wxz.intersect(wqz), intersection(wxz_wqz_int, wxz_wqz_da, wxz_wqz_db));
	}
}
