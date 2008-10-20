//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.PatternMatch;

import java.util.Map;

import BritefuryJ.PatternMatch.MatchAction;
import BritefuryJ.PatternMatch.Guard;
import BritefuryJ.PatternMatch.PatternMatcher;
import BritefuryJ.PatternMatch.Pattern.Anything;
import BritefuryJ.PatternMatch.Pattern.Bind;
import BritefuryJ.PatternMatch.Pattern.ListPattern;
import BritefuryJ.PatternMatch.Pattern.Literal;
import BritefuryJ.PatternMatch.Pattern.Pattern;
import BritefuryJ.PatternMatch.Pattern.ListPattern.OnlyOneRepeatAllowedException;

public class Test_PatternMatcher extends PatternMatchTestCase
{
	private MatchAction stringMatchAction(final String s)
	{
		return new MatchAction()
		{
			public Object invoke(Object x, Map<String, Object> bindings)
			{
				return s;
			}
		};
	}
	
	private MatchAction getXAction()
	{
		return new MatchAction()
		{
			public Object invoke(Object x, Map<String, Object> bindings)
			{
				return bindings.get( "x" );
			}
		};
	}
	
	
	public void testMatch() throws OnlyOneRepeatAllowedException
	{
		Guard x0 = new Guard( new ListPattern( new Pattern[] {} ), stringMatchAction( "0" ) );
		Guard x1 = new Guard( new ListPattern( new Pattern[] { new Anything() } ), stringMatchAction( "1" ) );
		Guard x2 = new Guard( new ListPattern( new Pattern[] { new Anything(), new Anything() } ), stringMatchAction( "2" ) );
		Guard x3 = new Guard( new ListPattern( new Pattern[] { new Anything(), new Anything(), new Anything() } ), stringMatchAction( "3" ) );
		Guard x4 = new Guard( new ListPattern( new Pattern[] { new Anything(), new Anything(), new Anything(), new Anything() } ), stringMatchAction( "4" ) );
		PatternMatcher matcher = new PatternMatcher( new Guard[] { x0, x1, x2, x3, x4 } );
		
		matchTestSX( matcher, "()", "0" );
		matchTestSX( matcher, "(a)", "1" );
		matchTestSX( matcher, "(a b)", "2" );
		matchTestSX( matcher, "(a b c)", "3" );
		matchTestSX( matcher, "(a b c d)", "4" );
		matchFailTestSX( matcher, "(a b c d e)" );
	}


	public void testMatchBindings() throws OnlyOneRepeatAllowedException
	{
		Guard x0 = new Guard( new ListPattern( new Pattern[] { new Literal( "foo" ), new Bind( "x", new Anything() ), new Anything(), new Anything() } ), getXAction() );
		Guard x1 = new Guard( new ListPattern( new Pattern[] { new Literal( "bar" ), new Anything(), new Bind( "x", new Anything() ), new Anything() } ), getXAction() );
		Guard x2 = new Guard( new ListPattern( new Pattern[] { new Literal( "ray" ), new Anything(), new Anything(), new Bind( "x", new Anything() ) } ), getXAction() );
		PatternMatcher matcher = new PatternMatcher( new Guard[] { x0, x1, x2 } );
		
		matchTestSX( matcher, "(foo a b c)", "a" );
		matchTestSX( matcher, "(bar a b c)", "b" );
		matchTestSX( matcher, "(ray a b c)", "c" );
	}
}
