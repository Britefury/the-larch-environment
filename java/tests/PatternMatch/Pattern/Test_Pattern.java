//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.PatternMatch.Pattern;

import java.util.Arrays;
import java.util.HashMap;

import BritefuryJ.PatternMatch.Pattern.Anything;
import BritefuryJ.PatternMatch.Pattern.Bind;
import BritefuryJ.PatternMatch.Pattern.Condition;
import BritefuryJ.PatternMatch.Pattern.ListPattern;
import BritefuryJ.PatternMatch.Pattern.Literal;
import BritefuryJ.PatternMatch.Pattern.Pattern;
import BritefuryJ.PatternMatch.Pattern.PatternCondition;
import BritefuryJ.PatternMatch.Pattern.ListPattern.OnlyOneRepeatAllowedException;

public class Test_Pattern extends PatternTestCase
{
	public void testAnything()
	{
		Pattern p0 = new Anything();
		
		assertEquals( p0, new Anything() );

		matchTest( p0, "x" );
	}
	
	public void testLiteralValue()
	{
		Pattern p0 = new Literal( "x" );
		
		assertEquals( p0, new Literal( "x" ) );
		assertFalse( p0.equals( new Literal( "y" ) ) );
		
		matchTest( p0, "x" );
		matchFailTest( p0, "y" );
	}
	
	public void testListPattern() throws OnlyOneRepeatAllowedException
	{
		// Basic list
		Pattern p0 = new ListPattern( new Pattern[] { new Literal( "x" ), new Literal( "y" ) } );
		matchTestSX( p0, "(x y)" );
		matchFailTestSX( p0, "(a y)" );
		matchFailTestSX( p0, "(x b)" );
		matchFailTestSX( p0, "(a b)" );
		matchFailTestSX( p0, "(x)" );
		matchFailTestSX( p0, "(x y z)" );

		// List with an anything
		Pattern p1 = new ListPattern( new Pattern[] { new Literal( "x" ), new Anything() } );
		matchTestSX( p1, "(x y)" );
		matchFailTestSX( p1, "(a y)" );
		matchTestSX( p1, "(x b)" );
		matchFailTestSX( p1, "(a b)" );
		matchFailTestSX( p1, "(x)" );
		matchFailTestSX( p1, "(x y z)" );

		// Nested list
		Pattern p2 = new ListPattern( new Pattern[] { new ListPattern( new Pattern[] { new Literal( "x" ), new Literal( "y" ) } ), new Literal( "z" ) } );
		matchTestSX( p2, "((x y) z)" );
		matchFailTestSX( p2, "((a y) z)" );
		matchFailTestSX( p2, "((x y) c)" );

	
		assertEquals( p0, new ListPattern( new Pattern[] { new Literal( "x" ), new Literal( "y" ) } ) );
		assertFalse( p0.equals( new ListPattern( new Pattern[] { new Literal( "x" ), new Literal( "z" ) } ) ) );
	}
	
	public void testListRepeat() throws OnlyOneRepeatAllowedException
	{
		boolean bCaught = false;
		try
		{
			new ListPattern( new Pattern[] { new ListPattern.ZeroOrMore( new Anything() ), new ListPattern.ZeroOrMore( new Anything() ) } );
		}
		catch (OnlyOneRepeatAllowedException e)
		{
			bCaught = true;
		}
		assertTrue( bCaught );
		
		
		HashMap<String, String> bindingsNone = new HashMap<String, String>();
		HashMap<String, String> bindingsA = new HashMap<String, String>();
		HashMap<String, String> bindingsAB = new HashMap<String, String>();
		bindingsNone.put( "x", "()" );
		bindingsA.put( "x", "(a)" );
		bindingsAB.put( "x", "(a b)" );
		
		
		Pattern p0 = new ListPattern( new Pattern[] { new Bind( "x", new ListPattern.ZeroOrMore( new Anything() ) ), new Literal( "x" ), new Literal( "y" ) } );
		matchTestSX( p0, "(x y)", bindingsNone );
		matchTestSX( p0, "(a x y)", bindingsA );
		matchTestSX( p0, "(a b x y)", bindingsAB );
		matchFailTestSX( p0, "(x a y)" );
		matchFailTestSX( p0, "(x y a)" );

	
		Pattern p1 = new ListPattern( new Pattern[] { new Literal( "x" ), new Bind( "x", new ListPattern.ZeroOrMore( new Anything() ) ), new Literal( "y" ) } );
		matchTestSX( p1, "(x y)", bindingsNone );
		matchTestSX( p1, "(x a y)", bindingsA );
		matchTestSX( p1, "(x a b y)", bindingsAB );
		matchFailTestSX( p1, "(a x y)" );
		matchFailTestSX( p1, "(x y a)" );

		
		Pattern p2 = new ListPattern( new Pattern[] { new Literal( "x" ), new Literal( "y" ), new Bind( "x", new ListPattern.ZeroOrMore( new Anything() ) ) } );
		matchTestSX( p2, "(x y)", bindingsNone );
		matchTestSX( p2, "(x y a)", bindingsA );
		matchTestSX( p2, "(x y a b)", bindingsAB );
		matchFailTestSX( p2, "(a x y)" );
		matchFailTestSX( p2, "(x a y)" );

	
		Pattern p3 = new ListPattern( new Pattern[] { new Literal( "x" ), new ListPattern.ZeroOrMore( new ListPattern( new Pattern[] { new Literal( "a" ), new Anything() } ) ), new Literal( "y" ) } );
		matchTestSX( p3, "(x y)" );
		matchTestSX( p3, "(x (a b) y)" );
		matchTestSX( p3, "(x (a c) y)" );
		matchFailTestSX( p3, "(x (p c) y)" );
		matchFailTestSX( p3, "(p (a b) y)" );
		matchFailTestSX( p3, "(x (a b) p)" );
		matchTestSX( p3, "(x (a b) (a c) (a d) y)" );
		
		
		Pattern p4 = new ListPattern( new Pattern[] { new Literal( "x" ), new ListPattern.OneOrMore( new ListPattern( new Pattern[] { new Literal( "a" ), new Anything() } ) ), new Literal( "y" ) } );
		matchFailTestSX( p4, "(x y)" );
		matchTestSX( p4, "(x (a b) y)" );
		matchTestSX( p4, "(x (a b) (a c) y)" );
		matchTestSX( p4, "(x (a b) (a c) (a d) y)" );

	
		Pattern p5 = new ListPattern( new Pattern[] { new Literal( "x" ), new ListPattern.Repeat( 2, 4, new ListPattern( new Pattern[] { new Literal( "a" ), new Anything() } ) ), new Literal( "y" ) } );
		matchFailTestSX( p5, "(x y)" );
		matchFailTestSX( p5, "(x (a b) y)" );
		matchTestSX( p5, "(x (a b) (a c) y)" );
		matchTestSX( p5, "(x (a b) (a c) (a d) y)" );
		matchTestSX( p5, "(x (a b) (a c) (a d) (a e) y)" );
		matchFailTestSX( p5, "(x (a b) (a c) (a d) (a e) (a f) y)" );

		assertEquals( p5, new ListPattern( new Pattern[] { new Literal( "x" ), new ListPattern.Repeat( 2, 4, new ListPattern( new Pattern[] { new Literal( "a" ), new Anything() } ) ), new Literal( "y" ) } ) );
		assertFalse( p5.equals( new ListPattern( new Pattern[] { new Literal( "a" ), new ListPattern.Repeat( 2, 4, new ListPattern( new Pattern[] { new Literal( "a" ), new Anything() } ) ), new Literal( "y" ) } ) ) );
		assertFalse( p5.equals( new ListPattern( new Pattern[] { new Literal( "x" ), new ListPattern.Repeat( 3, 4, new ListPattern( new Pattern[] { new Literal( "a" ), new Anything() } ) ), new Literal( "y" ) } ) ) );
		assertFalse( p5.equals( new ListPattern( new Pattern[] { new Literal( "x" ), new ListPattern.Repeat( 2, 5, new ListPattern( new Pattern[] { new Literal( "a" ), new Anything() } ) ), new Literal( "y" ) } ) ) );
		assertFalse( p5.equals( new ListPattern( new Pattern[] { new Literal( "x" ), new ListPattern.Repeat( 2, 4, new ListPattern( new Pattern[] { new Literal( "b" ) } ) ), new Literal( "y" ) } ) ) );
		assertFalse( p5.equals( new ListPattern( new Pattern[] { new Literal( "x" ), new ListPattern.Repeat( 2, 4, new ListPattern( new Pattern[] { new Literal( "a" ), new Anything() } ) ), new Literal( "z" ) } ) ) );
	}

	
	
	public void testBind()
	{
		HashMap<String, Object> bindingsA = new HashMap<String, Object>();
		bindingsA.put( "x", "a" );

		HashMap<String, Object> bindingsB = new HashMap<String, Object>();
		bindingsB.put( "x", "b" );

		
		Pattern p0 = new Bind( "x", new Literal( "a" ) );
		matchTest( p0, "a", bindingsA );
		matchFailTest( p0, "b" );

		Pattern p1 = new Bind( "x", new Anything() );
		matchTest( p1, "a", bindingsA );
		matchTest( p1, "b", bindingsB );

		assertEquals( p0, new Bind( "x", new Literal( "a" ) ) );
		assertFalse( p0.equals( new Bind( "y", new Literal( "a" ) ) ) );
		assertFalse( p0.equals( new Bind( "x", new Literal( "b" ) ) ) );
		
		assertEquals( new Literal( "a" ).bindTo( "x" ), new Bind( "x", new Literal( "a" ) ) );
		assertEquals( new Literal( "a" ).__rlshift__( "x" ), new Bind( "x", new Literal( "a" ) ) );
	}

	

	public void testCondition()
	{
		PatternCondition c = new PatternCondition()
		{
			public boolean test(Object x)
			{
				if ( x instanceof String )
				{
					String xx = (String)x;
					return xx.startsWith( "hi" );
				}
				else
				{
					return false;
				}
			}
		};
		
		Pattern p0 = new Condition( new Anything(), c );
		matchTest( p0, "hi there" );
		matchFailTest( p0, "b" );


		PatternCondition c2 = new PatternCondition()
		{
			public boolean test(Object x)
			{
				if ( x instanceof String )
				{
					String xx = (String)x;
					return xx.startsWith( "hi" );
				}
				else
				{
					return false;
				}
			}
		};
		
		assertEquals( p0, new Condition( new Anything(), c ) );
		assertFalse( p0.equals( new Condition( new Literal( "a" ), c ) ) );
		assertFalse( p0.equals( new Condition( new Anything(), c2 ) ) );

	
		assertEquals( new Anything().condition( c ), new Condition( new Anything(), c ) );
		assertEquals( new Anything().__and__( c ), new Condition( new Anything(), c ) );
	}



	public void testCoerce() throws OnlyOneRepeatAllowedException
	{
		Pattern a = new Anything();
		assertSame( a, Pattern.toPattern( a ) );

		assertEquals( new Literal( "x" ), Pattern.toPattern( "x" ) );
		
		assertEquals( new ListPattern( new Pattern[] { new Literal( "x" ), new Literal( "y" ) } ),   Pattern.toPattern( Arrays.asList( new String[] { "x", "y" } ) ) );
	}
}
