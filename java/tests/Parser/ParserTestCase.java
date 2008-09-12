//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser;

import BritefuryJ.Parser.ParseResult;
import BritefuryJ.Parser.ParserExpression;
import junit.framework.TestCase;

public class ParserTestCase extends TestCase
{
	public void matchTest(ParserExpression parser, String input, Object expected)
	{
		matchTest( parser, input, expected, "[ \t\n]*" );
	}

	public void matchTest(ParserExpression parser, String input, Object expected, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseString( input );
		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input + ", stopped at " + String.valueOf( result.end ) + ": " + input.substring(  0, result.end ) );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
		}
		assertTrue( result.isValid() );
		
		Object value = result.value;
		
		if ( result.end != input.length() )
		{
			System.out.println( "INCOMPLETE PARSE while parsing " + input );
			System.out.println( "Parsed " + String.valueOf( result.end ) + "/" + String.valueOf( input.length() ) + " characters" );
			System.out.println( "Parsed text " + input.substring( 0, result.end ) );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
			System.out.println( "RESULT:" );
			System.out.println( value.toString() );
		}
		assertEquals( result.end, input.length() );
		
		
		if ( !value.equals( expected ) )
		{
			System.out.println( "VALUE DIFFERS FROM EXPECTED" );
			System.out.println( "EXPECTED: (a " + expected.getClass().getName() + ")" );
			System.out.println( expected.toString() );
			System.out.println( "RESULT: (a " + value.getClass().getName() + ")" );
			System.out.println( value.toString() );
		}
		assertTrue( value.equals( expected ) );
	}



	
	
	public void matchSubTest(ParserExpression parser, String input, Object expected, int end)
	{
		matchSubTest( parser, input, expected, end, "[ \t\n]*" );
	}
	
	public void matchSubTest(ParserExpression parser, String input, Object expected, int end, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseString( input );
		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input.substring(  0, end ) + ", stopped at " + String.valueOf( result.end ) + ": " + input.substring(  0, result.end ) );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
		}
		assertTrue( result.isValid() );
		
		Object value = result.value;
		
		if ( result.end != end )
		{
			System.out.println( "DID NOT PARSE CORRECT AMOUNT while parsing " + input.substring( 0, end ) );
			System.out.println( "Parsed " + String.valueOf( result.end ) + "/" + String.valueOf( end ) + " characters" );
			System.out.println( input.substring( 0, result.end ) );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
			System.out.println( "RESULT:" );
			System.out.println( value.toString() );
		}
		assertEquals( result.end, end );
		
		
		if ( !value.equals( expected ) )
		{
			System.out.println( "VALUE DIFFERS FROM EXPECTED" );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
			System.out.println( "RESULT:" );
			System.out.println( value.toString() );
		}
		assertTrue( value.equals( expected ) );
	}



	public void matchFailTest(ParserExpression parser, String input)
	{
		matchFailTest( parser, input, "[ \t\n]*" );
	}
	
	public void matchFailTest(ParserExpression parser, String input, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseString( input );
		if ( result.isValid() )
		{
			System.out.println( "FAILURE EXPECTED; GOT SUCCESS" );
			System.out.println( "EXPECTED:" );
			System.out.println( "<fail>" );
			System.out.println( "RESULT:" );
			System.out.println( result.value.toString() );
			System.out.println( "Consumed " + String.valueOf( result.end ) + "/" + String.valueOf( input.length() ) + " characters" );
		}
		assertFalse( result.isValid() );
	}

}


/*
	def _matchTestSX(self, parser, input, expectedSX, ignoreChars=string.whitespace):
		result, pos = parser.parseString( input, ignoreChars=ignoreChars )

		expected = readSX( expectedSX )

		if result is None:
			print 'PARSE FAILURE while parsing', input
			print 'EXPECTED:'
			print expectedSX
		self.assert_( result is not None )

		res = result.result

		if result.end != len( input ):
			print 'INCOMPLETE PARSE while parsing', input
			print 'Parsed %d/%d characters'  %  ( result.end, len( input ) )
			print input[:result.end]
			print 'EXPECTED:'
			print expectedSX
			print 'RESULT:'
			stream = cStringIO.StringIO()
			writeSX( stream, res )
			print stream.getvalue()

		if res != expected:
			print 'EXPECTED:'
			print expectedSX
			print ''
			print 'RESULT:'
			stream = cStringIO.StringIO()
			writeSX( stream, res )
			print stream.getvalue()
		self.assert_( res == expected )


	def _matchFailTest(self, parser, input, ignoreChars=string.whitespace):
		result, pos = parser.parseString( input, ignoreChars=ignoreChars )
		if result is not None   and   result.end == len( input ):
			print 'EXPECTED:'
			print '<fail>'
			print ''
			print 'RESULT:'
			print result.result
			print 'consumed %d/%d chars'  %  ( result.end, len( input ) )
		self.assert_( result is None  or  result.end != len( input ) )
*/
