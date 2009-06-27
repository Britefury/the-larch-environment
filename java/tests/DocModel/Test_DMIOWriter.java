//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMIOReader;
import BritefuryJ.DocModel.DMIOWriter;
import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMModule.ClassAlreadyDefinedException;

public class Test_DMIOWriter extends TestCase
{
	protected static class TestWriter extends DMIOWriter
	{
		protected static void test_escape(StringBuilder builder, String x)
		{
			escape( builder, x );
		}
		
		protected static String test_quoteString(String s)
		{
			return quoteString( s );
		}
	}
	
	private DMModule module, module2;
	private DMObjectClass A, A2;
	
	
	public void setUp()
	{
		module = new DMModule( "module", "m", "test.module" );
		module2 = new DMModule( "module2", "m", "test.module2" );
		try
		{
			A = module.newClass( "A", new String[] { "x", "y" } );
			A2 = module2.newClass( "A2", new String[] { "x", "y" } );
		}
		catch (ClassAlreadyDefinedException e)
		{
			throw new RuntimeException();
		}
	}
	
	public void tearDown()
	{
		module = null;
		module2 = null;
		A = null;
		A2 = null;
	}
	
	
	
	
	public void matchTest(Pattern pattern, String input, String expected)
	{
		DMIOReader.MatchResult res = Test_DMIOReader.TestReader.test_match( pattern, input, 0 );
		
		if ( res.value == null )
		{
			System.out.println( "MATCH FAILURE" );
			System.out.println( "EXPECTED: " + expected );
		}
		

		assertNotNull( res.value );
		
		if ( !res.value.equals( expected ) )
		{
			System.out.println( "VALUES ARE NOT THE SAME" );
			System.out.println( "EXPECTED: " + expected );
			System.out.println( "RESULT: " + res.value );
		}
		
		
		assertTrue( res.value.equals( expected ) );
	}

	public void matchFailTest(Pattern pattern, String input)
	{
		DMIOReader.MatchResult res = Test_DMIOReader.TestReader.test_match( pattern, input, 0 );
		
		if ( res.value != null  &&  res.value.equals( input ) )
		{
			System.out.println( "MATCH SHOULD HAVE FAILED" );
			System.out.println( "RESULT: " + res.value );
		}
		

		assertTrue( res.value == null  ||  !res.value.equals( input ) );
	}
	
	
	public void escapeTest(String input, String expected)
	{
		StringBuilder builder = new StringBuilder();
		TestWriter.test_escape( builder, input );
		assertEquals( builder.toString(), expected );
	}


	public void writeTest(Object input, String expected)
	{
		String res = null;
		try
		{
			res = DMIOWriter.writeAsString( input );
		}
		catch (DMIOWriter.InvalidDataTypeException e)
		{
			System.out.println( "PARSE FAILURE" );
			fail();
		}
		
		if ( !res.equals( expected ) )
		{
			System.out.println( "VALUES ARE NOT THE SAME" );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
			System.out.println( "RESULT:" );
			System.out.println( res.toString() );
		}
		
		assertTrue( res.equals( expected ) );
	}
	
	


	
	public void testUnquotedString()
	{
		matchTest( DMIOWriter.unquotedString, "abc123ABC_", "abc123ABC_" );
		matchTest( DMIOWriter.unquotedString, "abc123ABC_+-*/%^&|!$@.<>~", "abc123ABC_+-*/%^&|!$@.<>~" );
		matchFailTest( DMIOWriter.unquotedString, "abc123ABC_+-*/%^&|!$@.<>=[]~(" );
		matchFailTest( DMIOWriter.unquotedString, "abc123ABC_+-*/%^&|!$@.<>=[]~)" );
		matchFailTest( DMIOWriter.unquotedString, "abc123ABC_+-*/%^&|!$@.<>=[]~\"" );
		matchFailTest( DMIOWriter.unquotedString, "abc123ABC_+-*/%^&|!$@.<>=[]~ " );
		matchFailTest( DMIOWriter.unquotedString, "abc123ABC_+-*/%^&|!$@.<>=[]~\t" );
		matchFailTest( DMIOWriter.unquotedString, "abc123ABC_+-*/%^&|!$@.<>=[]~\n" );
		matchFailTest( DMIOWriter.unquotedString, "abc123ABC_+-*/%^&|!$@.<>=[]~\\" );
	}

	public void testQuotedStringContents()
	{
		matchTest( DMIOWriter.quotedStringContents, "abc123ABC_", "abc123ABC_" );
		matchTest( DMIOWriter.quotedStringContents, "abc123ABC_`", "abc123ABC_`" );
		matchTest( DMIOWriter.quotedStringContents, "abc123()ABC_", "abc123()ABC_" );
		matchTest( DMIOWriter.quotedStringContents, "abc123( )ABC_", "abc123( )ABC_" );
		matchFailTest( DMIOWriter.quotedStringContents, "abc123(\\)ABC_" );
		matchFailTest( DMIOWriter.quotedStringContents, "abc123(\n)ABC_" );
		matchFailTest( DMIOWriter.quotedStringContents, "abc123(\r)ABC_" );
		matchFailTest( DMIOWriter.quotedStringContents, "abc123(\t)ABC_" );
	}
	
	public void testEscape()
	{
		escapeTest( "\n", "\\n" );
		escapeTest( "\r", "\\r" );
		escapeTest( "\t", "\\t" );
		escapeTest( "\\", "\\\\" );
		escapeTest( new Character( (char)8 ).toString(), "\\x8x" );
		escapeTest( new Character( (char)0x1258 ).toString(), "\\x1258x" );
	}
	
	public void testQuoteString()
	{
		assertEquals( TestWriter.test_quoteString( "a" ), "\"a\"" );
		assertEquals( TestWriter.test_quoteString( "a b" ), "\"a b\"" );
		assertEquals( TestWriter.test_quoteString( "abcdefgh" ), "\"abcdefgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcdefgh`" ), "\"abcdefgh`\"" );
		assertEquals( TestWriter.test_quoteString( "\nabcdefgh" ), "\"\\nabcdefgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcd\nefgh" ), "\"abcd\\nefgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcdefgh\n" ), "\"abcdefgh\\n\"" );
		assertEquals( TestWriter.test_quoteString( "\nab\ncd\nef\ngh\n" ), "\"\\nab\\ncd\\nef\\ngh\\n\"" );
		assertEquals( TestWriter.test_quoteString( "ab\ncd\nef\ngh\n" ), "\"ab\\ncd\\nef\\ngh\\n\"" );
		assertEquals( TestWriter.test_quoteString( "\nab\ncd\nef\ngh" ), "\"\\nab\\ncd\\nef\\ngh\"" );
		assertEquals( TestWriter.test_quoteString( "ab\ncd\nef\ngh" ), "\"ab\\ncd\\nef\\ngh\"" );
		assertEquals( TestWriter.test_quoteString( "abcd\refgh" ), "\"abcd\\refgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcd\tefgh" ), "\"abcd\\tefgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcd\\efgh" ), "\"abcd\\\\efgh\"" );
		assertEquals( TestWriter.test_quoteString( "abcd" + new Character( (char)0x1258 ).toString() + "efgh" ), "\"abcd\\x1258xefgh\"" );
	}
	
	
	public void testWriteString()
	{
		writeTest( "a", "a" );
		writeTest( "a b", "\"a b\"" );
		writeTest( "abcdefgh", "abcdefgh" );
		writeTest( "abcdefgh()", "\"abcdefgh()\"" );
		writeTest( "\nabcdefgh", "\"\\nabcdefgh\"" );
		writeTest( "abcd\nefgh", "\"abcd\\nefgh\"" );
		writeTest( "abcdefgh\n", "\"abcdefgh\\n\"" );
		writeTest( "\nab\ncd\nef\ngh\n", "\"\\nab\\ncd\\nef\\ngh\\n\"" );
		writeTest( "ab\ncd\nef\ngh\n", "\"ab\\ncd\\nef\\ngh\\n\"" );
		writeTest( "\nab\ncd\nef\ngh", "\"\\nab\\ncd\\nef\\ngh\"" );
		writeTest( "ab\ncd\nef\ngh", "\"ab\\ncd\\nef\\ngh\"" );
		writeTest( "abcd\refgh", "\"abcd\\refgh\"" );
		writeTest( "abcd\tefgh", "\"abcd\\tefgh\"" );
		writeTest( "abcd\\efgh", "\"abcd\\\\efgh\"" );
		writeTest( "abcd" + new Character( (char)0x1258 ).toString() + "efgh", "\"abcd\\x1258xefgh\"" );
	}
	
	
	public void testWriteNull()
	{
		writeTest( null, "`null`" );
	}
	
	
	public void testWriteEmptyString()
	{
		Object[] xs = { "" };
		writeTest( Arrays.asList( xs ), "[\"\"]" );
	}

	
	public void testWriteEmptyList()
	{
		Object[] xs = { };
		writeTest( Arrays.asList( xs ), "[]" );
	}

	
	public void testWriteList()
	{
		Object[] xs = { "a", "b", "c" };
		writeTest( Arrays.asList( xs ), "[a b c]" );
	}
	
	
	public void testWriteNestedList()
	{
		Object[] a = {};
		Object[] b = { "a", "b", "c" };
		Object[] xs = { "x", "y", Arrays.asList( a ), "z", Arrays.asList( b ), "w" };
		writeTest( Arrays.asList( xs ), "[x y [] z [a b c] w]" );
	}
	
	
	
	public void testWriteObject()
	{
		DMObject a = A.newInstance( new Object[] { "0", "1" } );
		writeTest( a, "{m=test.module : (m A x=0 y=1)}" );
		DMObject a1 = A.newInstance( new Object[] { "0" } );
		writeTest( a1, "{m=test.module : (m A x=0)}" );
	}


	public void testWriteNestedObject()
	{
		DMObject a = A.newInstance( new Object[] { "0", "1" } );
		DMObject b = A.newInstance( new Object[] { a, "2" } );
		writeTest( b, "{m=test.module : (m A x=(m A x=0 y=1) y=2)}" );
	}

	public void testWriteObjectInListInObject()
	{
		DMObject a = A.newInstance( new Object[] { "0", "1" } );
		List<Object> b = Arrays.asList( new Object[] { a, "abc" } );
		DMObject c = A.newInstance( new Object[] { b, "2" } );
		writeTest( c, "{m=test.module : (m A x=[(m A x=0 y=1) abc] y=2)}" );
	}


	public void testWriteObject_moduleNameCollision()
	{
		DMObject a = A.newInstance( new Object[] { "0", "1" } );
		DMObject b = A2.newInstance( new Object[] { "0", "1" } );
		List<Object> l = Arrays.asList( new Object[] { a, b } );
		writeTest( l, "{m=test.module m2=test.module2 : [(m A x=0 y=1) (m2 A2 x=0 y=1)]}" );
	}
}
