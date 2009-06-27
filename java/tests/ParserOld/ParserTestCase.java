//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.ParserOld;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMIOReader;
import BritefuryJ.DocModel.DMModuleResolver;
import BritefuryJ.DocModel.DMIOReader.BadModuleNameException;
import BritefuryJ.DocModel.DMIOReader.ParseErrorException;
import BritefuryJ.DocModel.DMModule.UnknownClassException;
import BritefuryJ.DocModel.DMModuleResolver.CouldNotResolveModuleException;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.ParserOld.ParseResult;
import BritefuryJ.ParserOld.ParserExpression;

abstract public class ParserTestCase extends TestCase
{
	abstract protected DMModuleResolver getModuleResolver();

	
	
	private Object readExpectedSX(String expectedSX)
	{
		Object expected = null;
		try
		{
			expected = DMIOReader.readFromString( expectedSX, getModuleResolver() );
		}
		catch (ParseErrorException e)
		{
			System.out.println( "Could not parse expected SX" );
			fail();
		}
		catch (BadModuleNameException e)
		{
			System.out.println( "Bad module name - expected" );
			fail();
		}
		catch (UnknownClassException e)
		{
			System.out.println( "Unknown class name - expected" );
			fail();
		}
		catch (CouldNotResolveModuleException e)
		{
			System.out.println( "Could not resolve module - expected" );
			fail();
		}
		return expected;
	}
	
	public void matchTestSX(ParserExpression parser, String input, String expectedSX)
	{
		matchTestSX( parser, input, expectedSX, "[ \t\n]*" );
	}

	public void matchTestSX(ParserExpression parser, String input, String expectedSX, String ignoreCharsRegex)
	{
		Object expected = readExpectedSX( expectedSX );
		matchTest( parser, input, expected, ignoreCharsRegex );
	}
	
	
	public void matchTest(ParserExpression parser, String input, Object expected)
	{
		matchTest( parser, input, expected, "[ \t\n]*" );
	}

	public void matchTest(ParserExpression parser, String input, Object expected, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseString( input, ignoreCharsRegex );
		
		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.substring(  0, result.getEnd() ) );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
		}
		assertTrue( result.isValid() );
		
		Object value = result.getValue();
		String valueStr = value != null  ?  value.toString()  :  "<null>";
		String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";
		String expectedStr = expected != null  ?  expected.toString()  :  "<null>";
		String expectedClassName = expected != null  ?  expected.getClass().getName()  :  "<null>";
		
		if ( result.getEnd() != input.length() )
		{
			System.out.println( "INCOMPLETE PARSE while parsing " + input );
			System.out.println( "Parsed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( input.length() ) + " characters" );
			System.out.println( "Parsed text " + input.substring( 0, result.getEnd() ) );
			System.out.println( "EXPECTED: (a " + expectedClassName + ")" );
			System.out.println( expectedStr );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
		}
		assertEquals( result.getEnd(), input.length() );
		
		
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



	
	
	
	public void matchTest(ParserExpression parser, ItemStream input, Object expected)
	{
		matchTest( parser, input, expected, "[ \t\n]*" );
	}

	public void matchTest(ParserExpression parser, ItemStream input, Object expected, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseStream( input, ignoreCharsRegex );
		
		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.subStream( 0, result.getEnd() ).toString() );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
		}
		assertTrue( result.isValid() );
		
		Object value = result.getValue();
		String valueStr = value != null  ?  value.toString()  :  "<null>";
		String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";
		String expectedStr = expected != null  ?  expected.toString()  :  "<null>";
		String expectedClassName = expected != null  ?  expected.getClass().getName()  :  "<null>";
		
		if ( result.getEnd() != input.length() )
		{
			System.out.println( "INCOMPLETE PARSE while parsing " + input );
			System.out.println( "Parsed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( input.length() ) + " characters" );
			System.out.println( "Parsed text " + input.subStream( 0, result.getEnd() ).toString() );
			System.out.println( "EXPECTED: (a " + expectedClassName + ")" );
			System.out.println( expectedStr );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
		}
		assertEquals( result.getEnd(), input.length() );
		
		
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
	
	public void matchTestSX(ParserExpression parser, ItemStream input, String expectedSX)
	{
		matchTestSX( parser, input, expectedSX, "[ \t\n]*" );
	}

	public void matchTestSX(ParserExpression parser, ItemStream input, String expectedSX, String ignoreCharsRegex)
	{
		Object expected = readExpectedSX( expectedSX );
		matchTest( parser, input, expected, ignoreCharsRegex );
	}
	
	
	
	public void matchSubTestSX(ParserExpression parser, String input, String expectedSX, int end)
	{
		matchSubTestSX( parser, input, expectedSX, end, "[ \t\n]*" );
	}

	public void matchSubTestSX(ParserExpression parser, String input, String expectedSX, int end, String ignoreCharsRegex)
	{
		Object expected = readExpectedSX( expectedSX );
		matchSubTest( parser, input, expected, end, ignoreCharsRegex );

	}
	
	
	public void matchSubTest(ParserExpression parser, String input, Object expected, int end)
	{
		matchSubTest( parser, input, expected, end, "[ \t\n]*" );
	}
	
	public void matchSubTest(ParserExpression parser, String input, Object expected, int end, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseString( input, ignoreCharsRegex );

		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input.substring(  0, end ) + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.substring(  0, result.getEnd() ) );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
		}
		assertTrue( result.isValid() );
		
		Object value = result.getValue();
		String valueStr = value != null  ?  value.toString()  :  "<null>";
		String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";
		String expectedStr = expected != null  ?  expected.toString()  :  "<null>";
		String expectedClassName = expected != null  ?  expected.getClass().getName()  :  "<null>";
		
		if ( result.getEnd() != end )
		{
			System.out.println( "DID NOT PARSE CORRECT AMOUNT while parsing " + input.substring( 0, end ) );
			System.out.println( "Parsed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( end ) + " characters" );
			System.out.println( input.substring( 0, result.getEnd() ) );
			System.out.println( "EXPECTED: (a " + expectedClassName + ")" );
			System.out.println( expectedStr );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
		}
		assertEquals( result.getEnd(), end );
		
		
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



	public void matchSubTest(ParserExpression parser, ItemStream input, Object expected, int end)
	{
		matchSubTest( parser, input, expected, end, "[ \t\n]*" );
	}
	
	public void matchSubTest(ParserExpression parser, ItemStream input, Object expected, int end, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseStream( input, ignoreCharsRegex );

		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input.subStream(  0, end ) + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.subStream(  0, result.getEnd() ) );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
		}
		assertTrue( result.isValid() );
		
		Object value = result.getValue();
		String valueStr = value != null  ?  value.toString()  :  "<null>";
		String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";
		String expectedStr = expected != null  ?  expected.toString()  :  "<null>";
		String expectedClassName = expected != null  ?  expected.getClass().getName()  :  "<null>";
		
		if ( result.getEnd() != end )
		{
			System.out.println( "DID NOT PARSE CORRECT AMOUNT while parsing " + input.subStream( 0, end ) );
			System.out.println( "Parsed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( end ) + " characters" );
			System.out.println( input.subStream( 0, result.getEnd() ) );
			System.out.println( "EXPECTED: (a " + expectedClassName + ")" );
			System.out.println( expectedStr );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
		}
		assertEquals( result.getEnd(), end );
		
		
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
		ParseResult result = parser.parseString( input, ignoreCharsRegex );

		if ( result.isValid() )
		{
			Object value = result.getValue();
			String valueStr = value != null  ?  value.toString()  :  "<null>";
			String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";

			System.out.println( "FAILURE EXPECTED; GOT SUCCESS" );
			System.out.println( "EXPECTED:" );
			System.out.println( "<fail>" );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
			System.out.println( "Consumed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( input.length() ) + " characters" );
		}
		assertFalse( result.isValid() );
	}



	public void matchFailTest(ParserExpression parser, ItemStream input)
	{
		matchFailTest( parser, input, "[ \t\n]*" );
	}
	
	public void matchFailTest(ParserExpression parser, ItemStream input, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseStream( input, ignoreCharsRegex );

		if ( result.isValid() )
		{
			Object value = result.getValue();
			String valueStr = value != null  ?  value.toString()  :  "<null>";
			String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";

			System.out.println( "FAILURE EXPECTED; GOT SUCCESS" );
			System.out.println( "EXPECTED:" );
			System.out.println( "<fail>" );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
			System.out.println( "Consumed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( input.length() ) + " characters" );
		}
		assertFalse( result.isValid() );
	}



	public void matchIncompleteTest(ParserExpression parser, String input)
	{
		matchIncompleteTest( parser, input, "[ \t\n]*" );
	}
	
	public void matchIncompleteTest(ParserExpression parser, String input, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseString( input, ignoreCharsRegex );

		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.substring(  0, result.getEnd() ) );
			System.out.println( "EXPECTED INCOMPLETE PARSE:" );
		}
		assertTrue( result.isValid() );
		
		Object value = result.getValue();
		String valueStr = value != null  ?  value.toString()  :  "<null>";
		String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";
		
		if ( result.getEnd() == input.length() )
		{
			System.out.println( "EXPECTED INCOMPLETE PARSE while parsing " + input );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
		}
		assertTrue( result.getEnd() != input.length() );
	}




	public void matchIncompleteTest(ParserExpression parser, ItemStream input)
	{
		matchIncompleteTest( parser, input, "[ \t\n]*" );
	}
	
	public void matchIncompleteTest(ParserExpression parser, ItemStream input, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseStream( input, ignoreCharsRegex );

		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.subStream(  0, result.getEnd() ) );
			System.out.println( "EXPECTED INCOMPLETE PARSE:" );
		}
		assertTrue( result.isValid() );
		
		Object value = result.getValue();
		String valueStr = value != null  ?  value.toString()  :  "<null>";
		String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";
		
		if ( result.getEnd() == input.length() )
		{
			System.out.println( "EXPECTED INCOMPLETE PARSE while parsing " + input );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
		}
		assertTrue( result.getEnd() != input.length() );
	}
}
