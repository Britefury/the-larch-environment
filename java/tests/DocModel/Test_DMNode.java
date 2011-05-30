//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.util.Arrays;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMSchema;

public class Test_DMNode extends TestCase
{
	private static DMSchema m = new DMSchema( "m", "m", "tests.DocModel.Test_DMNode.m" );
	private static DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );

	
	public void testTreeReorganise_become()
	{
		DMObject a = A.newInstance( new Object[] { "i", "x" } );
		DMObject b = A.newInstance( new Object[] { "j", a } );
		DMObject c = A.newInstance( new Object[] { "k", b } );
		DMObject d = A.newInstance( new Object[] { "l", c } );
		DMObject e = A.newInstance( new Object[] { "m", d } );
		
		e.realiseAsRoot();
		
		DMObject p = A.newInstance( new Object[] { "w", b } );
		DMObject q = A.newInstance( new Object[] { "x", p } );
		
		// e -> d -> c -> b -> a
		// q -> p -> ^(b)
		
		// Check values
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "i", "x" } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "j", a } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "k", b } ) );
		assertEquals( Arrays.asList( d.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "l", c } ) );
		assertEquals( Arrays.asList( e.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "m", d } ) );
		assertEquals( Arrays.asList( p.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "w", b } ) );
		assertEquals( Arrays.asList( q.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "x", p } ) );
		
		// Check parentage
		assertSame( a.getParent(), b );
		assertSame( b.getParent(), c );
		assertSame( c.getParent(), d );
		assertSame( d.getParent(), e );
		assertSame( e.getParent(), null );
		assertSame( p.getParent(), null );
		assertSame( q.getParent(), null );
		assertTrue( a.isRealised() );
		assertTrue( b.isRealised() );
		assertTrue( c.isRealised() );
		assertTrue( d.isRealised() );
		assertTrue( e.isRealised() );
		assertFalse( p.isRealised() );
		assertFalse( q.isRealised() );
		
		
		// d become q
		d.become( q );
		
		// Check values
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "i", "x" } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "j", a } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "k", b } ) );
		assertEquals( Arrays.asList( d.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "x", p } ) );
		assertEquals( Arrays.asList( e.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "m", d } ) );
		assertEquals( Arrays.asList( p.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "w", b } ) );
		assertEquals( Arrays.asList( q.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "x", p } ) );
		
		
		// Check parentage
		assertSame( a.getParent(), b );
		assertSame( b.getParent(), p );
		assertSame( c.getParent(), null );
		assertSame( d.getParent(), e );
		assertSame( e.getParent(), null );
		assertSame( p.getParent(), d );
		assertSame( q.getParent(), null );
		assertTrue( a.isRealised() );
		assertTrue( b.isRealised() );
		assertFalse( c.isRealised() );
		assertTrue( d.isRealised() );
		assertTrue( e.isRealised() );
		assertTrue( p.isRealised() );
		assertFalse( q.isRealised() );
		
	}




	public void testTreeReorganise_set()
	{
		DMObject a = A.newInstance( new Object[] { "i", "x" } );
		DMObject b = A.newInstance( new Object[] { "j", a } );
		DMObject c = A.newInstance( new Object[] { "k", b } );
		DMObject d = A.newInstance( new Object[] { "l", c } );
		DMObject e = A.newInstance( new Object[] { "m", d } );
		
		e.realiseAsRoot();
		
		DMObject p = A.newInstance( new Object[] { "w", b } );
		DMObject q = A.newInstance( new Object[] { "x", p } );
		
		// e -> d -> c -> b -> a
		// q -> p -> ^(b)
		
		// Check values
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "i", "x" } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "j", a } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "k", b } ) );
		assertEquals( Arrays.asList( d.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "l", c } ) );
		assertEquals( Arrays.asList( e.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "m", d } ) );
		assertEquals( Arrays.asList( p.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "w", b } ) );
		assertEquals( Arrays.asList( q.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "x", p } ) );
		
		// Check parentage
		assertSame( a.getParent(), b );
		assertSame( b.getParent(), c );
		assertSame( c.getParent(), d );
		assertSame( d.getParent(), e );
		assertSame( e.getParent(), null );
		assertSame( p.getParent(), null );
		assertSame( q.getParent(), null );
		assertTrue( a.isRealised() );
		assertTrue( b.isRealised() );
		assertTrue( c.isRealised() );
		assertTrue( d.isRealised() );
		assertTrue( e.isRealised() );
		assertFalse( p.isRealised() );
		assertFalse( q.isRealised() );
		
		
		// e.set( "y", q );
		e.set( "y", q );
		
		// Check values
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "i", "x" } ) );
		assertEquals( Arrays.asList( b.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "j", a } ) );
		assertEquals( Arrays.asList( c.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "k", b } ) );
		assertEquals( Arrays.asList( d.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "l", c } ) );
		assertEquals( Arrays.asList( e.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "m", q } ) );
		assertEquals( Arrays.asList( p.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "w", b } ) );
		assertEquals( Arrays.asList( q.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "x", p } ) );
		
		
		// Check parentage
		assertSame( a.getParent(), b );
		assertSame( b.getParent(), p );
		assertSame( c.getParent(), null );
		assertSame( d.getParent(), null );
		assertSame( e.getParent(), null );
		assertSame( p.getParent(), q );
		assertSame( q.getParent(), e );
		assertTrue( a.isRealised() );
		assertTrue( b.isRealised() );
		assertFalse( c.isRealised() );
		assertFalse( d.isRealised() );
		assertTrue( e.isRealised() );
		assertTrue( p.isRealised() );
		assertTrue( q.isRealised() );
	}
}
