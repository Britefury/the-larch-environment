//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;

public class InfixRightLevel extends BinaryOperatorLevel
{
	protected static class InfixRightOperatorResultBuilder
	{
		private BinaryOperatorParseAction action;
		private int operatorPos;
		private Object x;
		
		public InfixRightOperatorResultBuilder(BinaryOperatorParseAction action, int operatorPos, Object x)
		{
			this.action = action;
			this.operatorPos = operatorPos;
			this.x = x;
		}
		
		
		public int getOperatorPos()
		{
			return operatorPos;
		}
		

		public Object buildResult(String input, int begin, Object y)
		{
			return action.invoke( input, begin, x, y );
		}
	}

	
	
	private static class InfixRightOpLevelAction implements ParseAction
	{
		public InfixRightOpLevelAction()
		{
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(String input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;
			
			List<InfixRightOperatorResultBuilder> builders = (List<InfixRightOperatorResultBuilder>)xs.get( 0 );
			Object result = xs.get( 1 );
			
			for (int i = builders.size() - 1; i >= 0; i--)
			{
				InfixRightOperatorResultBuilder b = builders.get( i );
				result = b.buildResult( input, b.getOperatorPos(), result );
			}
			
			return result;
		}
	}

	
	
	private static class InfixRightOpAction implements ParseAction
	{
		private BinaryOperatorParseAction action;
		
		
		public InfixRightOpAction(BinaryOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(String input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;
			Object left = xs.get( 0 );
			return new InfixRightOperatorResultBuilder( action, begin, left );
		}
	}
	
	
	InfixRightOpLevelAction levelAction;

	
	//
	//
	// Constructor
	//
	//
	
	public InfixRightLevel(List<BinaryOperator> ops)
	{
		super( ops );
		
		levelAction = new InfixRightOpLevelAction();
	}
	
	
	protected ParserExpression buildParser(OperatorTable operatorTable, ParserExpression previousLevelParser, ArrayList<Production> reachupForwardDeclarations)
	{
		ParserExpression prefix = operatorTable.getPrefixLevelForReachUp( reachupForwardDeclarations, this );
		
		ParserExpression left = previousLevelParser, right = null;
		
		
		if ( prefix != null )
		{
			right = prefix.__or__( previousLevelParser );
		}
		else
		{
			right = previousLevelParser;
		}

		
		ParserExpression[] choices = new ParserExpression[operators.size()];
		for (int i = 0; i < operators.size(); i++)
		{
			BinaryOperator operator = operators.get( i );
			ParserExpression opLeft = left.__add__( operator.getOperatorExpression() );
			choices[i] = opLeft.action( new InfixRightOpAction( operator.getAction() ) );
		}
		ParserExpression ops = new Choice( choices );
		
		return ops.oneOrMore().__add__( right ).action( levelAction );
	}
}
