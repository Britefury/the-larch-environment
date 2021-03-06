//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.DocModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.DocModel.DMList;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMSchema;

public class Test_DMObject extends TestCase
{
	private ChangeHistory history;
	private static DMSchema m = new DMSchema( "m", "m", "tests.DocModel.Test_DMObject.m" );
	private static DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
	private static DMObjectClass B = m.newClass( "B", new String[] { "p", "q", "r" } );
	
	
	public void setUp()
	{
		history = new ChangeHistory();
	}
	
	public void tearDown()
	{
		history = null;
	}

	
	
	public void test_getDMClass()
	{
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		assertSame( a.getDMObjectClass(), A );
	}

		
	public void test_getFieldIndex()
	{
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		assertEquals( a.getFieldIndex( "x" ), 0 );
		assertEquals( a.getFieldIndex( "y" ), 1 );
	}


	public void test_DMObject_getFieldNames()
	{
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		assertEquals( Arrays.asList( a.getFieldNames() ), Arrays.asList( new String[] { "x", "y" } ) );
	}

		
	public void test_DMObject_getFieldValuesImmutable()
	{
		DMObject a = A.newInstance( new Object[] { "a", null } );
		history.track( a );
		
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", null } ) );
	}

	
	public void test_get()
	{
		DMObject a = A.newInstance( new Object[] { "a", null } );
		history.track( a );
		
		assertEquals( a.get( 0 ), "a" );
		assertEquals( a.get( 1 ), null );
		assertEquals( a.get( "x" ), "a" );
		assertEquals( a.get( "y" ), null );
	}


	public void test_getitem()
	{
		DMObject a = A.newInstance( new Object[] { "a", null } );
		history.track( a );
		
		assertEquals( a.__getitem__( 0 ), "a" );
		assertEquals( a.__getitem__( 1 ), null );
		assertEquals( a.__getitem__( "x" ), "a" );
		assertEquals( a.__getitem__( "y" ), null );
	}


	public void test_set()
	{
		DMList ij = new DMList();
		ij.extend( Arrays.asList( new Object[] { "i", "j" } ) );
		DMList pq = new DMList();
		pq.extend( Arrays.asList( new Object[] { "p", "q" } ) );
		
		DMObject a = A.newInstance( new Object[] { ij, "p" } );
		a.realiseAsRoot();
		history.track( a );
		
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { ij, "p" } ) );
		assertSame( ij.getParent(), a );
		assertSame( pq.getParent(), null );

		a.set( 0, "b" );
		a.set( 1, "q" );
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "q" } ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), null );
		
		a.set( "x", pq );
		a.set( "y", null );
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { pq, null } ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), a );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { pq, "q" } ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), a );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "q" } ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), null );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "p" } ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), null );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { ij, "p" } ) );
		assertSame( ij.getParent(), a );
		assertSame( pq.getParent(), null );
		
		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "p" } ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), null );
		
		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "q" } ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), null );
		
		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { pq, "q" } ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), a );
		
		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { pq, null } ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), a );
	}


	public void test_update()
	{
		DMList ij = new DMList();
		ij.extend( Arrays.asList( new Object[] { "i", "j" } ) );
		DMList pq = new DMList();
		pq.extend( Arrays.asList( new Object[] { "p", "q" } ) );
		
		DMObject a = A.newInstance( new Object[] { "a", ij } );
		a.realiseAsRoot();
		history.track( a );
		
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", ij } ) );
		assertSame( ij.getParent(), a );
		assertSame( pq.getParent(), null );

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put( "x", pq );
		data.put( "y", null );
		a.update( data );

		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { pq, null } ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), a );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", ij } ) );
		assertSame( ij.getParent(), a );
		assertSame( pq.getParent(), null );

		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { pq, null } ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), a );
	}


	public void test_become()
	{
		DMList ij = new DMList();
		ij.extend( Arrays.asList( new Object[] { "i", "j" } ) );
		DMList pq = new DMList();
		pq.extend( Arrays.asList( new Object[] { "p", "q" } ) );

		DMObject a = A.newInstance( new Object[] { "a", ij } );
		DMObject b = B.newInstance( new Object[] { "c", "d", pq } );
		a.realiseAsRoot();
		b.realiseAsRoot();
		history.track( a );
		
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", ij } ) );
		assertTrue( a.isInstanceOf( A ) );
		assertSame( ij.getParent(), a );			// a -> ij
		assertSame( pq.getParent(), b );		// b -> pq

		a.become( b );						// a -> pq, b -> pq

		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "d", pq } ) );
		assertTrue( a.isInstanceOf( B ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), a );		// a has displaced b

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", ij } ) );
		assertTrue( a.isInstanceOf( A ) );
		assertSame( ij.getParent(), a );
		assertSame( pq.getParent(), null );		// b does not return

		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "d", pq } ) );
		assertTrue( a.isInstanceOf( B ) );
		assertSame( ij.getParent(), null );
		assertSame( pq.getParent(), a );		// a has displaced b
	}


	public void test_setitem()
	{
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		a.__setitem__( 0, "e" );
		a.__setitem__( "y", null );

		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "e", null } ) );
	}

	public void test_getChildren()
	{
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		List<Object> ys = new ArrayList<Object>();
		for (Object x: a.getChildren())
		{
			ys.add( x );
		}
		
		assertEquals( ys, Arrays.asList( new Object[] { "a", "p" } ) );
	}

	
	
	public void test_DMObject_constructors()
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put( "x", "d" );
		data.put( "y", "s" );

		DMObject a0 = new DMObject( A, new Object[] { "a", "b" } );
		DMObject a1 = new DMObject( A, new String[] { "y", "x" }, new Object[] { "a", "b" } );
		DMObject a2 = new DMObject( A, data );
		
		DMObject a3 = A.newInstance( new Object[] { "a", "b" } );
		DMObject a4 = A.newInstance( new String[] { "y", "x" }, new Object[] { "a", null } );
		DMObject a5 = A.newInstance( data );

		assertEquals( Arrays.asList( a0.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", "b" } ) );
		assertEquals( Arrays.asList( a1.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "a" } ) );
		assertEquals( Arrays.asList( a2.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", "s" } ) );

		assertEquals( Arrays.asList( a3.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", "b" } ) );
		assertEquals( Arrays.asList( a4.getFieldValuesImmutable() ), Arrays.asList( new Object[] { null, "a" } ) );
		assertEquals( Arrays.asList( a5.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", "s" } ) );
	}
	
	
	
	public void test_trackTree()
	{
		DMObject c = A.newInstance( new Object[] { "c", "r" } );
		DMObject b = A.newInstance( new Object[] { "b", "q" } );
		DMObject a = A.newInstance( new Object[] { "a", b } );
		history.track( a );

		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", b } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "q" } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "r" } ) );
		
		((DMObject)a.get( "y" )).set( "x", "d" );
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", b } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", "q" } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "r" } ) );

		((DMObject)a.get( "y" )).set( "y", c );
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", b } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", c } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "r" } ) );

		((DMObject)((DMObject)a.get( "y" )).get( "y" )).set( "y", "h" );
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", b } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", c } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "h" } ) );
		
		
		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", b } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", c } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "r" } ) );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", b } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", "q" } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "r" } ) );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", b } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "q" } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "r" } ) );

	
		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", b } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", "q" } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "r" } ) );

		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", b } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", c } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "r" } ) );

		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", b } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", c } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "h" } ) );
	}
	
	
	public void test_equals()
	{
		DMObject c0 = A.newInstance( new Object[] { "c", null } );
		DMObject c0D = A.newInstance( new Object[] { "c", null } );
		
		DMObject c1 = A.newInstance( new Object[] { "c", c0 } );
		DMObject c1D = A.newInstance( new Object[] { "c", c0D } );

		assertEquals( c0, c0D );
		assertEquals( c1, c1D );
	}
}
