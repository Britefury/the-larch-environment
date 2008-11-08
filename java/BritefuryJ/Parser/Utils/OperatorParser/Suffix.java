//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.List;
import java.util.ArrayList;

import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Utils.OperatorParser.UnaryOperator.DefaultUnaryOperatorParseAction;

public class Suffix extends Operator
{
	private class SuffixOpAction implements ParseAction
	{
		private UnaryOperatorParseAction action;
		
		
		public SuffixOpAction(UnaryOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(Object input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;

			return action.invoke( input, begin, xs.get( 0 ) );
		}
	}
	
	
	protected SuffixOpAction action;

	
	
	
	//
	// Constructors
	//
	
	public Suffix(ParserExpression opExpression, UnaryOperatorParseAction action)
	{
		super( opExpression );
		
		this.action = new SuffixOpAction( action );
	}

	public Suffix(String operator)
	{
		this( ParserExpression.coerce( operator ), new DefaultUnaryOperatorParseAction( operator ) );
	}

	
	
	protected ParserExpression buildParser(OperatorTable operatorTable,
			ArrayList<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel,
			ParserExpression thisLevelParser, PrecedenceLevel previousLevel,
			ParserExpression previousLevelParser)
	{
		ParserExpression p = thisLevelParser.__add__( opExpression );
		return p.action( action );
	}
}
