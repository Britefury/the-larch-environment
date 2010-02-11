//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.util.Arrays;

import BritefuryJ.DocModel.DMSchema;
import junit.framework.TestCase;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectField;
import BritefuryJ.DocModel.DMSchema.ClassAlreadyDefinedException;

public class Test_DMObjectClass extends TestCase
{
	private DMSchema m;
	
	
	public void setUp()
	{
		m = new DMSchema( "m", "m", "test.m" );
	}
	
	public void tearDown()
	{
		m = null;
	}
	
	
	public void test_Constructor_fields() throws ClassAlreadyDefinedException
	{
		DMObjectField f1[] = { new DMObjectField( "x" ) };
		DMObjectClass a0 = m.newClass( "A", new DMObjectField[] { } );
		DMObjectClass a1 = m.newClass( "AA", f1 );
		
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
	}


	public void test_Constructor_names() throws ClassAlreadyDefinedException
	{
		String f1[] = { "x" };
		DMObjectClass a0 = m.newClass( "A", new String[] { } );
		DMObjectClass a1 = m.newClass( "AA", f1 );
		
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
	}


	public void test_Constructor_superclass_fields() throws ClassAlreadyDefinedException
	{
		DMObjectField af[] = { new DMObjectField( "x" ) };
		DMObjectClass a = m.newClass( "A", af );

		DMObjectField bf[] = { new DMObjectField( "y" ) };
		DMObjectClass b = m.newClass( "B", a, bf );
		
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
	}
}
