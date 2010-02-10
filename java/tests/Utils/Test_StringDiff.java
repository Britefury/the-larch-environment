//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import BritefuryJ.Utils.StringDiff;
import BritefuryJ.Utils.StringDiff.Operation;
import BritefuryJ.Utils.StringDiff.Operation.OpCode;

public class Test_StringDiff extends TestCase
{
	protected void levenshteinTestOps(String a, String b, Operation expected[])
	{
		ArrayList<Operation> result = StringDiff.levenshteinDiff( a, b );
		
		List<Operation> expectedList = Arrays.asList( expected );
		
		if ( !result.equals( expectedList ) )
		{
			System.out.println( "Operation lists differ." );
			System.out.println( "Expected:" );
			System.out.println( expectedList.toString() );
			System.out.println( "Result:" );
			System.out.println( result.toString() );
		}
		
		assertEquals( result, expectedList );
		
		levenshteinTestEdits( a, b );
		diffTestEdits( a, b );
	}
	

	protected void levenshteinTestEdits(String a, String b)
	{
		ArrayList<Operation> ops = StringDiff.levenshteinDiff( a, b );
		
		
		String c = a;
		for (Operation op: ops)
		{
			if ( op.opcode == OpCode.EQUAL )
			{
				assertSame( op.bEnd - op.bBegin, op.aEnd - op.aBegin );
				assertEquals( a.substring( op.aBegin, op.aEnd ), b.substring( op.bBegin, op.bEnd ) );
			}
			else if ( op.opcode == OpCode.REPLACE )
			{
				assertSame( op.bEnd - op.bBegin, op.aEnd - op.aBegin );
				c = c.substring( 0, op.aBegin ) + b.substring( op.bBegin, op.bEnd )  + c.substring( op.aEnd );
			}
			else if ( op.opcode == OpCode.INSERT )
			{
				assertSame( op.aBegin, op.aEnd );
				assertNotSame( op.bBegin, op.bEnd );
				c = c.substring( 0, op.aBegin ) + b.substring( op.bBegin, op.bEnd )  + c.substring( op.aEnd );
			}
			else if ( op.opcode == OpCode.DELETE )
			{
				assertNotSame( op.aBegin, op.aEnd );
				assertSame( op.bBegin, op.bEnd );
				c = c.substring( 0, op.aBegin ) + c.substring( op.aEnd );
			}
		}
		
		assertEquals( c, b );
	}
	

	protected void diffTestEdits(String a, String b)
	{
		ArrayList<Operation> ops = StringDiff.diff( a, b );
		
		
		String c = a;
		for (Operation op: ops)
		{
			if ( op.opcode == OpCode.EQUAL )
			{
				assertSame( op.bEnd - op.bBegin, op.aEnd - op.aBegin );
				assertEquals( a.substring( op.aBegin, op.aEnd ), b.substring( op.bBegin, op.bEnd ) );
			}
			else if ( op.opcode == OpCode.REPLACE )
			{
				assertSame( op.bEnd - op.bBegin, op.aEnd - op.aBegin );
				c = c.substring( 0, op.aBegin ) + b.substring( op.bBegin, op.bEnd )  + c.substring( op.aEnd );
			}
			else if ( op.opcode == OpCode.INSERT )
			{
				assertSame( op.aBegin, op.aEnd );
				assertNotSame( op.bBegin, op.bEnd );
				c = c.substring( 0, op.aBegin ) + b.substring( op.bBegin, op.bEnd )  + c.substring( op.aEnd );
			}
			else if ( op.opcode == OpCode.DELETE )
			{
				assertNotSame( op.aBegin, op.aEnd );
				assertSame( op.bBegin, op.bEnd );
				c = c.substring( 0, op.aBegin ) + c.substring( op.aEnd );
			}
		}
		
		assertEquals( c, b );
	}
	
	
	public void test_levenshteinDiff_insertAll()
	{
		levenshteinTestOps( "", "hello", new Operation[] { new Operation( OpCode.INSERT, 0, 0, 0, 5 ) } );
		levenshteinTestOps( "", "helloworld", new Operation[] { new Operation( OpCode.INSERT, 0, 0, 0, 10 ) } );
	}

	public void test_levenshteinDiff_deleteAll()
	{
		levenshteinTestOps( "hello", "", new Operation[] { new Operation( OpCode.DELETE, 0, 5, 0, 0 ) } );
		levenshteinTestOps( "helloworld", "", new Operation[] { new Operation( OpCode.DELETE, 0, 10, 0, 0 ) } );
	}

	public void test_levenshteinDiff_same()
	{
		levenshteinTestOps( "hello", "hello", new Operation[] { new Operation( OpCode.EQUAL, 0, 5, 0, 5 ) } );
		levenshteinTestOps( "helloworld", "helloworld", new Operation[] { new Operation( OpCode.EQUAL, 0, 10, 0, 10 ) } );
	}

	public void test_levenshteinDiff_insert()
	{
		levenshteinTestOps( "abcdef", "abc123def", new Operation[] {
				new Operation( OpCode.EQUAL, 3, 6, 6, 9 ),
				new Operation( OpCode.INSERT, 3, 3, 3, 6 ),
				new Operation( OpCode.EQUAL, 0, 3, 0, 3 ),
				} );
		levenshteinTestOps( "abcdef", "123abcdef", new Operation[] {
				new Operation( OpCode.EQUAL, 0, 6, 3, 9 ),
				new Operation( OpCode.INSERT, 0, 0, 0, 3 ),
				} );
		levenshteinTestOps( "abcdef", "abcdef123", new Operation[] {
				new Operation( OpCode.INSERT, 6, 6, 6, 9 ),
				new Operation( OpCode.EQUAL, 0, 6, 0, 6 ),
				} );
		levenshteinTestOps( "abcdef", "ab12cd45ef", new Operation[] {
				new Operation( OpCode.EQUAL, 4, 6, 8, 10 ),
				new Operation( OpCode.INSERT, 4, 4, 6, 8 ),
				new Operation( OpCode.EQUAL, 2, 4, 4, 6 ),
				new Operation( OpCode.INSERT, 2, 2, 2, 4 ),
				new Operation( OpCode.EQUAL, 0, 2, 0, 2 ),
				} );
	}


	public void test_levenshteinDiff_delete()
	{
		levenshteinTestOps( "abc123def", "abcdef", new Operation[] {
				new Operation( OpCode.EQUAL, 6, 9, 3, 6 ),
				new Operation( OpCode.DELETE, 3, 6, 3, 3 ),
				new Operation( OpCode.EQUAL, 0, 3, 0, 3 ),
				} );
		levenshteinTestOps( "123abcdef", "abcdef", new Operation[] {
				new Operation( OpCode.EQUAL, 3, 9, 0, 6 ),
				new Operation( OpCode.DELETE, 0, 3, 0, 0 ),
				} );
		levenshteinTestOps( "abcdef123", "abcdef", new Operation[] {
				new Operation( OpCode.DELETE, 6, 9, 6, 6 ),
				new Operation( OpCode.EQUAL, 0, 6, 0, 6 ),
				} );
		levenshteinTestOps( "ab12cd45ef", "abcdef", new Operation[] {
				new Operation( OpCode.EQUAL, 8, 10, 4, 6 ),
				new Operation( OpCode.DELETE, 6, 8, 4, 4 ),
				new Operation( OpCode.EQUAL, 4, 6, 2, 4 ),
				new Operation( OpCode.DELETE, 2, 4, 2, 2 ),
				new Operation( OpCode.EQUAL, 0, 2, 0, 2 ),
				} );
		levenshteinTestOps( "helloworld", "hello", new Operation[] {
				new Operation( OpCode.DELETE, 5, 10, 5, 5 ),
				new Operation( OpCode.EQUAL, 0, 5, 0, 5 ),
				} );
	}
	
	


	public void test_levenshteinDiff_replace()
	{
		levenshteinTestOps( "abc123def", "abc456def", new Operation[] {
				new Operation( OpCode.EQUAL, 6, 9, 6, 9 ),
				new Operation( OpCode.REPLACE, 3, 6, 3, 6 ),
				new Operation( OpCode.EQUAL, 0, 3, 0, 3 )
				} );
		levenshteinTestOps( "123abcdef", "456abcdef", new Operation[] {
				new Operation( OpCode.EQUAL, 3, 9, 3, 9 ),
				new Operation( OpCode.REPLACE, 0, 3, 0, 3 ),
				} );
		levenshteinTestOps( "abcdef123", "abcdef456", new Operation[] {
				new Operation( OpCode.REPLACE, 6, 9, 6, 9 ),
				new Operation( OpCode.EQUAL, 0, 6, 0, 6 ),
				} );
		levenshteinTestOps( "ab12cd45ef", "ab98cd76ef", new Operation[] {
				new Operation( OpCode.EQUAL, 8, 10, 8, 10 ),
				new Operation( OpCode.REPLACE, 6, 8, 6, 8 ),
				new Operation( OpCode.EQUAL, 4, 6, 4, 6 ),
				new Operation( OpCode.REPLACE, 2, 4, 2, 4 ),
				new Operation( OpCode.EQUAL, 0, 2, 0, 2 )
				} );
	}
	



	public void test_levenshteinDiff_other()
	{
		levenshteinTestEdits( "This is a test to see if this works", "Some other test to see what happens" );
	}
}
