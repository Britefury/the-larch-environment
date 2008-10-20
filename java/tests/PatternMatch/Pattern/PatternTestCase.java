//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.PatternMatch.Pattern;

import java.util.HashMap;
import java.util.Map;

import BritefuryJ.DocModel.DMIORead;
import BritefuryJ.DocModel.DMIORead.ParseSXErrorException;
import BritefuryJ.PatternMatch.Pattern.Pattern;
import junit.framework.TestCase;

public class PatternTestCase extends TestCase
{
	private Map<String, Object> readBindingsSX(Map<String, String> sx)
	{
		HashMap<String, Object> bindings = new HashMap<String, Object>();
		for (String key: sx.keySet())
		{
			try
			{
				Object x = DMIORead.readSX( sx.get( key ) );
				bindings.put( key, x );
			}
			catch (ParseSXErrorException e)
			{
				System.out.println( "Could not parse binding SX" );
				fail();
			}
		}
		return bindings;
	}
	
	
	public void matchTest(boolean expectedResult, Pattern pattern, Object x, Map<String, Object> expectedBindings)
	{
		HashMap<String, Object> bindings = new HashMap<String, Object>();
		boolean bResult = pattern.test( x, bindings );
		assertEquals( bResult, expectedResult );
		if ( expectedBindings != null )
		{
			boolean bBindingsEquals = bindings.equals( expectedBindings );
			
			if ( !bBindingsEquals )
			{
				System.out.println( "BINDINGS DO NOT MATCH" );
				System.out.println( "VALUE:" );
				System.out.println( bindings.toString() );
				System.out.println( "EXPECTED:" );
				System.out.println( expectedBindings.toString() );
			}
			assertEquals( bindings, expectedBindings );
		}
	}

	public void matchTestSX(boolean expectedResult, Pattern pattern, String sx, Map<String, Object> expectedBindings)
	{
		try
		{
			Object x = DMIORead.readSX( sx );
			matchTest( expectedResult, pattern, x, expectedBindings );
		}
		catch (ParseSXErrorException e)
		{
			System.out.println( "Could not parse data SX" );
			fail();
		}
	}
		

	
	public void matchTest(Pattern pattern, Object x, Map<String, Object> expectedBindings)
	{
		matchTest( true, pattern, x, expectedBindings );
	}

	public void matchFailTest(Pattern pattern, Object x, Map<String, Object> expectedBindings)
	{
		matchTest( false, pattern, x, expectedBindings );
	}

	
	public void matchTest(Pattern pattern, Object x)
	{
		matchTest( true, pattern, x, null );
	}

	public void matchFailTest(Pattern pattern, Object x)
	{
		matchTest( false, pattern, x, null );
	}


	
	public void matchTestSX(Pattern pattern, String sx, Map<String, String> expectedBindingsSX)
	{
		matchTestSX( true, pattern, sx, readBindingsSX( expectedBindingsSX ) );
	}

	public void matchFailTestSX(Pattern pattern, String sx, Map<String, String> expectedBindingsSX)
	{
		matchTestSX( false, pattern, sx, readBindingsSX( expectedBindingsSX ) );
	}

	
	public void matchTestSX(Pattern pattern, String sx)
	{
		matchTestSX( true, pattern, sx, null );
	}

	public void matchFailTestSX(Pattern pattern, String sx)
	{
		matchTestSX( false, pattern, sx, null );
	}
}
