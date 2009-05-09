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
import BritefuryJ.DocModel.DMIOReader.BadModuleNameException;
import BritefuryJ.DocModel.DMIOReader.ParseErrorException;
import BritefuryJ.DocModel.DMModule.UnknownClassException;
import BritefuryJ.DocModel.DMModuleResolver.CouldNotResolveModuleException;
import BritefuryJ.ParserDebugViewer.ParseViewFrame;
import BritefuryJ.TreeParser.Anything;
import BritefuryJ.TreeParser.Choice;
import BritefuryJ.TreeParser.DebugMatchResult;
import BritefuryJ.TreeParser.TreeParseAction;
import BritefuryJ.TreeParser.TreeParserExpression;
import BritefuryJ.TreeParser.Production;
import BritefuryJ.TreeParser.RegEx;
import BritefuryJ.TreeParser.Production.CannotOverwriteProductionExpressionException;

public class TreeParserDebugViewerTest
{
	static TreeParserExpression identifier = new RegEx( "[A-Za-z_][A-Za-z0-9_]*" );

	
	public static void main(final String[] args) throws ParseErrorException, BadModuleNameException, UnknownClassException, CouldNotResolveModuleException, CannotOverwriteProductionExpressionException
	{
		String inputSX = "[call [getAttr [getAttr [call [getAttr [load x] blah] [params]] foo] blah] [params]]";
		Object input = DMIOReader.readFromString( inputSX, null );
		TreeParserExpression parser = buildParser();
		DebugMatchResult result = parser.debugParseNode( input );
		new ParseViewFrame( result );
	}
	
	
	
	private static TreeParserExpression buildParser() throws CannotOverwriteProductionExpressionException
	{
		TreeParseAction methodCallRefactorAction = new TreeParseAction()
		{
			public Object invoke(Object input, Object x, Map<String, Object> bindings, Object arg)
			{
				return deepArrayToList( new Object[] { "invokeMethod", bindings.get( "target" ), bindings.get( "name" ), bindings.get( "params" ) } );
			}
		};
		
		TreeParserExpression load = TreeParserExpression.coerce( new Object[] { "load", new Anything() } );
		TreeParserExpression params = TreeParserExpression.coerce( new Object[] { "params" } );
		
		Production expression = new Production( "expression" );
		TreeParserExpression methodCall = new Production( "methodCall", MethodCallRefactorHelper.methodCall( expression.bindTo( "target" ), identifier.bindTo( "name" ), params.bindTo( "params" ) ).action( methodCallRefactorAction ) );
		TreeParserExpression call = new Production( "call", MethodCallRefactorHelper.call( expression, params ) );
		TreeParserExpression getAttr = new Production( "getAttr", MethodCallRefactorHelper.getAttr( expression, identifier ) );
		expression.setExpression( new Choice( new Object[] { methodCall, call, getAttr, load } ) );
		
		return expression;
	}


	private static class MethodCallRefactorHelper
	{
		static TreeParserExpression getAttr(TreeParserExpression target, TreeParserExpression name)
		{
			return TreeParserExpression.coerce( new Object[] { "getAttr", target, name } );
		}
		
		static TreeParserExpression call(TreeParserExpression target, TreeParserExpression params)
		{
			return TreeParserExpression.coerce( new Object[] { "call", target, params } );
		}

		static TreeParserExpression methodCall(TreeParserExpression target, TreeParserExpression name, TreeParserExpression params)
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
