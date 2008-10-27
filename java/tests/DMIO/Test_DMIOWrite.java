//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DMIO;

import java.util.Arrays;
import java.util.regex.Pattern;

import BritefuryJ.DocModel.DMIORead;
import BritefuryJ.DocModel.DMIOWrite;
import junit.framework.TestCase;

public class Test_DMIOWrite extends TestCase
{
	public void matchTest(Pattern pattern, String input, String expected)
	{
		DMIORead.MatchResult res = DMIORead.match( pattern, input, 0 );
		
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
		DMIORead.MatchResult res = DMIORead.match( pattern, input, 0 );
		
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
		DMIOWrite.escape( builder, input );
		assertEquals( builder.toString(), expected );
	}


	public void writeTest(Object input, String expected)
	{
		String res = null;
		try
		{
			res = DMIOWrite.writeSX( input );
		}
		catch (DMIOWrite.InvalidDataTypeException e)
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
		matchTest( DMIOWrite.unquotedString, "abc123ABC_", "abc123ABC_" );
		matchTest( DMIOWrite.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~", "abc123ABC_+-*/%^&|!$@.,<>=[]~" );
		matchFailTest( DMIOWrite.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~(" );
		matchFailTest( DMIOWrite.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~)" );
		matchFailTest( DMIOWrite.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~\"" );
		matchFailTest( DMIOWrite.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~ " );
		matchFailTest( DMIOWrite.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~\t" );
		matchFailTest( DMIOWrite.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~\n" );
		matchFailTest( DMIOWrite.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~\\" );
	}

	public void testQuotedStringContents()
	{
		matchTest( DMIOWrite.quotedStringContents, "abc123ABC_", "abc123ABC_" );
		matchTest( DMIOWrite.quotedStringContents, "abc123ABC_`", "abc123ABC_`" );
		matchTest( DMIOWrite.quotedStringContents, "abc123()ABC_", "abc123()ABC_" );
		matchTest( DMIOWrite.quotedStringContents, "abc123( )ABC_", "abc123( )ABC_" );
		matchFailTest( DMIOWrite.quotedStringContents, "abc123(\\)ABC_" );
		matchFailTest( DMIOWrite.quotedStringContents, "abc123(\n)ABC_" );
		matchFailTest( DMIOWrite.quotedStringContents, "abc123(\r)ABC_" );
		matchFailTest( DMIOWrite.quotedStringContents, "abc123(\t)ABC_" );
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
		assertEquals( DMIOWrite.quoteString( "a" ), "\"a\"" );
		assertEquals( DMIOWrite.quoteString( "a b" ), "\"a b\"" );
		assertEquals( DMIOWrite.quoteString( "abcdefgh" ), "\"abcdefgh\"" );
		assertEquals( DMIOWrite.quoteString( "abcdefgh`" ), "\"abcdefgh`\"" );
		assertEquals( DMIOWrite.quoteString( "\nabcdefgh" ), "\"\\nabcdefgh\"" );
		assertEquals( DMIOWrite.quoteString( "abcd\nefgh" ), "\"abcd\\nefgh\"" );
		assertEquals( DMIOWrite.quoteString( "abcdefgh\n" ), "\"abcdefgh\\n\"" );
		assertEquals( DMIOWrite.quoteString( "\nab\ncd\nef\ngh\n" ), "\"\\nab\\ncd\\nef\\ngh\\n\"" );
		assertEquals( DMIOWrite.quoteString( "ab\ncd\nef\ngh\n" ), "\"ab\\ncd\\nef\\ngh\\n\"" );
		assertEquals( DMIOWrite.quoteString( "\nab\ncd\nef\ngh" ), "\"\\nab\\ncd\\nef\\ngh\"" );
		assertEquals( DMIOWrite.quoteString( "ab\ncd\nef\ngh" ), "\"ab\\ncd\\nef\\ngh\"" );
		assertEquals( DMIOWrite.quoteString( "abcd\refgh" ), "\"abcd\\refgh\"" );
		assertEquals( DMIOWrite.quoteString( "abcd\tefgh" ), "\"abcd\\tefgh\"" );
		assertEquals( DMIOWrite.quoteString( "abcd\\efgh" ), "\"abcd\\\\efgh\"" );
		assertEquals( DMIOWrite.quoteString( "abcd" + new Character( (char)0x1258 ).toString() + "efgh" ), "\"abcd\\x1258xefgh\"" );
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
	
	
	public void testWriteEmptyString()
	{
		Object[] xs = { "" };
		writeTest( Arrays.asList( xs ), "(\"\")" );
	}

	
	public void testWriteEmptyList()
	{
		Object[] xs = { };
		writeTest( Arrays.asList( xs ), "()" );
	}

	
	public void testWriteList()
	{
		Object[] xs = { "a", "b", "c" };
		writeTest( Arrays.asList( xs ), "(a b c)" );
	}
	
	
	public void testWriteNestedList()
	{
		Object[] a = {};
		Object[] b = { "a", "b", "c" };
		Object[] xs = { "x", "y", Arrays.asList( a ), "z", Arrays.asList( b ), "w" };
		writeTest( Arrays.asList( xs ), "(x y () z (a b c) w)" );
	}
}
