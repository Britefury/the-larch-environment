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

public class Prefix extends UnaryOperator
{
	private static class PrefixOpAction implements ParseAction
	{
		private UnaryOperatorParseAction action;
		
		
		public PrefixOpAction(UnaryOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(String input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;

			return action.invoke( input, begin, xs.get( 1 ) );
		}
	}
	
	
	protected PrefixOpAction action;
	
	
	
	//
	// Constructors
	//
	
	public Prefix(ParserExpression opExpression, UnaryOperatorParseAction action)
	{
		super( opExpression );
		
		this.action = new PrefixOpAction( action );
	}

	public Prefix(String operator)
	{
		this( ParserExpression.coerce( operator ), new DefaultUnaryOperatorParseAction( operator ) );
	}

	
	
	protected ParserExpression buildParser(OperatorTable operatorTable,
			ArrayList<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel,
			ParserExpression thisLevelParser, PrecedenceLevel previousLevel,
			ParserExpression previousLevelParser)
	{
		ParserExpression p = opExpression.__add__( thisLevelParser );
		return p.action( action );
	}
}
