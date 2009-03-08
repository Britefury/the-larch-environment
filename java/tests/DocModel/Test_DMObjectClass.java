//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectField;
import junit.framework.TestCase;

public class Test_DMObjectClass extends TestCase
{
	public void test_Constructor_fields()
	{
		DMObjectField f1[] = { new DMObjectField( "x" ) };
		DMObjectClass a0 = new DMObjectClass( "A", new DMObjectField[] { } );
		DMObjectClass a1 = new DMObjectClass( "AA", f1 );
		
		assertEquals( a0.getName(), "A" );
		assertEquals( a1.getName(), "AA" );

		assertSame( a0.getSuperclass(), null );
		assertSame( a1.getSuperclass(), null );

		assertTrue( a0.isSubclassOf( a0 ) );
		assertTrue( a1.isSubclassOf( a1 ) );
		
		assertEquals( a0.getFieldIndex( "x" ), -1 );
		assertEquals( a1.getFieldIndex( "x" ), 0 );
		
		assertFalse( a0.hasField(  "x" ) );
		assertTrue( a1.hasField(  "x" ) );
		
		assertSame( a1.getField( 0 ), f1[0] );

		assertEquals( a0.getNumFields(), 0 );
		assertEquals( a1.getNumFields(), 1 );
		
		assertEquals( a0.getFields(), Arrays.asList( new DMObjectField[] {} ) );
		assertEquals( a1.getFields(), Arrays.asList( f1 ) );
		
		assertTrue( a0.isEmpty() );
		assertFalse( a1.isEmpty() );
		
		Iterator<DMObjectField> i0 = a0.iterator();
		assertFalse( i0.hasNext() );
		Iterator<DMObjectField> i1 = a1.iterator();
		assertTrue( i1.hasNext() );
		assertSame( i1.next(), f1[0] );
		assertFalse( i1.hasNext() );
	}


	public void test_Constructor_names()
	{
		String f1[] = { "x" };
		DMObjectClass a0 = new DMObjectClass( "A", new String[] { } );
		DMObjectClass a1 = new DMObjectClass( "AA", f1 );
		
		assertEquals( a0.getName(), "A" );
		assertEquals( a1.getName(), "AA" );

		assertSame( a0.getSuperclass(), null );
		assertSame( a1.getSuperclass(), null );

		assertTrue( a0.isSubclassOf( a0 ) );
		assertTrue( a1.isSubclassOf( a1 ) );
		
		assertEquals( a0.getFieldIndex( "x" ), -1 );
		assertEquals( a1.getFieldIndex( "x" ), 0 );
		
		assertFalse( a0.hasField(  "x" ) );
		assertTrue( a1.hasField(  "x" ) );
		
		assertEquals( a1.getField( 0 ).getName(), f1[0] );

		assertEquals( a0.getNumFields(), 0 );
		assertEquals( a1.getNumFields(), 1 );
		
		assertEquals( a0.getFields(), Arrays.asList( new DMObjectField[] {} ) );
		assertEquals( a1.getFields().get( 0 ).getName(), "x" );
		
		assertTrue( a0.isEmpty() );
		assertFalse( a1.isEmpty() );
		
		Iterator<DMObjectField> i0 = a0.iterator();
		assertFalse( i0.hasNext() );
		Iterator<DMObjectField> i1 = a1.iterator();
		assertTrue( i1.hasNext() );
		assertSame( i1.next().getName(), f1[0] );
		assertFalse( i1.hasNext() );
	}


	public void test_Constructor_superclass_fields()
	{
		DMObjectField af[] = { new DMObjectField( "x" ) };
		DMObjectClass a = new DMObjectClass( "A", af );

		DMObjectField bf[] = { new DMObjectField( "y" ) };
		DMObjectClass b = new DMObjectClass( "B", a, bf );
		
		assertEquals( b.getName(), "B" );

		assertSame( b.getSuperclass(), a );

		assertTrue( b.isSubclassOf( b ) );
		assertTrue( b.isSubclassOf( a ) );
		
		assertEquals( a.getFieldIndex( "x" ), 0 );
		assertEquals( a.getFieldIndex( "y" ), -1 );
		assertEquals( b.getFieldIndex( "x" ), 0 );
		assertEquals( b.getFieldIndex( "y" ), 1 );
		
		assertTrue( a.hasField(  "x" ) );
		assertFalse( a.hasField(  "y" ) );
		assertTrue( b.hasField(  "x" ) );
		assertTrue( b.hasField(  "y" ) );
		
		assertSame( a.getField( 0 ), af[0] );
		assertSame( b.getField( 0 ), af[0] );
		assertSame( b.getField( 1 ), bf[0] );

		assertEquals( a.getNumFields(), 1 );
		assertEquals( b.getNumFields(), 2 );
		
		assertEquals( a.getFields(), Arrays.asList( af ) );
		assertEquals( b.getFields(), Arrays.asList( new DMObjectField[] { af[0], bf[0] } ) );
		
		assertFalse( a.isEmpty() );
		assertFalse( b.isEmpty() );
		
		Iterator<DMObjectField> ia = a.iterator();
		assertTrue( ia.hasNext() );
		assertSame( ia.next(), af[0] );
		assertFalse( ia.hasNext() );
		Iterator<DMObjectField> ib = b.iterator();
		assertTrue( ib.hasNext() );
		assertSame( ib.next(), af[0] );
		assertTrue( ib.hasNext() );
		assertSame( ib.next(), bf[0] );
		assertFalse( ib.hasNext() );
		
		
		Set<String> names = b.fieldNameSet();
	
		assertTrue( names.contains( "x" ) );
		assertTrue( names.contains( "y" ) );
		assertFalse( names.contains( "z" ) );

		assertTrue( names.containsAll( Arrays.asList( new String[] { "x" } ) ) );
		assertTrue( names.containsAll( Arrays.asList( new String[] { "y" } ) ) );
		assertTrue( names.containsAll( Arrays.asList( new String[] { "x", "y" } ) ) );
		assertFalse( names.containsAll( Arrays.asList( new String[] { "z" } ) ) );
		
		assertFalse( names.isEmpty() );
		
		Iterator<String> ni = names.iterator();
		assertTrue( ni.hasNext() );
		assertEquals( ni.next(), "x"  );
		assertTrue( ni.hasNext() );
		assertEquals( ni.next(), "y"  );
		assertFalse( ni.hasNext() );
		
		assertTrue( names.size() == 2 );
		
		assertEquals( Arrays.asList( names.toArray() ),  Arrays.asList( new String[] { "x", "y" } ) );
	}
}
