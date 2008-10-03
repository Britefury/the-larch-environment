//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.Vector;

import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.ParserExpression;

public class InfixLeft extends BinaryOperator
{
	//
	// Constructors
	//
	
	public InfixLeft(ParserExpression opExpression, BinaryOperatorParseAction action)
	{
		super( opExpression, action );
	}

	public InfixLeft(String operator)
	{
		this( ParserExpression.coerce( operator ), new DefaultBinaryOperatorParseAction( operator ) );
	}

	
	
	protected ParserExpression buildOperatorParser(OperatorTable operatorTable,
			Vector<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel,
			ParserExpression thisLevelParser, PrecedenceLevel previousLevel,
			ParserExpression previousLevelParser)
	{
		ParserExpression prefix = operatorTable.getLowestPrecedenceUnaryOperatorLevelParserAbove( levelParserForwardDeclarations, thisLevel, new OperatorTable.PrefixFilter() );
		ParserExpression right;
		
		if ( prefix != null )
		{
			right = previousLevelParser.__or__( prefix );
		}
		else
		{
			right = previousLevelParser;
		}
		
		return thisLevelParser.__add__( opExpression ).__add__( right );
	}
}
