//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import BritefuryJ.Cell.Cell;
import BritefuryJ.Cell.CellEvaluator;
import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocModel.DMIOReader;
import BritefuryJ.DocModel.DMList;
import BritefuryJ.DocModel.DMIOReader.BadModuleNameException;
import BritefuryJ.DocModel.DMIOReader.ParseErrorException;
import BritefuryJ.DocModel.DMModule.UnknownClassException;
import BritefuryJ.DocModel.DMModuleResolver.CouldNotResolveModuleException;

public class Test_DMList extends TestCase
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
	
	
	
	public void cmpListSX(List<Object> xs, String expectedSX)
	{
		try
		{
			Object expected = DMIOReader.readFromString( expectedSX, null );
			assertEquals( expected, xs );
		}
		catch (ParseErrorException e)
		{
			System.out.println( "Could not parse expected SX" );
			fail();
		}
		catch (BadModuleNameException e)
		{
			System.out.println( "Bad module name while parsing expected SX" );
			fail();
		}
		catch (UnknownClassException e)
		{
			System.out.println( "Unknown class while parsing expected SX" );
			fail();
		}
		catch (CouldNotResolveModuleException e)
		{
			System.out.println( "Could not resolve module while parsing expected SX" );
			fail();
		}
	}
	
	@SuppressWarnings("unchecked")
	public DMList readDMListSX(String sx)
	{
		try
		{
			List<Object> xs = (List<Object>)DMIOReader.readFromString( sx, null );
			return new DMList( xs );
		}
		catch (ParseErrorException e)
		{
			System.out.println( "Could not parse" );
			fail();
			return null;
		}
		catch (BadModuleNameException e)
		{
			System.out.println( "Bad module name while parsing" );
			fail();
			return null;
		}
		catch (UnknownClassException e)
		{
			System.out.println( "Unknown class while parsing" );
			fail();
			return null;
		}
		catch (CouldNotResolveModuleException e)
		{
			System.out.println( "Could not resolve module while parsing" );
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
	
	
	public DMList readTrackedDMListSX(String sx)
	{
		DMList xs = readDMListSX( sx );
		history.track( xs );
		return xs;
	}
	
	
	
	public void testConstructor()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		cmpListSX( xs, "[a b c `null`]" );
		
		assertEquals( xs.get( 0 ), "a" );
		assertEquals( xs.get( 1 ), "b" );
		assertEquals( xs.get( 2 ), "c" );


		DMList ys = readDMListSX( "[a b c [x y z]]" );
		cmpListSX( ys, "[a b c [x y z]]" );
	}
	
	public void testClone()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		cmpListSX( (DMList)xs.clone(), "[a b c `null`]" );
	}

	public void testCell()
	{
		final DMList xs = readDMListSX( "[a b c]" );
		
		CellEvaluator eval = new CellEvaluator()
		{
			public Object evaluate()
			{
				return xs.size();
			}
		};
		Cell yCell = new Cell();
		yCell.setEvaluator( eval );
		
		assertEquals( yCell.getValue(), new Integer( 3 ) );
		
		xs.add( "xyz" );

		assertEquals( yCell.getValue(), new Integer( 4 ) );
	}
	
	
	
	public void testAdd()
	{
		DMList xs = readTrackedDMListSX( "[a b c]" );
		xs.add( "xyz" );
		cmpListSX( xs, "[a b c xyz]" );
		xs.add( null );
		cmpListSX( xs, "[a b c xyz `null`]" );
		
		history.undo();
		cmpListSX( xs, "[a b c xyz]" );
		history.undo();
		cmpListSX( xs, "[a b c]" );
		history.redo();
		cmpListSX( xs, "[a b c xyz]" );
		history.redo();
		cmpListSX( xs, "[a b c xyz `null`]" );
	}



	public void testAdd_insert()
	{
		DMList xs = readTrackedDMListSX( "[a b c]" );
		xs.add( 2, "xyz" );
		cmpListSX( xs, "[a b xyz c]" );
		xs.add( 2, null );
		cmpListSX( xs, "[a b `null` xyz c]" );
		
		history.undo();
		cmpListSX( xs, "[a b xyz c]" );
		history.undo();
		cmpListSX( xs, "[a b c]" );
		history.redo();
		cmpListSX( xs, "[a b xyz c]" );
		history.redo();
		cmpListSX( xs, "[a b `null` xyz c]" );
	}



	public void testAddAll()
	{
		DMList xs = readTrackedDMListSX( "[a b c]" );
		DMList ys = readDMListSX( "[x y z `null`]" );
		xs.addAll( ys );
		cmpListSX( xs, "[a b c x y z `null`]" );
		
		history.undo();
		cmpListSX( xs, "[a b c]" );
		history.redo();
		cmpListSX( xs, "[a b c x y z `null`]" );
	}


	public void testAddAll_insert()
	{
		DMList xs = readTrackedDMListSX( "[a b c]" );
		DMList ys = readDMListSX( "[x y z `null`]" );
		xs.addAll( 2, ys );
		cmpListSX( xs, "[a b x y z `null` c]" );
		
		history.undo();
		cmpListSX( xs, "[a b c]" );
		history.redo();
		cmpListSX( xs, "[a b x y z `null` c]" );
	}


	public void testClear()
	{
		DMList xs = readTrackedDMListSX( "[a b c]" );
		xs.clear();
		cmpListSX( xs, "[]" );
		
		history.undo();
		cmpListSX( xs, "[a b c]" );
		history.redo();
		cmpListSX( xs, "[]" );
	}



	public void testContains()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		assertTrue( xs.contains( "a" ) );
		assertTrue( xs.contains( "b" ) );
		assertTrue( xs.contains( "c" ) );
		assertTrue( xs.contains( null ) );
		assertFalse( xs.contains( "d" ) );
	}


	public void testContainsAll()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		DMList ys = readDMListSX( "[c a]" );
		DMList zs = readDMListSX( "[c d]" );
		DMList zzs = readDMListSX( "[c `null`]" );
		assertTrue( xs.containsAll( ys ) );
		assertTrue( xs.containsAll( zzs ) );
		assertFalse( xs.containsAll( zs ) );
	}



	public void testEquals()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		DMList ys = readDMListSX( "[a b c `null`]" );
		assertTrue( xs.equals( ys ) );
	}



	public void testGet()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		assertEquals( xs.get( 0 ), "a" );
		assertEquals( xs.get( 1 ), "b" );
		assertEquals( xs.get( 2 ), "c" );
		assertEquals( xs.get( 3 ), null );
	}



	public void testIndexOf()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		assertEquals( xs.indexOf( "a" ), 0 );
		assertEquals( xs.indexOf( "b" ), 1 );
		assertEquals( xs.indexOf( "c" ), 2 );
		assertEquals( xs.indexOf( null ), 3 );
	}


	public void testIsEmpty()
	{
		assertFalse( readDMListSX( "[a b c]" ).isEmpty() );
		assertTrue( readDMListSX( "[]" ).isEmpty() );
	}
	
	
	public void testIterator()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		assertEquals( xs, iterToList( xs.iterator() ) );
	}


	public void testLastIndexOf()
	{
		DMList xs = readDMListSX( "[a b c `null` a b c `null`]" );
		assertEquals( xs.lastIndexOf( "a" ), 4 );
		assertEquals( xs.lastIndexOf( "b" ), 5 );
		assertEquals( xs.lastIndexOf( "c" ), 6 );
		assertEquals( xs.lastIndexOf( null ), 7 );
	}


	public void testListIterator()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		assertEquals( xs, iterToList( xs.listIterator() ) );
	}


	public void testListIteratorFrom()
	{
		DMList xs = readDMListSX( "[a b c `null`]" );
		cmpListSX( iterToList( xs.listIterator( 0 ) ), "[a b c `null`]" );
		cmpListSX( iterToList( xs.listIterator( 1 ) ), "[b c `null`]" );
		cmpListSX( iterToList( xs.listIterator( 2 ) ), "[c `null`]" );
		cmpListSX( iterToList( xs.listIterator( 3 ) ), "[`null`]" );
		cmpListSX( iterToList( xs.listIterator( 4 ) ), "[]" );
	}


	public void testRemove()
	{
		DMList xs = readTrackedDMListSX( "[a b c `null`]" );
		xs.remove( 1 );
		cmpListSX( xs, "[a c `null`]" );
		
		history.undo();
		cmpListSX( xs, "[a b c `null`]" );
		history.redo();
		cmpListSX( xs, "[a c `null`]" );
	}

	public void testRemoveObject()
	{
		DMList xs = readTrackedDMListSX( "[a b c `null`]" );
		xs.remove( "b" );
		cmpListSX( xs, "[a c `null`]" );
		xs.remove( null );
		cmpListSX( xs, "[a c]" );
		
		history.undo();
		cmpListSX( xs, "[a c `null`]" );
		history.undo();
		cmpListSX( xs, "[a b c `null`]" );
		history.redo();
		cmpListSX( xs, "[a c `null`]" );
		history.redo();
		cmpListSX( xs, "[a c]" );
	}


	public void testSet()
	{
		DMList xs = readTrackedDMListSX( "[a b c]" );
		xs.set( 1, null );
		cmpListSX( xs, "[a `null` c]" );
		
		history.undo();
		cmpListSX( xs, "[a b c]" );
		history.redo();
		cmpListSX( xs, "[a `null` c]" );
	}


	public void testSize()
	{
		assertEquals( readDMListSX( "[a b c]" ).size(), 3 );
	}
	
	
	public void testSublist()
	{
		DMList xs = readDMListSX( "[a b c d e f]" );
		cmpListSX( xs.subList( 1, 5 ), "[b c d e]" );
	}
	
	
	public void testToArray()
	{
		String[] strs = { "a", "b", "c" };
		assertEquals( Arrays.asList( readDMListSX( "[a b c]" ).toArray() ), Arrays.asList( strs ) );

		String[] a = { "a", "b", "c" };
		assertEquals( Arrays.asList( readDMListSX( "[a b c]" ).toArray( a ) ), Arrays.asList( strs ) );
	}
	
	
	
	public void test_trackTree() throws BritefuryJ.DocModel.DMIOWriter.InvalidDataTypeException
	{
		DMList xs = readTrackedDMListSX( "[a b [c d [e f]]]" );
		cmpListSX( xs, "[a b [c d [e f]]]" );

		((DMList)((DMList)xs.get( 2 )).get( 2 )).set( 0, "x" );
		cmpListSX( xs, "[a b [c d [x f]]]" );
		
		history.undo();
		cmpListSX( xs, "[a b [c d [e f]]]" );

		history.redo();
		cmpListSX( xs, "[a b [c d [x f]]]" );
		
		
		DMList ys = readDMListSX( "[p q [s t]]" );

		((DMList)((DMList)xs.get( 2 )).get( 2 )).set( 0, ys );
		cmpListSX( xs, "[a b [c d [[p q [s t]] f]]]" );

		((DMList)((DMList)((DMList)((DMList)xs.get( 2 )).get( 2 )).get( 0 )).get( 2 )).set( 1, "h" );
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
