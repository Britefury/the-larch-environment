//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;
import BritefuryJ.Parser.Utils.OperatorParser.UnaryOperator.UnaryOperatorResultBuilder;

public class SuffixLevel extends UnaryOperatorLevel
{
	private static class SuffixOpLevelAction implements ParseAction
	{
		public SuffixOpLevelAction()
		{
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
		{
			List<Object> xs = (List<Object>)x;
			
			Object expression = xs.get( 0 );
			List<UnaryOperatorResultBuilder> builders = (List<UnaryOperatorResultBuilder>)xs.get( 1 );
			
			for (UnaryOperatorResultBuilder b: builders)
			{
				expression = b.buildResult( input, begin, b.getOperatorEnd(), expression );
			}
			
			return expression;
		}
	}
	
	
	SuffixOpLevelAction levelAction;

	
	//
	//
	// Constructor
	//
	//
	
	public SuffixLevel(List<UnaryOperator> ops)
	{
		super( ops );
		
		levelAction = new SuffixOpLevelAction();
	}

	
	protected ParserExpression buildParser(OperatorTable operatorTable, ParserExpression previousLevelParser, ArrayList<Production> reachupForwardDeclarations)
	{
		ParserExpression[] choices = new ParserExpression[operators.size()];
		for (int i = 0; i < operators.size(); i++)
		{
			UnaryOperator operator = operators.get( i );
			choices[i] = operator.applyAction( operator.getOperatorExpression() );
			assert choices[i] != null;
		}
		ParserExpression ops = new Choice( choices );
		
		return previousLevelParser.__add__( ops.oneOrMore().clearBindings() ).action( levelAction );
	}


	protected ParserExpression buildParserForReachUp(OperatorTable operatorTable, ParserExpression previousLevelParser)
	{
		ParserExpression[] choices = new ParserExpression[operators.size()];
		for (int i = 0; i < operators.size(); i++)
		{
			UnaryOperator operator = operators.get( i );
			choices[i] = operator.applyAction( operator.getOperatorExpression() );
		}
		ParserExpression ops = new Choice( choices );
		
		return previousLevelParser.__add__( ops.oneOrMore().clearBindings() ).action( levelAction );
	}
}
