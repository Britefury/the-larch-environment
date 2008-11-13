//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.ParserDebugViewer;

import java.util.ArrayList;
import java.util.Map;

import BritefuryJ.DocModel.DMIORead;
import BritefuryJ.DocModel.DMIORead.ParseSXErrorException;
import BritefuryJ.PatternMatch.Forward;
import BritefuryJ.PatternMatch.Production;
import BritefuryJ.ParserDebugViewer.ParseViewFrame;
import BritefuryJ.PatternMatch.Anything;
import BritefuryJ.PatternMatch.Choice;
import BritefuryJ.PatternMatch.DebugMatchResult;
import BritefuryJ.PatternMatch.MatchAction;
import BritefuryJ.PatternMatch.MatchExpression;
import BritefuryJ.PatternMatch.RegEx;

public class PatternMatchDebugViewerTest
{
	static MatchExpression identifier = new RegEx( "[A-Za-z_][A-Za-z0-9_]*" );

	
	public static void main(final String[] args) throws ParseSXErrorException
	{
		String inputSX = "(call (getAttr (getAttr (call (getAttr (load x) blah) (params)) foo) blah) (params))";
		Object input = DMIORead.readSX( inputSX );
		MatchExpression parser = buildParser();
		DebugMatchResult result = parser.debugParseNode( input );
		new ParseViewFrame( result );
	}
	
	
	
	private static MatchExpression buildParser()
	{
		MatchAction methodCallRefactorAction = new MatchAction()
		{
			public Object invoke(Object input, int begin, Object x, Map<String, Object> bindings)
			{
				return deepArrayToList( new Object[] { "invokeMethod", bindings.get( "target" ), bindings.get( "name" ), bindings.get( "params" ) } );
			}
		};
		
		MatchExpression load = MatchExpression.toParserExpression( new Object[] { "load", new Anything() } );
		MatchExpression params = MatchExpression.toParserExpression( new Object[] { "params" } );
		
		Forward expression = new Forward();
		MatchExpression methodCall = new Production( MethodCallRefactorHelper.methodCall( expression.bindTo( "target" ), identifier.bindTo( "name" ), params.bindTo( "params" ) ).action( methodCallRefactorAction ) );
		MatchExpression call = new Production( MethodCallRefactorHelper.call( expression, params ) );
		MatchExpression getAttr = new Production( MethodCallRefactorHelper.getAttr( expression, identifier ) );
		expression.setExpression( new Production( new Choice( new Object[] { methodCall, call, getAttr, load } ) ) );
		
		return expression;
	}


	private static class MethodCallRefactorHelper
	{
		static MatchExpression getAttr(MatchExpression target, MatchExpression name)
		{
			return MatchExpression.toParserExpression( new Object[] { "getAttr", target, name } );
		}
		
		static MatchExpression call(MatchExpression target, MatchExpression params)
		{
			return MatchExpression.toParserExpression( new Object[] { "call", target, params } );
		}

		static MatchExpression methodCall(MatchExpression target, MatchExpression name, MatchExpression params)
		{
			return call( getAttr( target, name ), params );
		}
	}

	
	static Object deepArrayToList(Object[] xs)
	{
		ArrayList<Object> l = new ArrayList<Object>();
		
		for (Object x: xs)
		{
			if ( x.getClass().isArray() )
			{
				l.add( deepArrayToList( (Object[])x ) );
			}
			else
			{
				l.add( x );
			}
		}
		
		return l;
	}
}
