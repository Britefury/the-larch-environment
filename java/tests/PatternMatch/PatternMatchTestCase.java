//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.PatternMatch;

import java.util.HashMap;
import java.util.List;

import BritefuryJ.DocModel.DMIOReader;
import BritefuryJ.DocModel.DMModuleResolver;
import BritefuryJ.DocModel.DMIOReader.BadModuleNameException;
import BritefuryJ.DocModel.DMIOReader.ParseErrorException;
import BritefuryJ.DocModel.DMModule.UnknownClassException;
import BritefuryJ.DocModel.DMModuleResolver.CouldNotResolveModuleException;
import BritefuryJ.PatternMatch.MatchResult;
import BritefuryJ.PatternMatch.MatchExpression;
import junit.framework.TestCase;

public abstract class PatternMatchTestCase extends TestCase
{
	abstract protected DMModuleResolver getModuleResolver();
	
	
	
	
	public void matchNodeTestSX(MatchExpression parser, String inputSX, String expectedSX)
	{
		Object input = null;
		try
		{
			input = DMIOReader.readFromString( inputSX, getModuleResolver() );
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
		catch (CouldNotResolveModuleException e)
		{
			System.out.println( "Could not resolve module - input" );
			fail();
		}

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

		matchNodeTest( parser, input, expected );
	}
	
	
	public void matchNodeTest(MatchExpression parser, Object input, Object expected)
	{
		MatchResult result = parser.parseNode( input );
		
		if ( !result.isValid() )
		{
			System.out.println( "MATCH FAILURE" );
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


	
	
	public void matchNodeFailTestSX(MatchExpression parser, String inputSX)
	{
		Object input = null;
		try
		{
			input = DMIOReader.readFromString( inputSX, getModuleResolver() );
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
		catch (CouldNotResolveModuleException e)
		{
			System.out.println( "Could not resolve module - input" );
			fail();
		}
		matchNodeFailTest( parser, input );
	}

	
	public void matchNodeFailTest(MatchExpression parser, Object input)
	{
		MatchResult result = parser.parseNode( input );

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


	
	
	public void bindingsNodeTestSX(MatchExpression parser, String inputSX, String bindingsSX)
	{
		Object input = null;
		try
		{
			input = DMIOReader.readFromString( inputSX, getModuleResolver() );
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
		catch (CouldNotResolveModuleException e)
		{
			System.out.println( "Could not resolve module - input" );
			fail();
		}

		Object bindings = null;
		try
		{
			bindings = DMIOReader.readFromString( bindingsSX, getModuleResolver() );
		}
		catch (ParseErrorException e)
		{
			System.out.println( "Could not parse bindings SX" );
			fail();
		}
		catch (BadModuleNameException e)
		{
			System.out.println( "Bad module name - bindings" );
			fail();
		}
		catch (UnknownClassException e)
		{
			System.out.println( "Unknown class name - bindings" );
			fail();
		}
		catch (CouldNotResolveModuleException e)
		{
			System.out.println( "Could not resolve module - bindings" );
			fail();
		}

		bindingsNodeTest( parser, input, bindings );
	}


	@SuppressWarnings("unchecked")
	public void bindingsNodeTest(MatchExpression parser, Object input, Object bindingsObject)
	{
		MatchResult result = parser.parseNode( input );

		if ( !result.isValid() )
		{
			System.out.println( "MATCH FAILURE" );
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
