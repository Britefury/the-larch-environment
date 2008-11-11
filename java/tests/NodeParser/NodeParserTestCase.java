//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.NodeParser;

import java.util.HashMap;
import java.util.List;

import BritefuryJ.DocModel.DMIORead;
import BritefuryJ.DocModel.DMIORead.ParseSXErrorException;
import BritefuryJ.NodeParser.ParseResult;
import BritefuryJ.NodeParser.ParserExpression;
import junit.framework.TestCase;

public class NodeParserTestCase extends TestCase
{
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
		String resBindingsString = resBindings != null  ?  resBindings.toString()  :  "{}";
		
		if ( bindings.size() != resBindingsSize )
		{
			System.out.println( "BINDINGS DO NOT MATCH; DIFFERENT NUMBER OF BINDINGS" );
			System.out.println( "EXPECTED:" );
			System.out.println( bindingsObject.toString() );
			System.out.println( "RESULT:" );
			System.out.println( resBindingsString.toString() );
		}
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
