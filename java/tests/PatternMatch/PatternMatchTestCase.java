//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.PatternMatch;

import BritefuryJ.DocModel.DMIORead;
import BritefuryJ.DocModel.DMIORead.ParseSXErrorException;
import BritefuryJ.PatternMatch.PatternMatcher;
import junit.framework.TestCase;

public class PatternMatchTestCase extends TestCase
{
	public void matchTest(PatternMatcher matcher, Object x, Object expectedResult)
	{
		Object result;
		try
		{
			result = matcher.match( x );
		}
		catch (PatternMatcher.MatchFailureException e)
		{
			System.out.println( "MATCH FAILURE" );
			System.out.println( "EXPECTED:" );
			System.out.println( expectedResult.toString() );
			fail();
			return;
		}
		
		
		boolean bEqual = result.equals( expectedResult );
		if ( !bEqual )
		{
			System.out.println( "RESULTS DO NOT MATCH" );
			System.out.println( "VALUE:" );
			System.out.println( result.toString() );
			System.out.println( "EXPECTED:" );
			System.out.println( expectedResult.toString() );
		}
		assertTrue( bEqual );
	}

	public void matchTestSX(PatternMatcher matcher, String sx, String expectedResultSX)
	{
		Object result;
		try
		{
			Object x = DMIORead.readSX( sx );
			result = matcher.match( x );
		}
		catch (ParseSXErrorException e)
		{
			System.out.println( "Could not parse data SX" );
			fail();
			return;
		}
		catch (PatternMatcher.MatchFailureException e)
		{
			System.out.println( "MATCH FAILURE" );
			System.out.println( "EXPECTED:" );
			System.out.println( expectedResultSX );
			fail();
			return;
		}
		
		
		Object expectedResult;
		try
		{
			expectedResult = DMIORead.readSX( expectedResultSX );
		}
		catch (ParseSXErrorException e)
		{
			System.out.println( "Could not parse expected SX" );
			fail();
			return;
		}

		boolean bEqual = result.equals( expectedResult );
		if ( !bEqual )
		{
			System.out.println( "RESULTS DO NOT MATCH" );
			System.out.println( "VALUE:" );
			System.out.println( result.toString() );
			System.out.println( "EXPECTED:" );
			System.out.println( expectedResultSX );
		}
		assertTrue( bEqual );
	}

	
	
	public void matchFailTest(PatternMatcher matcher, Object x)
	{
		Object result;
		try
		{
			result = matcher.match( x );
			System.out.println( "EXPECTED FAILURE, GOT RESULT" );
			System.out.println( "VALUE:" );
			System.out.println( result.toString() );
		}
		catch (PatternMatcher.MatchFailureException e)
		{
			return;
		}
	}

	public void matchFailTestSX(PatternMatcher matcher, String sx)
	{
		try
		{
			Object x = DMIORead.readSX( sx );
			matchFailTest( matcher, x );
		}
		catch (ParseSXErrorException e)
		{
			System.out.println( "Could not parse data SX" );
			fail();
			return;
		}
	}
}
