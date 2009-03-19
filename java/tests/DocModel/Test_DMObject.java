//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.util.Arrays;
import java.util.HashMap;

import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMModule.ClassAlreadyDefinedException;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;
import junit.framework.TestCase;

public class Test_DMObject extends TestCase
{
	CommandHistory history;
	
	public void setUp()
	{
		history = new CommandHistory();
	}
	
	public void tearDown()
	{
		history = null;
	}

	
	
	public void test_getDMClass() throws InvalidFieldNameException, ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m" );
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		assertSame( a.getDMClass(), A );
	}

		
	public void test_getFieldIndex() throws InvalidFieldNameException, ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m" );
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		assertEquals( a.getFieldIndex( "x" ), 0 );
		assertEquals( a.getFieldIndex( "y" ), 1 );
	}


	public void test_DMObject_getFieldNames() throws InvalidFieldNameException, ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m" );
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		assertEquals( Arrays.asList( a.getFieldNames() ), Arrays.asList( new String[] { "x", "y" } ) );
	}

		
	public void test_DMObject_getFieldValuesImmutable() throws InvalidFieldNameException, ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m" );
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", "p" } ) );
	}

	
	public void test_get() throws InvalidFieldNameException, ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m" );
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		assertEquals( a.get( 0 ), "a" );
		assertEquals( a.get( 1 ), "p" );
		assertEquals( a.get( "x" ), "a" );
		assertEquals( a.get( "y" ), "p" );
	}


	public void test_getitem() throws InvalidFieldNameException, ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m" );
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		assertEquals( a.__getitem__( 0 ), "a" );
		assertEquals( a.__getitem__( 1 ), "p" );
		assertEquals( a.__getitem__( "x" ), "a" );
		assertEquals( a.__getitem__( "y" ), "p" );
	}


	public void test_set() throws InvalidFieldNameException, ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m" );
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		a.set( 0, "b" );
		a.set( 1, "q" );
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "q" } ) );
		
		a.set( "x", "c" );
		a.set( "y", "r" );
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "r" } ) );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "q" } ) );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "q" } ) );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "p" } ) );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", "p" } ) );
		
		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "p" } ) );
		
		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "q" } ) );
		
		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "q" } ) );
		
		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "c", "r" } ) );
	}


	public void test_update() throws InvalidFieldNameException, ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m" );
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put( "x", "d" );
		data.put( "y", "s" );
		a.update( data );

		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", "s" } ) );

		history.undo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", "p" } ) );

		history.redo();
		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", "s" } ) );
	}


	public void test_setitem() throws InvalidFieldNameException, ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m" );
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
		DMObject a = A.newInstance( new Object[] { "a", "p" } );
		history.track( a );
		
		a.__setitem__( 0, "e" );
		a.__setitem__( "y", "t" );

		assertEquals( Arrays.asList( a.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "e", "t" } ) );
	}

	
	
	public void test_DMObject_constructors() throws InvalidFieldNameException, ClassAlreadyDefinedException
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put( "x", "d" );
		data.put( "y", "s" );

		DMModule m = new DMModule( "m" );
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
		DMObject a0 = new DMObject( A, new Object[] { "a", "b" } );
		DMObject a1 = new DMObject( A, new String[] { "y", "x" }, new Object[] { "a", "b" } );
		DMObject a2 = new DMObject( A, data );
		
		DMObject a3 = A.newInstance( new Object[] { "a", "b" } );
		DMObject a4 = A.newInstance( new String[] { "y", "x" }, new Object[] { "a", "b" } );
		DMObject a5 = A.newInstance( data );

		assertEquals( Arrays.asList( a0.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", "b" } ) );
		assertEquals( Arrays.asList( a1.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "a" } ) );
		assertEquals( Arrays.asList( a2.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", "s" } ) );

		assertEquals( Arrays.asList( a3.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "a", "b" } ) );
		assertEquals( Arrays.asList( a4.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "b", "a" } ) );
		assertEquals( Arrays.asList( a5.getFieldValuesImmutable() ), Arrays.asList( new Object[] { "d", "s" } ) );
	}
	
	
	
	public void test_trackTree() throws InvalidFieldNameException, ClassAlreadyDefinedException
	{
		DMModule m = new DMModule( "m" );
		DMObjectClass A = m.newClass( "A", new String[] { "x", "y" } );
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
}
