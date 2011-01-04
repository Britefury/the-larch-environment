//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.util.Arrays;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectField;
import BritefuryJ.DocModel.DMSchema;

public class Test_DMObjectClass extends TestCase
{
	private static DMSchema m = new DMSchema( "m", "m", "tests.DocModel.Test_DMObjectClass" );
	
	
	public void test_Constructor_fields()
	{
		DMObjectField f1[] = { new DMObjectField( "x" ) };
		DMObjectClass a0 = m.newClass( "A0", new DMObjectField[] { } );
		DMObjectClass a1 = m.newClass( "AA0", f1 );
		
		assertEquals( a0.getName(), "A0" );
		assertEquals( a1.getName(), "AA0" );

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
	}


	public void test_Constructor_names()
	{
		String f1[] = { "x" };
		DMObjectClass a0 = m.newClass( "A1", new String[] { } );
		DMObjectClass a1 = m.newClass( "AA1", f1 );
		
		assertEquals( a0.getName(), "A1" );
		assertEquals( a1.getName(), "AA1" );

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
	}


	public void test_Constructor_superclass_fields()
	{
		DMObjectField af[] = { new DMObjectField( "x" ) };
		DMObjectClass a = m.newClass( "A2", af );

		DMObjectField bf[] = { new DMObjectField( "y" ) };
		DMObjectClass b = m.newClass( "B2", a, bf );
		
		assertEquals( b.getName(), "B2" );

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
	}
}
