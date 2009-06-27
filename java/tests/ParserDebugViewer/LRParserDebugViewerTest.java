//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.ParserDebugViewer;

import BritefuryJ.ParserDebugViewer.ParseViewFrame;
import BritefuryJ.ParserOld.DebugParseResult;
import BritefuryJ.ParserOld.Literal;
import BritefuryJ.ParserOld.ParserExpression;
import BritefuryJ.ParserOld.Production;

public class LRParserDebugViewerTest
{
	public static void main(final String[] args) throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		String testSource = "";
		for (int i = 0; i < 100; i++)
		{
			testSource += "x";
		}
		ParserExpression parser = buildParser();
		DebugParseResult result = parser.debugParseString( testSource );
		new ParseViewFrame( result );
	}
	
	
	
	private static ParserExpression buildParser() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		Production A = new Production( "A" );
		Production B = new Production( "B" );
		A.setExpression( B.__add__( new Literal( "x" ) ).__or__( new Literal( "x" ) ) );
		B.setExpression( A );
		
		return A;
	}
}
