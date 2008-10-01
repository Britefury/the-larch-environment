//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;

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
		public Object invoke(String input, int begin, Object x, Map<String, Object> bindings)
		{
			List<Object> xs = (List<Object>)x;

			return action.invoke( input, begin, xs.get( 0 ), bindings );
		}
	}
	
	
	protected SuffixOpAction action;

	
	
	
	//
	// Constructors
	//
	
	protected Suffix(ParserExpression opExpression, UnaryOperatorParseAction action)
	{
		super( opExpression );
		
		this.action = new SuffixOpAction( action );
	}

	
	
	protected ParserExpression buildParser(OperatorTable operatorTable,
			Vector<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel,
			ParserExpression thisLevelParser, PrecedenceLevel previousLevel,
			ParserExpression previousLevelParser)
	{
		ParserExpression p = thisLevelParser.__add__( opExpression );
		return p.action( action );
	}
}
