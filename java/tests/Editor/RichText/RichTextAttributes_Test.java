//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.Editor.RichText;

import BritefuryJ.Editor.RichText.Attrs.RichTextAttributes;
import BritefuryJ.Editor.RichText.Attrs.AttrValCumulative;
import BritefuryJ.Editor.RichText.Attrs.AttrValOverride;
import BritefuryJ.Editor.RichText.Attrs.Intersection;
import junit.framework.TestCase;

import java.util.Arrays;

public class RichTextAttributes_Test extends TestCase {
	protected Intersection<RichTextAttributes> intersection(RichTextAttributes inter, RichTextAttributes da, RichTextAttributes db) {
		return new Intersection<RichTextAttributes>(inter, da, db);
	}

	public void testEquals() {
		RichTextAttributes a0 = new RichTextAttributes().withOverride("a", 1);
		RichTextAttributes a1 = new RichTextAttributes().withOverride("a", 1);
		RichTextAttributes b = new RichTextAttributes().withOverride("b", 2);
		RichTextAttributes c0 = new RichTextAttributes().withCumulative("c", 3).withCumulative("c", 4);
		RichTextAttributes c1 = new RichTextAttributes().withCumulative("c", 3).withCumulative("c", 4);

		assertEquals(a0, a1);
		assertEquals(c0, c1);
		assertFalse(a0.equals(b));
		assertFalse(b.equals(a0));
	}

	public void testWithAttr() {
		RichTextAttributes a = new RichTextAttributes().withOverride("a", 1);
		RichTextAttributes ab = new RichTextAttributes().withOverride("a", 1).withOverride("b", 2);
		RichTextAttributes c0 = new RichTextAttributes().withCumulative("c", 3).withCumulative("c", 4);

		assertEquals(a.getAttrVal("a"), new AttrValOverride(1));
		assertEquals(ab.getAttrVal("a"), new AttrValOverride(1));
		assertEquals(ab.getAttrVal("b"), new AttrValOverride(2));
		assertEquals(c0.getAttrVal("c"), new AttrValCumulative(Arrays.asList(new Object[]{3, 4})));
	}

	public void testIntersection() {
		RichTextAttributes a0 = new RichTextAttributes().withOverride("a", 1);
		RichTextAttributes a1 = new RichTextAttributes().withOverride("a", 1);
		RichTextAttributes abcd = new RichTextAttributes().withOverride("a", 1).withOverride("b", 2).withOverride("c", 3).withOverride("d", 4);
		RichTextAttributes abef = new RichTextAttributes().withOverride("a", 1).withOverride("b", 2).withOverride("e", 5).withOverride("f", 6);
		RichTextAttributes ab = new RichTextAttributes().withOverride("a", 1).withOverride("b", 2);
		RichTextAttributes cd = new RichTextAttributes().withOverride("c", 3).withOverride("d", 4);
		RichTextAttributes ef = new RichTextAttributes().withOverride("e", 5).withOverride("f", 6);

		RichTextAttributes i0j1 = new RichTextAttributes().withOverride("i", 0).withOverride("j", 1);
		RichTextAttributes i0j2 = new RichTextAttributes().withOverride("i", 0).withOverride("j", 2);
		RichTextAttributes i0 = new RichTextAttributes().withOverride("i", 0);
		RichTextAttributes j1 = new RichTextAttributes().withOverride("j", 1);
		RichTextAttributes j2 = new RichTextAttributes().withOverride("j", 2);

		RichTextAttributes wxz = new RichTextAttributes().withOverride("w", 0).withOverride("x", 1).withCumulative("z", 3).withCumulative("z", 4).withCumulative("z", 5).withCumulative("z", 10).withCumulative("z", 15);
		RichTextAttributes wyz = new RichTextAttributes().withOverride("w", 0).withOverride("y", 2).withCumulative("z", 3).withCumulative("z", 4).withCumulative("z", 100).withCumulative("z", 200);
		RichTextAttributes wqz = new RichTextAttributes().withOverride("w", 0).withOverride("q", -2).withCumulative("z", 3).withCumulative("z", 1000).withCumulative("z", 5).withCumulative("z", 10).withCumulative("z", 15);

		RichTextAttributes wxz_wyz_int = new RichTextAttributes().withOverride("w", 0).withCumulative("z", 3).withCumulative("z", 4);
		RichTextAttributes wxz_wyz_da = new RichTextAttributes().withOverride("x", 1).withCumulative("z", 5).withCumulative("z", 10).withCumulative("z", 15);
		RichTextAttributes wxz_wyz_db = new RichTextAttributes().withOverride("y", 2).withCumulative("z", 100).withCumulative("z", 200);

		RichTextAttributes wxz_wqz_int = new RichTextAttributes().withOverride("w", 0).withCumulative("z", 3);
		RichTextAttributes wxz_wqz_da = new RichTextAttributes().withOverride("x", 1).withCumulative("z", 4).withCumulative("z", 5).withCumulative("z", 10).withCumulative("z", 15);
		RichTextAttributes wxz_wqz_db = new RichTextAttributes().withOverride("q", -2).withCumulative("z", 1000).withCumulative("z", 5).withCumulative("z", 10).withCumulative("z", 15);


		assertNull(a0.intersect(ef));
		assertEquals(a0.intersect(a1), intersection(a0, null, null));
		assertEquals(abcd.intersect(abef), intersection(ab, cd, ef));
		assertEquals(i0j1.intersect(i0j2), intersection(i0, j1, j2));
		assertEquals(wxz.intersect(wyz), intersection(wxz_wyz_int, wxz_wyz_da, wxz_wyz_db));
		assertEquals(wxz.intersect(wqz), intersection(wxz_wqz_int, wxz_wqz_da, wxz_wqz_db));
	}

	public void testDifference() {
		RichTextAttributes empty = new RichTextAttributes();
		RichTextAttributes a = new RichTextAttributes().withOverride("a", 1);
		RichTextAttributes abcd = new RichTextAttributes().withOverride("a", 1).withOverride("b", 2).withOverride("c", 3).withOverride("d", 4);
		RichTextAttributes bcd = new RichTextAttributes().withOverride("b", 2).withOverride("c", 3).withOverride("d", 4);

		RichTextAttributes wxz = new RichTextAttributes().withOverride("w", 0).withOverride("x", 1).withCumulative("z", 3).withCumulative("z", 4).withCumulative("z", 5).withCumulative("z", 10).withCumulative("z", 15);
		RichTextAttributes wz = new RichTextAttributes().withOverride("w", 0).withCumulative("z", 3).withCumulative("z", 4);
		RichTextAttributes xz = new RichTextAttributes().withOverride("x", 1).withCumulative("z", 5).withCumulative("z", 10).withCumulative("z", 15);


		assertEquals(a.difference(a), empty);
		assertEquals(abcd.difference(a), bcd);
		assertEquals(wxz.difference(wz), xz);
	}

	public void testConcatenate() {
		RichTextAttributes empty = new RichTextAttributes();
		RichTextAttributes a = new RichTextAttributes().withOverride("a", 1);
		RichTextAttributes abcd = new RichTextAttributes().withOverride("a", 1).withOverride("b", 2).withOverride("c", 3).withOverride("d", 4);
		RichTextAttributes bcd = new RichTextAttributes().withOverride("b", 2).withOverride("c", 3).withOverride("d", 4);

		RichTextAttributes wxz = new RichTextAttributes().withOverride("w", 0).withOverride("x", 1).withCumulative("z", 3).withCumulative("z", 4).withCumulative("z", 5).withCumulative("z", 10).withCumulative("z", 15);
		RichTextAttributes wz = new RichTextAttributes().withOverride("w", 0).withCumulative("z", 3).withCumulative("z", 4);
		RichTextAttributes xz = new RichTextAttributes().withOverride("x", 1).withCumulative("z", 5).withCumulative("z", 10).withCumulative("z", 15);


		assertEquals(a.concatenate(empty), a);
		assertEquals(a.concatenate(bcd), abcd);
		assertEquals(wz.concatenate(xz), wxz);
	}
}
