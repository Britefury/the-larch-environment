//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Parser.DebugViewer;

import BritefuryJ.Parser.DebugParseResult;
import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.Production;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.DebugViewer.ParseView;

public class DebugViewerTest
{
	public static void main(final String[] args)
	{
		String input = "this[i][j].x.m()";
		ParserExpression parser = buildParser();
		DebugParseResult result = parser.debugParseString( input );
		ParseView view = new ParseView( result, input );
		view.showInFrame();
	}
	
	
	
	private static ParserExpression buildParser()
	{
		Forward primary = new Forward();
		
		
		ParserExpression expression = new Production( new Literal( "i" ).__or__( new Literal( "j" ) ) ).debug( "expression" );
		ParserExpression methodName = new Production( new Literal( "m" ).__or__( new Literal( "n" ) ) ).debug( "methodName" );
		ParserExpression interfaceTypeName = new Production( new Literal( "I" ).__or__( new Literal( "J" ) ) ).debug( "interfaceTypeName" );
		ParserExpression className = new Production( new Literal( "C" ).__or__( new Literal( "D" ) ) ).debug( "className" );

		ParserExpression classOrInterfaceType = new Production( className.__or__( interfaceTypeName ) ).debug( "classOrInterfaceType" );

		ParserExpression identifier = new Production( new Literal( "x" ).__or__( new Literal( "y" ).__or__( classOrInterfaceType ) ) ).debug( "identifier" );
		ParserExpression expressionName = new Production( identifier ).debug( "expressionName" );

		ParserExpression arrayAccess = new Production( ( primary.__add__( "[" ).__add__( expression ).__add__( "]" ) ).__or__(
											expressionName.__add__( "[" ).__add__( expression ).__add__( "]" ) ) ).debug( "expressionName" );
		ParserExpression fieldAccess = new Production( ( primary.__add__( "." ).__add__( identifier ) ).__or__(
				new Literal( "super" ).__add__( "." ).__add__( identifier ) ) ).debug( "expressionName" );
		ParserExpression methodInvocation = new Production( ( primary.__add__( "." ).__add__( methodName ).__add__( "()" ) ).__or__(
				methodName.__add__( "()" ) ) ).debug( "methodInvocation" );
		
		ParserExpression classInstanceCreationExpression = new Production( ( new Literal( "new" ).__add__( classOrInterfaceType ).__add__( "()" ) ).__or__(
				primary.__add__( "." ).__add__( "new" ).__add__( identifier ).__add__( "()" ) ) ).debug( "classInstanceCreationExpression" );
		
		ParserExpression primaryNoNewArray = new Production( classInstanceCreationExpression.__or__( methodInvocation ).__or__( fieldAccess ).__or__( arrayAccess ).__or__( "this" ) ).debug( "primaryNoNewArray" );

		primary.setExpression( new Production( primaryNoNewArray ).debug( "primary" ) );
		
		return primary;
	}
}
