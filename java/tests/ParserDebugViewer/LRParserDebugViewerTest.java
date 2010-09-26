//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.ParserDebugViewer;

import BritefuryJ.Parser.TracedParseResult;
import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;

public class LRParserDebugViewerTest
{
	public static TracedParseResult lrDebugParseResultTest() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		String testSource = "";
		for (int i = 0; i < 100; i++)
		{
			testSource += "x";
		}
		ParserExpression parser = buildParser();
		return parser.traceParseStringChars( testSource );
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
