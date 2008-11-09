//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser;

import java.util.HashMap;
import java.util.List;

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



	public void bindingsTestSX(ParserExpression parser, String input, String bindingsSX)
	{
		try
		{
			Object bindings = DMIORead.readSX( bindingsSX );
			bindingsTest( parser, input, bindings );
		}
		catch (ParseSXErrorException e)
		{
			System.out.println( "Could not parse bindings SX" );
			fail();
		}
	}


	public void bindingsTest(ParserExpression parser, String input, Object bindingsObject)
	{
		bindingsTest( parser, input, bindingsObject, "[ \t\n]*" );
	}

	@SuppressWarnings("unchecked")
	public void bindingsTest(ParserExpression parser, String input, Object bindingsObject, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseString( input, ignoreCharsRegex );

		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.substring(  0, result.getEnd() ) );
		}
		assertTrue( result.isValid() );
		
		if ( result.getEnd() != input.length() )
		{
			System.out.println( "INCOMPLETE PARSE while parsing " + input );
			System.out.println( "Parsed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( input.length() ) + " characters" );
			System.out.println( "Parsed text " + input.substring( 0, result.getEnd() ) );
		}
		assertEquals( result.getEnd(), input.length() );
		
		
		boolean bBindingsMatch = true;
		
		HashMap<String, Object> resBindings = result.getBindings();
		List<List<Object>> bindings = (List<List<Object>>)bindingsObject;
		int resBindingsSize = resBindings != null  ?  resBindings.size()  :  0;   
		assertEquals( bindings.size(), resBindingsSize );
		
		if ( bindings.size() > 0 )
		{
			for (List<Object> binding: bindings)
			{
				String name = (String)binding.get( 0 );
				Object value = binding.get( 1 );
				if ( resBindings.containsKey( name ) )
				{
					Object resValue = resBindings.get( name );
					if ( !resValue.equals( value ) )
					{
						System.out.println( "BINDING VALUES DO NOT MATCH FOR NAME " + name );
						System.out.println( "EXPECTED:" );
						System.out.println( value.toString() );
						System.out.println( "RESULT:" );
						System.out.println( resValue.toString() );
						bBindingsMatch = false;
					}
				}
				else
				{
					System.out.println( "EXPECTED BINDING FOR NAME " + name );
					bBindingsMatch = false;
				}
			}
		}
		assertTrue( bBindingsMatch );
	}

	
	
	
	//
	//
	//
	// NODE PARSING TESTS
	//
	//
	//


	public void matchNodeTestSX(ParserExpression parser, String inputSX, String expectedSX)
	{
		try
		{
			Object input = DMIORead.readSX( inputSX );
			Object expected = DMIORead.readSX( expectedSX );
			matchNodeTest( parser, input, expected );
		}
		catch (ParseSXErrorException e)
		{
			System.out.println( "Could not parse expected SX" );
			fail();
		}
	}
	
	
	public void matchNodeTest(ParserExpression parser, Object input, Object expected)
	{
		ParseResult result = parser.parseNode( input );
		
		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE" );
			System.out.println( "EXPECTED:" );
			System.out.println( expected.toString() );
		}
		assertTrue( result.isValid() );
		
		Object value = result.getValue();
		String valueStr = value != null  ?  value.toString()  :  "<null>";
		String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";
		String expectedStr = expected != null  ?  expected.toString()  :  "<null>";
		String expectedClassName = expected != null  ?  expected.getClass().getName()  :  "<null>";		
		
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


	
	
	public void matchNodeFailTestSX(ParserExpression parser, String inputSX)
	{
		try
		{
			Object input = DMIORead.readSX( inputSX );
			matchNodeFailTest( parser, input );
		}
		catch (ParseSXErrorException e)
		{
			System.out.println( "Could not parse expected SX" );
			fail();
		}
	}

	
	public void matchNodeFailTest(ParserExpression parser, Object input)
	{
		ParseResult result = parser.parseNode( input );

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
		}
		assertFalse( result.isValid() );
	}


	
	
	public void bindingsNodeTestSX(ParserExpression parser, String inputSX, String bindingsSX)
	{
		try
		{
			Object input = DMIORead.readSX( inputSX );
			Object bindings = DMIORead.readSX( bindingsSX );
			bindingsNodeTest( parser, input, bindings );
		}
		catch (ParseSXErrorException e)
		{
			System.out.println( "Could not parse bindings SX" );
			fail();
		}
	}


	@SuppressWarnings("unchecked")
	public void bindingsNodeTest(ParserExpression parser, Object input, Object bindingsObject)
	{
		ParseResult result = parser.parseNode( input );

		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE" );
		}
		assertTrue( result.isValid() );
		
	
		
		boolean bBindingsMatch = true;
		
		HashMap<String, Object> resBindings = result.getBindings();
		List<List<Object>> bindings = (List<List<Object>>)bindingsObject;
		int resBindingsSize = resBindings != null  ?  resBindings.size()  :  0;   
		assertEquals( bindings.size(), resBindingsSize );
		
		if ( bindings.size() > 0 )
		{
			for (List<Object> binding: bindings)
			{
				String name = (String)binding.get( 0 );
				Object value = binding.get( 1 );
				if ( resBindings.containsKey( name ) )
				{
					Object resValue = resBindings.get( name );
					if ( !resValue.equals( value ) )
					{
						System.out.println( "BINDING VALUES DO NOT MATCH FOR NAME " + name );
						System.out.println( "EXPECTED:" );
						System.out.println( value.toString() );
						System.out.println( "RESULT:" );
						System.out.println( resValue.toString() );
						bBindingsMatch = false;
					}
				}
				else
				{
					System.out.println( "EXPECTED BINDING FOR NAME " + name );
					bBindingsMatch = false;
				}
			}
		}
		assertTrue( bBindingsMatch );
	}
}
