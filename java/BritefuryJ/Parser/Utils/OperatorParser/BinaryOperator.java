//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;

abstract class BinaryOperator extends Operator
{
	private class BinaryOpAction implements ParseAction
	{
		private BinaryOperatorParseAction action;
		
		
		public BinaryOpAction(BinaryOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(String input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;
			
			return action.invoke( input, begin, xs.get( 0 ), xs.get( 2 ) );
		}
	}
	
	
	protected static class BuildASTNodeAction implements BinaryOperatorParseAction
	{
		private String operator;
		
		
		public BuildASTNodeAction(String operator)
		{
			this.operator = operator;
		}
		
		
		public Object invoke(String input, int begin, Object x, Object y)
		{
			Object[] xs = { operator, x, y };
			return Arrays.asList( xs );
		}
	}

	
	
	protected BinaryOperatorParseAction action;
	
	
	
	
	//
	// Constructors
	//
	
	protected BinaryOperator(ParserExpression opExpression, BinaryOperatorParseAction action)
	{
		super( opExpression );
		
		this.action = action;
	}

	
	
	
	protected abstract ParserExpression buildOperatorParser(OperatorTable operatorTable, ArrayList<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel,
			ParserExpression thisLevelParser, PrecedenceLevel previousLevel, ParserExpression previousLevelParser);


	protected ParserExpression buildParser(OperatorTable operatorTable,
			ArrayList<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel,
			ParserExpression thisLevelParser, PrecedenceLevel previousLevel,
			ParserExpression previousLevelParser)
	{
		ParserExpression p = buildOperatorParser( operatorTable, levelParserForwardDeclarations, thisLevel, thisLevelParser, previousLevel, previousLevelParser );
		return p.action( new BinaryOpAction( action ) );
	}
}
