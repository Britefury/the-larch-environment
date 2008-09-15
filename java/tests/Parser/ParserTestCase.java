//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser;

import BritefuryJ.DocModel.DMIORead;
import BritefuryJ.DocModel.DMIORead.ParseSXErrorException;
import BritefuryJ.Parser.ParseResult;
import BritefuryJ.Parser.ParserExpression;
import junit.framework.TestCase;

public class ParserTestCase extends TestCase
{
	public void matchTestSX(ParserExpression parser, String input, String expectedSX)
	{
		matchTestSX( parser, input, expectedSX, "[ \t\n]*" );
	}

	public void matchTestSX(ParserExpression parser, String input, String expectedSX, String ignoreCharsRegex)
	{
		try
		{
			Object expected = DMIORead.readSX( expectedSX );
			matchTest( parser, input, expected, ignoreCharsRegex );
		}
		catch (ParseSXErrorException e)
		{
			System.out.println( "Could not parse expected SX" );
			fail();
		}
	}
	
	
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
		String valueStr = value != null  ?  value.toString()  :  "<null>";
		String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";
		String expectedStr = expected != null  ?  expected.toString()  :  "<null>";
		String expectedClassName = expected != null  ?  expected.getClass().getName()  :  "<null>";
		
		if ( result.end != input.length() )
		{
			System.out.println( "INCOMPLETE PARSE while parsing " + input );
			System.out.println( "Parsed " + String.valueOf( result.end ) + "/" + String.valueOf( input.length() ) + " characters" );
			System.out.println( "Parsed text " + input.substring( 0, result.end ) );
			System.out.println( "EXPECTED: (a " + expectedClassName + ")" );
			System.out.println( expectedStr );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
		}
		assertEquals( result.end, input.length() );
		
		
		boolean bValuesMatch = true;
		if ( value == null )
		{
			bValuesMatch = expected == null;
		}
		else
		{
			bValuesMatch = value.equals( expected );
		}
		if ( !bValuesMatch )
		{
			System.out.println( "VALUE DIFFERS FROM EXPECTED" );
			System.out.println( "EXPECTED: (a " + expectedClassName + ")" );
			System.out.println( expectedStr );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
		}
		assertTrue( bValuesMatch );
	}



	
	
	
	
	
	
	public void matchSubTestSX(ParserExpression parser, String input, String expectedSX, int end)
	{
		matchSubTestSX( parser, input, expectedSX, end, "[ \t\n]*" );
	}

	public void matchSubTestSX(ParserExpression parser, String input, String expectedSX, int end, String ignoreCharsRegex)
	{
		try
		{
			Object expected = DMIORead.readSX( expectedSX );
			matchSubTest( parser, input, expected, end, ignoreCharsRegex );
		}
		catch (ParseSXErrorException e)
		{
			System.out.println( "Could not parse expected SX" );
			fail();
		}
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
		String valueStr = value != null  ?  value.toString()  :  "<null>";
		String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";
		String expectedStr = expected != null  ?  expected.toString()  :  "<null>";
		String expectedClassName = expected != null  ?  expected.getClass().getName()  :  "<null>";
		
		if ( result.end != end )
		{
			System.out.println( "DID NOT PARSE CORRECT AMOUNT while parsing " + input.substring( 0, end ) );
			System.out.println( "Parsed " + String.valueOf( result.end ) + "/" + String.valueOf( end ) + " characters" );
			System.out.println( input.substring( 0, result.end ) );
			System.out.println( "EXPECTED: (a " + expectedClassName + ")" );
			System.out.println( expectedStr );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
		}
		assertEquals( result.end, end );
		
		
		boolean bValuesMatch = true;
		if ( value == null )
		{
			bValuesMatch = expected == null;
		}
		else
		{
			bValuesMatch = value.equals( expected );
		}
		if ( !bValuesMatch )
		{
			System.out.println( "VALUE DIFFERS FROM EXPECTED" );
			System.out.println( "EXPECTED: (a " + expectedClassName + ")" );
			System.out.println( expectedStr );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
		}
		assertTrue( bValuesMatch );
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
			Object value = result.value;
			String valueStr = value != null  ?  value.toString()  :  "<null>";
			String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";

			System.out.println( "FAILURE EXPECTED; GOT SUCCESS" );
			System.out.println( "EXPECTED:" );
			System.out.println( "<fail>" );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
			System.out.println( "Consumed " + String.valueOf( result.end ) + "/" + String.valueOf( input.length() ) + " characters" );
		}
		assertFalse( result.isValid() );
	}

}
