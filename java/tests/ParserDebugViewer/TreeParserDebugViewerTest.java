//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.ParserDebugViewer;

import java.util.ArrayList;
import java.util.Map;

import BritefuryJ.DocModel.DMIOReader;
import BritefuryJ.DocModel.DMIOReader.ParseErrorException;
import BritefuryJ.Parser.AnyNode;
import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.TracedParseResult;
import BritefuryJ.Parser.ListNode;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;
import BritefuryJ.Parser.RegEx;
import BritefuryJ.Parser.ParserExpression.ParserCoerceException;
import BritefuryJ.Parser.Production.CannotOverwriteProductionExpressionException;

public class TreeParserDebugViewerTest
{
	static ParserExpression identifier = new RegEx( "[A-Za-z_][A-Za-z0-9_]*" );

	
	public static TracedParseResult treeParseDebugResultTest() throws ParseErrorException, CannotOverwriteProductionExpressionException, ParserCoerceException
	{
		String inputSX = "[call [getAttr [getAttr [call [getAttr [load x] blah] [params]] foo] blah] [params]]";
		Object input = DMIOReader.readFromString( inputSX, null );
		ParserExpression parser = buildParser();
		return parser.traceParseNode( input );
	}
	
	
	
	private static ParserExpression buildParser() throws CannotOverwriteProductionExpressionException, ParserCoerceException
	{
		ParseAction methodCallRefactorAction = new ParseAction()
		{
			public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
			{
				return deepArrayToList( new Object[] { "invokeMethod", bindings.get( "target" ), bindings.get( "name" ), bindings.get( "params" ) } );
			}
		};
		
		ParserExpression load = new ListNode( new Object[] { "load", new AnyNode() } );
		ParserExpression params = new ListNode( new Object[] { "params" } );
		
		Production expression = new Production( "expression" );
		ParserExpression methodCall = new Production( "methodCall", MethodCallRefactorHelper.methodCall( expression.bindTo( "target" ), identifier.bindTo( "name" ), params.bindTo( "params" ) ).action( methodCallRefactorAction ) );
		ParserExpression call = new Production( "call", MethodCallRefactorHelper.call( expression, params ) );
		ParserExpression getAttr = new Production( "getAttr", MethodCallRefactorHelper.getAttr( expression, identifier ) );
		expression.setExpression( new Choice( new Object[] { methodCall, call, getAttr, load } ) );
		
		return expression;
	}


	private static class MethodCallRefactorHelper
	{
		static ParserExpression getAttr(ParserExpression target, ParserExpression name) throws ParserCoerceException
		{
			return new ListNode( new Object[] { "getAttr", target, name } );
		}
		
		static ParserExpression call(ParserExpression target, ParserExpression params) throws ParserCoerceException
		{
			return new ListNode( new Object[] { "call", target, params } );
		}

		static ParserExpression methodCall(ParserExpression target, ParserExpression name, ParserExpression params) throws ParserCoerceException
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
