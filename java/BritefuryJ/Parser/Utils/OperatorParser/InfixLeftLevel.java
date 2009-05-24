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
import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class InfixLeftLevel extends BinaryOperatorLevel
{
	protected static class InfixLeftOperatorResultBuilder
	{
		private BinaryOperatorParseAction action;
		private int operatorPos;
		private Object x;
		
		public InfixLeftOperatorResultBuilder(BinaryOperatorParseAction action, int operatorPos, Object x)
		{
			this.action = action;
			this.operatorPos = operatorPos;
			this.x = x;
		}
		
		
		public int getOperatorPos()
		{
			return operatorPos;
		}
		

		public Object buildResult(ItemStreamAccessor input, int begin, Object y)
		{
			return action.invoke( input, begin, y, x );
		}
	}
	

	private static class InfixLeftOpLevelAction implements ParseAction
	{
		public InfixLeftOpLevelAction()
		{
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(ItemStreamAccessor input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;
			
			Object result = xs.get( 0 );
			List<InfixLeftOperatorResultBuilder> builders = (List<InfixLeftOperatorResultBuilder>)xs.get( 1 );
			
			for (InfixLeftOperatorResultBuilder b: builders)
			{
				result = b.buildResult( input, begin, result );
			}
			
			return result;
		}
	}
	
	
	
	private static class InfixLeftOpAction implements ParseAction
	{
		private BinaryOperatorParseAction action;
		
		
		public InfixLeftOpAction(BinaryOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(ItemStreamAccessor input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;
			Object right = xs.get( 1 );
			return new InfixLeftOperatorResultBuilder( action, begin, right );
		}
	}


	
	
	
	InfixLeftOpLevelAction levelAction;

	
	//
	//
	// Constructor
	//
	//
	
	public InfixLeftLevel(List<BinaryOperator> ops)
	{
		super( ops );
		
		levelAction = new InfixLeftOpLevelAction();
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
			ParserExpression opRight = operator.getOperatorExpression().__add__( right );
			choices[i] = opRight.action( new InfixLeftOpAction( operator.getAction() ) );
		}
		ParserExpression ops = new Choice( choices );
		
		return left.__add__( ops.oneOrMore() ).action( levelAction );
	}
}
