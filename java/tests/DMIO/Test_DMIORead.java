//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DMIO;

import java.util.ArrayList;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import BritefuryJ.DocModel.DMIORead;
import BritefuryJ.DocModel.DMIORead.ParseSXErrorException;

public class Test_DMIORead extends TestCase
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
	
	
	
	public void readTest(String input, Object expected)
	{
		Object res = null;
		try
		{
			res = DMIORead.readSX( input );
		}
		catch (ParseSXErrorException e)
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



	
	public void testWhitespace()
	{
		matchTest( DMIORead.whitespace, " \t\n", " \t\n" );
	}

	public void testUnquotedString()
	{
		matchTest( DMIORead.unquotedString, "abc123ABC_", "abc123ABC_" );
		matchTest( DMIORead.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~", "abc123ABC_+-*/%^&|!$@.,<>=[]~" );
		matchFailTest( DMIORead.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~(" );
		matchFailTest( DMIORead.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~)" );
		matchFailTest( DMIORead.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~\"" );
		matchFailTest( DMIORead.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~ " );
		matchFailTest( DMIORead.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~\t" );
		matchFailTest( DMIORead.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~\n" );
		matchFailTest( DMIORead.unquotedString, "abc123ABC_+-*/%^&|!$@.,<>=[]~\\" );
	}

	public void testQuotedString()
	{
		matchTest( DMIORead.quotedString, "\"abc123ABC_\"", "\"abc123ABC_\"" );
		matchTest( DMIORead.quotedString, "\"abc123()ABC_\"", "\"abc123()ABC_\"" );
		matchTest( DMIORead.quotedString, "\"abc123( )ABC_\"", "\"abc123( )ABC_\"" );
		matchTest( DMIORead.quotedString, "\"abc123(\\\\)ABC_\"", "\"abc123(\\\\)ABC_\"" );
		matchTest( DMIORead.quotedString, "\"abc123(\\n)ABC_\"", "\"abc123(\\n)ABC_\"" );
		matchTest( DMIORead.quotedString, "\"abc123(\\r)ABC_\"", "\"abc123(\\r)ABC_\"" );
		matchTest( DMIORead.quotedString, "\"abc123(\\t)ABC_\"", "\"abc123(\\t)ABC_\"" );
		matchTest( DMIORead.quotedString, "\"abc123(\\x123abcx)ABC_\"", "\"abc123(\\x123abcx)ABC_\"" );
		matchFailTest( DMIORead.quotedString, "\"abc123(\\x)ABC_\"" );
		matchFailTest( DMIORead.quotedString, "\"abc123(\\x123)ABC_\"" );
	}
	
	public void testHexCharEscape()
	{
		Pattern pat = Pattern.compile( DMIORead.hexCharEscape );
		matchFailTest( pat, "x" );
		matchTest( pat, "\\x0x", "\\x0x" );
		matchTest( pat, "\\xAx", "\\xAx" );
		matchFailTest( pat, "\\xx" );
		matchFailTest( pat, "\\xA" );
		matchFailTest( pat, "\\xG" );
	}

	public void testWhitespaceEscape()
	{
		Pattern pat = Pattern.compile( DMIORead.whitespaceEscape );
		matchFailTest( pat, "\\x" );
		matchTest( pat, "\\n", "\\n" );
		matchTest( pat, "\\r", "\\r" );
		matchTest( pat, "\\t", "\\t" );
		matchTest( pat, "\\\\", "\\\\" );
	}

	public void testEscapeSequence()
	{
		Pattern pat = Pattern.compile( DMIORead.escapeSequence );
		matchTest( pat, "\\n", "\\n" );
		matchTest( pat, "\\r", "\\r" );
		matchTest( pat, "\\t", "\\t" );
		matchTest( pat, "\\\\", "\\\\" );
		matchFailTest( pat, "\\q" );
		matchTest( pat, "\\x0x", "\\x0x" );
		matchTest( pat, "\\xAx", "\\xAx" );
	}
	
	
	public void testReadUnquotedString()
	{
		readTest( "abc", "abc" );
	}

	public void testReadQuotedString()
	{
		readTest( "\"abcd\"", "abcd" );
		readTest( "\"ab\\ncd\"", "ab\ncd" );
		readTest( "\"ab\\tcd\"", "ab\tcd" );
		readTest( "\"ab\\rcd\"", "ab\rcd" );
		readTest( "\"ab\\\\cd\"", "ab\\cd" );
		readTest( "\"ab\\x0107xcd\"", "ab" + new Character( (char)Integer.valueOf( "107", 16 ).intValue() ).toString() + "cd" );
	}

	public void testReadEmptyString()
	{
		readTest( "\"\"", "" );
	}
	
	public void testReadList()
	{
		ArrayList<Object> h = new ArrayList<Object>();
		h.add( "h" );
		h.add( "1" );
		h.add( "2L" );
		h.add( "3.0" );
		
		ArrayList<Object> g = new ArrayList<Object>();
		g.add( "g" );
		g.add( h );
		g.add( "Hi" );

		ArrayList<Object> f = new ArrayList<Object>();
		f.add( "f" );
		f.add( g );
		f.add( "There" );
		f.add( new Character( (char)Integer.valueOf( "0107", 16 ).intValue() ).toString() );

		readTest( "(f (g (h 1 2L 3.0) \"Hi\") \"There\" \"\\x0107x\")", f );
	}
}
