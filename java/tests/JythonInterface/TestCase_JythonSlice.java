//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.JythonInterface;

import junit.framework.TestCase;
import BritefuryJ.JythonInterface.JythonSlice;

public class TestCase_JythonSlice extends TestCase
{
	public void test_sliceLength()
	{
		assertEquals( JythonSlice.sliceLength( 0, 10, 1 ),  10 );
		assertEquals( JythonSlice.sliceLength( 1, 10, 1 ),  9 );
		assertEquals( JythonSlice.sliceLength( 0, 10, 2 ),  5 );
		assertEquals( JythonSlice.sliceLength( 0, 11, 2 ),  6 );
		assertEquals( JythonSlice.sliceLength( 0, 12, 2 ),  6 );
		assertEquals( JythonSlice.sliceLength( 0, 9, 3 ),  3 );
		assertEquals( JythonSlice.sliceLength( 0, 10, 3 ),  4 );
		assertEquals( JythonSlice.sliceLength( 1, 10, 3 ),  3 );
		assertEquals( JythonSlice.sliceLength( 10, 1, -3 ),  3 );
		assertEquals( JythonSlice.sliceLength( 10, 0, -3 ),  4 );
	}
	
	
	protected Object[] range(int start, int stop)
	{
		Object[] result = new Object[stop-start];
		
		for (int i = start, j = 0; i < stop; i++, j++)
		{
			result[j] = i;
		}
		
		return result;
	}
	
	
	protected Object[] intArrayToObj(int[] in)
	{
		Object[] result = new Object[in.length];
		
		for (int i = 0; i < in.length; i++)
		{
			result[i] = in[i];
		}
		
		return result;
	}
	
	
	protected boolean compareObjectArrays(Object[] a, Object[] b)
	{
		boolean bResult = true;
		if ( a.length != b.length )
		{
			bResult = false;
		}
		else
		{
			for (int i = 0; i < a.length; i++)
			{
				if ( !a[i].equals( b[i] ) )
				{
					bResult = false;
				}
			}
		}
		
		if ( !bResult )
		{
			String aStr = "A: " + String.valueOf( a.length ) + ": ";
			String bStr = "B: " + String.valueOf( b.length ) + ": ";
			
			for (int i = 0; i < a.length; i++)
			{
				aStr = aStr + a[i].toString() + "  ";
			}
			
			for (int i = 0; i < b.length; i++)
			{
				bStr = bStr + b[i].toString() + "  ";
			}
			
			System.out.println( aStr );
			System.out.println( bStr );
		}
		return bResult;
	}
	
	
	public void test_arrayGetSlice()
	{
		Object[] in = range( 0, 10 );
		
		int[] a = { 0,1,2,3,4,5,6,7,8,9 };
		assertTrue( compareObjectArrays( JythonSlice.arrayGetSlice( in, 0, 10, 1 ),   intArrayToObj( a ) ) );

		int[] b = { 2,3,4,5,6,7,8,9 };
		assertTrue( compareObjectArrays( JythonSlice.arrayGetSlice( in, 2, 10, 1 ),   intArrayToObj( b ) ) );

		int[] c = { 0,1,2,3,4,5,6,7 };
		assertTrue( compareObjectArrays( JythonSlice.arrayGetSlice( in, 0, 8, 1 ),   intArrayToObj( c ) ) );

		int[] d = { 2,3,4,5,6,7 };
		assertTrue( compareObjectArrays( JythonSlice.arrayGetSlice( in, 2, 8, 1 ),   intArrayToObj( d ) ) );


		int[] e = { 0,2,4,6,8 };
		assertTrue( compareObjectArrays( JythonSlice.arrayGetSlice( in, 0, 10, 2 ),   intArrayToObj( e ) ) );

		int[] f = { 1,3,5,7,9 };
		assertTrue( compareObjectArrays( JythonSlice.arrayGetSlice( in, 1, 10, 2 ),   intArrayToObj( f ) ) );
		
		int[] g = { 9,6,3 };
		assertTrue( compareObjectArrays( JythonSlice.arrayGetSlice( in, 9, 1, -3 ),   intArrayToObj( g ) ) );
		
		int[] h = { 8,5,2 };
		assertTrue( compareObjectArrays( JythonSlice.arrayGetSlice( in, 8, 1, -3 ),   intArrayToObj( h ) ) );

		int[] i = {};
		assertTrue( compareObjectArrays( JythonSlice.arrayGetSlice( in, 1, 8, -3 ),   intArrayToObj( i ) ) );
	}
	
	
	public void test_arraySetSlice()
	{
		Object[] in = range( 0, 10 );   // { 0,1,2,3,4,5,6,7,8,9 }
	
		int[] aSrc={ 10,20,30 }, aRes = { 0,1,10,20,30,8,9 };
		assertTrue( compareObjectArrays( JythonSlice.arraySetSlice( in, 2, 8, 1, intArrayToObj( aSrc ) ),   intArrayToObj( aRes ) ) );

		int[] bSrc={ 10,20,30 }, bRes = { 10,20,30,8,9 };
		assertTrue( compareObjectArrays( JythonSlice.arraySetSlice( in, 0, 8, 1, intArrayToObj( bSrc ) ),   intArrayToObj( bRes ) ) );

		int[] cSrc={ 10,20,30 }, cRes = { 0,1,10,20,30 };
		assertTrue( compareObjectArrays( JythonSlice.arraySetSlice( in, 2, 10, 1, intArrayToObj( cSrc ) ),   intArrayToObj( cRes ) ) );

		int[] dSrc={ 10,20,30,40 }, dRes = { 10, 1, 2, 20, 4, 5, 30, 7, 8, 40 };
		assertTrue( compareObjectArrays( JythonSlice.arraySetSlice( in, 0, 10, 3, intArrayToObj( dSrc ) ),   intArrayToObj( dRes ) ) );

		int[] eSrc={ 10,20,30 }, eRes = { 0,1,10,3,4,20,6,7,30,9 };
		assertTrue( compareObjectArrays( JythonSlice.arraySetSlice( in, 2, 10, 3, intArrayToObj( eSrc ) ),   intArrayToObj( eRes ) ) );

		int[] fSrc={ 10,20,30 }, fRes = { 0,1,2,30,4,5,20,7,8,10 };
		assertTrue( compareObjectArrays( JythonSlice.arraySetSlice( in, 9, 1, -3, intArrayToObj( fSrc ) ),   intArrayToObj( fRes ) ) );

		int[] gSrc={ 10,20,30 }, gRes = { 0,1,30,3,4,20,6,7,10,9 };
		assertTrue( compareObjectArrays( JythonSlice.arraySetSlice( in, 8, 1, -3, intArrayToObj( gSrc ) ),   intArrayToObj( gRes ) ) );

		int[] hSrc={}, hRes = { 0,1,2,3,4,5,6,7,8,9 };
		assertTrue( compareObjectArrays( JythonSlice.arraySetSlice( in, 1, 8, -3, intArrayToObj( hSrc ) ),   intArrayToObj( hRes ) ) );
	}


	public void test_arrayDelSlice()
	{
		Object[] in = range( 0, 10 );   // { 0,1,2,3,4,5,6,7,8,9 }
	
		int[] a = { 0,1,8,9 };
		assertTrue( compareObjectArrays( JythonSlice.arrayDelSlice( in, 2, 8, 1 ),   intArrayToObj( a ) ) );

		int[] b = { 8,9 };
		assertTrue( compareObjectArrays( JythonSlice.arrayDelSlice( in, 0, 8, 1 ),   intArrayToObj( b ) ) );

		int[] c = { 0,1 };
		assertTrue( compareObjectArrays( JythonSlice.arrayDelSlice( in, 2, 10, 1 ),   intArrayToObj( c ) ) );

		int[] d = { 0,1,3,4,6,7,9 };
		assertTrue( compareObjectArrays( JythonSlice.arrayDelSlice( in, 2, 10, 3 ),   intArrayToObj( d ) ) );

		int[] e = { 0,2,3,5,6,8,9 };
		assertTrue( compareObjectArrays( JythonSlice.arrayDelSlice( in, 1, 10, 3 ),   intArrayToObj( e ) ) );

		int[] f = { 0,1,3,4,6,7,9 };
		assertTrue( compareObjectArrays( JythonSlice.arrayDelSlice( in, 8, 0, -3 ),   intArrayToObj( f ) ) );
	}
}
