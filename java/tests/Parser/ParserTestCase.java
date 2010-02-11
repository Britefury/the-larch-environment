//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BritefuryJ.DocModel.DMSchemaResolver;
import junit.framework.TestCase;
import BritefuryJ.DocModel.DMIOReader;
import BritefuryJ.DocModel.DMIOReader.BadModuleNameException;
import BritefuryJ.DocModel.DMIOReader.ParseErrorException;
import BritefuryJ.DocModel.DMSchema.UnknownClassException;
import BritefuryJ.DocModel.DMSchemaResolver.CouldNotResolveSchemaException;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParseResult;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

abstract public class ParserTestCase extends TestCase
{
	abstract protected DMSchemaResolver getModuleResolver();

	
	
	protected Object readInputSX(String expectedSX)
	{
		Object expected = null;
		try
		{
			expected = DMIOReader.readFromString( expectedSX, getModuleResolver() );
		}
		catch (ParseErrorException e)
		{
			System.out.println( "Could not parse input SX" );
			fail();
		}
		catch (BadModuleNameException e)
		{
			System.out.println( "Bad module name - input" );
			fail();
		}
		catch (UnknownClassException e)
		{
			System.out.println( "Unknown class name - input" );
			fail();
		}
		catch (CouldNotResolveSchemaException e)
		{
			System.out.println( "Could not resolve module - input" );
			fail();
		}
		return expected;
	}
	
	
	protected Object readExpectedSX(String expectedSX)
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
		catch (CouldNotResolveSchemaException e)
		{
			System.out.println( "Could not resolve module - expected" );
			fail();
		}
		return expected;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> readExpectedBindingsSX(String expectedBindingsSX)
	{
		List<Object> expected = (List<Object>)readExpectedSX( expectedBindingsSX );
		HashMap<String, Object> expectedBindings = new HashMap<String, Object>();
		for (Object x: expected)
		{
			List<Object> pair = (List<Object>)x;
			if ( pair.size() != 2 )
			{
				throw new RuntimeException( "Bindings pair must contain two elements" );
			}
			expectedBindings.put( (String)pair.get( 0 ), pair.get( 1 ) );
		}
		return expectedBindings;
	}
	
	
	
	public void matchTestStringAndStreamSX(ParserExpression parser, String input, String expectedSX)
	{
		matchTestStringAndStreamSX( parser, input, expectedSX, "[ \t\n]*", null );
	}

	public void matchTestStringAndStreamSX(ParserExpression parser, String input, String expectedSX, ParseAction delegateAction)
	{
		matchTestStringAndStreamSX( parser, input, expectedSX, "[ \t\n]*", delegateAction );
	}

	public void matchTestStringAndStreamSX(ParserExpression parser, String input, String expectedSX, String ignoreCharsRegex, ParseAction delegateAction)
	{
		Object expected = readExpectedSX( expectedSX );
		matchTestStringAndStream( parser, input, expected, ignoreCharsRegex, delegateAction );
	}
	
	
	public void matchTestStringAndStream(ParserExpression parser, String input, Object expected)
	{
		matchTestStringAndStream( parser, input, expected, "[ \t\n]*", null );
	}

	public void matchTestStringAndStream(ParserExpression parser, String input, Object expected, ParseAction delegateAction)
	{
		matchTestStringAndStream( parser, input, expected, "[ \t\n]*", delegateAction );
	}

	public void matchTestStringAndStream(ParserExpression parser, String input, Object expected, String ignoreCharsRegex, ParseAction delegateAction)
	{
		ItemStreamBuilder builder = new ItemStreamBuilder();
		builder.appendTextValue( input );
		
		matchTestString( parser, input, expected, ignoreCharsRegex, delegateAction );
		matchTestStream( parser, builder.stream(), expected, ignoreCharsRegex, delegateAction );
	}

	
	
	
	public void bindingsTestStringAndStreamSX(ParserExpression parser, String input, String expectedBindingsSX)
	{
		bindingsTestStringAndStreamSX( parser, input, expectedBindingsSX, "[ \t\n]*", null );
	}

	public void bindingsTestStringAndStreamSX(ParserExpression parser, String input, String expectedBindingsSX, ParseAction delegateAction)
	{
		bindingsTestStringAndStreamSX( parser, input, expectedBindingsSX, "[ \t\n]*", delegateAction );
	}

	public void bindingsTestStringAndStreamSX(ParserExpression parser, String input, String expectedBindingsSX, String ignoreCharsRegex, ParseAction delegateAction)
	{
		Map<String, Object> expectedBindings = readExpectedBindingsSX( expectedBindingsSX );
		bindingsTestStringAndStream( parser, input, expectedBindings, ignoreCharsRegex, delegateAction );
	}
	
	
	public void bindingsTestStringAndStream(ParserExpression parser, String input, Map<String, Object> expectedBindings)
	{
		bindingsTestStringAndStream( parser, input, expectedBindings, "[ \t\n]*", null );
	}

	public void bindingsTestStringAndStream(ParserExpression parser, String input, Map<String, Object> expectedBindings, ParseAction delegateAction)
	{
		bindingsTestStringAndStream( parser, input, expectedBindings, "[ \t\n]*", delegateAction );
	}

	public void bindingsTestStringAndStream(ParserExpression parser, String input, Map<String, Object> expectedBindings, String ignoreCharsRegex, ParseAction delegateAction)
	{
		ItemStreamBuilder builder = new ItemStreamBuilder();
		builder.appendTextValue( input );
		
		bindingsTestString( parser, input, expectedBindings, ignoreCharsRegex, delegateAction );
		bindingsTestStream( parser, builder.stream(), expectedBindings, ignoreCharsRegex, delegateAction );
	}

	
	
	
	public void matchTestStringSX(ParserExpression parser, String input, String expectedSX)
	{
		matchTestStringSX( parser, input, expectedSX, "[ \t\n]*", null );
	}

	public void matchTestStringSX(ParserExpression parser, String input, String expectedSX, ParseAction delegateAction)
	{
		matchTestStringSX( parser, input, expectedSX, "[ \t\n]*", delegateAction );
	}

	public void matchTestStringSX(ParserExpression parser, String input, String expectedSX, String ignoreCharsRegex, ParseAction delegateAction)
	{
		Object expected = readExpectedSX( expectedSX );
		matchTestString( parser, input, expected, ignoreCharsRegex, delegateAction );
	}
	
	
	public void matchTestString(ParserExpression parser, String input, Object expected)
	{
		matchTestString( parser, input, expected, "[ \t\n]*", null );
	}

	public void matchTestString(ParserExpression parser, String input, Object expected, ParseAction delegateAction)
	{
		matchTestString( parser, input, expected, "[ \t\n]*", delegateAction );
	}

	public void matchTestString(ParserExpression parser, String input, Object expected, String ignoreCharsRegex, ParseAction delegateAction)
	{
		ParseResult result = parser.parseStringChars( input, ignoreCharsRegex, delegateAction );
		
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



	
	
	public void bindingsTestStringSX(ParserExpression parser, String input, String expectedBindingsSX)
	{
		bindingsTestStringSX( parser, input, expectedBindingsSX, "[ \t\n]*", null );
	}

	public void bindingsTestStringSX(ParserExpression parser, String input, String expectedBindingsSX, ParseAction delegateAction)
	{
		bindingsTestStringSX( parser, input, expectedBindingsSX, "[ \t\n]*", delegateAction );
	}

	public void bindingsTestStringSX(ParserExpression parser, String input, String expectedBindingsSX, String ignoreCharsRegex, ParseAction delegateAction)
	{
		bindingsTestString( parser, input, readExpectedBindingsSX( expectedBindingsSX ), ignoreCharsRegex, delegateAction );
	}
	
	public void bindingsTestString(ParserExpression parser, String input, Map<String, Object> expectedBindings)
	{
		bindingsTestString( parser, input, expectedBindings, "[ \t\n]*", null );
	}

	public void bindingsTestString(ParserExpression parser, String input, Map<String, Object> expectedBindings, ParseAction delegateAction)
	{
		bindingsTestString( parser, input, expectedBindings, "[ \t\n]*", delegateAction );
	}

	public void bindingsTestString(ParserExpression parser, String input, Map<String, Object> expectedBindings, String ignoreCharsRegex, ParseAction delegateAction)
	{
		ParseResult result = parser.parseStringChars( input, ignoreCharsRegex, delegateAction );
		
		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.substring(  0, result.getEnd() ) );
		}
		assertTrue( result.isValid() );
		
		String expectedStr = expectedBindings.toString();
		
		if ( result.getEnd() != input.length() )
		{
			System.out.println( "INCOMPLETE PARSE while parsing " + input );
			System.out.println( "Parsed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( input.length() ) + " characters" );
			System.out.println( "Parsed text " + input.substring( 0, result.getEnd() ) );
		}
		assertEquals( result.getEnd(), input.length() );
		
		
		boolean bValuesMatch = result.getBindings() == null  ?  expectedBindings.size() == 0  :  expectedBindings.equals( result.getBindings() );
		if ( !bValuesMatch )
		{
			System.out.println( "VALUE DIFFERS FROM EXPECTED" );
			System.out.println( "EXPECTED:" );
			System.out.println( expectedStr );
			System.out.println( "RESULT:" );
			System.out.println( result.getBindings() );
		}
		assertTrue( bValuesMatch );
	}

	
	
	public void matchTestStreamSX(ParserExpression parser, ItemStream input, String expectedSX)
	{
		matchTestStreamSX( parser, input, expectedSX, "[ \t\n]*", null );
	}

	public void matchTestStreamSX(ParserExpression parser, ItemStream input, String expectedSX, ParseAction delegateAction)
	{
		matchTestStreamSX( parser, input, expectedSX, "[ \t\n]*", delegateAction );
	}

	public void matchTestStreamSX(ParserExpression parser, ItemStream input, String expectedSX, String ignoreCharsRegex, ParseAction delegateAction)
	{
		Object expected = readExpectedSX( expectedSX );
		matchTestStream( parser, input, expected, ignoreCharsRegex, delegateAction );
	}
	

	public void matchTestStream(ParserExpression parser, ItemStream input, Object expected)
	{
		matchTestStream( parser, input, expected, "[ \t\n]*", null );
	}

	public void matchTestStream(ParserExpression parser, ItemStream input, Object expected, ParseAction delegateAction)
	{
		matchTestStream( parser, input, expected, "[ \t\n]*", delegateAction );
	}

	public void matchTestStream(ParserExpression parser, ItemStream input, Object expected, String ignoreCharsRegex, ParseAction delegateAction)
	{
		ParseResult result = parser.parseStreamItems( input, ignoreCharsRegex, delegateAction );
		
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
	
	
	

	public void bindingsTestStreamSX(ParserExpression parser, ItemStream input, String expectedBindingsSX)
	{
		bindingsTestStreamSX( parser, input, expectedBindingsSX, "[ \t\n]*", null );
	}

	public void bindingsTestStreamSX(ParserExpression parser, ItemStream input, String expectedBindingsSX, ParseAction delegateAction)
	{
		bindingsTestStreamSX( parser, input, expectedBindingsSX, "[ \t\n]*", delegateAction );
	}

	public void bindingsTestStreamSX(ParserExpression parser, ItemStream input, String expectedBindingsSX, String ignoreCharsRegex, ParseAction delegateAction)
	{
		bindingsTestStream( parser, input, readExpectedBindingsSX( expectedBindingsSX ), ignoreCharsRegex, delegateAction );
	}
	
	public void bindingsTestStream(ParserExpression parser, ItemStream input, Map<String, Object> expectedBindings)
	{
		bindingsTestStream( parser, input, expectedBindings, "[ \t\n]*", null );
	}

	public void bindingsTestStream(ParserExpression parser, ItemStream input, Map<String, Object> expectedBindings, ParseAction delegateAction)
	{
		bindingsTestStream( parser, input, expectedBindings, "[ \t\n]*", delegateAction );
	}

	public void bindingsTestStream(ParserExpression parser, ItemStream input, Map<String, Object> expectedBindings, String ignoreCharsRegex, ParseAction delegateAction)
	{
		ParseResult result = parser.parseStreamItems( input, ignoreCharsRegex, delegateAction );
		
		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.subStream(  0, result.getEnd() ) );
		}
		assertTrue( result.isValid() );
		
		String expectedStr = expectedBindings.toString();
		
		if ( result.getEnd() != input.length() )
		{
			System.out.println( "INCOMPLETE PARSE while parsing " + input );
			System.out.println( "Parsed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( input.length() ) + " characters" );
			System.out.println( "Parsed text " + input.subStream( 0, result.getEnd() ) );
		}
		assertEquals( result.getEnd(), input.length() );
		
		
		boolean bValuesMatch = result.getBindings() == null  ?  expectedBindings.size() == 0  :  expectedBindings.equals( result.getBindings() );
		if ( !bValuesMatch )
		{
			System.out.println( "VALUE DIFFERS FROM EXPECTED" );
			System.out.println( "EXPECTED:" );
			System.out.println( expectedStr );
			System.out.println( "RESULT:" );
			System.out.println( result.getBindings() );
		}
		assertTrue( bValuesMatch );
	}

	
	

	public void matchTestNodeSX(ParserExpression parser, String inputSX, String expectedSX)
	{
		matchTestNodeSX( parser, inputSX, expectedSX, "[ \t\n]*", null );
	}

	public void matchTestNodeSX(ParserExpression parser, String inputSX, String expectedSX, ParseAction delegateAction)
	{
		matchTestNodeSX( parser, inputSX, expectedSX, "[ \t\n]*", delegateAction );
	}

	public void matchTestNodeSX(ParserExpression parser, String inputSX, String expectedSX, String ignoreCharsRegex, ParseAction delegateAction)
	{
		Object input = readInputSX( inputSX );
		Object expected = readExpectedSX( expectedSX );
		matchTestNode( parser, input, expected, ignoreCharsRegex, delegateAction );
	}
	
	
	public void matchTestNode(ParserExpression parser, Object input, Object expected)
	{
		matchTestNode( parser, input, expected, "[ \t\n]*", null );
	}

	public void matchTestNode(ParserExpression parser, Object input, Object expected, ParseAction delegateAction)
	{
		matchTestNode( parser, input, expected, "[ \t\n]*", delegateAction );
	}

	public void matchTestNode(ParserExpression parser, Object input, Object expected, String ignoreCharsRegex, ParseAction delegateAction)
	{
		ParseResult result = parser.parseNode( input, ignoreCharsRegex, delegateAction );
		
		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input );
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

	
	
	public void bindingsTestNodeSX(ParserExpression parser, String inputSX, String expectedBindingsSX)
	{
		bindingsTestNodeSX( parser, inputSX, expectedBindingsSX, "[ \t\n]*", null );
	}

	public void bindingsTestNodeSX(ParserExpression parser, String inputSX, String expectedBindingsSX, ParseAction delegateAction)
	{
		bindingsTestNodeSX( parser, inputSX, expectedBindingsSX, "[ \t\n]*", delegateAction );
	}

	public void bindingsTestNodeSX(ParserExpression parser, String inputSX, String expectedBindingsSX, String ignoreCharsRegex, ParseAction delegateAction)
	{
		bindingsTestNode( parser, readInputSX( inputSX ), readExpectedBindingsSX( expectedBindingsSX ), ignoreCharsRegex, delegateAction );
	}
	
	public void bindingsTestNode(ParserExpression parser, Object input, Map<String, Object> expectedBindings)
	{
		bindingsTestNode( parser, input, expectedBindings, "[ \t\n]*", null );
	}

	public void bindingsTestNode(ParserExpression parser, Object input, Map<String, Object> expectedBindings, ParseAction delegateAction)
	{
		bindingsTestNode( parser, input, expectedBindings, "[ \t\n]*", delegateAction );
	}

	public void bindingsTestNode(ParserExpression parser, Object input, Map<String, Object> expectedBindings, String ignoreCharsRegex, ParseAction delegateAction)
	{
		ParseResult result = parser.parseNode( input, ignoreCharsRegex, delegateAction );
		
		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input );
		}
		assertTrue( result.isValid() );
		
		String expectedStr = expectedBindings.toString();
		
		boolean bValuesMatch = result.getBindings() == null  ?  expectedBindings.size() == 0  :  expectedBindings.equals( result.getBindings() );
		if ( !bValuesMatch )
		{
			System.out.println( "VALUE DIFFERS FROM EXPECTED" );
			System.out.println( "EXPECTED:" );
			System.out.println( expectedStr );
			System.out.println( "RESULT:" );
			System.out.println( result.getBindings() );
		}
		assertTrue( bValuesMatch );
	}

	
	


	public void matchTestListSX(ParserExpression parser, String inputSX, String expectedSX)
	{
		matchTestListSX( parser, inputSX, expectedSX, "[ \t\n]*", null );
	}

	public void matchTestListSX(ParserExpression parser, String inputSX, String expectedSX, ParseAction delegateAction)
	{
		matchTestListSX( parser, inputSX, expectedSX, "[ \t\n]*", delegateAction );
	}

	@SuppressWarnings("unchecked")
	public void matchTestListSX(ParserExpression parser, String inputSX, String expectedSX, String ignoreCharsRegex, ParseAction delegateAction)
	{
		List<Object> input = (List<Object>)readInputSX( inputSX );
		Object expected = readExpectedSX( expectedSX );
		matchTestList( parser, input, expected, ignoreCharsRegex, delegateAction );
	}
	
	
	public void matchTestList(ParserExpression parser, List<Object> input, Object expected)
	{
		matchTestList( parser, input, expected, "[ \t\n]*", null );
	}

	public void matchTestList(ParserExpression parser, List<Object> input, Object expected, ParseAction delegateAction)
	{
		matchTestList( parser, input, expected, "[ \t\n]*", delegateAction );
	}

	public void matchTestList(ParserExpression parser, List<Object> input, Object expected, String ignoreCharsRegex, ParseAction delegateAction)
	{
		ParseResult result = parser.parseListItems( input, ignoreCharsRegex, delegateAction );
		
		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.subList(  0, result.getEnd() ) );
			System.out.println( "EXPECTED:" );
			System.out.println( "" + expected );
		}
		assertTrue( result.isValid() );
		
		Object value = result.getValue();
		String valueStr = value != null  ?  value.toString()  :  "<null>";
		String valueClassName = value != null  ?  value.getClass().getName()  :  "<null>";
		String expectedStr = expected != null  ?  expected.toString()  :  "<null>";
		String expectedClassName = expected != null  ?  expected.getClass().getName()  :  "<null>";
		
		if ( result.getEnd() != input.size() )
		{
			System.out.println( "INCOMPLETE PARSE while parsing " + input );
			System.out.println( "Parsed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( input.size() ) + " items" );
			System.out.println( "Parsed items " + input.subList( 0, result.getEnd() ) );
			System.out.println( "EXPECTED: (a " + expectedClassName + ")" );
			System.out.println( expectedStr );
			System.out.println( "RESULT: (a " + valueClassName + ")" );
			System.out.println( valueStr );
		}
		assertEquals( result.getEnd(), input.size() );
		
		
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

	
	
	public void bindingsTestListSX(ParserExpression parser, String inputSX, String expectedBindingsSX)
	{
		bindingsTestListSX( parser, inputSX, expectedBindingsSX, "[ \t\n]*", null );
	}

	public void bindingsTestListSX(ParserExpression parser, String inputSX, String expectedBindingsSX, ParseAction delegateAction)
	{
		bindingsTestListSX( parser, inputSX, expectedBindingsSX, "[ \t\n]*", delegateAction );
	}

	@SuppressWarnings("unchecked")
	public void bindingsTestListSX(ParserExpression parser, String inputSX, String expectedBindingsSX, String ignoreCharsRegex, ParseAction delegateAction)
	{
		List<Object> input = (List<Object>)readInputSX( inputSX );
		bindingsTestList( parser, input, readExpectedBindingsSX( expectedBindingsSX ), ignoreCharsRegex, delegateAction );
	}
	
	public void bindingsTestList(ParserExpression parser, List<Object> input, Map<String, Object> expectedBindings)
	{
		bindingsTestList( parser, input, expectedBindings, "[ \t\n]*", null );
	}

	public void bindingsTestList(ParserExpression parser, List<Object> input, Map<String, Object> expectedBindings, ParseAction delegateAction)
	{
		bindingsTestList( parser, input, expectedBindings, "[ \t\n]*", delegateAction );
	}

	public void bindingsTestList(ParserExpression parser, List<Object> input, Map<String, Object> expectedBindings, String ignoreCharsRegex, ParseAction delegateAction)
	{
		ParseResult result = parser.parseListItems( input, ignoreCharsRegex, delegateAction );
		
		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.subList(  0, result.getEnd() ) );
		}
		assertTrue( result.isValid() );
		
		String expectedStr = expectedBindings.toString();
		
		if ( result.getEnd() != input.size() )
		{
			System.out.println( "INCOMPLETE PARSE while parsing " + input );
			System.out.println( "Parsed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( input.size() ) + " characters" );
			System.out.println( "Parsed items " + input.subList( 0, result.getEnd() ) );
		}
		assertEquals( result.getEnd(), input.size() );
		
		
		boolean bValuesMatch = result.getBindings() == null  ?  expectedBindings.size() == 0  :  expectedBindings.equals( result.getBindings() );
		if ( !bValuesMatch )
		{
			System.out.println( "VALUE DIFFERS FROM EXPECTED" );
			System.out.println( "EXPECTED:" );
			System.out.println( expectedStr );
			System.out.println( "RESULT:" );
			System.out.println( result.getBindings() );
		}
		assertTrue( bValuesMatch );
	}

	
	

	
	public void matchSubTestStringAndStreamSX(ParserExpression parser, String input, String expectedSX, int end)
	{
		matchSubTestStringAndStreamSX( parser, input, expectedSX, end, "[ \t\n]*" );
	}
	
	public void matchSubTestStringAndStreamSX(ParserExpression parser, String input, String expectedSX, int end, String ignoreCharsRegex)
	{
		Object expected = readExpectedSX( expectedSX );
		matchSubTestStringAndStream( parser, input, expected, end, ignoreCharsRegex );
	}
		
	public void matchSubTestStringAndStream(ParserExpression parser, String input, Object expected, int end)
	{
		matchSubTestStringAndStream( parser, input, expected, end, "[ \t\n]*" );
	}
	
	public void matchSubTestStringAndStream(ParserExpression parser, String input, Object expected, int end, String ignoreCharsRegex)
	{
		ItemStreamBuilder builder = new ItemStreamBuilder();
		builder.appendTextValue( input );
		
		matchSubTestString( parser, input, expected, end, ignoreCharsRegex );
		matchSubTestStream( parser, builder.stream(), expected, end, ignoreCharsRegex );
	}

		
	public void matchSubTestStringSX(ParserExpression parser, String input, String expectedSX, int end)
	{
		matchSubTestStringSX( parser, input, expectedSX, end, "[ \t\n]*" );
	}

	public void matchSubTestStringSX(ParserExpression parser, String input, String expectedSX, int end, String ignoreCharsRegex)
	{
		Object expected = readExpectedSX( expectedSX );
		matchSubTestString( parser, input, expected, end, ignoreCharsRegex );
	}
	
	
	public void matchSubTestString(ParserExpression parser, String input, Object expected, int end)
	{
		matchSubTestString( parser, input, expected, end, "[ \t\n]*" );
	}
	
	public void matchSubTestString(ParserExpression parser, String input, Object expected, int end, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseStringChars( input, ignoreCharsRegex );

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



	public void matchSubTestStreamSX(ParserExpression parser, ItemStream input, String expectedSX, int end)
	{
		matchSubTestStreamSX( parser, input, expectedSX, end, "[ \t\n]*" );
	}

	public void matchSubTestStreamSX(ParserExpression parser, ItemStream input, String expectedSX, int end, String ignoreCharsRegex)
	{
		Object expected = readExpectedSX( expectedSX );
		matchSubTestStream( parser, input, expected, end, ignoreCharsRegex );

	}
	
	
	public void matchSubTestStream(ParserExpression parser, ItemStream input, Object expected, int end)
	{
		matchSubTestStream( parser, input, expected, end, "[ \t\n]*" );
	}
	
	public void matchSubTestStream(ParserExpression parser, ItemStream input, Object expected, int end, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseStreamItems( input, ignoreCharsRegex );

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

	
	
	
	public void matchSubTestListSX(ParserExpression parser, String inputSX, String expectedSX, int end)
	{
		matchSubTestListSX( parser, inputSX, expectedSX, end, "[ \t\n]*" );
	}

	@SuppressWarnings("unchecked")
	public void matchSubTestListSX(ParserExpression parser, String inputSX, String expectedSX, int end, String ignoreCharsRegex)
	{
		List<Object> input = (List<Object>)readInputSX( inputSX );
		Object expected = readExpectedSX( expectedSX );
		matchSubTestList( parser, input, expected, end, ignoreCharsRegex );
	}
	
	
	public void matchSubTestList(ParserExpression parser, List<Object> input, Object expected, int end)
	{
		matchSubTestList( parser, input, expected, end, "[ \t\n]*" );
	}
	
	public void matchSubTestList(ParserExpression parser, List<Object> input, Object expected, int end, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseListItems( input, ignoreCharsRegex );

		if ( !result.isValid() )
		{
			System.out.println( "PARSE FAILURE while parsing " + input.subList(  0, end ) + ", stopped at " + String.valueOf( result.getEnd() ) + ": " + input.subList(  0, result.getEnd() ) );
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
			System.out.println( "DID NOT PARSE CORRECT AMOUNT while parsing " + input.subList( 0, end ) );
			System.out.println( "Parsed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( end ) + " characters" );
			System.out.println( input.subList( 0, result.getEnd() ) );
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



	public void matchFailTestStringAndStream(ParserExpression parser, String input)
	{
		matchFailTestStringAndStream( parser, input, "[ \t\n]*" );
	}
	
	public void matchFailTestStringAndStream(ParserExpression parser, String input, String ignoreCharsRegex)
	{
		ItemStreamBuilder builder = new ItemStreamBuilder();
		builder.appendTextValue( input );
		
		matchFailTestString( parser, input, ignoreCharsRegex );
		matchFailTestStream( parser, builder.stream(), ignoreCharsRegex );
	}

	public void matchFailTestString(ParserExpression parser, String input)
	{
		matchFailTestString( parser, input, "[ \t\n]*" );
	}
	
	public void matchFailTestString(ParserExpression parser, String input, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseStringChars( input, ignoreCharsRegex );

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



	public void matchFailTestStream(ParserExpression parser, ItemStream input)
	{
		matchFailTestStream( parser, input, "[ \t\n]*" );
	}
	
	public void matchFailTestStream(ParserExpression parser, ItemStream input, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseStreamItems( input, ignoreCharsRegex );

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



	public void matchFailTestNodeSX(ParserExpression parser, String inputSX)
	{
		matchFailTestNodeSX( parser, inputSX, "[ \t\n]*" );
	}
	
	public void matchFailTestNodeSX(ParserExpression parser, String inputSX, String ignoreCharsRegex)
	{
		Object input = readInputSX( inputSX );
		matchFailTestNode( parser, input, ignoreCharsRegex );
	}

	public void matchFailTestNode(ParserExpression parser, Object input)
	{
		matchFailTestNode( parser, input, "[ \t\n]*" );
	}
	
	public void matchFailTestNode(ParserExpression parser, Object input, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseNode( input, ignoreCharsRegex );

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



	public void matchFailTestListSX(ParserExpression parser, String inputSX)
	{
		matchFailTestListSX( parser, inputSX, "[ \t\n]*" );
	}
	
	@SuppressWarnings("unchecked")
	public void matchFailTestListSX(ParserExpression parser, String inputSX, String ignoreCharsRegex)
	{
		List<Object> input = (List<Object>)readInputSX( inputSX );
		matchFailTestList( parser, input, "[ \t\n]*" );
	}
	
	public void matchFailTestList(ParserExpression parser, List<Object> input)
	{
		matchFailTestList( parser, input, "[ \t\n]*" );
	}
	
	public void matchFailTestList(ParserExpression parser, List<Object> input, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseListItems( input, ignoreCharsRegex );

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
			System.out.println( "Consumed " + String.valueOf( result.getEnd() ) + "/" + String.valueOf( input.size() ) + " items" );
		}
		assertFalse( result.isValid() );
	}




	
	
	public void matchIncompleteTestStringAndStream(ParserExpression parser, String input)
	{
		matchIncompleteTestStringAndStream( parser, input, "[ \t\n]*" );
	}
	
	public void matchIncompleteTestStringAndStream(ParserExpression parser, String input, String ignoreCharsRegex)
	{
		ItemStreamBuilder builder = new ItemStreamBuilder();
		builder.appendTextValue( input );
		
		matchIncompleteTestString( parser, input, ignoreCharsRegex );
		matchIncompleteTestStream( parser, builder.stream(), ignoreCharsRegex );
	}

	public void matchIncompleteTestString(ParserExpression parser, String input)
	{
		matchIncompleteTestString( parser, input, "[ \t\n]*" );
	}
	
	public void matchIncompleteTestString(ParserExpression parser, String input, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseStringChars( input, ignoreCharsRegex );

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




	public void matchIncompleteTestStream(ParserExpression parser, ItemStream input)
	{
		matchIncompleteTestStream( parser, input, "[ \t\n]*" );
	}
	
	public void matchIncompleteTestStream(ParserExpression parser, ItemStream input, String ignoreCharsRegex)
	{
		ParseResult result = parser.parseStreamItems( input, ignoreCharsRegex );

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
