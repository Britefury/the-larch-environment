//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.Util;

import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.DocModel.DMIOReader;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Util.LiveTrackedList;
import junit.framework.TestCase;
import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PySlice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Test_LiveTrackedList extends TestCase {
	private class ChangeCounter implements LiveTrackedList.Listener {
		public int count = 0;
		public Object oldContents[], newContents[];

		public void onChange(Object[] oldContents, Object[] newContents) {
			count++;
			this.oldContents = oldContents;
			this.newContents = newContents;
		}

		public void assertOldContentsEquals(Object expected[]) {
			assertEquals(Arrays.asList(expected), Arrays.asList(oldContents));
		}

		public void assertOldContentsEquals(List<Object> expected) {
			assertEquals(expected, Arrays.asList(oldContents));
		}

		public void assertOldContentsEqualsSX(String expectedSX) {
			assertOldContentsEquals(readListSX(expectedSX));
		}

		public void assertNewContentsEquals(Object expected[]) {
			assertEquals(Arrays.asList(expected), Arrays.asList(newContents));
		}

		public void assertNewContentsEquals(List<Object> expected) {
			assertEquals(expected, Arrays.asList(newContents));
		}

		public void assertNewContentsEqualsSX(String expectedSX) {
			assertNewContentsEquals(readListSX(expectedSX));
		}


		public void checkSX(int expectedCount, String expectedOldSX, String expectedNewSX) {
			assertEquals(expectedCount, count);
			assertOldContentsEqualsSX(expectedOldSX);
			assertNewContentsEqualsSX(expectedNewSX);
		}
	}



	ChangeHistory history;

	public void setUp()
	{
		history = new ChangeHistory();
	}

	public void tearDown()
	{
		history = null;
	}



	private static Object coerce(Object obj) {
		if (obj instanceof List) {
			LiveTrackedList live = new LiveTrackedList();
			for (Object x: (List<?>)obj) {
				live.add(coerce(x));
			}
			return live;
		}
		else {
			return obj;
		}
	}

	private static boolean compareListToArray(List<Object> x, Object y[]) {
		return x.equals(Arrays.asList(y));
	}

	public void cmpListSX(List<Object> xs, String expectedSX)
	{
		try
		{
			Object expected = DMIOReader.readFromString(expectedSX);
			assertEquals( expected, xs );
		}
		catch (DMIOReader.ParseErrorException e)
		{
			System.out.println( "Could not parse expected SX" );
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Object> readListSX(String sx)
	{
		try
		{
			List<Object> xs = (List<Object>) DMIOReader.readFromString(sx);
			return xs;
		}
		catch (DMIOReader.ParseErrorException e)
		{
			System.out.println( "Could not parse" );
			fail();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public LiveTrackedList readLiveListSX(String sx)
	{
		try
		{
			List<Object> xs = (List<Object>) DMIOReader.readFromString(sx);
			return (LiveTrackedList)coerce(xs);
		}
		catch (DMIOReader.ParseErrorException e)
		{
			System.out.println( "Could not parse" );
			fail();
			return null;
		}
	}


	List<Object> iterToList(Iterator<Object> iter)
	{
		ArrayList<Object> l = new ArrayList<Object>();

		while ( iter.hasNext() )
		{
			l.add( iter.next() );
		}
		return l;
	}


	public LiveTrackedList readTrackedDMListSX(String sx)
	{
		LiveTrackedList xs = readLiveListSX(sx);
		history.track( xs );
		return xs;
	}


	public void assertTracked(LiveTrackedList xs)
	{
		assertSame( xs.getChangeHistory(), history );
	}

	public void assertNotTracked(LiveTrackedList xs)
	{
		assertNull( xs.getChangeHistory() );
	}



	public void testConstructor()
	{
		LiveTrackedList xs = readLiveListSX("[a b c `null`]");
		cmpListSX( xs, "[a b c `null`]" );

		assertEquals( xs.get( 0 ), "a" );
		assertEquals( xs.get( 1 ), "b" );
		assertEquals( xs.get( 2 ), "c" );


		LiveTrackedList ys = readLiveListSX("[a b c [x y z]]");
		cmpListSX( ys, "[a b c [x y z]]" );
	}

	public void testCell()
	{
		final LiveTrackedList xs = readLiveListSX("[a b c]");

		LiveFunction.Function eval = new LiveFunction.Function()
		{
			public Object evaluate()
			{
				return xs.size();
			}
		};
		LiveFunction yCell = new LiveFunction();
		yCell.setFunction( eval );

		assertEquals( yCell.getValue(), 3 );

		xs.add( "xyz" );

		assertEquals( yCell.getValue(), 4 );
	}



	public void testAdd()
	{
		LiveTrackedList xs = readTrackedDMListSX( "[a b c]" );
		LiveTrackedList ys = readLiveListSX("[x y z]");
		assertTracked( xs );
		assertNotTracked(ys);

		ChangeCounter listener = new ChangeCounter();
		xs.setListener(listener);

		xs.add( "xyz" );
		cmpListSX( xs, "[a b c xyz]" );
		listener.checkSX(1, "[a b c]", "[a b c xyz]");
		xs.add(null);
		cmpListSX(xs, "[a b c xyz `null`]");
		listener.checkSX(2, "[a b c xyz]", "[a b c xyz `null`]");
		xs.add(ys);
		cmpListSX(xs, "[a b c xyz `null` [x y z]]");
		listener.checkSX(3, "[a b c xyz `null`]", "[a b c xyz `null` [x y z]]");
		assertTracked( xs );
		assertTracked( ys );

		history.undo();
		cmpListSX( xs, "[a b c xyz `null`]" );
		listener.checkSX(4, "[a b c xyz `null` [x y z]]", "[a b c xyz `null`]");
		assertTracked( xs );
		assertNotTracked( ys );
		history.undo();
		cmpListSX( xs, "[a b c xyz]" );
		listener.checkSX(5, "[a b c xyz `null`]", "[a b c xyz]");
		history.undo();
		cmpListSX( xs, "[a b c]" );
		listener.checkSX(6, "[a b c xyz]", "[a b c]");
		history.redo();
		cmpListSX( xs, "[a b c xyz]" );
		listener.checkSX(7, "[a b c]", "[a b c xyz]");
		history.redo();
		cmpListSX( xs, "[a b c xyz `null`]" );
		listener.checkSX(8, "[a b c xyz]", "[a b c xyz `null`]");
		history.redo();
		cmpListSX( xs, "[a b c xyz `null` [x y z]]" );
		listener.checkSX(9, "[a b c xyz `null`]", "[a b c xyz `null` [x y z]]");
		assertTracked( xs );
		assertTracked( ys );
	}



	public void testAdd_insert()
	{
		LiveTrackedList xs = readTrackedDMListSX( "[a b c]" );
		LiveTrackedList ys = readLiveListSX("[x y z]");
		assertTracked( xs );
		assertNotTracked( ys );

		ChangeCounter listener = new ChangeCounter();
		xs.setListener(listener);

		xs.add( 2, "xyz" );
		cmpListSX( xs, "[a b xyz c]" );
		listener.checkSX(1, "[a b c]", "[a b xyz c]");
		xs.add( 2, null );
		cmpListSX( xs, "[a b `null` xyz c]" );
		listener.checkSX(2, "[a b xyz c]", "[a b `null` xyz c]");
		xs.add( 2, ys );
		cmpListSX( xs, "[a b [x y z] `null` xyz c]" );
		listener.checkSX(3, "[a b `null` xyz c]", "[a b [x y z] `null` xyz c]");
		assertTracked( xs );
		assertTracked( ys );

		history.undo();
		cmpListSX( xs, "[a b `null` xyz c]" );
		listener.checkSX(4, "[a b [x y z] `null` xyz c]", "[a b `null` xyz c]");
		assertTracked( xs );
		assertNotTracked( ys );
		history.undo();
		cmpListSX( xs, "[a b xyz c]" );
		history.undo();
		cmpListSX( xs, "[a b c]" );
		history.redo();
		cmpListSX( xs, "[a b xyz c]" );
		history.redo();
		cmpListSX( xs, "[a b `null` xyz c]" );
		history.redo();
		cmpListSX( xs, "[a b [x y z] `null` xyz c]" );
		listener.checkSX(9, "[a b `null` xyz c]", "[a b [x y z] `null` xyz c]");
		assertTracked( xs );
		assertTracked( ys );
	}



	public void testAddAll()
	{
		LiveTrackedList xs = readTrackedDMListSX( "[a b c]" );
		LiveTrackedList ys = readLiveListSX("[x y z `null`]");
		LiveTrackedList zs = readLiveListSX("[i j [k l] m n]");
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );

		ChangeCounter listener = new ChangeCounter();
		xs.setListener(listener);

		xs.addAll(ys);
		cmpListSX(xs, "[a b c x y z `null`]");
		listener.checkSX(1, "[a b c]", "[a b c x y z `null`]");
		assertTracked(xs);
		assertNotTracked( ys );
		assertNotTracked(zs);
		xs.addAll(zs);
		cmpListSX(xs, "[a b c x y z `null` i j [k l] m n]");
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );

		history.undo();
		cmpListSX(xs, "[a b c x y z `null`]");
		assertTracked(xs);
		assertNotTracked( ys );
		assertNotTracked(zs);
		history.undo();
		cmpListSX(xs, "[a b c]");
		listener.checkSX(4, "[a b c x y z `null`]", "[a b c]");
		assertTracked(xs);
		assertNotTracked( ys );
		assertNotTracked(zs);
		history.redo();
		cmpListSX(xs, "[a b c x y z `null`]");
		listener.checkSX(5, "[a b c]", "[a b c x y z `null`]");
		assertTracked(xs);
		assertNotTracked( ys );
		assertNotTracked(zs);
		history.redo();
		cmpListSX(xs, "[a b c x y z `null` i j [k l] m n]");
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
	}


	public void testAddAll_insert()
	{
		LiveTrackedList xs = readTrackedDMListSX( "[a b c]" );
		LiveTrackedList ys = readLiveListSX("[x y z `null`]");
		LiveTrackedList zs = readLiveListSX("[i j [k l] m n]");
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked(zs);

		ChangeCounter listener = new ChangeCounter();
		xs.setListener(listener);

		xs.addAll(2, ys);
		cmpListSX(xs, "[a b x y z `null` c]");
		listener.checkSX(1, "[a b c]", "[a b x y z `null` c]");
		assertTracked(xs);
		assertNotTracked( ys );
		assertNotTracked(zs);
		xs.addAll(2, zs);
		cmpListSX(xs, "[a b i j [k l] m n x y z `null` c]");
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );

		history.undo();
		cmpListSX(xs, "[a b x y z `null` c]");
		assertTracked(xs);
		assertNotTracked( ys );
		assertNotTracked(zs);
		history.undo();
		cmpListSX(xs, "[a b c]");
		listener.checkSX(4, "[a b x y z `null` c]", "[a b c]");
		assertTracked(xs);
		assertNotTracked( ys );
		assertNotTracked(zs);
		history.redo();
		cmpListSX(xs, "[a b x y z `null` c]");
		listener.checkSX(5, "[a b c]", "[a b x y z `null` c]");
		assertTracked(xs);
		assertNotTracked( ys );
		assertNotTracked(zs);
		history.redo();
		cmpListSX(xs, "[a b i j [k l] m n x y z `null` c]");
		assertTracked( xs );
		assertNotTracked( ys );
		assertNotTracked( zs );
	}


	public void testClear()
	{
		LiveTrackedList xs = readTrackedDMListSX( "[a b c [x y]]" );
		LiveTrackedList zs = (LiveTrackedList)xs.get( 3 );

		assertTracked( xs );
		assertTracked( (LiveTrackedList)zs );

		ChangeCounter listener = new ChangeCounter();
		xs.setListener(listener);

		xs.clear();
		cmpListSX(xs, "[]");
		listener.checkSX(1, "[a b c [x y]]", "[]");
		assertTracked( xs );
		assertNotTracked( (LiveTrackedList)zs );

		history.undo();
		cmpListSX(xs, "[a b c [x y]]");
		listener.checkSX(2, "[]", "[a b c [x y]]");
		assertTracked( xs );
		assertTracked( (LiveTrackedList)zs );
		history.redo();
		cmpListSX( xs, "[]" );
		listener.checkSX(3, "[a b c [x y]]", "[]");
		assertTracked( xs );
		assertNotTracked( (LiveTrackedList)zs );
	}



	public void testContains()
	{
		LiveTrackedList xs = readLiveListSX("[a b c `null`]");
		assertTrue( xs.contains( "a" ) );
		assertTrue( xs.contains( "b" ) );
		assertTrue( xs.contains( "c" ) );
		assertTrue( xs.contains( null ) );
		assertFalse( xs.contains( "d" ) );
	}


	public void testContainsAll()
	{
		LiveTrackedList xs = readLiveListSX("[a b c `null`]");
		LiveTrackedList ys = readLiveListSX("[c a]");
		LiveTrackedList zs = readLiveListSX("[c d]");
		LiveTrackedList zzs = readLiveListSX("[c `null`]");
		assertTrue( xs.containsAll( ys ) );
		assertTrue( xs.containsAll( zzs ) );
		assertFalse( xs.containsAll( zs ) );
	}



	public void testEquals()
	{
		LiveTrackedList xs = readLiveListSX("[a b c `null`]");
		LiveTrackedList ys = readLiveListSX("[a b c `null`]");
		assertTrue( xs.equals( ys ) );
	}



	public void testGet()
	{
		LiveTrackedList xs = readLiveListSX("[a b c `null`]");
		assertEquals( xs.get( 0 ), "a" );
		assertEquals( xs.get( 1 ), "b" );
		assertEquals( xs.get( 2 ), "c" );
		assertEquals( xs.get( 3 ), null );
	}



	public void testIndexOf()
	{
		LiveTrackedList xs = readLiveListSX("[a b c `null`]");
		assertEquals( xs.indexOf( "a" ), 0 );
		assertEquals( xs.indexOf( "b" ), 1 );
		assertEquals( xs.indexOf( "c" ), 2 );
		assertEquals( xs.indexOf( null ), 3 );
	}


	public void testIsEmpty()
	{
		assertFalse( readLiveListSX("[a b c]").isEmpty() );
		assertTrue( readLiveListSX("[]").isEmpty() );
	}


	public void testIterator()
	{
		LiveTrackedList xs = readLiveListSX("[a b c `null`]");
		assertEquals( xs, iterToList( xs.iterator() ) );
	}


	public void testLastIndexOf()
	{
		LiveTrackedList xs = readLiveListSX("[a b c `null` a b c `null`]");
		assertEquals( xs.lastIndexOf( "a" ), 4 );
		assertEquals( xs.lastIndexOf( "b" ), 5 );
		assertEquals( xs.lastIndexOf( "c" ), 6 );
		assertEquals( xs.lastIndexOf( null ), 7 );
	}


	public void testListIterator()
	{
		LiveTrackedList xs = readLiveListSX("[a b c `null`]");
		assertEquals( xs, iterToList( xs.listIterator() ) );
	}


	public void testListIteratorFrom()
	{
		LiveTrackedList xs = readLiveListSX("[a b c `null`]");
		cmpListSX( iterToList( xs.listIterator( 0 ) ), "[a b c `null`]" );
		cmpListSX( iterToList( xs.listIterator( 1 ) ), "[b c `null`]" );
		cmpListSX( iterToList( xs.listIterator( 2 ) ), "[c `null`]" );
		cmpListSX( iterToList( xs.listIterator( 3 ) ), "[`null`]" );
		cmpListSX( iterToList( xs.listIterator( 4 ) ), "[]" );
	}


	public void testRemove()
	{
		LiveTrackedList xs = readTrackedDMListSX( "[a b c `null` [x y]]" );
		LiveTrackedList zs = (LiveTrackedList)xs.get( 4 );
		assertTracked( xs );
		assertTracked((LiveTrackedList) zs);

		ChangeCounter listener = new ChangeCounter();
		xs.setListener(listener);

		xs.remove(1);
		cmpListSX(xs, "[a c `null` [x y]]");
		listener.checkSX(1, "[a b c `null` [x y]]", "[a c `null` [x y]]");
		xs.remove(3);
		cmpListSX(xs, "[a c `null`]");
		assertTracked( xs );
		assertNotTracked( (LiveTrackedList)zs );

		history.undo();
		cmpListSX(xs, "[a c `null` [x y]]");
		assertTracked(xs);
		assertTracked((LiveTrackedList) zs);
		history.undo();
		cmpListSX(xs, "[a b c `null` [x y]]");
		listener.checkSX(4, "[a c `null` [x y]]", "[a b c `null` [x y]]");
		history.redo();
		cmpListSX(xs, "[a c `null` [x y]]");
		listener.checkSX(5, "[a b c `null` [x y]]", "[a c `null` [x y]]");
		history.redo();
		cmpListSX(xs, "[a c `null`]");
		assertTracked( xs );
		assertNotTracked( (LiveTrackedList)zs );
	}

	public void testRemoveObject()
	{
		LiveTrackedList xs = readTrackedDMListSX( "[a b c `null` [x y]]" );
		LiveTrackedList zs = (LiveTrackedList)xs.get( 4 );
		assertTracked( xs );
		assertTracked((LiveTrackedList) zs);

		ChangeCounter listener = new ChangeCounter();
		xs.setListener(listener);

		xs.remove("b");
		cmpListSX(xs, "[a c `null` [x y]]");
		listener.checkSX(1, "[a b c `null` [x y]]", "[a c `null` [x y]]");
		xs.remove(null);
		cmpListSX(xs, "[a c [x y]]");
		xs.remove(zs);
		cmpListSX(xs, "[a c]");
		assertTracked(xs);
		assertNotTracked( (LiveTrackedList)zs );

		history.undo();
		cmpListSX(xs, "[a c [x y]]");
		assertTracked(xs);
		assertTracked((LiveTrackedList) zs);
		history.undo();
		cmpListSX(xs, "[a c `null` [x y]]");
		history.undo();
		cmpListSX(xs, "[a b c `null` [x y]]");
		listener.checkSX(6, "[a c `null` [x y]]", "[a b c `null` [x y]]");
		history.redo();
		cmpListSX(xs, "[a c `null` [x y]]");
		listener.checkSX(7, "[a b c `null` [x y]]", "[a c `null` [x y]]");
		history.redo();
		cmpListSX(xs, "[a c [x y]]");
		history.redo();
		cmpListSX(xs, "[a c]");
		assertTracked(xs);
		assertNotTracked((LiveTrackedList) zs);
	}


	public void testSet()
	{
		LiveTrackedList xs = readTrackedDMListSX( "[a [i j] c]" );
		Object ij = xs.get( 1 );
		LiveTrackedList pq = readLiveListSX("[p q]");

		ChangeCounter listener = new ChangeCounter();
		xs.setListener(listener);

		cmpListSX(xs, "[a [i j] c]");
		assertTracked(xs);
		assertTracked( (LiveTrackedList)ij );
		assertNotTracked(pq);

		xs.set(1, null);
		cmpListSX(xs, "[a `null` c]");
		listener.checkSX(1, "[a [i j] c]", "[a `null` c]");
		assertTracked(xs);
		assertNotTracked((LiveTrackedList) ij);
		assertNotTracked(pq);

		xs.set(1, pq);
		cmpListSX(xs, "[a [p q] c]");
		assertTracked(xs);
		assertNotTracked((LiveTrackedList) ij);
		assertTracked(pq);


		history.undo();
		cmpListSX(xs, "[a `null` c]");
		assertTracked( xs );
		assertNotTracked((LiveTrackedList) ij);
		assertNotTracked(pq);

		history.undo();
		cmpListSX(xs, "[a [i j] c]");
		listener.checkSX(4, "[a `null` c]", "[a [i j] c]");
		assertTracked(xs);
		assertTracked((LiveTrackedList) ij);
		assertNotTracked( pq );

		history.redo();
		cmpListSX(xs, "[a `null` c]");
		listener.checkSX(5, "[a [i j] c]", "[a `null` c]");
		assertTracked( xs );
		assertNotTracked((LiveTrackedList) ij);
		assertNotTracked( pq );

		history.redo();
		cmpListSX(xs, "[a [p q] c]");
		assertTracked(xs);
		assertNotTracked((LiveTrackedList) ij);
		assertTracked( pq );
	}


	public void test__setitem__slice()
	{
		LiveTrackedList xs = readTrackedDMListSX( "[a b [i j] c]" );
		Object ij = xs.get( 2 );
		LiveTrackedList pq = readLiveListSX("[[p] q]");
		LiveTrackedList p = (LiveTrackedList)pq.get( 0 );

		cmpListSX(xs, "[a b [i j] c]");
		assertTracked( xs );
		assertTracked( (LiveTrackedList)ij );
		assertNotTracked( pq );
		assertNotTracked( p );

		ChangeCounter listener = new ChangeCounter();
		xs.setListener(listener);

		xs.__setitem__(new PySlice(new PyInteger(1), new PyInteger(-1), Py.None), Arrays.asList(new Object[]{null}));
		cmpListSX(xs, "[a `null` c]");
		listener.checkSX(1, "[a b [i j] c]", "[a `null` c]");
		assertTracked( xs );
		assertNotTracked( (LiveTrackedList)ij );
		assertNotTracked( pq );
		assertNotTracked( p );

		xs.__setitem__(new PySlice(new PyInteger(1), new PyInteger(-1), Py.None), Arrays.asList(new Object[]{pq}));
		cmpListSX(xs, "[a [[p] q] c]");
		listener.checkSX(2, "[a `null` c]", "[a [[p] q] c]");
		assertTracked( xs );
		assertNotTracked( (LiveTrackedList)ij );
		assertTracked( pq );
		assertTracked( p );


		history.undo();
		cmpListSX(xs, "[a `null` c]");
		listener.checkSX(3, "[a [[p] q] c]", "[a `null` c]");
		assertTracked( xs );
		assertNotTracked( (LiveTrackedList)ij );
		assertNotTracked( pq );
		assertNotTracked( p );

		history.undo();
		cmpListSX(xs, "[a b [i j] c]");
		listener.checkSX(4, "[a `null` c]", "[a b [i j] c]");
		assertTracked( xs );
		assertTracked( (LiveTrackedList)ij );
		assertNotTracked( pq );
		assertNotTracked( p );

		history.redo();
		cmpListSX(xs, "[a `null` c]");
		listener.checkSX(5, "[a b [i j] c]", "[a `null` c]");
		assertTracked( xs );
		assertNotTracked( (LiveTrackedList)ij );
		assertNotTracked( pq );
		assertNotTracked( p );

		history.redo();
		cmpListSX(xs, "[a [[p] q] c]");
		listener.checkSX(6, "[a `null` c]", "[a [[p] q] c]");
		assertTracked( xs );
		assertNotTracked( (LiveTrackedList)ij );
		assertTracked( pq );
		assertTracked( p );
	}


	public void testSize()
	{
		assertEquals(readLiveListSX("[a b c]").size(), 3);
	}


	public void testSublist()
	{
		LiveTrackedList xs = readLiveListSX("[a b c d e f]");
		cmpListSX( xs.subList( 1, 5 ), "[b c d e]" );
	}


	public void testToArray()
	{
		String[] strs = { "a", "b", "c" };
		assertEquals( Arrays.asList( readLiveListSX("[a b c]").toArray() ), Arrays.asList( strs ) );

		String[] a = { "a", "b", "c" };
		assertEquals( Arrays.asList( readLiveListSX("[a b c]").toArray(a) ), Arrays.asList( strs ) );
	}



	public void test_trackTree()
	{
		LiveTrackedList xs = readTrackedDMListSX( "[a b [c d [e f]]]" );
		cmpListSX( xs, "[a b [c d [e f]]]" );

		((LiveTrackedList)((LiveTrackedList)xs.get( 2 )).get( 2 )).set( 0, "x" );
		cmpListSX( xs, "[a b [c d [x f]]]" );

		history.undo();
		cmpListSX( xs, "[a b [c d [e f]]]" );

		history.redo();
		cmpListSX( xs, "[a b [c d [x f]]]" );


		LiveTrackedList ys = readLiveListSX("[p q [s t]]");

		((LiveTrackedList)((LiveTrackedList)xs.get( 2 )).get( 2 )).set( 0, ys );
		cmpListSX( xs, "[a b [c d [[p q [s t]] f]]]" );

		((LiveTrackedList)((LiveTrackedList)((LiveTrackedList)((LiveTrackedList)xs.get( 2 )).get( 2 )).get( 0 )).get( 2 )).set( 1, "h" );
		cmpListSX( xs, "[a b [c d [[p q [s h]] f]]]" );

		history.undo();
		cmpListSX( xs, "[a b [c d [[p q [s t]] f]]]" );

		history.undo();
		cmpListSX( xs, "[a b [c d [x f]]]" );

		history.redo();
		cmpListSX( xs, "[a b [c d [[p q [s t]] f]]]" );

		history.redo();
		cmpListSX( xs, "[a b [c d [[p q [s h]] f]]]" );
	}
}
